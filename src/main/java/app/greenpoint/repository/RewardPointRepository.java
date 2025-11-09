package app.greenpoint.repository;

import app.greenpoint.domain.AppUser;
import app.greenpoint.domain.RewardPoint;
import app.greenpoint.domain.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RewardPointRepository extends JpaRepository<RewardPoint, Long> {
    List<RewardPoint> findTop5ByUserOrderByCreatedAtDesc(AppUser user);
    @Query("SELECT rp FROM RewardPoint rp JOIN FETCH rp.transaction t LEFT JOIN FETCH t.merchant WHERE rp.user = :user AND rp.createdAt BETWEEN :start AND :end")
    Page<RewardPoint> findByUserAndCreatedAtBetween(@Param("user") AppUser user, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
    List<RewardPoint> findAllByTransactionIn(List<Transaction> transactions);
    Optional<RewardPoint> findByTransaction(Transaction transaction);
}
