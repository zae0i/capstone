package app.greenpoint.repository;

import app.greenpoint.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    List<AppUser> findTop10ByRegionOrderByPointsDesc(String region);

    List<AppUser> findTop10ByOrderByPointsDesc();

    long countByRegionAndPointsGreaterThan(String region, int points);

    long countByPointsGreaterThan(int points);

    @Query("SELECT DISTINCT a.region FROM AppUser a")
    List<String> findDistinctRegions();
}
