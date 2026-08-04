package Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Entity.UserProgress;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long>{

}
