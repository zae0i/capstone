package app.greenpoint.dto;

import app.greenpoint.domain.Merchant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdminMerchantDto {
    @NotBlank
    private String name;
    @NotBlank
    private String categoryCode;
    @NotNull
    private BigDecimal lat;
    @NotNull
    private BigDecimal lng;
    @NotBlank
    private String region;
    @NotNull
    private Merchant.EsgTier esgTier;
}
