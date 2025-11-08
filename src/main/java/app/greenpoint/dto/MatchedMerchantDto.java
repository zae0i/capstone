package app.greenpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class MatchedMerchantDto {
    private Long id;
    private String name;
    private BigDecimal lat;
    private BigDecimal lng;


}
