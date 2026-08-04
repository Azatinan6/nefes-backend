package DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Fizyoterapist Kayıt İsteği DTO'su — Fizyoterapist kayıt formundan gelen veriyi temsil eder.
 * Admin onayı gerektiren kayıt tipi (PENDING durumda başlar).
 */
@Data
public class RegisterFizyoRequest {

    // Fizyoterapistin tam adı
    @NotBlank(message = "Ad soyad boş olamaz")
    private String fullName;

    // Giriş için kullanılacak e-posta — sistem genelinde benzersiz olmalı
    @NotBlank(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String email;

    // Minimum 8 karakter şifre zorunlu — güvenlik gereği
    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
    private String password;

    // Diploma veya lisans numarası — admin onay sürecinde doğrulama için
    @NotBlank(message = "Lisans numarası boş olamaz")
    private String licenseNumber;

    // Fizyoterapistin uzmanlık alanı (isteğe bağlı ama önerilir)
    private String specialization;
}
