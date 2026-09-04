package Controller;

import Service.AiReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*") 
public class AiReportController {

    @Autowired
    private AiReportService aiReportService;

    // 1. AİLE PANELİ İÇİN (Gerçek Veri Entegrasyonu)
    @PostMapping("/generate-report")
    public ResponseEntity<String> generateReport(@RequestBody ReportRequest request) {
        // Artık isim ve skor almıyoruz, sadece hastanın ID'sini servise yolluyoruz
        String aiReport = aiReportService.generateWeeklyReportForUser(request.getUserId());
        return ResponseEntity.ok(aiReport);
    }

    // 2. FİZYOTERAPİST PANELİ İÇİN 
    @PostMapping("/generate-clinical-report")
    public ResponseEntity<String> generateClinicalReport(@RequestBody ClinicalReportRequest request) {
        String aiReport = aiReportService.generateClinicalReport(
            request.getPatientName(),
            request.getAge(),
            request.getCpType(),
            request.getGmfcsLevel(),
            request.getCompliance(),
            request.getAvgDb(),
            request.getLastModule(),
            request.getTotalTime()
        );
        return ResponseEntity.ok(aiReport);
    }
}

// --- DTO Sınıfları ---

class ReportRequest {
    // Aile paneli isteği artık sadece userId taşıyor
    private UUID userId;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}

class ClinicalReportRequest {
    private String patientName;
    private int age;
    private String cpType;
    private String gmfcsLevel;
    private int compliance;
    private int avgDb;
    private String lastModule;
    private String totalTime;

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getCpType() { return cpType; }
    public void setCpType(String cpType) { this.cpType = cpType; }
    public String getGmfcsLevel() { return gmfcsLevel; }
    public void setGmfcsLevel(String gmfcsLevel) { this.gmfcsLevel = gmfcsLevel; }
    public int getCompliance() { return compliance; }
    public void setCompliance(int compliance) { this.compliance = compliance; }
    public int getAvgDb() { return avgDb; }
    public void setAvgDb(int avgDb) { this.avgDb = avgDb; }
    public String getLastModule() { return lastModule; }
    public void setLastModule(String lastModule) { this.lastModule = lastModule; }
    public String getTotalTime() { return totalTime; }
    public void setTotalTime(String totalTime) { this.totalTime = totalTime; }
}