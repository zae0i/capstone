package app.greenpoint.dto;

import app.greenpoint.domain.Merchant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdminMerchantDto {
    @Schema(description = "가맹점 이름", example = "그린카페")
    @NotBlank
    private String name;

    @Schema(description = "카테고리 코드", example = "ECO")
    @NotBlank
    private String categoryCode;

    @Schema(description = "위도", example = "37.5512345")
    @NotNull
    private BigDecimal lat;

    @Schema(description = "경도", example = "126.9812345")
    @NotNull
    private BigDecimal lng;

    @Schema(description = "지역", example = "Seoul")
    @NotBlank
    private String region;

    @Schema(description = "ESG 등급", example = "A")
    @NotNull
    private Merchant.EsgTier esgTier;
}
