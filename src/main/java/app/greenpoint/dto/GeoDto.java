package app.greenpoint.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GeoDto {
    @Schema(description = "위도", example = "37.55")
    private BigDecimal lat;
    @Schema(description = "경도", example = "126.98")
    private BigDecimal lng;
}
