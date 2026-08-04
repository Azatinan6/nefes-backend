package Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Aşağıdaki CORS ayarımızı Spring Security'ye entegre ediyoruz
            .cors(Customizer.withDefaults())
            
            // 2. REST API yapısında CSRF kapalı olur
            .csrf(csrf -> csrf.disable())
            
            // 3. ERİŞİM İZİNLERİ (Senin gelecekteki Login sisteminin temeli)
            .authorizeHttpRequests(auth -> auth
                // ÇOK ÖNEMLİ: Tarayıcının önden gönderdiği Preflight (OPTIONS) isteklerine KESİNLİKLE şifre sorma!
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Yapay zeka servisimize şu anlık şifresiz girilebilsin
                .requestMatchers("/api/ai/**").permitAll()
                
                // İLERİSİ İÇİN: Bunlar haricindeki TÜM API isteklerinde kullanıcı giriş yapmış (Authenticated) olmalı!
                .anyRequest().authenticated()
            )
            // İleride kendi Login ekranını bağlayana kadar Spring'in varsayılan HTTP güvenliğini kullanır
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // CORS Kurallarımız (401 Hatasını çözen ana merkez)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // React'in çalıştığı 5173 portuna ve alternatif olarak 3000 portuna VIP giriş izni veriyoruz
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        
        // Kimlik bilgileri (ileride kullanacağın Token/Cookie) aktarımına şimdiden izin ver
        configuration.setAllowCredentials(true); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}