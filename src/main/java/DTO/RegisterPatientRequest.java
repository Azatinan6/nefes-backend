package DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Hasta/Aile Kayıt İsteği DTO'su — Aile ve çocuk kayıt formundan gelen veriyi temsil eder.
 * Admin onayı gerekmez — kaydolunca ACTIVE durumda başlar.
 * Fizyoterapistin davet kodunu içermesi zorunludur.
 */
@Data
public class RegisterPatientRequest {

    // Ailenin (velinin) tam adı
    @NotBlank(message = "Ad soyad boş olamaz")
    private String fullName;

    // Giriş için kullanılacak e-posta adresi
    @NotBlank(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String email;

    // Minimum 8 karakter şifre zorunlu
    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
    private String password;

    // Fizyoterapistin sisteme özgü davet kodu — bu kod olmadan kayıt yapılamaz
    // Doğrulama: Kodun geçerli olup olmadığı ve kapasitenin (40) dolmadığı kontrol edilir
    @NotBlank(message = "Fizyoterapist davet kodu boş olamaz")
    private String inviteCode;

    // Kayıt tipi: "AILE" veya "COCUK" — frontend'den seçilir
    @NotBlank(message = "Kayıt tipi seçilmelidir")
    private String role;
}
