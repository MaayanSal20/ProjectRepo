package Server;
import entities.Reservation;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import entities.Subscriber;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;



/**
 * NotificationService handles sending notifications to customers and subscribers.
 * It supports emails and simulated SMS messages.
 *
 * All notifications are sent asynchronously using a single background thread
 * to avoid blocking the server main flow.
 */
public class NotificationService {

	
	 /**
     * Single-thread executor used for sending notifications asynchronously.
     * Runs as a daemon thread.
     */
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "notifications");
        t.setDaemon(true);
        return t;
    });

    /**
     * Sender email address used for all outgoing emails.
     */
    private static final String SENDER_EMAIL = "projectg14n5@gmail.com";
    
    /**
     * Application password for the sender email account.
     */
    private static final String APP_PASSWORD = "uzai hawv ctvj jylb";

    
    /**
     * Sends a subscription details email to a subscriber asynchronously.
     *
     * @param s the subscriber to notify
     */
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

    
    /**
     * Sends a subscription details email to a subscriber.
     *
     * @param s the subscriber to notify
     * @throws MessagingException if email sending fails
     */
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

    /**
     * Builds and configures a mail session for Gmail SMTP.
     *
     * @return configured mail session
     */
    private static Session buildSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });
    }
    
    /**
     * Sends a waitlist availability notification email asynchronously.
     *
     * @param email    customer email address
     * @param confCode reservation confirmation code
     */
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

    
    /**
     * Sends a waitlist availability notification email.
     *
     * @param email    customer email address
     * @param confCode reservation confirmation code
     * @throws MessagingException if email sending fails
     */
    private static void sendWaitlistNotification(String email, int confCode) throws MessagingException {
        Session session = buildSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        message.setSubject("Bistro: Your table is available now!");

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
    
 
    /**
     * Sends a reservation confirmation email asynchronously.
     *
     * @param email         customer email
     * @param r             reservation details
     * @param phoneOptional optional phone number
     */
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

    /**
     * Sends a reservation confirmation email.
     *
     * @param email         customer email
     * @param r             reservation details
     * @param phoneOptional optional phone number
     * @throws MessagingException if email sending fails
     */
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




    /**
     * Simulates sending a reservation confirmation SMS asynchronously.
     *
     * @param phone customer phone number
     * @param r     reservation details
     */
    public static void sendReservationSmsSimAsync(String phone, Reservation r) {
        if (phone == null || phone.trim().isEmpty()) return;

        EXEC.submit(() -> {
            try {
                String text =
                        "Bistro Reservation Confirmed ✅\n" +
                        "Confirmation Code: " + r.getConfCode() + "\n" +
                        "Date/Time: " + r.getReservationTime() + "\n" +
                        "Guests: " + r.getNumOfDin();

                
                System.out.println("[SMS SIM] to " + phone.trim() + ":\n" + text);

            } catch (Throwable t) {
                System.out.println("[NotificationService] Failed to send SMS sim: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

 
    /**
     * Sends a reservation reminder email asynchronously.
     * The reminder is sent approximately two hours before the reservation time.
     *
     * @param email customer email
     * @param r     reservation details
     */
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

    /**
     * Sends a reservation reminder email.
     *
     * @param email customer email
     * @param r     reservation details
     * @throws MessagingException if email sending fails
     */
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

    /**
     * Sends a simulated SMS reminder about an upcoming reservation.
     * The reminder is sent asynchronously and printed to the console.
     *
     * @param phone the destination phone number
     * @param r the reservation to remind about
     */
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
    
    /**
     * Sends a waitlist offer email asynchronously.
     * The email notifies the customer that a table is available.
     * Uses an existing confirmation code.
     *
     * @param email recipient email address
     * @param confCode existing confirmation code
     * @param tableNum assigned table number
     */
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

    



     /**
      * Builds and sends the actual waitlist offer email.
      * This method is synchronous and uses Jakarta Mail.
      *
      * @param email recipient email address
      * @param confCode confirmation code
      * @param tableNum table number offered
      * @throws MessagingException if email sending fails
      */
     private static void sendWaitlistOfferEmail(String email, int confCode, int tableNum) throws MessagingException {
         Session session = buildSession();

         Message message = new MimeMessage(session);
         message.setFrom(new InternetAddress(SENDER_EMAIL));
         message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
         message.setSubject("Bistro: Your table is available now!");

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




     /**
      * Sends a simulated SMS informing a waitlist customer that a table is available.
      * The message is sent asynchronously and logged to the console.
      *
      * @param phone destination phone number
      * @param confirmationCode confirmation code
      * @param tableNum available table number
      */
     public static void sendWaitlistOfferSmsSimAsync(String phone, int confirmationCode, int tableNum) {
         EXEC.submit(() -> {
             try {
                 System.out.println("[SMS] To: " + phone + " | Table available. Code: " + confirmationCode + ", Table: " + tableNum);
             } catch (Throwable t) {
                 System.out.println("[NotificationService] Failed to send waitlist offer sms: " + t.getMessage());
             }
         });
 
    }
 
     /**
      * Sends a bill email asynchronously after the reservation duration has ended.
      *
      * @param email recipient email address
      * @param confCode confirmation code
      * @param finalAmount total amount to be paid
      */
     public static void sendBillEmailAsync(String email, int confCode, java.math.BigDecimal finalAmount) {
    	    if (email == null || email.trim().isEmpty()) return;

    	    EXEC.submit(() -> {
    	        try {
    	            sendBillEmail(email.trim(), confCode, finalAmount);
    	        } catch (Throwable t) {
    	            System.out.println("[NotificationService] Failed to send bill email: " + t.getMessage());
    	            t.printStackTrace();
    	        }
    	    });
    	}

     
     /**
      * Builds and sends the bill email synchronously.
      *
      * @param email recipient email address
      * @param confCode confirmation code
      * @param finalAmount total amount to be paid
      * @throws MessagingException if email sending fails
      */
    	private static void sendBillEmail(String email, int confCode, java.math.BigDecimal finalAmount)
    	        throws MessagingException {

    	    Session session = buildSession();

    	    Message message = new MimeMessage(session);
    	    message.setFrom(new InternetAddress(SENDER_EMAIL));
    	    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
    	    message.setSubject("Bistro - Your bill is ready 🧾");

    	    String html = """
    	        <html>
    	          <body>
    	            <h2 style='color:#1565c0'>Your bill is ready 🧾</h2>
    	            <p>Two hours have passed since you were seated.</p>
    	            <p><b>Confirmation Code:</b> %s</p>
    	            <p><b>Amount to pay:</b> %s ₪</p>
    	            <p>You can pay using your confirmation code in the app or at the restaurant terminal.</p>
    	            <hr/>
    	            <p style='color:gray;font-size:12px'>Bistro system notification</p>
    	          </body>
    	        </html>
    	        """.formatted(confCode , finalAmount);

    	    message.setContent(html, "text/html; charset=UTF-8");
    	    Transport.send(message);

    	    System.out.println("[NotificationService] Bill email sent to " + email);
    	}

    	/**
    	 * Sends a simulated bill SMS asynchronously.
    	 *
    	 * @param phone destination phone number
    	 * @param confCode confirmation code
    	 * @param finalAmount total amount to be paid
    	 */
    	public static void sendBillSmsSimAsync(String phone, int confCode, java.math.BigDecimal finalAmount) {
    	    if (phone == null || phone.trim().isEmpty()) return;

    	    EXEC.submit(() -> {
    	        try {
    	            String text =
    	                "Bistro Bill 🧾\n" +
    	                "2 hours passed since seating.\n" +
    	                "ConfCode: " + confCode + "\n" +
    	                "Amount: " + finalAmount + " ₪\n" +
    	                "Pay via app/terminal using your code.";

    	            System.out.println("[SMS SIM] to " + phone.trim() + ":\n" + text);

    	        } catch (Throwable t) {
    	            System.out.println("[NotificationService] Failed to send bill SMS sim: " + t.getMessage());
    	            t.printStackTrace();
    	        }
    	    });
    	 }
    	
    	/**
    	 * Sends a "reservation canceled" email asynchronously.
    	 *
    	 * This method is used when a reservation is canceled automatically due to an
    	 * operational update, such as table configuration changes or opening-hours updates.
    	 *
    	 * If the email is missing or empty, the method does not attempt to send and logs
    	 * that the operation was skipped.
    	 *
    	 * The email is sent on a background executor thread and includes basic logging
    	 * for success and failure to support debugging during tests and grading.
    	 *
    	 * @param email recipient email address
    	 * @param r reservation data used for message details (date/time, number of guests)
    	 * @param reason short reason describing the cancellation cause (nullable)
    	 */

    	public static void sendReservationCanceledAsync(String email, entities.Reservation r, String reason) {
    		if (email == null || email.trim().isEmpty()) {
    	        System.out.println("[Email] cancel mail SKIPPED (empty email)");
    	        return;
    	    }

    	    System.out.println("[Email] queue cancel mail to: " + email);


    	    EXEC.submit(() -> {
    	        try {
    	            Session session = buildSession();

    	            Message message = new MimeMessage(session);
    	            message.setFrom(new InternetAddress(SENDER_EMAIL));
    	            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.trim()));
    	            message.setSubject("Bistro - Reservation Canceled");

    	            String html = """
    	                <html>
    	                  <body>
    	                    <h2 style='color:#d32f2f'>Reservation Canceled ❌</h2>
    	                    <p>Unfortunately, your reservation was canceled due to an operational update.</p>
    	                    <p><b>Date/Time:</b> %s</p>
    	                    <p><b>Guests:</b> %s</p>
    	                    <p><b>Reason:</b> %s</p>
    	                    <hr/>
    	                    <p style='color:gray;font-size:12px'>Bistro system notification</p>
    	                  </body>
    	                </html>
    	                """.formatted(
    	                    r.getReservationTime(),
    	                    r.getNumOfDin(),
    	                    (reason == null ? "Restaurant update" : reason)
    	                );

    	            message.setContent(html, "text/html; charset=UTF-8");
    	            Transport.send(message);
    	            
    	            System.out.println("[Email] cancel mail SENT OK to: " + email);

    	        } catch (Throwable t) {
    	            System.out.println("[Email] cancel mail FAILED to: " + email + " reason=" + t);
    	            t.printStackTrace(); // הכי חשוב
    	        }
    	    });
    	}


}
