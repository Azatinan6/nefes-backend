package Controller;

import DTO.ProgressRequest;
import Entity.UserProgress;
import Service.ProgressService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*") // React tarafından gelecek isteklere izin veriyoruz
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("/save")
    public ResponseEntity<String> saveProgress(@RequestBody ProgressRequest request) {
        // React'ten gelen JSON formatındaki veri DTO'ya dönüşür ve Service'e iletilir
        progressService.saveGameProgress(request);
        
        // İşlem başarılı olursa React tarafına "200 OK" mesajı döner
        return ResponseEntity.ok("Harika! Oyun skoru ve nefes kristalleri başarıyla kaydedildi.");
    }
    @GetMapping("/user/{userId}")
        public ResponseEntity<List<UserProgress>> getUserProgress(@PathVariable String userId) {
            // String olarak gelen ID'yi UUID nesnesine çeviriyoruz
            UUID uuid = UUID.fromString(userId);
            return ResponseEntity.ok(progressService.getUserProgress(uuid));
    }
}