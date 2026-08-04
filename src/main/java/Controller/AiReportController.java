package Controller;

import Service.AiReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:5173") 
public class AiReportController {

    @Autowired
    private AiReportService aiReportService;

    // 1. AİLE PANELİ İÇİN
    @PostMapping("/generate-report")
    public ResponseEntity<String> generateReport(@RequestBody ReportRequest request) {
        String aiReport = aiReportService.generateWeeklyReport(request.getChildName(), request.getWeeklyScores());
        return ResponseEntity.ok(aiReport);
    }

    // 2. FİZYOTERAPİST PANELİ İÇİN (Gerçek Yapay Zeka Entegrasyonu)
    @PostMapping("/generate-clinical-report")
    public ResponseEntity<String> generateClinicalReport(@RequestBody ClinicalReportRequest request) {
        
        // request objesinin içindeki verileri tek tek çıkarıp servise gönderiyoruz
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

// --- DTO (Data Transfer Object) Sınıfları ---

class ReportRequest {
    private String childName;
    private Map<String, Integer> weeklyScores;
    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }
    public Map<String, Integer> getWeeklyScores() { return weeklyScores; }
    public void setWeeklyScores(Map<String, Integer> weeklyScores) { this.weeklyScores = weeklyScores; }
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

    // Getter ve Setter metodları
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