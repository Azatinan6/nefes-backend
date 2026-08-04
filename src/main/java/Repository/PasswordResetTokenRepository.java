package Repository;

import Entity.PasswordResetToken;
import Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Şifre Sıfırlama Token Repository'si — password_reset_tokens tablosunu yönetir.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    // Token değerine göre kaydı bulur — şifre sıfırlama aşamasında kullanılır
    Optional<PasswordResetToken> findByToken(String token);

    // Belirli bir kullanıcıya ait tüm eski token'ları siler
    // Yeni token istendiğinde önceki token'lar temizlenir (güvenlik için)
    void deleteByUser(User user);
}
