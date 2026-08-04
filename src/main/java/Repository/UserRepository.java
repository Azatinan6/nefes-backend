package Repository;

import Entity.User;
import Entity.User.Role;
import Entity.User.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kullanıcı Repository'si — users tablosu üzerindeki veritabanı işlemlerini yönetir.
 * JpaRepository sayesinde temel CRUD işlemleri (save, findById, delete...) otomatik gelir.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // E-posta adresine göre kullanıcı bulur — login ve kayıt sırasında kullanılır
    Optional<User> findByEmail(String email);

    // Belirtilen role sahip tüm kullanıcıları listeler (örn: tüm fizyoterapistler)
    List<User> findByRole(Role role);

    // Belirtilen statüdeki tüm kullanıcıları listeler (örn: PENDING durumundaki fizyoterapistler)
    List<User> findByStatus(Status status);

    // Belirtilen rol VE statüde olan kullanıcıları listeler
    // Örnek kullanım: Admin panelinde "Onay bekleyen fizyoterapistler" listesi
    List<User> findByRoleAndStatus(Role role, Status status);

    // Bu e-posta adresiyle kayıtlı kullanıcı var mı kontrol eder — mükerrer kayıt önleme
    boolean existsByEmail(String email);
}
