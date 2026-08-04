package Config;

import Entity.User;
import Entity.User.Role;
import Entity.User.Status;
import Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Veri Başlatıcı — Uygulama açıldığında çalışır.
 *
 * Amaç: Sistemde hiç admin yoksa ilk admin hesabını otomatik oluşturur.
 * BCryptPasswordEncoder kullanıldığı için şifre GERÇEK hash değeriyle kaydedilir.
 *
 * Varsayılan Admin Bilgileri:
 *   E-posta : admin@nefes.com
 *   Şifre   : Nefes@2026!
 *
 * ÖNEMLİ: Production ortamına geçmeden şifreyi değiştirin!
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    // Kullanıcı veritabanı işlemleri için repository
    private final UserRepository userRepository;

    // BCrypt ile şifreleme — SecurityConfig'den inject edilir
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {

        // admin@nefes.com adresiyle kayıtlı kullanıcı var mı kontrol et
        if (!userRepository.existsByEmail("admin@nefes.com")) {

            // Admin hesabı yoksa oluştur
            User admin = new User();
            admin.setFullName("Sistem Yöneticisi");
            admin.setEmail("admin@nefes.com");

            // Şifreyi BCryptPasswordEncoder ile hashle — gerçek hash değeri kaydedilir
            // Düz metin "Nefes@2026!" veritabanına ASLA gitmiyor, sadece BCrypt hash'i gidiyor
            admin.setPasswordHash(passwordEncoder.encode("Nefes@2026!"));

            admin.setRole(Role.ROLE_ADMIN);
            admin.setStatus(Status.ACTIVE); // Admin direkt aktif

            userRepository.save(admin);

            // Konsola bilgi mesajı bas — sadece geliştirme ortamı için
            System.out.println("========================================");
            System.out.println("✅ İlk admin hesabı oluşturuldu:");
            System.out.println("   E-posta : admin@nefes.com");
            System.out.println("   Şifre   : Nefes@2026!");
            System.out.println("   (Lütfen production'da şifreni değiştir!)");
            System.out.println("========================================");
        }
    }
}
