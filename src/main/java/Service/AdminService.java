package Service;

import Entity.Physiotherapist;
import Entity.User;
import Entity.User.Role;
import Entity.User.Status;
import Repository.PhysiotherapistRepository;
import Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Admin Servisi — Yönetim paneli işlemlerini yönetir.
 *
 * Admin yetkileri:
 * - Fizyoterapist başvurularını onaylama/reddetme
 * - Tüm kullanıcıları listeleme ve yönetme
 * - Kullanıcı silme ve rol değiştirme
 * - Sistem istatistiklerini görüntüleme
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PhysiotherapistRepository physiotherapistRepository;
    private final EmailService emailService;

    /**
     * Onay bekleyen fizyoterapist başvurularını listeler.
     * Admin panelindeki "Onay Bekleyenler" sekmesi için kullanılır.
     * @return PENDING durumundaki tüm fizyoterapist kullanıcıları
     */
    public List<User> getPendingFizyotherapists() {
        return userRepository.findByRoleAndStatus(Role.ROLE_FIZYO, Status.PENDING);
    }

    /**
     * Tüm kullanıcıları listeler — Admin yönetim tablosu için.
     * @return Sistemdeki tüm kullanıcılar
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Fizyoterapist başvurusunu onaylar.
     * Onay sonrası sisteme özgü davet kodu üretilir ve e-posta gönderilir.
     * @param userId Onaylanacak fizyoterapistin User ID'si
     */
    @Transactional
    public void approveFizyotherapist(UUID userId) {

        // Kullanıcıyı bul
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Sadece fizyoterapist başvuruları onaylanabilir
        if (user.getRole() != Role.ROLE_FIZYO) {
            throw new RuntimeException("Bu kullanıcı fizyoterapist değil");
        }

        // Kullanıcı durumunu APPROVED olarak güncelle
        user.setStatus(Status.APPROVED);
        userRepository.save(user);

        // Fizyoterapist profilini bul ve davet kodunu oluştur
        Physiotherapist fizyo = physiotherapistRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Fizyoterapist profili bulunamadı"));

        // Benzersiz 6 haneli davet kodu üret
        String inviteCode = generateUniqueInviteCode();
        fizyo.setInviteCode(inviteCode);
        physiotherapistRepository.save(fizyo);

        // Fizyoterapiste onay e-postasını ve davet kodunu gönder — mail hatası işlemi engellemesin
        try {
            emailService.sendFizyoApproved(user.getEmail(), user.getFullName(), inviteCode);
        } catch (Exception e) {
            System.err.println("[UYARI] Onay e-postası gönderilemedi: " + e.getMessage());
        }
    }

    /**
     * Fizyoterapist başvurusunu reddeder.
     * @param userId Reddedilecek fizyoterapistin User ID'si
     */
    @Transactional
    public void rejectFizyotherapist(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (user.getRole() != Role.ROLE_FIZYO) {
            throw new RuntimeException("Bu kullanıcı fizyoterapist değil");
        }

        // Kullanıcı durumunu REJECTED olarak güncelle
        user.setStatus(Status.REJECTED);
        userRepository.save(user);

        // Fizyoterapiste red bildirim e-postası gönder — mail hatası işlemi engellemesin
        try {
            emailService.sendFizyoRejected(user.getEmail(), user.getFullName());
        } catch (Exception e) {
            System.err.println("[UYARI] Red e-postası gönderilemedi: " + e.getMessage());
        }
    }

    /**
     * Onaylanmış fizyoterapistin davet kodunu döndürür.
     * Admin panelinde davet kodu görüntülemek için kullanılır.
     * @param userId Fizyoterapistin User ID'si
     * @return Davet kodu
     */
    public String getFizyoInviteCode(UUID userId) {
        Physiotherapist fizyo = physiotherapistRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Fizyoterapist profili bulunamadı"));
        if (fizyo.getInviteCode() == null) {
            throw new RuntimeException("Bu fizyoterapist henüz onaylanmamış, davet kodu yok");
        }
        return fizyo.getInviteCode();
    }

    /**
     * Kullanıcıyı sistemden siler.
     * Uyarı: Bu işlem geri alınamaz. İlgili hasta ve ilerleme kayıtları da etkilenebilir.
     * @param userId Silinecek kullanıcının ID'si
     */
    @Transactional
    public void deleteUser(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Admin kendi hesabını silemesin — sistem kilitlenmesini önler
        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new RuntimeException("Admin hesabı silinemez");
        }

        userRepository.delete(user);
    }

    /**
     * Benzersiz 6 haneli davet kodu üretir.
     * Büyük harf ve rakamlardan oluşur (okunması kolay).
     * Veritabanında zaten var mı kontrol eder — çakışma önlenir.
     * @return Benzersiz davet kodu (örn: "XY7Z2K")
     */
    private String generateUniqueInviteCode() {
        // Davet kodunda kullanılacak karakterler — karışıklık yaratan I, O, 0, 1 çıkarıldı
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        String code;

        // Benzersiz olana kadar üret
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                int index = (int) (Math.random() * characters.length());
                sb.append(characters.charAt(index));
            }
            code = sb.toString();
        } while (physiotherapistRepository.existsByInviteCode(code));

        return code;
    }
}
