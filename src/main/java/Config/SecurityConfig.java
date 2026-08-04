package Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Güvenlik Konfigürasyonu — Tüm HTTP güvenlik kurallarını, JWT entegrasyonunu
 * ve rol bazlı erişim kontrollerini burada tanımlarız.
 *
 * Erişim Hiyerarşisi:
 *   /api/auth/**  → Herkese açık (login, kayıt, şifre sıfırlama)
 *   /api/admin/** → Yalnızca ROLE_ADMIN
 *   /api/fizyo/** → ROLE_ADMIN + ROLE_FIZYO
 *   /api/hasta/** → Tüm roller (giriş yapılmış olması yeterli)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity           // @PreAuthorize anotasyonlarını aktif eder
@RequiredArgsConstructor
public class SecurityConfig {

    // JWT doğrulama filtresi — her istekte çalışır, UsernamePasswordAuthFilter'dan önce
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS ayarları — React uygulamasının isteklerini kabul et
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // CSRF koruması kapalı — JWT tabanlı REST API'lerde CSRF gerekmez
            // JWT token her istekte gönderildiği için oturum tabanlı saldırılar çalışmaz
            .csrf(csrf -> csrf.disable())

            // Oturum yönetimi: STATELESS — sunucu hiçbir oturum saklamaz
            // Her istek kendi JWT token'ıyla kimliğini kanıtlar
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ===== ENDPOINT ERİŞİM KURALLARI =====
            .authorizeHttpRequests(auth -> auth

                // Tarayıcının ön kontrolü (preflight) için OPTIONS isteklerine her zaman izin ver
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Kimlik doğrulama endpoint'leri — giriş yapılmadan erişilebilir
                .requestMatchers("/api/auth/**").permitAll()

                // Yapay zeka rapor servisi — mevcut yapı korundu
                .requestMatchers("/api/ai/**").permitAll()

                // Admin endpoint'leri — yalnızca süper yönetici
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                // Fizyoterapist endpoint'leri — admin ve fizyoterapistler erişebilir
                .requestMatchers("/api/fizyo/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_FIZYO")

                // Hasta/aile endpoint'leri — giriş yapmış herkes erişebilir
                .requestMatchers("/api/hasta/**").authenticated()

                // İlerleme endpoint'leri (mevcut) — giriş yapmış herkes
                .requestMatchers("/api/progress/**").authenticated()

                // Yukarıdaki hiçbir kuralla eşleşmeyen diğer istekler — giriş zorunlu
                .anyRequest().authenticated()
            )

            // JWT filtremizi Spring'in varsayılan kimlik doğrulama filtresinden ÖNCE çalıştır
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt Şifre Encoder — şifreleri veritabanına kaydetmeden önce hashler.
     * strength=12 → brute-force saldırılarına karşı güçlü ama performanslı denge
     * @return BCrypt şifre encoder bean'i
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt strength 12 — endüstri standardı güvenlik seviyesi
        return new BCryptPasswordEncoder(12);
    }

    /**
     * CORS (Cross-Origin Resource Sharing) Konfigürasyonu
     * React uygulamasının (localhost:5173) backend'e istek atmasına izin verir.
     * Production'da bu adres gerçek domain ile değiştirilmeli!
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // İzin verilen kaynaklar: React geliştirme sunucusu ve alternatifleri
        // Vite port meşgulse sırayla 5174, 5175 gibi alternatif portlara geçer
        // PROD: Bu listeye gerçek domain adını ekle (örn: "https://nefes-app.com")
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",  // Vite varsayılan portu
            "http://localhost:5174",  // Vite alternatif port
            "http://localhost:5175",  // Vite alternatif port
            "http://localhost:5176",  // Vite alternatif port
            "http://localhost:3000"   // Alternatif geliştirme portu
        ));

        // İzin verilen HTTP metodları
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Tüm başlıklara izin ver (Authorization başlığı dahil)
        configuration.setAllowedHeaders(List.of("*"));

        // JWT token'larının Authorization başlığıyla gönderilmesine izin ver
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}