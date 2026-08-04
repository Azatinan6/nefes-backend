package Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kullanıcı Entity'si — sistemdeki tüm kullanıcıları (Admin, Fizyoterapist, Aile, Çocuk) temsil eder.
 * Bu tablo hem giriş bilgilerini hem de rol/durum bilgisini tutar.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    // Benzersiz kullanıcı kimliği — UUID kullanılır, sıralı ID'ye göre tahmin edilemez
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Kullanıcının tam adı (ad + soyad)
    @Column(nullable = false)
    private String fullName;

    // Giriş yapılacak e-posta adresi — sistem genelinde benzersiz olmalı
    @Column(nullable = false, unique = true)
    private String email;

    // BCrypt ile hashlenmiş şifre — düz metin ASLA saklanmaz
    @Column(nullable = false)
    private String passwordHash;

    // Kullanıcının sistemdeki rolü (ADMIN, FIZYO, AILE, COCUK)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Hesabın onay durumu:
    // PENDING  → Fizyoterapist başvurusu admin onayı bekliyor
    // APPROVED → Admin onayladı (fizyoterapistler için kullanılır)
    // REJECTED → Admin reddetti
    // ACTIVE   → Normal aktif hesap (aile/çocuk için)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // Eski sistemden gelen avatar alanı — korundu
    private String avatarId;

    // Hesabın oluşturulma tarihi — otomatik olarak kaydedilir
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Hesabın son güncellenme tarihi
    private LocalDateTime updatedAt;

    // Kayıt anında otomatik olarak tarih ve varsayılan durum atanır
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Durum henüz belirlenmemişse ACTIVE olarak başlat
        if (this.status == null) {
            this.status = Status.ACTIVE;
        }
    }

    // Güncellemede otomatik olarak tarih güncellenir
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Sistemdeki kullanıcı rollerini tanımlayan enum
    public enum Role {
        ROLE_ADMIN,   // Sistem yöneticisi — tüm yetkilere sahip
        ROLE_FIZYO,   // Fizyoterapist — max 40 hastayı yönetebilir
        ROLE_AILE,    // Aile üyesi — çocuğun raporlarını takip eder
        ROLE_COCUK    // Çocuk — oyunlara erişir
    }

    // Hesap durum bilgisini tanımlayan enum
    public enum Status {
        PENDING,   // Onay bekliyor (fizyoterapist başvurusu)
        APPROVED,  // Admin tarafından onaylandı
        REJECTED,  // Admin tarafından reddedildi
        ACTIVE     // Normal aktif hesap
    }
}