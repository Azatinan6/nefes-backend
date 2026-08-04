package Controller;

import DTO.ProgressRequest;
import Service.ProgressService;
import lombok.RequiredArgsConstructor;
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
}