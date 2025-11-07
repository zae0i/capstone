package app.greenpoint.dto;

import app.greenpoint.domain.Merchant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TransactionResponseDto {
    private Long txId;
    private MatchedMerchantDto matchedMerchant;
    private int esgScore;
    private int pointsEarned;
    private int userPoints;
    private Merchant.EsgTier tier;
}
