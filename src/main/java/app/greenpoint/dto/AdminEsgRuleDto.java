package app.greenpoint.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminEsgRuleDto {
    @Schema(description = "규칙 이름", example = "단골 보너스")
    @NotBlank
    private String name;

    @Schema(description = "규칙 조건 (JSON 형식)", example = "{\"type\": \"VISIT_COUNT\", \"merchant_id\": 123, \"count\": 5}")
    private String conditionJson;

    @Schema(description = "점수 계산식", example = "baseScore * 1.1 + 5")
    private String scoreFormula;
}
