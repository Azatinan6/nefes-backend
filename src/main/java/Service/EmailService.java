package Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * E-posta Servisi — Sistem tarafından gönderilen tüm bildirimleri yönetir.
 *
 * Gönderilen e-posta tipleri:
 * 1. Fizyoterapist başvuru alındı bildirimi
 * 2. Fizyoterapist onay/red bildirimi (admin kararı sonrası)
 * 3. Şifremi unuttum sıfırlama linki
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    // Spring'in e-posta gönderme aracı — application.properties'ten otomatik yapılandırılır
    private final JavaMailSender mailSender;

    // Gönderici e-posta adresi — properties'ten okunur
    @Value("${spring.mail.username}")
    private String fromEmail;

    // Frontend URL'si — e-postalardaki bağlantılar için kullanılır
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Fizyoterapist başvurusu alındığında gönderilen bildirim e-postası.
     * Başvurunun admin tarafından inceleneceğini bildirir.
     * @param toEmail Fizyoterapistin e-posta adresi
     * @param fullName Fizyoterapistin tam adı
     */
    public void sendFizyoApplicationReceived(String toEmail, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("N.E.F.E.S. — Başvurunuz Alındı");
        message.setText(
            "Sayın " + fullName + ",\n\n" +
            "Fizyoterapist başvurunuz N.E.F.E.S. sistemine başarıyla iletilmiştir.\n\n" +
            "Başvurunuz yönetici tarafından incelendikten sonra e-posta yoluyla bilgilendirileceksiniz.\n" +
            "Onay süreci genellikle 1-3 iş günü sürmektedir.\n\n" +
            "Saygılarımızla,\nN.E.F.E.S. Ekibi"
        );
        mailSender.send(message);
    }

    /**
     * Admin fizyoterapist başvurusunu onayladığında gönderilen e-posta.
     * Davet kodunu içerir — fizyoterapist bunu hastalarıyla paylaşır.
     * @param toEmail Fizyoterapistin e-posta adresi
     * @param fullName Fizyoterapistin tam adı
     * @param inviteCode Sisteme özgü 6 haneli davet kodu
     */
    public void sendFizyoApproved(String toEmail, String fullName, String inviteCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("N.E.F.E.S. — Başvurunuz Onaylandı! 🎉");
        message.setText(
            "Sayın " + fullName + ",\n\n" +
            "Fizyoterapist başvurunuz onaylanmıştır. Artık N.E.F.E.S. sistemine giriş yapabilirsiniz.\n\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "DAVET KODUNUZ: " + inviteCode + "\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
            "Bu kodu hastalarınız ve ailelerle paylaşın. Hasta kayıt formunda bu kodu girerek\n" +
            "size otomatik olarak bağlanacaklar. Her fizyoterapist en fazla 40 hasta yönetebilir.\n\n" +
            "Giriş için: " + frontendUrl + "/giris\n\n" +
            "Saygılarımızla,\nN.E.F.E.S. Ekibi"
        );
        mailSender.send(message);
    }

    /**
     * Admin fizyoterapist başvurusunu reddettiğinde gönderilen e-posta.
     * @param toEmail Fizyoterapistin e-posta adresi
     * @param fullName Fizyoterapistin tam adı
     */
    public void sendFizyoRejected(String toEmail, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("N.E.F.E.S. — Başvuru Durumu Hakkında");
        message.setText(
            "Sayın " + fullName + ",\n\n" +
            "Fizyoterapist başvurunuz incelenmiş ancak onaylanamamıştır.\n\n" +
            "Daha fazla bilgi almak için bizimle iletişime geçebilirsiniz.\n\n" +
            "Saygılarımızla,\nN.E.F.E.S. Ekibi"
        );
        mailSender.send(message);
    }

    /**
     * Kullanıcı "Şifremi Unuttum" formunu doldurduğunda gönderilen sıfırlama e-postası.
     * Bağlantı 1 saat geçerlidir ve yalnızca bir kez kullanılabilir.
     * @param toEmail Kullanıcının e-posta adresi
     * @param fullName Kullanıcının tam adı
     * @param resetToken Güvenli sıfırlama token'ı (UUID)
     */
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetToken) {
        // Şifre sıfırlama URL'sini oluştur — frontend'deki reset sayfasına yönlendirir
        String resetLink = frontendUrl + "/sifre-sifirla?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("N.E.F.E.S. — Şifre Sıfırlama Talebi");
        message.setText(
            "Sayın " + fullName + ",\n\n" +
            "Hesabınız için şifre sıfırlama talebinde bulunulmuştur.\n\n" +
            "Şifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın:\n" +
            resetLink + "\n\n" +
            "⚠️  Bu bağlantı 1 saat geçerlidir ve yalnızca bir kez kullanılabilir.\n\n" +
            "Eğer bu talebi siz yapmadıysanız bu e-postayı görmezden gelebilirsiniz.\n" +
            "Hesabınız güvende olmaya devam edecektir.\n\n" +
            "Saygılarımızla,\nN.E.F.E.S. Ekibi"
        );
        mailSender.send(message);
    }

    /**
     * Fizyoterapist hastayı sisteme eklediğinde aileye gönderilen karşılama ve şifre e-postası.
     * @param toEmail Ailenin e-posta adresi
     * @param fullName Ailenin tam adı
     * @param generatedPassword Sistem tarafından üretilmiş geçici şifre
     */
    public void sendPatientCredentials(String toEmail, String fullName, String generatedPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("N.E.F.E.S. — Sisteme Hoş Geldiniz!");
        message.setText(
            "Sayın " + fullName + ",\n\n" +
            "Fizyoterapistiniz sizi N.E.F.E.S. sistemine başarıyla ekledi.\n\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "GİRİŞ BİLGİLERİNİZ:\n" +
            "E-posta: " + toEmail + "\n" +
            "Şifre: " + generatedPassword + "\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
            "Sisteme giriş yaptıktan sonra güvenliğiniz için şifrenizi değiştirmeyi unutmayın.\n\n" +
            "Giriş için: " + frontendUrl + "/giris\n\n" +
            "Saygılarımızla,\nN.E.F.E.S. Ekibi"
        );
        mailSender.send(message);
    }
}
