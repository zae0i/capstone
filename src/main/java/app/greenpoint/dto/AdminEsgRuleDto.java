package app.greenpoint.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminEsgRuleDto {
    @NotBlank
    private String name;
    private String conditionJson;
    private String scoreFormula;
}
