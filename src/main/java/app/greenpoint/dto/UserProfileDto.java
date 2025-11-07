package app.greenpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String email;
    private String nickname;
    private String region;
    private int level;
    private int points;
    private List<UserBadgeDto> badges;
}
