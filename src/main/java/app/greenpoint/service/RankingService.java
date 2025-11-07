package app.greenpoint.service;

import app.greenpoint.domain.AppUser;
import app.greenpoint.dto.MyRankDto;
import app.greenpoint.dto.RankingItemDto;
import app.greenpoint.dto.RankingResponseDto;
import app.greenpoint.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public RankingResponseDto getRanking(String region, String period, String userEmail) {
        // 1. Fetch Top 10 Users
        List<AppUser> topUsers = appUserRepository.findTop10ByRegionOrderByPointsDesc(region);

        AtomicInteger rankCounter = new AtomicInteger(1);
        List<RankingItemDto> topRankingItems = topUsers.stream()
                .map(user -> new RankingItemDto(
                        rankCounter.getAndIncrement(),
                        user.getNickname(),
                        user.getPoints()))
                .collect(Collectors.toList());

        // 2. Fetch Current User's Rank
        AppUser currentUser = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        MyRankDto myRank = null;
        // Only calculate "my rank" if the user belongs to the queried region
        if (currentUser.getRegion().equals(region)) {
            long usersWithMorePoints = appUserRepository.countByRegionAndPointsGreaterThan(region, currentUser.getPoints());
            long myRankValue = usersWithMorePoints + 1;
            myRank = new MyRankDto(myRankValue, currentUser.getPoints());
        }

        // Note: The 'period' parameter is ignored in this live-query implementation.
        // A full implementation would query a snapshot table based on the period.
        return new RankingResponseDto(region, period, topRankingItems, myRank);
    }
}
