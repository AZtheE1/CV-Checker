package com.cvreviewapp.utils;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Production-ready Email Utility with HTML support, retry logic, and background processing.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public class EmailUtil {
    private static final Logger LOGGER = Logger.getLogger(EmailUtil.class.getName());
    private static final ExecutorService EMAIL_EXECUTOR = Executors.newFixedThreadPool(2);
    private static final Properties SMTP_PROPS = new Properties();

    static {
        try (InputStream input = EmailUtil.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                LOGGER.severe("Unable to find application.properties for Email Settings");
            } else {
                SMTP_PROPS.load(input);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load email configurations", e);
        }
    }

    private EmailUtil() {}

    /**
     * Sends an HTML email in a background thread with retry logic.
     */
    public static void sendHtmlEmailAsync(String to, String subject, String body) {
        EMAIL_EXECUTOR.submit(() -> {
            int attempt = 0;
            boolean sent = false;
            while (attempt < Constants.MAX_RETRY_ATTEMPTS && !sent) {
                try {
                    sendHtmlEmailSync(to, subject, body);
                    sent = true;
                    LOGGER.info("Email sent successfully to: " + to);
                } catch (Exception e) {
                    attempt++;
                    LOGGER.log(Level.WARNING, "Failed to send email to " + to + " (Attempt " + attempt + ")", e);
                    if (attempt < Constants.MAX_RETRY_ATTEMPTS) {
                        try {
                            Thread.sleep(Constants.RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
            if (!sent) {
                LOGGER.severe("Failed to send email to " + to + " after " + Constants.MAX_RETRY_ATTEMPTS + " attempts.");
            }
        });
    }

    private static void sendHtmlEmailSync(String to, String subject, String htmlContent) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_PROPS.getProperty("smtp.host"));
        props.put("mail.smtp.port", SMTP_PROPS.getProperty("smtp.port"));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    SMTP_PROPS.getProperty("smtp.user"),
                    SMTP_PROPS.getProperty("smtp.pass")
                );
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_PROPS.getProperty("smtp.user")));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(htmlContent, "text/html; charset=utf-8");

        Transport.send(message);
    }

    public static void shutdown() {
        EMAIL_EXECUTOR.shutdown();
        LOGGER.info("Email executor service shut down.");
    }
}