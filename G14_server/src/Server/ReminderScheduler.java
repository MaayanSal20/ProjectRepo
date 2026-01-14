package Server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    private static final ScheduledExecutorService SES =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "reservation-reminders");
                t.setDaemon(true);
                return t;
            });

    private static volatile boolean started = false;

    public static synchronized void start() {
        if (started) return;
        started = true;

        SES.scheduleAtFixedRate(() -> {
            try {
                DBController.runReservationReminderJob();
                DBController.runWaitlistExpireJob();
            } catch (Exception e) {
                System.out.println("[ReminderScheduler] error: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }
}