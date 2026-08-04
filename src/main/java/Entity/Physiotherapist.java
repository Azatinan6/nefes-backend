package Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

/**
 * Fizyoterapist Entity'si — Fizyoterapist rolündeki kullanıcıların ek bilgilerini tutar.
 * Her fizyoterapist en fazla 40 hasta yönetebilir.
 * User tablosuyla @OneToOne ilişkisi vardır.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "physiotherapists")
public class Physiotherapist {

    // Fizyoterapist ID'si — User tablosundaki ID ile aynı (paylaşımlı primary key)
    @Id
    private UUID id;

    // Bu fizyoterapist profilinin bağlı olduğu kullanıcı hesabı
    // @MapsId → Aynı UUID'yi User ile paylaşır, tekrarlı ID oluşmaz
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // Fizyoterapistin diploma/lisans numarası — admin onay sürecinde doğrulanır
    @Column(nullable = false)
    private String licenseNumber;

    // Uzmanlık alanı (örn: "Nörolojik Fizyoterapi", "Pediatrik Fizyoterapi")
    private String specialization;

    // Fizyoterapistin hastaları kaydederken paylaşacağı benzersiz davet kodu
    // Örnek: "XY7Z2K" — Aile bu kodu girerek bu fizyoterapiste bağlanır
    @Column(unique = true, length = 10)
    private String inviteCode;

    // Şu an bağlı hasta sayısı — 40'a ulaşınca yeni hasta kaydı durdurulur
    @Column(nullable = false)
    private int patientCount = 0;

    // Maksimum hasta kapasitesi — sistemde sabit olarak 40 tutulur
    public static final int MAX_PATIENT_CAPACITY = 40;

    /**
     * Fizyoterapistin yeni hasta kabul edip edemeyeceğini kontrol eder.
     * @return Kapasite dolmamışsa true, dolmuşsa false
     */
    public boolean hasCapacity() {
        return this.patientCount < MAX_PATIENT_CAPACITY;
    }
}
