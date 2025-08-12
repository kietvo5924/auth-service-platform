package com.authplatform.authservice.service;

import com.authplatform.authservice.model.EndUser;
import com.authplatform.authservice.model.Owner;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.verify-email-url}")
    private String verifyEmailFrontendUrl;

    @Async // Chạy bất đồng bộ để không làm chậm request đăng ký
    public void sendOwnerVerificationEmail(Owner owner, String token) {
        String subject = "Verify Your Email for Auth Platform";
        // Tạo link xác thực trỏ đến frontend
        String verificationUrl = verifyEmailFrontendUrl + "?token=" + token;
        String htmlBody = String.format("""
           <h1>Chào mừng, %s!</h1>
           <p>Vui lòng nhấp vào liên kết bên dưới để xác minh địa chỉ email của bạn:</p>
           <a href="%s">Xác minh Email</a>
            """, owner.getFullName(), verificationUrl);

        sendHtmlEmail(owner.getEmail(), subject, htmlBody);
    }

    @Async
    public void sendEndUserVerificationEmail(EndUser endUser, String token) {
        String subject = "Verify Your Email for " + endUser.getProject().getName();
        // Tạo link xác thực trỏ đến frontend, kèm theo cả apiKey
        String verificationUrl = verifyEmailFrontendUrl
                + "?apiKey=" + endUser.getProject().getApiKey()
                + "&token=" + token;

        String htmlBody = String.format("""
            <h1>Chào mừng, %s!</h1>
            <p>Vui lòng nhấp vào liên kết bên dưới để xác minh email cho dự án '%s':</p>
            <a href="%s">Xác minh Email</a>
            """, endUser.getFullName(), endUser.getProject().getName(), verificationUrl);

        sendHtmlEmail(endUser.getEmail(), subject, htmlBody);
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = nội dung là HTML
            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}", to, e);
        }
    }

}
