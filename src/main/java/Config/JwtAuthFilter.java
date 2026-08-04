package Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Kimlik Doğrulama Filtresi — Her HTTP isteğinde bir kez çalışır.
 *
 * Çalışma mantığı:
 * 1. İstek başlığındaki "Authorization: Bearer <token>" verisini al
 * 2. Token'ı JwtService ile doğrula
 * 3. Geçerliyse kullanıcıyı Spring Security bağlamına kaydet
 * 4. İsteği devam ettir
 *
 * Bu filtre sayesinde her korunan endpoint otomatik olarak token kontrolü yapar.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    // JWT işlemlerini gerçekleştiren servis — token doğrulama ve bilgi çıkarma
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // İsteğin Authorization başlığını al
        final String authHeader = request.getHeader("Authorization");

        // Başlık yoksa veya "Bearer " ile başlamıyorsa token doğrulama yapma, devam et
        // Bu durum genellikle login/register gibi herkese açık endpoint'lerde olur
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " prefix'ini kaldırarak sadece token metnini al
        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            // Token'dan kullanıcı e-postasını çıkarmayı dene
            userEmail = jwtService.extractEmail(jwt);
        } catch (Exception e) {
            // Token bozuksa veya imzası geçersizse — 401 döndür, işlemi durdur
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Geçersiz veya süresi dolmuş token\"}");
            return;
        }

        // E-posta çıkarılabildi VE güvenlik bağlamında zaten bir oturum yoksa doğrulama yap
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Token'ın geçerliliğini kontrol et (e-posta eşleşimi + süre kontrolü)
            if (jwtService.isTokenValid(jwt, userEmail)) {

                // Token'dan rolü çıkar — yetki kontrolü için kullanılır
                // Claim yapısından "role" alanını al
                String role = extractRoleFromToken(jwt);

                // Spring Security için kimlik doğrulama nesnesi oluştur
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail,   // Principal: kullanıcının e-postası
                        null,        // Credentials: JWT akışında şifre tekrar gönderilmez
                        List.of(new SimpleGrantedAuthority(role)) // Yetki listesi
                );

                // İstek detaylarını (IP, session) authentication nesnesine ekle
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Kullanıcıyı güvenlik bağlamına kaydet — bundan sonra @PreAuthorize çalışır
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Filtreyi devam ettir — bir sonraki filtre veya Controller'a geç
        filterChain.doFilter(request, response);
    }

    /**
     * JWT token'ından rol bilgisini çıkarır.
     * Token payload'ında "role" claim'i olması beklenir.
     * @param token JWT token metni
     * @return Kullanıcı rolü (örn: ROLE_ADMIN)
     */
    private String extractRoleFromToken(String token) {
        try {
            // Token'ı Base64 ile decode et ve payload kısmından role'ü al
            // Not: Bu basit bir yaklaşım — daha güvenli için JwtService.extractClaim kullanılabilir
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                // JSON içinden "role":"ROLE_XXXX" verisini çıkar
                int roleStart = payload.indexOf("\"role\":\"") + 8;
                int roleEnd = payload.indexOf("\"", roleStart);
                if (roleStart > 7 && roleEnd > roleStart) {
                    return payload.substring(roleStart, roleEnd);
                }
            }
        } catch (Exception e) {
            // Rol çıkarılamazsa varsayılan olarak en düşük yetkiyi ver
        }
        return "ROLE_COCUK"; // Güvenli varsayılan
    }
}
