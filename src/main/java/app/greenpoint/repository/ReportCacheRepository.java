package app.greenpoint.repository;

import app.greenpoint.domain.AppUser;
import app.greenpoint.domain.ReportCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportCacheRepository extends JpaRepository<ReportCache, Long> {
    Optional<ReportCache> findByUserAndPeriod(AppUser user, String period);
}
