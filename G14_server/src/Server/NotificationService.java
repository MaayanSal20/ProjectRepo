package Server;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import entities.Subscriber;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class NotificationService {

    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "notifications");
        t.setDaemon(true);
        return t;
    });

    private static final String SENDER_EMAIL = "projectg14n5@gmail.com";
    private static final String APP_PASSWORD = "uzai hawv ctvj jylb";

    public static void sendSubscriberEmailAsync(Subscriber s) {
        EXEC.submit(() -> {
            try {
                sendSubscriberEmail(s);
            } catch (Throwable t) {
                System.out.println("[NotificationService] Failed to send email: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    public static void sendSubscriberEmail(Subscriber s) throws MessagingException {
        Session session = buildSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(s.getEmail()));
        message.setSubject("Your Bistro Subscription Details");

        String html = """
            <html>
              <body>
                <h2 style='color:#2e7d32'>Welcome to Bistro!</h2>
                <p>Hello %s,</p>
                <p>Your subscriber code is: <b style='color:#d32f2f'>%s</b></p>
                <p>Phone: %s<br/>Email: %s</p>
              </body>
            </html>
            """.formatted(s.getName(), s.getSubscriberId(), s.getPhone(), s.getEmail());

        message.setContent(html, "text/html; charset=UTF-8");
        Transport.send(message);

        System.out.println("[NotificationService] Email sent to " + s.getEmail());
    }

    private static Session buildSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        //System.out.println("Mail Session class: " + jakarta.mail.Session.class.getProtectionDomain().getCodeSource());

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });
    }
}
