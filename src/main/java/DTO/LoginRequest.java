package DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Giriş İsteği DTO'su — Login endpoint'ine gönderilen veriyi temsil eder.
 * Sadece gerekli alanları alır, güvenlik için gereksiz veri istenmez.
 */
@Data
public class LoginRequest {

    // Kullanıcının sisteme kayıtlı e-posta adresi
    @NotBlank(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String email;

    // Kullanıcının düz metin şifresi — backend'de BCrypt ile hashlenmiş versiyonla karşılaştırılır
    @NotBlank(message = "Şifre boş olamaz")
    private String password;
}
