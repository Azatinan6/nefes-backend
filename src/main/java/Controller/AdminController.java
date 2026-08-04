package Controller;

import Entity.User;
import Service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Admin Controller'ı — Yalnızca ROLE_ADMIN yetkisiyle erişilebilen endpoint'ler.
 *
 * Endpoint'ler:
 *   GET    /api/admin/users                     → Tüm kullanıcıları listele
 *   GET    /api/admin/fizyo/pending             → Onay bekleyen fizyoterapistler
 *   POST   /api/admin/fizyo/{id}/approve        → Fizyoterapist başvurusunu onayla
 *   POST   /api/admin/fizyo/{id}/reject         → Fizyoterapist başvurusunu reddet
 *   DELETE /api/admin/users/{id}                → Kullanıcıyı sil
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")  // Tüm endpoint'ler yalnızca Admin'e açık
@RequiredArgsConstructor
public class AdminController {

    // Admin işlemlerini yöneten servis katmanı
    private final AdminService adminService;

    /**
     * Sistemdeki tüm kullanıcıları listeler.
     * Admin panelindeki "Tüm Kullanıcılar" sekmesi için kullanılır.
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Admin onayı bekleyen fizyoterapist başvurularını listeler.
     * Admin panelindeki "Onay Bekleyenler" sekmesi için kullanılır.
     */
    @GetMapping("/fizyo/pending")
    public ResponseEntity<List<User>> getPendingFizyos() {
        List<User> pendingFizyos = adminService.getPendingFizyotherapists();
        return ResponseEntity.ok(pendingFizyos);
    }

    /**
     * Fizyoterapist başvurusunu onaylar.
     * Onay sonrası: durum APPROVED olur, davet kodu üretilir, e-posta gönderilir.
     * @param id Onaylanacak fizyoterapistin UUID'si
     */
    @PostMapping("/fizyo/{id}/approve")
    public ResponseEntity<String> approveFizyo(@PathVariable UUID id) {
        adminService.approveFizyotherapist(id);
        return ResponseEntity.ok("Fizyoterapist başvurusu onaylandı ve davet kodu e-posta ile gönderildi.");
    }

    /**
     * Fizyoterapist başvurusunu reddeder.
     * Red sonrası: durum REJECTED olur, bildirim e-postası gönderilir.
     * @param id Reddedilecek fizyoterapistin UUID'si
     */
    @PostMapping("/fizyo/{id}/reject")
    public ResponseEntity<String> rejectFizyo(@PathVariable UUID id) {
        adminService.rejectFizyotherapist(id);
        return ResponseEntity.ok("Fizyoterapist başvurusu reddedildi.");
    }

    /**
     * Onaylanmış bir fizyoterapistin davet kodunu döndürür.
     * Admin panelinde hasta kayıt sürecinde kullanılmak üzere kodu görüntüler.
     */
    @GetMapping("/fizyo/{id}/invite-code")
    public ResponseEntity<String> getFizyoInviteCode(@PathVariable UUID id) {
        String code = adminService.getFizyoInviteCode(id);
        return ResponseEntity.ok(code);
    }

    /**
     * Kullanıcıyı sistemden kalıcı olarak siler.
     * Uyarı: Bu işlem geri alınamaz.
     * @param id Silinecek kullanıcının UUID'si
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("Kullanıcı başarıyla silindi.");
    }
}
