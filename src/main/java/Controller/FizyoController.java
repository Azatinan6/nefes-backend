package Controller;

import DTO.PatientDTO;
import Entity.Patient;
import Service.FizyoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Fizyoterapist Controller'ı — Fizyoterapist paneline özel endpoint'ler.
 *
 * Endpoint'ler:
 *   GET /api/fizyo/my-patients     → Kendi hastalarını listele
 *   GET /api/fizyo/invite-code     → Davet kodunu görüntüle
 */
@RestController
@RequestMapping("/api/fizyo")
@PreAuthorize("hasAnyAuthority('ROLE_FIZYO', 'ROLE_ADMIN')")
@RequiredArgsConstructor
public class FizyoController {

    private final FizyoService fizyoService;

    /**
     * Giriş yapmış fizyoterapistin hasta listesini döndürür.
     * JwtAuthFilter principal olarak e-postayı set eder — authentication.getName() ile alınır.
     */
    @GetMapping("/my-patients")
    public ResponseEntity<List<PatientDTO>> getMyPatients(Authentication authentication) {
        String email = authentication.getName(); // JWT token'daki email
        List<Patient> patients = fizyoService.getMyPatients(email);
        List<PatientDTO> dtos = patients.stream()
                .map(PatientDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Giriş yapmış fizyoterapistin davet kodunu döndürür.
     */
    @GetMapping("/invite-code")
    public ResponseEntity<String> getMyInviteCode(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(fizyoService.getMyInviteCode(email));
    }

    /**
     * Fizyoterapistin kendi paneline yeni hasta eklemesini sağlar.
     */
    @PostMapping("/add-patient")
    public ResponseEntity<?> addPatient(@RequestBody DTO.FizyoAddPatientDTO request, Authentication authentication) {
        try {
            String email = authentication.getName();
            String generatedPassword = fizyoService.addPatient(request, email);
            return ResponseEntity.ok(java.util.Map.of(
                "message", "Hasta başarıyla eklendi.",
                "generatedPassword", generatedPassword
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
