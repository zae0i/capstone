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

@Tag(name = "Users", description = "User profile and report APIs")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ReportService reportService;

    @Operation(summary = "Get current user's profile",
               description = "Fetches the profile information, points, level, and badges for the currently authenticated user.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        UserProfileDto userProfile = userService.getUserProfile(email);
        return ResponseEntity.ok(userProfile);
    }

    @Operation(summary = "Get user's monthly report",
               description = "Fetches a monthly activity report for a given user. Users can only access their own reports, unless they are an ADMIN. Requires authentication.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{userId}/report")
    public ResponseEntity<ReportResponseDto> getUserReport(
            @Parameter(description = "ID of the user to generate the report for") @PathVariable Long userId,
            @Parameter(description = "The report period in YYYY-MM format") @RequestParam String period,
            Authentication authentication) {

        // Authorization check
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !currentUser.getAppUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to access this report.");
        }

        ReportResponseDto report = reportService.generateReport(userId, period);
        return ResponseEntity.ok(report);
    }
}
