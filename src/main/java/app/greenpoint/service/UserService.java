package app.greenpoint.service;

import app.greenpoint.domain.AppUser;
import app.greenpoint.domain.Merchant;
import app.greenpoint.domain.RewardPoint;
import app.greenpoint.domain.Transaction;
import app.greenpoint.domain.UserBadge;
import app.greenpoint.dto.MatchedMerchantDto;
import app.greenpoint.dto.TransactionResponseDto;
import app.greenpoint.dto.UserBadgeDto;
import app.greenpoint.dto.UserProfileDto;
import app.greenpoint.repository.AppUserRepository;
import app.greenpoint.repository.MerchantRepository;
import app.greenpoint.repository.RewardPointRepository;
import app.greenpoint.repository.TransactionRepository;
import app.greenpoint.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;
    private final RewardPointRepository rewardPointRepository; // Inject RewardPointRepository

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<UserBadge> userBadges = userBadgeRepository.findByUser(user);

        List<UserBadgeDto> badgeDtos = userBadges.stream()
                .map(userBadge -> new UserBadgeDto(
                        userBadge.getBadge().getCode(),
                        userBadge.getBadge().getName(),
                        userBadge.getAcquiredAt()))
                .collect(Collectors.toList());

        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRegion(),
                user.getLevel(),
                user.getPoints(),
                badgeDtos
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getUserTransactionHistory(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        List<Transaction> transactions = transactionRepository.findByUserOrderByTxTimeDesc(user);

        return transactions.stream().map(transaction -> {
            Merchant merchant = transaction.getMerchant();
            MatchedMerchantDto matchedMerchantDto = (merchant != null)
                    ? new MatchedMerchantDto(merchant.getId(), merchant.getName(), merchant.getLat(), merchant.getLng())
                    : null;

            int esgScore = 0;
            int pointsEarned = 0;

            Optional<RewardPoint> rewardPointOptional = rewardPointRepository.findByTransaction(transaction);
            if (rewardPointOptional.isPresent()) {
                RewardPoint rewardPoint = rewardPointOptional.get();
                esgScore = rewardPoint.getEsgScore();
                pointsEarned = rewardPoint.getPoints();
            }

            return new TransactionResponseDto(
                    transaction.getId(),
                    matchedMerchantDto,
                    esgScore,
                    pointsEarned,
                    user.getPoints(), // Current user points, not points at time of transaction
                    (merchant != null) ? merchant.getEsgTier() : null
            );
        }).collect(Collectors.toList());
    }
}
