package app.greenpoint.service;

import app.greenpoint.domain.AppUser;
import app.greenpoint.dto.MyRankDto;
import app.greenpoint.dto.RankingItemDto;
import app.greenpoint.dto.RankingResponseDto;
import app.greenpoint.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public RankingResponseDto getRanking() {
        List<AppUser> allUsers = appUserRepository.findAll();

        // Sort users by points in descending order
        allUsers.sort(Comparator.comparingInt(AppUser::getPoints).reversed());

        // Create DTOs with ranks
        List<RankingItemDto> rankings = IntStream.range(0, allUsers.size())
                .mapToObj(i -> {
                    AppUser user = allUsers.get(i);
                    return new RankingItemDto(i + 1, user.getNickname(), user.getLevel(), user.getPoints());
                })
                .collect(Collectors.toList());

        return new RankingResponseDto(rankings);
    }

    @Transactional(readOnly = true)
    public MyRankDto getMyRank(String userEmail) {
        AppUser currentUser = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        long rank = appUserRepository.countByPointsGreaterThan(currentUser.getPoints()) + 1;

        return new MyRankDto(rank, currentUser.getNickname(), currentUser.getLevel(), currentUser.getPoints());
    }
}
