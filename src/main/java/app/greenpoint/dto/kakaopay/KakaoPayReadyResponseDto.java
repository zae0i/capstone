package app.greenpoint.dto.kakaopay;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KakaoPayReadyResponseDto {
    private String tid;
    private String next_redirect_app_url;
    private String next_redirect_mobile_url;
    private String next_redirect_pc_url;
    private String android_app_scheme;
    private String ios_app_scheme;
    private LocalDateTime created_at;
}
