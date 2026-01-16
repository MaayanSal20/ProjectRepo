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
                runMonthlyIfNeeded();
                DBController.runBillAfterTwoHoursJob();
            } catch (Exception e) {
                System.out.println("[ReminderScheduler] error: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }
    
    private static void runMonthlyIfNeeded() {
        java.time.LocalDate today = java.time.LocalDate.now();
        if (today.getDayOfMonth() != 1) return;

        java.time.YearMonth prev = java.time.YearMonth.from(today).minusMonths(1);
        int year = prev.getYear();
        int month = prev.getMonthValue();

        try {
            DBController.runMonthlyReportsSnapshot(year, month);
        } catch (Exception e) {
            System.out.println("[MonthlySnapshot] error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}