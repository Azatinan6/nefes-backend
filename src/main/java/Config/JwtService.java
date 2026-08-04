package Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Servisi — JSON Web Token oluşturma, doğrulama ve çözümleme işlemlerini yönetir.
 *
 * JWT Token yapısı:
 *   Header.Payload.Signature
 *   - Header: Algoritma bilgisi (HMAC-SHA256)
 *   - Payload: Kullanıcı bilgileri (email, rol, id) — şifrelenmez ama imzalanır
 *   - Signature: Sahtecilik önleyen imza
 */
@Service
public class JwtService {

    // Uygulama.properties'ten okunan gizli anahtar — en az 32 karakter olmalı
    @Value("${jwt.secret}")
    private String secretKey;

    // Token geçerlilik süresi — properties'ten okunur (default: 86400000 ms = 24 saat)
    @Value("${jwt.expiration:86400000}")
    private long tokenExpirationMs;

    /**
     * Kullanıcı bilgilerinden JWT token üretir.
     * @param email Kullanıcının e-posta adresi (Subject olarak kullanılır)
     * @param role Kullanıcının rolü (ROLE_ADMIN vb.) — token içine gömülür
     * @param userId Kullanıcının UUID'si — frontend'in referans için kullanacağı alan
     * @return İmzalanmış JWT token metni
     */
    public String generateToken(String email, String role, String userId) {
        // Token içine eklenecek ek bilgiler (claim)
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", role);     // Rol bilgisi — yetki kontrolü için
        extraClaims.put("userId", userId); // Kullanıcı ID'si — referans için

        return Jwts.builder()
                .claims(extraClaims)                               // Ek bilgileri ekle
                .subject(email)                                     // Token'ın "sahibi" e-posta
                .issuedAt(new Date())                               // Token'ın oluşturulma zamanı
                .expiration(new Date(System.currentTimeMillis() + tokenExpirationMs)) // Geçerlilik süresi
                .signWith(getSigningKey())                          // Gizli anahtarla imzala
                .compact();                                         // String formatına dönüştür
    }

    /**
     * Token'dan e-posta adresini (subject) çıkarır.
     * @param token JWT token metni
     * @return Token'ın sahibinin e-posta adresi
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Token'ın geçerliliğini kontrol eder.
     * @param token JWT token metni
     * @param email Beklenen kullanıcı e-postası — token sahibiyle eşleşmeli
     * @return Token geçerliyse ve doğru kişiye aitse true
     */
    public boolean isTokenValid(String token, String email) {
        final String tokenEmail = extractEmail(token);
        // E-posta eşleşmeli VE token süresi dolmamış olmalı
        return tokenEmail.equals(email) && !isTokenExpired(token);
    }

    /**
     * Token'ın süresinin dolup dolmadığını kontrol eder.
     * @param token JWT token metni
     * @return Süre dolduysa true
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Token içindeki tüm claim'leri çıkarır.
     * Geçersiz veya imzası bozuk token'da otomatik istisna fırlatır.
     * @param token JWT token metni
     * @return Tüm claim bilgileri
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // Doğrulama için gizli anahtar
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Secret key metnini HMAC-SHA256 imzalama anahtarına dönüştürür.
     * @return Kriptografik imzalama anahtarı
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
