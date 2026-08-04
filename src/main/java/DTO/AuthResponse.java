package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kimlik Doğrulama Yanıt DTO'su — Başarılı login sonrası frontend'e gönderilen yanıt.
 * Frontend bu bilgileri localStorage'a kaydedip her API isteğinde kullanır.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    // JWT token — frontend'in her API isteğinde "Authorization: Bearer <token>" başlığıyla göndereceği değer
    private String token;

    // Kullanıcının rolü — hangi panele yönlendirileceğini belirler
    // Değerler: ROLE_ADMIN, ROLE_FIZYO, ROLE_AILE, ROLE_COCUK
    private String role;

    // Kullanıcının tam adı — karşılama mesajı ve navbar için
    private String fullName;

    // Kullanıcının e-posta adresi — profil sayfası ve gösterim için
    private String email;

    // Kullanıcının benzersiz kimliği — API isteklerinde referans için
    private String userId;
}
