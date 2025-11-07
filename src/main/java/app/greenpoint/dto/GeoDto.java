package app.greenpoint.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GeoDto {
    private BigDecimal lat;
    private BigDecimal lng;
}
