package com.cvreviewapp.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;
import com.cvreviewapp.utils.Config;

public class EmailUtil {
    // Configure your SMTP server here
    private static final String SMTP_HOST = Config.get("SMTP_HOST", "smtp.example.com");
    private static final String SMTP_PORT = Config.get("SMTP_PORT", "587");
    private static final String SMTP_USER = Config.get("SMTP_USER", "your_email@example.com");
    private static final String SMTP_PASS = Config.get("SMTP_PASS", "your_email_password");
    private static final Logger logger = Logger.getLogger(EmailUtil.class.getName());

    public static String send2FACode(String toEmail) {
        String code = String.format("%06d", new Random().nextInt(999999));
        String subject = "Your 2FA Code";
        String body = "Your 2FA code is: " + code;
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            logger.info("[EmailUtil] Sent 2FA code to " + toEmail);
            return code;
        } catch (Exception e) {
            logger.severe("[EmailUtil] Failed to send 2FA code: " + e.getMessage());
            return null;
        }
    }
} 