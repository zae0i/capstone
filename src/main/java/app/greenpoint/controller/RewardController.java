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

@Tag(name = "리워드", description = "포인트, 내역, 랭킹 관련 API")
@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;
    private final RankingService rankingService;

    @Operation(summary = "사용자 포인트 잔액 조회",
               description = "특정 사용자의 현재 포인트 잔액, 레벨, 그리고 최근 적립 내역 5건을 조회합니다.")
    @GetMapping("/{userId}/balance")
    public ResponseEntity<UserBalanceDto> getUserBalance(@PathVariable Long userId) {
        UserBalanceDto userBalance = rewardService.getUserBalance(userId);
        return ResponseEntity.ok(userBalance);
    }

    @Operation(summary = "내 포인트 적립 내역 조회",
               description = "현재 로그인된 사용자의 포인트 적립 내역을 기간별로 조회합니다. 인증이 필요합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/history")
    public ResponseEntity<Page<RewardHistoryItemDto>> getRewardHistory(
            @Parameter(description = "조회 시작일 (YYYY-MM-DD)") @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "조회 종료일 (YYYY-MM-DD)") @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 당 항목 수") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String userEmail = authentication.getName();
        // 페이징 객체 생성 (최신순 정렬)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<RewardHistoryItemDto> historyPage = rewardService.getRewardHistory(userEmail, from, to, pageable);
        return ResponseEntity.ok(historyPage);
    }

    @Operation(summary = "지역별 랭킹 조회",
               description = "특정 지역 및 기간의 상위 10위 랭킹과 현재 사용자의 순위를 조회합니다. 인증이 필요합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/ranking")
    public ResponseEntity<RankingResponseDto> getRanking(
            @Parameter(description = "랭킹을 조회할 지역") @RequestParam String region,
            @Parameter(description = "랭킹을 조회할 기간 (예: 2025-10). 참고: 현재는 실시간 데이터만 반환됩니다.") @RequestParam String period,
            Authentication authentication) {

        String userEmail = authentication.getName();
        RankingResponseDto rankingResponse = rankingService.getRanking(region, period, userEmail);
        return ResponseEntity.ok(rankingResponse);
    }
}
