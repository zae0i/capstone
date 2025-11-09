package app.greenpoint.repository;

import app.greenpoint.domain.RankingSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RankingSnapshotRepository extends JpaRepository<RankingSnapshot, Long> {
    Optional<RankingSnapshot> findByRegionAndPeriodYm(String region, String periodYm);
}
