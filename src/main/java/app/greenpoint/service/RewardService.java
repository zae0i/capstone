package app.greenpoint.service;

import app.greenpoint.domain.AppUser;
import app.greenpoint.domain.RewardPoint;
import app.greenpoint.domain.Transaction;
import app.greenpoint.dto.RecentRewardDto;
import app.greenpoint.dto.RewardHistoryItemDto;
import app.greenpoint.dto.UserBalanceDto;
import app.greenpoint.repository.AppUserRepository;
import app.greenpoint.repository.RewardPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final AppUserRepository appUserRepository;
    private final RewardPointRepository rewardPointRepository;

    @Transactional(readOnly = true)
    public UserBalanceDto getUserBalance(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        List<RewardPoint> recentRewards = rewardPointRepository.findTop5ByUserOrderByCreatedAtDesc(user);

        List<RecentRewardDto> recentRewardDtos = recentRewards.stream()
                .map(reward -> new RecentRewardDto(
                        reward.getPoints(),
                        reward.getReason(),
                        reward.getCreatedAt()))
                .collect(Collectors.toList());

        return new UserBalanceDto(
                user.getPoints(),
                user.getLevel(),
                recentRewardDtos
        );
    }

    @Transactional(readOnly = true)
    public Page<RewardHistoryItemDto> getRewardHistory(String userEmail, LocalDate from, LocalDate to, Pageable pageable) {
        AppUser user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        // The 'to' date should be inclusive, so we search until the end of that day.
        Page<RewardPoint> rewardPointsPage = rewardPointRepository.findByUserAndCreatedAtBetween(
                user,
                from.atStartOfDay(),
                to.atTime(LocalTime.MAX),
                pageable
        );

        return rewardPointsPage.map(this::convertToDto);
    }

    private RewardHistoryItemDto convertToDto(RewardPoint rewardPoint) {
        Transaction transaction = rewardPoint.getTransaction();
        String merchantName = "N/A";
        String categoryName = "N/A";
        if (transaction.getMerchant() != null) {
            merchantName = transaction.getMerchant().getName();
            categoryName = transaction.getMerchant().getCategoryCode();
        }

        return new RewardHistoryItemDto(
                transaction.getId(),
                merchantName,
                categoryName,
                transaction.getAmount(),
                rewardPoint.getPoints(),
                rewardPoint.getEsgScore(),
                transaction.getTxTime()
        );
    }
}
