package app.greenpoint.controller;

import app.greenpoint.dto.RankingResponseDto;
import app.greenpoint.dto.RewardHistoryItemDto;
import app.greenpoint.dto.UserBalanceDto;
import app.greenpoint.service.RankingService;
import app.greenpoint.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Rewards", description = "APIs for rewards, history, and ranking")
@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;
    private final RankingService rankingService;

    @Operation(summary = "Get user balance and recent rewards",
               description = "Fetches a user's current point balance, level, and a list of their 5 most recent reward activities.")
    @GetMapping("/{userId}/balance")
    public ResponseEntity<UserBalanceDto> getUserBalance(@PathVariable Long userId) {
        UserBalanceDto userBalance = rewardService.getUserBalance(userId);
        return ResponseEntity.ok(userBalance);
    }

    @Operation(summary = "Get reward history for current user",
               description = "Fetches a paginated history of rewards for the currently authenticated user within a date range. Requires authentication.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/history")
    public ResponseEntity<Page<RewardHistoryItemDto>> getRewardHistory(
            @Parameter(description = "Start date for the query (YYYY-MM-DD)") @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date for the query (YYYY-MM-DD)") @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Page number, starting from 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String userEmail = authentication.getName();
        // Create pageable object, sorting by creation date descending
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<RewardHistoryItemDto> historyPage = rewardService.getRewardHistory(userEmail, from, to, pageable);
        return ResponseEntity.ok(historyPage);
    }

    @Operation(summary = "Get regional ranking",
               description = "Fetches the top 10 user ranking for a given region and period, and includes the current user's rank. Requires authentication.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/ranking")
    public ResponseEntity<RankingResponseDto> getRanking(
            @Parameter(description = "Region to get the ranking for") @RequestParam String region,
            @Parameter(description = "Period to get the ranking for (e.g., 2025-10). Note: Currently returns live data, not historical.") @RequestParam String period,
            Authentication authentication) {

        String userEmail = authentication.getName();
        RankingResponseDto rankingResponse = rankingService.getRanking(region, period, userEmail);
        return ResponseEntity.ok(rankingResponse);
    }
}
