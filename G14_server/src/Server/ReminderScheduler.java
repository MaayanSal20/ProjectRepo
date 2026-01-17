package Server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ReminderScheduler is responsible for running background jobs
 * related to reservations, waitlists, billing, and monthly reports.
 * The scheduler runs periodically using a single daemon thread.
 */
public class ReminderScheduler {

	/**
     * Single-thread scheduled executor used to run background jobs.
     * The thread runs as a daemon so it does not block server shutdown.
     */
    private static final ScheduledExecutorService SES =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "reservation-reminders");
                t.setDaemon(true);
                return t;
            });

    /**
     * Indicates whether the scheduler has already been started.
     * Prevents starting the scheduler more than once.
     */
    private static volatile boolean started = false;

    /**
     * Starts the reminder scheduler.
     * This method is safe to call multiple times, but the scheduler
     * will only be started once.
     * The scheduled task runs every minute.
     */
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
   

    /**
     * Runs the monthly reports snapshot job if today is the first
     * day of the month.
     * The snapshot is generated for the previous month.
     */
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