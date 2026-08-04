package Service;

import Config.JwtService;
import DTO.*;
import Entity.*;
import Entity.User.Role;
import Entity.User.Status;
import Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kimlik Doğrulama Servisi — Giriş, kayıt ve şifre sıfırlama işlemlerini yönetir.
 *
 * Güvenlik prensipleri:
 * - Şifreler BCrypt ile hashlanır, düz metin ASLA saklanmaz
 * - JWT token üretimi bu servis üzerinden yapılır
 * - Fizyoterapist kayıtları PENDING durumda başlar (admin onayı gerekir)
 * - Hasta/aile kayıtları direkt ACTIVE durumda başlar
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PhysiotherapistRepository physiotherapistRepository;
    private final PatientRepository patientRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService; // E-posta gönderme servisi

    /**
     * Giriş işlemi — E-posta ve şifre doğrulanır, JWT token üretilir.
     * @param request Giriş bilgileri (email + password)
     * @return JWT token ve kullanıcı bilgilerini içeren yanıt
     */
    public AuthResponse login(LoginRequest request) {

        // Kullanıcıyı e-posta adresine göre bul
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("E-posta veya şifre hatalı"));

        // Giriş yapılmak istenen şifreyi BCrypt hashiyle karşılaştır
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("E-posta veya şifre hatalı");
        }

        // Fizyoterapist onay kontrolü — PENDING veya REJECTED durumundaki fizyo giriş yapamaz
        if (user.getRole() == Role.ROLE_FIZYO) {
            if (user.getStatus() == Status.PENDING) {
                throw new RuntimeException("Hesabınız henüz admin tarafından onaylanmamıştır. Lütfen bekleyiniz.");
            }
            if (user.getStatus() == Status.REJECTED) {
                throw new RuntimeException("Başvurunuz reddedilmiştir. Detaylar için bizimle iletişime geçin.");
            }
        }

        // Kullanıcı aktif değilse giriş engelleE
        if (user.getStatus() == Status.REJECTED) {
            throw new RuntimeException("Bu hesap devre dışı bırakılmıştır.");
        }

        // Başarılı giriş — JWT token üret
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId().toString()
        );

        // Token ve kullanıcı bilgilerini döndür
        return new AuthResponse(
                token,
                user.getRole().name(),
                user.getFullName(),
                user.getEmail(),
                user.getId().toString()
        );
    }

    /**
     * Fizyoterapist kayıt işlemi — PENDING durumda kaydedilir, admin onayı beklenir.
     * @param request Fizyoterapist kayıt bilgileri
     */
    @Transactional
    public void registerFizyo(RegisterFizyoRequest request) {

        // Bu e-posta zaten kayıtlı mı kontrol et
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu e-posta adresi zaten kayıtlı");
        }

        // Kullanıcı kaydını oluştur
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        // Şifreyi BCrypt ile hashle — veritabanına düz metin gitmez
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_FIZYO);
        user.setStatus(Status.PENDING); // Admin onayı bekleniyor

        User savedUser = userRepository.save(user);

        // Fizyoterapist profili oluştur — lisans ve uzmanlık bilgileriyle
        Physiotherapist fizyo = new Physiotherapist();
        // @MapsId kullanıldığında ID, user ilişkisinden otomatik alınır — setId() çağrılmaz
        fizyo.setUser(savedUser);
        fizyo.setLicenseNumber(request.getLicenseNumber());
        fizyo.setSpecialization(request.getSpecialization());
        fizyo.setPatientCount(0);
        // Davet kodu henüz oluşturulmaz — admin onaylayınca atanacak
        physiotherapistRepository.save(fizyo);

        // Fizyoterapiste "başvuru alındı" e-postası gönder — mail hatası kaydı engellemesin
        try {
            emailService.sendFizyoApplicationReceived(user.getEmail(), user.getFullName());
        } catch (Exception e) {
            System.err.println("[UYARI] E-posta gönderilemedi: " + e.getMessage());
        }
    }

    /**
     * Hasta/Aile kayıt işlemi — Davet kodu doğrulanır, direkt ACTIVE olarak kaydedilir.
     * @param request Hasta/aile kayıt bilgileri (davet kodu dahil)
     */
    @Transactional
    public void registerPatient(RegisterPatientRequest request) {

        // Bu e-posta zaten kayıtlı mı kontrol et
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu e-posta adresi zaten kayıtlı");
        }

        // Davet kodunun geçerliliğini kontrol et
        Physiotherapist fizyo = physiotherapistRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new RuntimeException("Geçersiz davet kodu. Lütfen fizyoterapistinizden doğru kodu alın."));

        // Fizyoterapistin onaylanmış olması gerekiyor
        if (fizyo.getUser().getStatus() != Status.APPROVED) {
            throw new RuntimeException("Bu davet kodu şu anda aktif değil.");
        }

        // Kapasite kontrolü — 40 hasta sınırı
        if (!fizyo.hasCapacity()) {
            throw new RuntimeException("Bu fizyoterapistin hasta kapasitesi dolmuştur (maksimum 40 hasta). Lütfen farklı bir fizyoterapist ile iletişime geçin.");
        }

        // Rol belirle — request'ten gelen değere göre (AILE veya COCUK)
        Role userRole;
        try {
            userRole = Role.valueOf("ROLE_" + request.getRole().toUpperCase());
            // Sadece AILE ve COCUK rolleri kabul edilir
            if (userRole != Role.ROLE_AILE && userRole != Role.ROLE_COCUK) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Geçersiz kayıt tipi. Lütfen 'AILE' veya 'COCUK' seçin.");
        }

        // Kullanıcı kaydını oluştur
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(userRole);
        user.setStatus(Status.ACTIVE); // Direkt aktif

        User savedUser = userRepository.save(user);

        // Hasta profili oluştur ve fizyoterapiste bağla
        // @MapsId kullanıldığında ID, user ilişkisinden otomatik alınır — setId() çağrılmaz
        Patient patient = new Patient();
        patient.setUser(savedUser);
        patient.setPhysiotherapist(fizyo);
        patientRepository.save(patient);

        // Fizyoterapistin hasta sayısını artır
        fizyo.setPatientCount(fizyo.getPatientCount() + 1);
        physiotherapistRepository.save(fizyo);
    }

    /**
     * Şifremi Unuttum — Kullanıcıya e-posta ile sıfırlama bağlantısı gönderir.
     * Güvenlik notu: E-posta sistemde yoksa da "mail gönderildi" yanıtı döner.
     * Bu sayede kötü niyetli kişiler sistemdeki e-postaları test edemez.
     * @param request Şifre sıfırlama isteği (sadece e-posta)
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        // Kullanıcıyı bul — bulunamazsa sessizce geç (güvenlik gereği)
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {

            // Önceki sıfırlama token'larını temizle — eski linklerin işe yaramaması için
            passwordResetTokenRepository.deleteByUser(user);

            // Yeni güvenli token oluştur
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setUser(user);
            resetToken.setExpiresAt(LocalDateTime.now().plusHours(1)); // 1 saat geçerli
            resetToken.setUsed(false);

            passwordResetTokenRepository.save(resetToken);

            // Sıfırlama linkini e-posta ile gönder — mail hatası işlemi engellemesin
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken.getToken());
            } catch (Exception e) {
                System.err.println("[UYARI] E-posta gönderilemedi: " + e.getMessage());
            }
        });
    }

    /**
     * Şifre Sıfırlama — Token doğrulanır ve yeni şifre kaydedilir.
     * @param request Yeni şifre ve geçerli token
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        // Token'ı veritabanında bul
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Geçersiz şifre sıfırlama bağlantısı"));

        // Token'ın geçerliliğini kontrol et (süre ve kullanım durumu)
        if (!resetToken.isValid()) {
            throw new RuntimeException("Bu şifre sıfırlama bağlantısının süresi dolmuş veya daha önce kullanılmış. Lütfen yeni bir bağlantı talep edin.");
        }

        // Yeni şifreyi BCrypt ile hashle ve kaydet
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Token'ı kullanılmış olarak işaretle — ikinci kullanımı önlemek için
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
