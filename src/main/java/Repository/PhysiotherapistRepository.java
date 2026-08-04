package Repository;

import Entity.Physiotherapist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Fizyoterapist Repository'si — physiotherapists tablosu üzerindeki işlemleri yönetir.
 */
@Repository
public interface PhysiotherapistRepository extends JpaRepository<Physiotherapist, UUID> {

    // Davet koduna göre fizyoterapist bulur — hasta kayıt aşamasında kullanılır
    Optional<Physiotherapist> findByInviteCode(String inviteCode);

    // Bu davet kodu zaten kullanılıyor mu kontrol eder — benzersizlik garantisi
    boolean existsByInviteCode(String inviteCode);
}
