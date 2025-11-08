package app.greenpoint.service;

import app.greenpoint.domain.*;
import app.greenpoint.dto.MatchedMerchantDto;
import app.greenpoint.dto.TransactionRequestDto;
import app.greenpoint.dto.TransactionResponseDto;
import app.greenpoint.repository.*;
import app.greenpoint.dto.kakaopay.KakaoPayApproveRequestDto;
import app.greenpoint.dto.kakaopay.KakaoPayApproveResponseDto;
import app.greenpoint.dto.kakaopay.KakaoPayReadyRequestDto;
import app.greenpoint.dto.kakaopay.KakaoPayReadyResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AppUserRepository appUserRepository;
    private final MerchantRepository merchantRepository;
    private final TransactionRepository transactionRepository;
    private final RewardPointRepository rewardPointRepository;
    private final CategoryRepository categoryRepository; // Assuming Category has code and esg_weight
    private final KakaoPayService kakaoPayService;

    private static final int POINT_MULTIPLIER = 10;

    @Transactional
    public TransactionResponseDto processTransaction(String userEmail, TransactionRequestDto requestDto) {
        // 1. Find User
        AppUser user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        // 2. Match Merchant
        Merchant merchant = matchMerchant(requestDto);

        // Set txTime to now if it's null
        LocalDateTime txTime = (requestDto.getTxTime() != null) ? requestDto.getTxTime() : LocalDateTime.now();

        // 3. Create and Save Initial Transaction
        Transaction transaction = Transaction.builder()
                .user(user)
                .merchant(merchant)
                .amount(requestDto.getAmount())
                .txTime(txTime)
                .lat(requestDto.getGeo() != null ? requestDto.getGeo().getLat() : null)
                .lng(requestDto.getGeo() != null ? requestDto.getGeo().getLng() : null)
                .source(requestDto.getSource())
                .status(Transaction.Status.PENDING) // Start as PENDING
                .build();
        transaction = transactionRepository.save(transaction);

        // 4. Calculate ESG Score and Reward Points
        int esgScore = calculateEsgScore(transaction);
        int pointsEarned = esgScore * POINT_MULTIPLIER;

        // 5. Create and Save RewardPoint
        RewardPoint rewardPoint = RewardPoint.builder()
                .user(user)
                .transaction(transaction)
                .points(pointsEarned)
                .esgScore(esgScore)
                .reason("Transaction reward")
                .build();
        rewardPointRepository.save(rewardPoint);

        // 6. Update User\'s Points and Level
        user.setPoints(user.getPoints() + pointsEarned);
        user.setLevel((int) Math.floor((double) user.getPoints() / 1000) + 1);
        appUserRepository.save(user);

        // 7. Finalize Transaction Status
        transaction.setStatus(Transaction.Status.CONFIRMED);
        transactionRepository.save(transaction);

        // 8. Prepare and Return Response
        MatchedMerchantDto matchedMerchantDto = (merchant != null)
                ? new MatchedMerchantDto(merchant.getId(), merchant.getName(), merchant.getLat(), merchant.getLng())
                : null;

        return new TransactionResponseDto(
                transaction.getId(),
                matchedMerchantDto,
                esgScore,
                pointsEarned,
                user.getPoints(),
                (merchant != null) ? merchant.getEsgTier() : null
        );
    }

    @Transactional
    public KakaoPayReadyResponseDto initiateKakaoPayPayment(String userEmail, TransactionRequestDto requestDto) {
        AppUser user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        Merchant merchant = matchMerchant(requestDto);

        // Set txTime to now if it's null
        LocalDateTime txTime = (requestDto.getTxTime() != null) ? requestDto.getTxTime() : LocalDateTime.now();

        // Create a pending transaction for KakaoPay
        Transaction transaction = Transaction.builder()
                .user(user)
                .merchant(merchant)
                .amount(requestDto.getAmount())
                .txTime(txTime)
                .lat(requestDto.getGeo() != null ? requestDto.getGeo().getLat() : null)
                .lng(requestDto.getGeo() != null ? requestDto.getGeo().getLng() : null)
                .source(Transaction.Source.KAKAOPAY)
                .status(Transaction.Status.PENDING)
                .build();
        transaction = transactionRepository.save(transaction);

        // Prepare KakaoPay ready request
        KakaoPayReadyRequestDto kakaoPayReadyRequest = new KakaoPayReadyRequestDto();
        kakaoPayReadyRequest.setPartner_order_id(String.valueOf(transaction.getId())); // Use transaction ID as order ID
        kakaoPayReadyRequest.setPartner_user_id(String.valueOf(user.getId()));
        kakaoPayReadyRequest.setItem_name(requestDto.getItemName() != null ? requestDto.getItemName() : "GreenPoint Payment");
        kakaoPayReadyRequest.setQuantity(requestDto.getQuantity() != null ? requestDto.getQuantity() : 1);
        kakaoPayReadyRequest.setTotal_amount(requestDto.getAmount());
        kakaoPayReadyRequest.setTax_free_amount(0); // Assuming 0 for now, can be extended
        kakaoPayReadyRequest.setVat_amount(requestDto.getAmount() / 11); // Assuming 10% VAT

        // Set dynamic approval URL to include the transaction ID
        String approvalUrl = kakaoPayService.getRedirectHost() + kakaoPayService.getApprovalPath() + "/" + transaction.getId();
        kakaoPayReadyRequest.setApproval_url(approvalUrl);

        KakaoPayReadyResponseDto kakaoPayReadyResponse = kakaoPayService.readyPayment(kakaoPayReadyRequest);

        // Update transaction with KakaoPay tid
        transaction.setTid(kakaoPayReadyResponse.getTid());
        transactionRepository.save(transaction);

        return kakaoPayReadyResponse;
    }

    @Transactional
    public TransactionResponseDto approveKakaoPayPayment(String pgToken, Long orderId) {
        // Find the pending transaction by its ID (which was used as partner_order_id)
        Transaction transaction = transactionRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with ID: " + orderId));

        // Security and state check
        if (!transaction.getStatus().equals(Transaction.Status.PENDING)) {
            throw new IllegalStateException("Transaction is not in a pending state for approval.");
        }
        if (transaction.getTid() == null) {
            throw new IllegalStateException("Transaction does not have a KakaoPay TID.");
        }

        // Prepare KakaoPay approve request
        KakaoPayApproveRequestDto kakaoPayApproveRequest = new KakaoPayApproveRequestDto();
        kakaoPayApproveRequest.setTid(transaction.getTid());
        kakaoPayApproveRequest.setPartner_order_id(String.valueOf(transaction.getId()));
        kakaoPayApproveRequest.setPartner_user_id(String.valueOf(transaction.getUser().getId()));
        kakaoPayApproveRequest.setPg_token(pgToken);

        KakaoPayApproveResponseDto kakaoPayApproveResponse = kakaoPayService.approvePayment(kakaoPayApproveRequest);

        // Update transaction status and KakaoPay details
        transaction.setStatus(Transaction.Status.CONFIRMED);
        transaction.setAid(kakaoPayApproveResponse.getAid());
        transaction.setPaymentMethodType(kakaoPayApproveResponse.getPayment_method_type());
        transactionRepository.save(transaction);

        // Calculate ESG Score and Reward Points
        AppUser user = transaction.getUser();
        int esgScore = calculateEsgScore(transaction);
        int pointsEarned = esgScore * POINT_MULTIPLIER;

        RewardPoint rewardPoint = RewardPoint.builder()
                .user(user)
                .transaction(transaction)
                .points(pointsEarned)
                .esgScore(esgScore)
                .reason("KakaoPay Transaction reward")
                .build();
        rewardPointRepository.save(rewardPoint);

        user.setPoints(user.getPoints() + pointsEarned);
        user.setLevel((int) Math.floor((double) user.getPoints() / 1000) + 1);
        appUserRepository.save(user);

        // Prepare and Return Response
        MatchedMerchantDto matchedMerchantDto = (transaction.getMerchant() != null)
                ? new MatchedMerchantDto(transaction.getMerchant().getId(), transaction.getMerchant().getName(), transaction.getMerchant().getLat(), transaction.getMerchant().getLng())
                : null;

        return new TransactionResponseDto(
                transaction.getId(),
                matchedMerchantDto,
                esgScore,
                pointsEarned,
                user.getPoints(),
                (transaction.getMerchant() != null) ? transaction.getMerchant().getEsgTier() : null
        );
    }

    private Merchant matchMerchant(TransactionRequestDto requestDto) {
        // Priority 1: Use provided merchant_id
        if (requestDto.getMerchantId() != null) {
            return merchantRepository.findById(requestDto.getMerchantId()).orElse(null);
        }
        // Priority 2 & 3 (LBS & Fallback) are complex and will be simplified for now.
        // In a real scenario, this would involve geospatial queries.
        // For now, we return null if no ID is provided.
        return null;
    }

    private int calculateEsgScore(Transaction transaction) {
        if (transaction.getMerchant() == null) {
            return 0; // No merchant, no score
        }

        Merchant merchant = transaction.getMerchant();
        AppUser user = transaction.getUser();

        // Base score: floor(10 * log10(amount + 10))
        double baseScore = Math.floor(10 * Math.log10(transaction.getAmount() + 10));

        // Category weight
        // In a real app, you\'d cache categories.
        Optional<Category> categoryOpt = categoryRepository.findById(merchant.getCategoryCode());
        double weight = categoryOpt.map(Category::getEsgWeight).orElse(1.0);

        // Regional bonus
        int regionalBonus = 0;
        if (user.getRegion() != null && user.getRegion().equals(merchant.getRegion())) {
            regionalBonus = 2;
        }

        // ESG Tier bonus
        int tierBonus = 0;
        switch (merchant.getEsgTier()) {
            case A: tierBonus = 3; break;
            case B: tierBonus = 2; break;
            case C: tierBonus = 1; break;
            case D: tierBonus = 0; break;
        }

        // Final score
        double finalScore = baseScore * weight + regionalBonus + tierBonus;
        return (int) Math.round(finalScore);
    }
}
