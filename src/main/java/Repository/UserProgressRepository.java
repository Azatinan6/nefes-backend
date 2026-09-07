package Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import Entity.UserProgress;
import java.util.List;
import java.util.UUID;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    
    List<UserProgress> findByUserId(UUID userId);
}