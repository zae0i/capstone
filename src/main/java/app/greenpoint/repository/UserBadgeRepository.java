package app.greenpoint.repository;

import app.greenpoint.domain.AppUser;
import app.greenpoint.domain.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUser(AppUser user);
}
