package app.greenpoint.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignupRequestDto {

    @Schema(description = "사용자 이메일", example = "user@example.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "비밀번호 (8자 이상)", example = "password123")
    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Schema(description = "닉네임", example = "GreenUser")
    @NotBlank
    private String nickname;

    @Schema(description = "주요 활동 지역", example = "Seoul")
    @NotBlank
    private String region;
}
