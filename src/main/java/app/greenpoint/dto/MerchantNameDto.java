package app.greenpoint.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantNameDto {
    @Schema(description = "가맹점 ID", example = "1")
    private Long id;

    @Schema(description = "가맹점 이름", example = "그린카페")
    private String name;
}
