package app.greenpoint.service;

import app.greenpoint.domain.*;
import app.greenpoint.dto.CategoryBreakdownDto;
import app.greenpoint.dto.ReportResponseDto;
import app.greenpoint.dto.TopMerchantDto;
import app.greenpoint.repository.AppUserRepository;
import app.greenpoint.repository.ReportCacheRepository;
import app.greenpoint.repository.RewardPointRepository;
import app.greenpoint.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final AppUserRepository appUserRepository;
    private final TransactionRepository transactionRepository;
    private final RewardPointRepository rewardPointRepository;
    private final ReportCacheRepository reportCacheRepository;
    private final ObjectMapper objectMapper;

    private static final double CARBON_SAVED_MULTIPLIER = 0.05;
    private static final DateTimeFormatter DAILY_PERIOD_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD

    @Transactional
    public ReportResponseDto getMonthlyReport(Long userId, String period) {
        // 1. Find user
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // 2. Check cache first
        Optional<ReportCache> cachedReportOpt = reportCacheRepository.findByUserAndPeriod(user, period);
        if (cachedReportOpt.isPresent()) {
            try {
                log.debug("Returning cached monthly report for user {} and period {}", userId, period);
                return objectMapper.readValue(cachedReportOpt.get().getPayloadJson(), ReportResponseDto.class);
            } catch (Exception e) {
                log.error("Error deserializing cached report for user {} and period {}", userId, period, e);
                // If deserialization fails, proceed to generate a new one
            }
        }

        // 3. Generate, cache, and return if not in cache
        log.debug("Generating new monthly report for user {} and period {}", userId, period);
        ReportResponseDto newReport = buildMonthlyReport(user, period);
        cacheReport(user, period, newReport);
        return newReport;
    }

    @Transactional
    public ReportResponseDto getDailyReport(Long userId, LocalDate day) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        String period = day.format(DAILY_PERIOD_FORMAT);

        // For the current day, always generate a live report to reflect recent transactions
        if (day.isEqual(LocalDate.now())) {
            log.debug("Generating live daily report for user {} for today's date {}", userId, period);
            return buildDailyReport(user, day);
        }

        // For past days, use the cache
        Optional<ReportCache> cachedReportOpt = reportCacheRepository.findByUserAndPeriod(user, period);
        if (cachedReportOpt.isPresent()) {
            try {
                log.debug("Returning cached daily report for user {} and period {}", userId, period);
                return objectMapper.readValue(cachedReportOpt.get().getPayloadJson(), ReportResponseDto.class);
            } catch (Exception e) {
                log.error("Error deserializing cached daily report for user {} and period {}", userId, period, e);
            }
        }

        // Generate, cache, and return if not in cache for a past day
        log.debug("Generating new daily report for user {} and period {}", userId, period);
        ReportResponseDto newReport = buildDailyReport(user, day);
        cacheReport(user, period, newReport);
        return newReport;
    }

    @Transactional
    public void cacheDailyReportsForAllUsers() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String period = yesterday.format(DAILY_PERIOD_FORMAT);
        List<AppUser> allUsers = appUserRepository.findAll();

        log.info("Starting to cache daily reports for {} users for period {}", allUsers.size(), period);

        for (AppUser user : allUsers) {
            try {
                ReportResponseDto report = buildDailyReport(user, yesterday);
                cacheReport(user, period, report);
                log.debug("Successfully cached daily report for user {}", user.getId());
            } catch (Exception e) {
                log.error("Failed to cache daily report for user {}", user.getId(), e);
            }
        }
        log.info("Finished caching daily reports for period {}", period);
    }

    private void cacheReport(AppUser user, String period, ReportResponseDto report) {
        try {
            String payloadJson = objectMapper.writeValueAsString(report);
            ReportCache reportCache = ReportCache.builder()
                    .user(user)
                    .period(period)
                    .payloadJson(payloadJson)
                    .build();
            // Use a method that overwrites existing cache for the same user and period
            reportCacheRepository.save(reportCache);
        } catch (Exception e) {
            log.error("Failed to serialize and cache report for user {} and period {}", user.getId(), period, e);
        }
    }

    private ReportResponseDto buildMonthlyReport(AppUser user, String period) {
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(period); // Expects "YYYY-MM"
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid period format. Expected YYYY-MM.");
        }
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return buildReport(user, startDate, endDate, period);
    }

    private ReportResponseDto buildDailyReport(AppUser user, LocalDate day) {
        return buildReport(user, day, day, day.format(DAILY_PERIOD_FORMAT));
    }

    private ReportResponseDto buildReport(AppUser user, LocalDate startDate, LocalDate endDate, String period) {
        List<Transaction> transactions = transactionRepository.findAllByUserAndTxTimeBetween(user, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        if (transactions.isEmpty()) {
            return ReportResponseDto.builder().period(period).totalSpend(0).greenMerchantsUsed(0L).carbonSavedKg(0.0).esgScoreTotal(0).categoryBreakdown(List.of()).topMerchants(List.of()).build();
        }
        List<RewardPoint> rewards = rewardPointRepository.findAllByTransactionIn(transactions);
        Map<Long, RewardPoint> rewardsMap = rewards.stream()
                .collect(Collectors.toMap(r -> r.getTransaction().getId(), Function.identity()));

        int totalSpend = transactions.stream().mapToInt(Transaction::getAmount).sum();
        int esgScoreTotal = rewards.stream().mapToInt(RewardPoint::getEsgScore).sum();

        long greenMerchantsUsed = transactions.stream()
                .map(Transaction::getMerchant)
                .filter(m -> m != null && (m.getEsgTier() == Merchant.EsgTier.A || m.getEsgTier() == Merchant.EsgTier.B))
                .distinct()
                .count();

        double carbonSavedKg = esgScoreTotal * CARBON_SAVED_MULTIPLIER;

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
                .limit(5)
                .collect(Collectors.toList());

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
