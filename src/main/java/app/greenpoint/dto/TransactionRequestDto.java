package app.greenpoint.dto;

import app.greenpoint.domain.Transaction;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionRequestDto {

    @NotNull
    private Integer amount;

    @NotNull
    private LocalDateTime txTime;

    private GeoDto geo;

    private Long merchantId;

    @NotNull
    private Transaction.Source source;
}
