package app.greenpoint.dto;

import app.greenpoint.domain.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TransactionHistoryDto {
    private Long txId;
    private LocalDateTime txTime;
    private int amount;
    private Transaction.Source source;
    private Transaction.Status status;
    private String merchantName;
    private String categoryName;
    private int esgScore;
    private int pointsEarned;
}
