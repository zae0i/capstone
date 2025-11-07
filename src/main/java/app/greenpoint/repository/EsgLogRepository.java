package app.greenpoint.repository;

import app.greenpoint.domain.EsgLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EsgLogRepository extends JpaRepository<EsgLog, Long> {
}
