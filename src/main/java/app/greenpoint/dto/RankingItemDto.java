package app.greenpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RankingItemDto {
    private int rank;
    private String nickname;
    private int level;
    private int points;
}
