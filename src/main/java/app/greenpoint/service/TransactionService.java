package app.greenpoint.service;

import app.greenpoint.domain.*;
import app.greenpoint.dto.MatchedMerchantDto;
import app.greenpoint.dto.TransactionRequestDto;
import app.greenpoint.dto.TransactionResponseDto;
import app.greenpoint.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AppUserRepository appUserRepository;
    private final MerchantRepository merchantRepository;
    private final TransactionRepository transactionRepository;
    private final RewardPointRepository rewardPointRepository;
    private final CategoryRepository categoryRepository; // Assuming Category has code and esg_weight

    private static final int POINT_MULTIPLIER = 10;

    @Transactional
    public TransactionResponseDto processTransaction(String userEmail, TransactionRequestDto requestDto) {
        // 1. Find User
        AppUser user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        // 2. Match Merchant
        Merchant merchant = matchMerchant(requestDto);

        // 3. Create and Save Initial Transaction
        Transaction transaction = Transaction.builder()
                .user(user)
                .merchant(merchant)
                .amount(requestDto.getAmount())
                .txTime(requestDto.getTxTime())
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

        // 6. Update User's Points and Level
        user.setPoints(user.getPoints() + pointsEarned);
        user.setLevel((int) Math.floor((double) user.getPoints() / 1000) + 1);
        appUserRepository.save(user);

        // 7. Finalize Transaction Status
        transaction.setStatus(Transaction.Status.CONFIRMED);
        transactionRepository.save(transaction);

        // 8. Prepare and Return Response
        MatchedMerchantDto matchedMerchantDto = (merchant != null)
                ? new MatchedMerchantDto(merchant.getId(), merchant.getName())
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
        // In a real app, you'd cache categories.
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
