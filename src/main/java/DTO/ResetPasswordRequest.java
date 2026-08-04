package DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Şifre Sıfırlama İsteği DTO'su — E-posta bağlantısına tıkladıktan sonra yeni şifre belirlerken kullanılır.
 * Token URL parametresinden alınır, yeni şifre formdan gelir.
 */
@Data
public class ResetPasswordRequest {

    // E-posta ile gönderilen güvenli tek kullanımlık token
    // Bu token veritabanında kontrol edilir: geçerli mi, süresi dolmamış mı, kullanılmış mı?
    @NotBlank(message = "Token boş olamaz")
    private String token;

    // Kullanıcının belirlemek istediği yeni şifre — BCrypt ile hashlenecek
    @NotBlank(message = "Yeni şifre boş olamaz")
    @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
    private String newPassword;
}
