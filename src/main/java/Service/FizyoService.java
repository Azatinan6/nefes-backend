package Service;

import DTO.FizyoAddPatientDTO;
import Entity.Patient;
import Entity.Physiotherapist;
import Entity.User;
import Entity.User.Role;
import Entity.User.Status;
import Repository.PatientRepository;
import Repository.PhysiotherapistRepository;
import Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Fizyoterapist Servisi — Fizyoterapist paneline özel işlemleri yönetir.
 */
@Service
@RequiredArgsConstructor
public class FizyoService {

    private final PhysiotherapistRepository physiotherapistRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Giriş yapmış fizyoterapistin hastalarını listeler.
     * @param email Fizyoterapistin e-postası (JWT token'dan alınır)
     * @return Fizyoterapistin hasta listesi
     */
    public List<Patient> getMyPatients(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        Physiotherapist fizyo = physiotherapistRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Fizyoterapist profili bulunamadı"));
        return patientRepository.findByPhysiotherapist(fizyo);
    }

    /**
     * Giriş yapmış fizyoterapistin davet kodunu döndürür.
     * @param email Fizyoterapistin e-postası (JWT token'dan alınır)
     * @return Davet kodu
     */
    public String getMyInviteCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        Physiotherapist fizyo = physiotherapistRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Fizyoterapist profili bulunamadı"));
        if (fizyo.getInviteCode() == null) {
            throw new RuntimeException("Henüz davet kodunuz yok. Lütfen admin onayını bekleyin.");
        }
        return fizyo.getInviteCode();
    }

    /**
     * Fizyoterapist tarafından yeni hasta (aile) ekler.
     * Rastgele şifre üretip e-posta ile gönderir.
     * @param dto Yeni hasta bilgileri
     * @param fizyoEmail İşlemi yapan fizyoterapistin e-postası
     * @return Üretilen şifre (test kolaylığı ve UI'da göstermek için)
     */
    @Transactional
    public String addPatient(FizyoAddPatientDTO dto, String fizyoEmail) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Bu e-posta adresi zaten kayıtlı");
        }

        User fizyoUser = userRepository.findByEmail(fizyoEmail)
                .orElseThrow(() -> new RuntimeException("Fizyoterapist bulunamadı"));
        Physiotherapist fizyo = physiotherapistRepository.findById(fizyoUser.getId())
                .orElseThrow(() -> new RuntimeException("Fizyoterapist profili bulunamadı"));

        if (!fizyo.hasCapacity()) {
            throw new RuntimeException("Hasta kapasiteniz dolmuştur (maksimum 40 hasta).");
        }

        // 8 haneli rastgele bir şifre üret
        String generatedPassword = UUID.randomUUID().toString().substring(0, 8);

        User patientUser = new User();
        patientUser.setFullName(dto.getFullName());
        patientUser.setEmail(dto.getEmail());
        patientUser.setPasswordHash(passwordEncoder.encode(generatedPassword));
        patientUser.setRole(Role.ROLE_AILE);
        patientUser.setStatus(Status.ACTIVE);
        
        User savedUser = userRepository.save(patientUser);

        Patient patient = new Patient();
        patient.setUser(savedUser);
        patient.setPhysiotherapist(fizyo);
        
        // String'den Enum'a çevir, boşsa varsayılan veya null bırak
        if (dto.getDiagnosisType() != null && !dto.getDiagnosisType().isEmpty()) {
            try {
                patient.setDiagnosisType(Patient.DiagnosisType.valueOf(dto.getDiagnosisType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Geçersiz bir enum değeri gelirse null kalabilir veya karma olarak atanabilir
            }
        }
        
        patient.setGmfcsLevel(dto.getGmfcsLevel());
        patient.setDateOfBirth(dto.getDateOfBirth());
        
        patientRepository.save(patient);

        fizyo.setPatientCount(fizyo.getPatientCount() + 1);
        physiotherapistRepository.save(fizyo);

        // Aileye mail gönder
        try {
            emailService.sendPatientCredentials(dto.getEmail(), dto.getFullName(), generatedPassword);
        } catch (Exception e) {
            System.err.println("[UYARI] Şifre e-postası gönderilemedi: " + e.getMessage());
        }

        return generatedPassword;
    }
}
