package Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Hasta (Çocuk) Entity'si — Serebral Palsili çocukların klinik bilgilerini tutar.
 * Her hasta bir fizyoterapiste bağlıdır.
 * User tablosuyla @OneToOne ilişkisi vardır.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "patients")
public class Patient {

    // Hasta ID'si — User tablosundaki ID ile aynı
    @Id
    private UUID id;

    // Bu hasta profilinin bağlı olduğu kullanıcı hesabı
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // Hastanın bağlı olduğu fizyoterapist
    // ManyToOne: Bir fizyoterapistin birden fazla hastası olabilir (max 40)
    @ManyToOne
    @JoinColumn(name = "physiotherapist_id", nullable = false)
    private Physiotherapist physiotherapist;

    // Çocuğun doğum tarihi — yaş hesaplamaları için kullanılır
    private LocalDate dateOfBirth;

    // Serebral Palsi tanı tipi
    @Enumerated(EnumType.STRING)
    private DiagnosisType diagnosisType;

    // Kaba Motor Fonksiyon Sınıflandırma Sistemi (GMFCS) seviyesi
    // 1 → Bağımsız yürüme, 5 → Akülü tekerlekli sandalye
    @Column(columnDefinition = "integer default 1")
    private int gmfcsLevel = 1;

    // SP tanı tiplerini tanımlayan enum
    public enum DiagnosisType {
        SPASTIK,     // Kas tonusunda artış ve sertlik
        DISKINETIK,  // İstem dışı, kontrol edilemeyen hareketler
        ATAKSIK,     // Koordinasyon kaybı
        KARMA        // Birden fazla tip bir arada
    }
}
