package app.greenpoint.controller;

import app.greenpoint.dto.MyRankDto;
import app.greenpoint.dto.RankingResponseDto;
import app.greenpoint.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "랭킹", description = "사용자 랭킹 조회 API")
@RestController
@RequestMapping("/api/v1/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "전체 사용자 랭킹 조회",
               description = "전체 사용자의 포인트 기준 랭킹을 조회합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<RankingResponseDto> getRanking() {
        RankingResponseDto rankingResponse = rankingService.getRanking();
        return ResponseEntity.ok(rankingResponse);
    }

    @Operation(summary = "내 랭킹 조회",
            description = "현재 로그인된 사용자의 랭킹 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/my-rank")
    public ResponseEntity<MyRankDto> getMyRank(Authentication authentication) {
        String userEmail = authentication.getName();
        MyRankDto myRankDto = rankingService.getMyRank(userEmail);
        return ResponseEntity.ok(myRankDto);
    }
}
