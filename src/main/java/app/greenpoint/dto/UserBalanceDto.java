package app.greenpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserBalanceDto {
    private int points;
    private int level;
    private List<RecentRewardDto> recent;
}
