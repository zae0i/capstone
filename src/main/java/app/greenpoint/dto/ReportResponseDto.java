package app.greenpoint.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ReportResponseDto {
    private String period;
    private int totalSpend;
    private long greenMerchantsUsed;
    private double carbonSavedKg;
    private List<CategoryBreakdownDto> categoryBreakdown;
    private List<TopMerchantDto> topMerchants;
    private int esgScoreTotal;
}
