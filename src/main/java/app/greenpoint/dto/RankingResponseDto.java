package app.greenpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RankingResponseDto {
    private List<RankingItemDto> topRankings;
}
