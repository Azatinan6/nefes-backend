package Service;

import Entity.User;
import Entity.UserProgress;
import Repository.UserRepository;
import Repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AiReportService {

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    // 1. AİLE PANELİ İÇİN (Gerçek Veri Entegrasyonu)
    public String generateWeeklyReportForUser(UUID userId) {
        try {
            // 1. Veritabanından hastayı buluyoruz
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı."));
            
            // 2. Hastanın oynadığı oyunların skorlarını veritabanından çekiyoruz
            List<UserProgress> progressList = userProgressRepository.findByUserId(userId);

            // 3. Skorları Gemini'nin anlayacağı bir liste formatına dönüştürüyoruz
            StringBuilder scoreDetails = new StringBuilder();
            if (progressList.isEmpty()) {
                scoreDetails.append("Hastanın bu hafta henüz oynadığı bir oyun kaydı bulunmuyor.\n");
            } else {
                for (UserProgress progress : progressList) {
                    String gameName = progress.getGame() != null ? progress.getGame().getName() : "İsimsiz Oyun";
                    scoreDetails.append("- Oyun: ").append(gameName)
                                .append(" | Skor: ").append(progress.getScore())
                                .append(" | Kazanılan Nefes Kristali: ").append(progress.getBreathCrystals())
                                .append("\n");
                }
            }

            // 4. Prompt'u (Yapay Zeka Komutunu) gerçek verilerle oluşturuyoruz
            String prompt = String.format(
                "Sen uzman ve şefkatli bir çocuk solunum fizyoterapistisin. " +
                "%s isimli hastanın bu haftaki nefes egzersizi müfredatına ait oyun skorları aşağıdadır:\n" +
                "%s\n" +
                "Bu verilere bakarak çocuğun hangi nefes becerilerinde (derin nefes, uzun üfleme, diyafram kontrolü vb.) " +
                "güçlü olduğunu ve nerelerde gelişime açık olduğunu analiz et. Aileye cesaret verici, anlaşılır ve profesyonel bir haftalık özet raporu yaz.",
                user.getFullName(), scoreDetails.toString()
            );

            // 5. İsteği Gemini'ye gönderiyoruz
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey.trim());

            Map<String, Object> parts = new HashMap<>();
            parts.put("text", prompt);

            Map<String, Object> contents = new HashMap<>();
            contents.put("parts", List.of(parts));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(contents));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String cleanUrl = apiUrl.trim();
            String response = restTemplate.postForObject(cleanUrl, request, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.path("candidates").get(0)
                       .path("content")
                       .path("parts").get(0)
                       .path("text").asText();

        } catch (Exception e) {
            System.out.println("Aile Yapay Zeka Hatası: " + e.getMessage());
            return "Şu anda yapay zeka raporu oluşturulamıyor. Lütfen daha sonra tekrar deneyin.";
        }
    }

    // 2. FİZYOTERAPİST PANELİ İÇİN (Mevcut kodun aynen kalıyor)
    public String generateClinicalReport(String patientName, int age, String cpType, String gmfcsLevel, int compliance, int avgDb, String lastModule, String totalTime) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey.trim());

            String prompt = String.format(
                "Sen uzman bir pediatrik fizyoterapist ve N.E.F.E.S. yapay zeka klinik asistanısın. " +
                "Aşağıda verileri sunulan Serebral Palsi (SP) hastası için resmi, akademik ve tamamen tıbbi terminoloji içeren bir klinik değerlendirme raporu yaz.\n\n" +
                "Lütfen hastanın GMFCS seviyesi, CP tipi, uyum yüzdesi ve desibel (dB) verilerini kullanarak postüral kontrol, solunum kapasitesi ve motor öğrenme süreçleri hakkında klinik bir analiz yap.\n\n" +
                "--- HASTA VERİLERİ ---\n" +
                "İsim: %s\nYaş: %d\nSP Tipi: %s\nGMFCS: %s\nTedavi Uyumu: %%%d\nOrtalama Desibel: %d dB\nSon Oynanan Modül: %s\nToplam Seans Süresi: %s\n" +
                "----------------------\n\n" +
                "Analizin sonuna fizyoterapi müdahalesi için somut modül önerisi ekle.",
                patientName, age, cpType, gmfcsLevel, compliance, avgDb, lastModule, totalTime
            );

            Map<String, Object> parts = new HashMap<>();
            parts.put("text", prompt);

            Map<String, Object> contents = new HashMap<>();
            contents.put("parts", List.of(parts));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(contents));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String cleanUrl = apiUrl.trim();
            String response = restTemplate.postForObject(cleanUrl, request, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.path("candidates").get(0)
                       .path("content")
                       .path("parts").get(0)
                       .path("text").asText();

        } catch (Exception e) {
            System.out.println("Klinik Yapay Zeka Hatası: " + e.getMessage());
            return "Şu anda klinik yapay zeka raporu oluşturulamıyor. Lütfen API anahtarınızı veya bağlantınızı kontrol edin.";
        }
    }
}