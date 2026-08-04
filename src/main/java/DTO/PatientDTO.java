package DTO;

import Entity.Patient;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Hasta Veri Transfer Nesnesi — Frontend'e gönderilecek hasta bilgilerini taşır.
 * Patient entity'sindeki hassas alanlar (passwordHash vb.) bu DTO'da yer almaz.
 */
@Data
public class PatientDTO {

    private UUID id;
    private String fullName;
    private String email;
    private String role;        // ROLE_AILE veya ROLE_COCUK
    private LocalDate dateOfBirth;
    private String diagnosisType;
    private int gmfcsLevel;

    /**
     * Patient entity'sinden PatientDTO oluşturur.
     * @param patient Veritabanından gelen hasta entity'si
     * @return Frontend için hazır DTO
     */
    public static PatientDTO from(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getUser().getId());
        dto.setFullName(patient.getUser().getFullName());
        dto.setEmail(patient.getUser().getEmail());
        dto.setRole(patient.getUser().getRole().name());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setDiagnosisType(
            patient.getDiagnosisType() != null ? patient.getDiagnosisType().name() : null
        );
        dto.setGmfcsLevel(patient.getGmfcsLevel());
        return dto;
    }
}
