package Server;
import entities.Reservation;

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
    
    public static void sendWaitlistNotificationAsync(String email, int confCode) {
        EXEC.submit(() -> {
            try {
                sendWaitlistNotification(email, confCode);
            } catch (Throwable t) {
                System.out.println("[NotificationService] Failed to send waitlist email: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private static void sendWaitlistNotification(String email, int confCode) throws MessagingException {
        Session session = buildSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        message.setSubject("Bistro: Your table is available עכשיו!");

        String html = """
            <html>
              <body>
                <h2 style='color:#1565c0'>Your table is ready!</h2>
                <p>We have a table available for you.</p>
                <p><b>Confirmation code:</b> %s</p>
                <p><b>Important:</b> The table is reserved for <b>2 hours from this notification time</b>.</p>
              </body>
            </html>
            """.formatted(confCode);

        message.setContent(html, "text/html; charset=UTF-8");
        Transport.send(message);

        System.out.println("[NotificationService] Waitlist email sent to " + email);
    }
    
 // ✅ Reservation confirmation (EMAIL)
    public static void sendReservationEmailAsync(String email, Reservation r, String phoneOptional) {
        if (email == null || email.trim().isEmpty()) return;

        EXEC.submit(() -> {
            try {
                sendReservationEmail(email.trim(), r, phoneOptional);
            } catch (Throwable t) {
                System.out.println("[NotificationService] Failed to send reservation email: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private static void sendReservationEmail(String email, Reservation r, String phoneOptional) throws MessagingException {
	    Session session = buildSession();
	
	    Message message = new MimeMessage(session);
	    message.setFrom(new InternetAddress(SENDER_EMAIL));
	    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
	    message.setSubject("Bistro - Reservation Confirmation");
	
	    String phoneLine = (phoneOptional != null && !phoneOptional.trim().isEmpty())
	            ? "<p><b>Phone:</b> " + phoneOptional + "</p>"
	            : "";
	
	    String html = """
	        <html>
	          <body>
	            <h2 style='color:#1565c0'>Reservation Confirmed ✅</h2>
	            <p>Your reservation has been created successfully.</p>
	            <p><b>Confirmation Code:</b> %s</p>
	            <p><b>Date/Time:</b> %s</p>
	            <p><b>Guests:</b> %s</p>
	            %s
	            <hr/>
	            <p style='color:gray;font-size:12px'>Bistro system notification</p>
	          </body>
	        </html>
	        """.formatted(
	            r.getConfCode(),
	            r.getReservationTime(),
	            r.getNumOfDin(),
	            phoneLine
	        );
	
	    message.setContent(html, "text/html; charset=UTF-8");
	    Transport.send(message);
	
	    System.out.println("[NotificationService] Reservation email sent to " + email);
	}



    // ✅ SMS (simulation)
    public static void sendReservationSmsSimAsync(String phone, Reservation r) {
        if (phone == null || phone.trim().isEmpty()) return;

        EXEC.submit(() -> {
            try {
                String text =
                        "Bistro Reservation Confirmed ✅\n" +
                        "Confirmation Code: " + r.getConfCode() + "\n" +
                        "Date/Time: " + r.getReservationTime() + "\n" +
                        "Guests: " + r.getNumOfDin();

                // סימולציה: מדפיסים לשרת (כי SMS אמיתי דורש ספק חיצוני כמו Twilio)
                System.out.println("[SMS SIM] to " + phone.trim() + ":\n" + text);

            } catch (Throwable t) {
                System.out.println("[NotificationService] Failed to send SMS sim: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

 // ✅ Reservation reminder (EMAIL) - 2 hours before
    public static void sendReservationReminderEmailAsync(String email, Reservation r) {
        if (email == null || email.trim().isEmpty() || r == null) return;

        EXEC.submit(() -> {
            try {
                sendReservationReminderEmail(email.trim(), r);
            } catch (Throwable t) {
                System.out.println("[NotificationService] Failed to send reminder email: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private static void sendReservationReminderEmail(String email, Reservation r) throws MessagingException {
        Session session = buildSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        message.setSubject("Bistro - Reminder: Your reservation is in 2 hours ⏰");

        String html = """
            <html>
              <body>
                <h2 style='color:#ef6c00'>Reservation Reminder ⏰</h2>
                <p>This is a reminder that your reservation is coming up in <b>2 hours</b>.</p>
                <p><b>Confirmation Code:</b> %s</p>
                <p><b>Date/Time:</b> %s</p>
                <p><b>Guests:</b> %s</p>
                <hr/>
                <p style='color:gray;font-size:12px'>Bistro system notification</p>
              </body>
            </html>
            """.formatted(
                r.getConfCode(),
                r.getReservationTime(),
                r.getNumOfDin()
            );

        message.setContent(html, "text/html; charset=UTF-8");
        Transport.send(message);

        System.out.println("[NotificationService] Reminder email sent to " + email);
    }

    // ✅ Reservation reminder (SMS simulation)
    public static void sendReservationReminderSmsSimAsync(String phone, Reservation r) {
        if (phone == null || phone.trim().isEmpty() || r == null) return;

        EXEC.submit(() -> {
            try {
                String text =
                    "Bistro Reminder ⏰\n" +
                    "Your reservation is in ~2 hours.\n" +
                    "ConfCode: " + r.getConfCode() + "\n" +
                    "Date/Time: " + r.getReservationTime() + "\n" +
                    "Guests: " + r.getNumOfDin();

                System.out.println("[SMS SIM] to " + phone.trim() + ":\n" + text);

            } catch (Throwable t) {
                System.out.println("[NotificationService] Failed to send reminder SMS sim: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }
    
 // Sends a real email notifying a waitlist customer that a table is available.
 // Uses the EXISTING confirmation code (no generation here).
 public static void sendWaitlistOfferEmailAsync(String email, int confCode, int tableNum) {
     if (email == null || email.trim().isEmpty()) return;

     EXEC.submit(() -> {
         try {
             sendWaitlistOfferEmail(email.trim(), confCode, tableNum);
         } catch (Throwable t) {
             System.out.println("[NotificationService] Failed to send waitlist offer email: " + t.getMessage());
             t.printStackTrace();
         }
     });
 }

 // Builds and sends the actual waitlist offer email using Jakarta Mail.
 // This does NOT duplicate your existing logic: it reuses buildSession() and Transport.send().
 private static void sendWaitlistOfferEmail(String email, int confCode, int tableNum) throws MessagingException {
     Session session = buildSession();

     Message message = new MimeMessage(session);
     message.setFrom(new InternetAddress(SENDER_EMAIL));
     message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
     message.setSubject("Bistro: Your table is available עכשיו!");

     String html = """
         <html>
           <body>
             <h2 style='color:#1565c0'>Your table is ready!</h2>
             <p>We have a table available for you at <b>Bistro</b>.</p>
             <p><b>Table Number:</b> %s</p>
             <p><b>Confirmation code:</b> %s</p>
             <p><b>Important:</b> The table is reserved for <b>15 minutes</b> from this notification time.</p>
             <hr/>
             <p style='color:gray;font-size:12px'>Bistro system notification</p>
           </body>
         </html>
         """.formatted(tableNum, confCode);

     message.setContent(html, "text/html; charset=UTF-8");
     Transport.send(message);

     System.out.println("[NotificationService] Waitlist offer email sent to " + email);
 }


     // Simulates sending an SMS (or prints/logs it).
     public static void sendWaitlistOfferSmsSimAsync(String phone, int confirmationCode, int tableNum) {
         EXEC.submit(() -> {
             try {
                 System.out.println("[SMS] To: " + phone + " | Table available. Code: " + confirmationCode + ", Table: " + tableNum);
             } catch (Throwable t) {
                 System.out.println("[NotificationService] Failed to send waitlist offer sms: " + t.getMessage());
             }
         });
 
    }


}
