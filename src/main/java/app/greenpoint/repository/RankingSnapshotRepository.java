package app.greenpoint.repository;

import app.greenpoint.domain.RankingSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingSnapshotRepository extends JpaRepository<RankingSnapshot, Long> {
}
