package app.greenpoint.service;

import app.greenpoint.domain.AppUser;
import app.greenpoint.domain.UserBadge;
import app.greenpoint.dto.UserBadgeDto;
import app.greenpoint.dto.UserProfileDto;
import app.greenpoint.repository.AppUserRepository;
import app.greenpoint.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;
    private final UserBadgeRepository userBadgeRepository;

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<UserBadge> userBadges = userBadgeRepository.findByUser(user);

        List<UserBadgeDto> badgeDtos = userBadges.stream()
                .map(userBadge -> new UserBadgeDto(
                        userBadge.getBadge().getCode(),
                        userBadge.getBadge().getName(),
                        userBadge.getAcquiredAt()))
                .collect(Collectors.toList());

        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRegion(),
                user.getLevel(),
                user.getPoints(),
                badgeDtos
        );
    }
}
