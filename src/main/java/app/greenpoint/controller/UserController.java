package app.greenpoint.controller;

import app.greenpoint.dto.ReportResponseDto;
import app.greenpoint.dto.UserProfileDto;
import app.greenpoint.service.CustomUserDetails;
import app.greenpoint.service.ReportService;
import app.greenpoint.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "사용자", description = "사용자 프로필 및 리포트 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ReportService reportService;

    @Operation(summary = "내 프로필 조회",
               description = "현재 로그인된 사용자의 프로필 정보(포인트, 레벨, 배지 포함)를 조회합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        UserProfileDto userProfile = userService.getUserProfile(email);
        return ResponseEntity.ok(userProfile);
    }

    @Operation(summary = "월간 리포트 조회",
               description = "특정 사용자의 월간 활동 리포트를 조회합니다. 사용자는 자신의 리포트만 조회할 수 있으며, 관리자는 모든 사용자의 리포트를 조회할 수 있습니다. 인증이 필요합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{userId}/report")
    public ResponseEntity<ReportResponseDto> getUserReport(
            @Parameter(description = "리포트를 생성할 사용자의 ID") @PathVariable Long userId,
            @Parameter(description = "리포트 기간 (YYYY-MM 형식)") @RequestParam String period,
            Authentication authentication) {

        // 인가 확인
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !currentUser.getAppUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 리포트에 접근할 권한이 없습니다.");
        }

        ReportResponseDto report = reportService.generateReport(userId, period);
        return ResponseEntity.ok(report);
    }
}
