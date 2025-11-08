package app.greenpoint.dto;

import app.greenpoint.domain.Transaction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionRequestDto {

    @JsonProperty("transactionAmount")
    @Schema(description = "결제 금액", example = "15800")
    @NotNull
    private Integer amount;

    @JsonProperty("transactionDate")
    @Schema(description = "결제 시간", example = "2025-10-31T12:30:00")
    private LocalDateTime txTime;

    @Schema(description = "결제 위치 정보")
    private GeoDto geo;

    @Schema(description = "가맹점 ID (없을 경우 null)", example = "45")
    private Long merchantId;

    @Schema(description = "결제 수단", example = "MOCK")
    @NotNull
    private Transaction.Source source;

    @Schema(description = "상품명", example = "초코파이")
    private String itemName;

    @Schema(description = "상품 수량", example = "1")
    private Integer quantity;
}
