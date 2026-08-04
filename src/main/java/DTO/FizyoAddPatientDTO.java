package DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FizyoAddPatientDTO {
    private String fullName;
    private String email;
    private String diagnosisType;
    private Integer gmfcsLevel;
    private LocalDate dateOfBirth;
}
