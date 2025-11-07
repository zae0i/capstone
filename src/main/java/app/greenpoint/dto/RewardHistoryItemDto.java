package app.greenpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class RewardHistoryItemDto {
    private Long txId;
    private String merchantName;
    private int points;
    private int esgScore;
    private LocalDateTime txTime;
}
