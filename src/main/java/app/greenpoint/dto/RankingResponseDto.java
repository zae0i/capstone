package app.greenpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RankingResponseDto {
    private String region;
    private String period;
    private List<RankingItemDto> top;
    private MyRankDto me;
}
