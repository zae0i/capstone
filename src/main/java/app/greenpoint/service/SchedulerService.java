package app.greenpoint.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
    private final ReportService reportService;

    /**
     * 매일 오전 2시에 모든 사용자의 전날 리포트를 캐시합니다.
     */
    @Scheduled(cron = "0 0 2 * * ?") // Every day at 2 AM
    public void cachePreviousDayReports() {
        logger.info("Starting daily report caching for all users...");
        try {
            reportService.cacheDailyReportsForAllUsers();
            logger.info("Successfully cached daily reports for all users.");
        } catch (Exception e) {
            logger.error("Error during daily report caching", e);
        }
    }
}
