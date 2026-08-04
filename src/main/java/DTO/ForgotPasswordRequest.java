package DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Şifremi Unuttum İsteği DTO'su — Kullanıcı şifresini unuttuysa e-posta adresini gönderir.
 * Backend bu e-postaya sıfırlama bağlantısı içeren bir mail gönderir.
 */
@Data
public class ForgotPasswordRequest {

    // Sıfırlama bağlantısının gönderileceği e-posta adresi
    // Sistemde kayıtlı değilse hata değil, "mail gönderildi" yanıtı döner (güvenlik gereği)
    @NotBlank(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String email;
}
