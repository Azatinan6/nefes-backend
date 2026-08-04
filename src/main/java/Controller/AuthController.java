package Controller;

import DTO.*;
import Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Kimlik Doğrulama Controller'ı — Herkese açık auth endpoint'lerini yönetir.
 *
 * Endpoint'ler:
 *   POST /api/auth/login               → Giriş yap
 *   POST /api/auth/register/fizyo      → Fizyoterapist olarak kayıt ol
 *   POST /api/auth/register/patient    → Hasta/aile olarak kayıt ol
 *   POST /api/auth/forgot-password     → Şifre sıfırlama e-postası gönder
 *   POST /api/auth/reset-password      → Yeni şifre belirle
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Kimlik doğrulama işlemlerini gerçekleştiren servis katmanı
    private final AuthService authService;

    /**
     * Giriş endpoint'i — E-posta ve şifre ile token alınır.
     * Başarılı girişte JWT token ve kullanıcı bilgilerini döndürür.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // @Valid anotasyonu — gelen verinin doğrulamasını otomatik yapar (boş alan, e-posta formatı)
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Fizyoterapist kayıt endpoint'i — Başvuru PENDING durumda oluşturulur.
     * Admin onayına kadar giriş yapılamaz.
     */
    @PostMapping("/register/fizyo")
    public ResponseEntity<String> registerFizyo(@Valid @RequestBody RegisterFizyoRequest request) {
        authService.registerFizyo(request);
        // Başarılı kayıt mesajı — admin onayı bekleniyor
        return ResponseEntity.ok("Başvurunuz alındı. Yönetici onayının ardından e-posta ile bilgilendirileceksiniz.");
    }

    /**
     * Hasta/Aile kayıt endpoint'i — Davet kodu doğrulanarak direkt aktif olarak kaydedilir.
     */
    @PostMapping("/register/patient")
    public ResponseEntity<String> registerPatient(@Valid @RequestBody RegisterPatientRequest request) {
        authService.registerPatient(request);
        return ResponseEntity.ok("Kayıt başarıyla tamamlandı. Giriş yapabilirsiniz.");
    }

    /**
     * Şifremi Unuttum endpoint'i — E-posta ile sıfırlama bağlantısı gönderir.
     * Güvenlik: E-posta sistemde olmasa bile aynı yanıt döner.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        // Kullanıcıya her zaman aynı mesaj göster — e-postanın sistemde olup olmadığını gizle
        return ResponseEntity.ok("Şifre sıfırlama bağlantısı e-posta adresinize gönderildi.");
    }

    /**
     * Şifre Sıfırlama endpoint'i — E-postadaki linke tıklandıktan sonra çağrılır.
     * Token doğrulanır ve yeni şifre kaydedilir.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("Şifreniz başarıyla güncellendi. Yeni şifrenizle giriş yapabilirsiniz.");
    }
}
