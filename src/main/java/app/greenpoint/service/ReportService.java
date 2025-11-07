package app.greenpoint.service;

import app.greenpoint.domain.*;
import app.greenpoint.dto.CategoryBreakdownDto;
import app.greenpoint.dto.ReportResponseDto;
import app.greenpoint.dto.TopMerchantDto;
import app.greenpoint.repository.AppUserRepository;
import app.greenpoint.repository.RewardPointRepository;
import app.greenpoint.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AppUserRepository appUserRepository;
    private final TransactionRepository transactionRepository;
    private final RewardPointRepository rewardPointRepository;

    private static final double CARBON_SAVED_MULTIPLIER = 0.05;

    @Transactional(readOnly = true)
    public ReportResponseDto generateReport(Long userId, String period) {
        // 1. Parse period and find user
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(period); // Expects "YYYY-MM"
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid period format. Expected YYYY-MM.");
        }
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // 2. Fetch all data for the period
        List<Transaction> transactions = transactionRepository.findAllByUserAndTxTimeBetween(user, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        if (transactions.isEmpty()) {
            return ReportResponseDto.builder().period(period).build(); // Return empty report
        }
        List<RewardPoint> rewards = rewardPointRepository.findAllByTransactionIn(transactions);
        Map<Long, RewardPoint> rewardsMap = rewards.stream()
                .collect(Collectors.toMap(r -> r.getTransaction().getId(), Function.identity()));

        // 3. Perform calculations
        int totalSpend = transactions.stream().mapToInt(Transaction::getAmount).sum();
        int esgScoreTotal = rewards.stream().mapToInt(RewardPoint::getEsgScore).sum();

        long greenMerchantsUsed = transactions.stream()
                .map(Transaction::getMerchant)
                .filter(m -> m != null && (m.getEsgTier() == Merchant.EsgTier.A || m.getEsgTier() == Merchant.EsgTier.B))
                .distinct()
                .count();

        double carbonSavedKg = esgScoreTotal * CARBON_SAVED_MULTIPLIER;

        // 4. Aggregate breakdowns
        // Category Breakdown
        Map<String, List<Transaction>> byCategory = transactions.stream()
                .filter(t -> t.getMerchant() != null && t.getMerchant().getCategoryCode() != null)
                .collect(Collectors.groupingBy(t -> t.getMerchant().getCategoryCode()));

        List<CategoryBreakdownDto> categoryBreakdown = byCategory.entrySet().stream()
                .map(entry -> {
                    String categoryCode = entry.getKey();
                    long count = entry.getValue().size();
                    int points = entry.getValue().stream()
                            .mapToInt(t -> rewardsMap.getOrDefault(t.getId(), new RewardPoint()).getPoints())
                            .sum();
                    return new CategoryBreakdownDto(categoryCode, count, points);
                })
                .sorted(Comparator.comparingInt(CategoryBreakdownDto::getPoints).reversed())
                .collect(Collectors.toList());

        // Top Merchants
        Map<Merchant, List<Transaction>> byMerchant = transactions.stream()
                .filter(t -> t.getMerchant() != null)
                .collect(Collectors.groupingBy(Transaction::getMerchant));

        List<TopMerchantDto> topMerchants = byMerchant.entrySet().stream()
                .map(entry -> {
                    String merchantName = entry.getKey().getName();
                    long visits = entry.getValue().size();
                    int points = entry.getValue().stream()
                            .mapToInt(t -> rewardsMap.getOrDefault(t.getId(), new RewardPoint()).getPoints())
                            .sum();
                    return new TopMerchantDto(merchantName, visits, points);
                })
                .sorted(Comparator.comparingInt(TopMerchantDto::getPoints).reversed())
                .limit(5) // Top 5
                .collect(Collectors.toList());

        // 5. Build and return response
        return ReportResponseDto.builder()
                .period(period)
                .totalSpend(totalSpend)
                .greenMerchantsUsed(greenMerchantsUsed)
                .carbonSavedKg(carbonSavedKg)
                .categoryBreakdown(categoryBreakdown)
                .topMerchants(topMerchants)
                .esgScoreTotal(esgScoreTotal)
                .build();
    }
}
