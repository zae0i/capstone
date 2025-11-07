package app.greenpoint.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCategoryDto {
    @Schema(description = "카테고리 코드", example = "ECO")
    @NotBlank
    private String categoryCode;

    @Schema(description = "카테고리 이름", example = "친환경 매장")
    @NotBlank
    private String name;

    @Schema(description = "ESG 가중치", example = "1.2")
    @NotNull
    private Double esgWeight;
}
