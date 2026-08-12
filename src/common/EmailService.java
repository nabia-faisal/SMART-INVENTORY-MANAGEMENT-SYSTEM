package common;

import config.AppConfig;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Reusable SMTP email sender.
 * All credentials come from AppConfig — do not hardcode them here.
 */
public class EmailService {

    public static void sendEmail(String to, String subject, String body) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", AppConfig.SMTP_HOST);
        props.put("mail.smtp.port", AppConfig.SMTP_PORT);
        // Without explicit timeouts, JavaMail waits indefinitely on a slow/blocked
        // network, which freezes the UI (login runs this on the Swing event thread).
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(AppConfig.SMTP_USER, AppConfig.SMTP_PASSWORD);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(AppConfig.SMTP_USER));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}
