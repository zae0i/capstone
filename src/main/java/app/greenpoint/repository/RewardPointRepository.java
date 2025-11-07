package app.greenpoint.repository;

import app.greenpoint.domain.AppUser;
import app.greenpoint.domain.RewardPoint;
import app.greenpoint.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RewardPointRepository extends JpaRepository<RewardPoint, Long> {
    List<RewardPoint> findTop5ByUserOrderByCreatedAtDesc(AppUser user);
    Page<RewardPoint> findByUserAndCreatedAtBetween(AppUser user, LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<RewardPoint> findAllByTransactionIn(List<Transaction> transactions);
}
