package Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Şifre Sıfırlama Token Entity'si — "Şifremi Unuttum" özelliği için kullanılır.
 * Kullanıcıya e-posta ile gönderilen tek kullanımlık güvenli token buraya kaydedilir.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    // Token'ın benzersiz veritabanı kimliği
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Kullanıcıya e-posta ile gönderilen güvenli token değeri
    // Her token yalnızca bir kez kullanılabilir
    @Column(nullable = false, unique = true)
    private String token;

    // Bu token'ın hangi kullanıcıya ait olduğu
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Token'ın geçerlilik bitiş tarihi — 1 saat sonra geçersiz olur
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // Token'ın daha önce kullanılıp kullanılmadığını tutar
    // Kullanılmış bir token'ı tekrar göndermeyi engeller
    @Column(nullable = false)
    private boolean used = false;

    /**
     * Token'ın hâlâ geçerli olup olmadığını kontrol eder.
     * Geçerlilik: Süresi dolmamış VE daha önce kullanılmamış olmalı.
     * @return Token geçerliyse true
     */
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }
}
