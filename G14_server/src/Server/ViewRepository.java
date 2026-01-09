package Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import entities.CurrentDinerRow;
import entities.Subscriber;
import entities.WaitlistRow;

/**
 * ViewRepository contains SQL queries for "view" screens:
 * waitlist, subscribers, current diners.
 * It does NOT manage the connection pool (DBController does).
 */
public class ViewRepository {

    public ArrayList<WaitlistRow> getWaitlist(Connection conn) throws Exception {
        String sql =
            "SELECT w.ConfirmationCode, w.timeEnterQueue, w.NumberOfDiners, w.costumerId, " +
            "       c.PhoneNum, c.Email " +
            "FROM waitinglist w " +
            "JOIN costumer c ON w.costumerId = c.CostumerId " +
            "ORDER BY w.timeEnterQueue ASC";

        ArrayList<WaitlistRow> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
            	list.add(new WaitlistRow(
            		    rs.getInt("ConfirmationCode"),
            		    rs.getTimestamp("timeEnterQueue"),
            		    rs.getInt("NumberOfDiners"),
            		    rs.getInt("costumerId"),
            		    rs.getString("PhoneNum"),
            		    rs.getString("Email")
            		));


            }
        }
        return list;
    }

    public ArrayList<WaitlistRow> getWaitlistByMonth(Connection conn, int year, int month) throws Exception {
        String sql =
            "SELECT w.ConfirmationCode, w.timeEnterQueue, w.NumberOfDiners, w.costumerId, " +
            "       c.PhoneNum, c.Email " +
            "FROM waitinglist w " +
            "JOIN costumer c ON w.costumerId = c.CostumerId " +
            "WHERE YEAR(w.timeEnterQueue) = ? AND MONTH(w.timeEnterQueue) = ? " +
            "ORDER BY w.timeEnterQueue ASC";

        ArrayList<WaitlistRow> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new WaitlistRow(
                        rs.getInt("ConfirmationCode"),
                        rs.getTimestamp("timeEnterQueue"),
                        rs.getInt("NumberOfDiners"),
                        rs.getInt("costumerId"),
                        rs.getString("PhoneNum"),
                        rs.getString("Email")
                    ));
                }
            }
        }
        return list;
    }

    public ArrayList<Subscriber> getSubscribers(Connection conn) throws Exception {
        String sql =
            "SELECT s.subscriberId, s.Name, s.Personalinfo, s.CostumerId, " +
            "       c.PhoneNum, c.Email " +
            "FROM subscriber s " +
            "JOIN costumer c ON s.CostumerId = c.CostumerId " +
            "ORDER BY s.subscriberId ASC";

        ArrayList<Subscriber> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Subscriber(
                    rs.getInt("subscriberId"),
                    rs.getString("Name"),
                    rs.getString("Personalinfo"),
                    rs.getInt("CostumerId"),
                    rs.getString("PhoneNum"),
                    rs.getString("Email")
                ));
            }
        }
        return list;
    }


    /**
     * "Current diners" = ACTIVE reservations with arrivalTime and without leaveTime.
     */
    public ArrayList<CurrentDinerRow> getCurrentDiners(Connection conn) throws Exception {
        String sql =
            "SELECT r.ResId, r.reservationTime, r.NumOfDin, r.Status, r.CustomerId, " +
            "       r.arrivalTime, c.PhoneNum, c.Email " +
            "FROM reservation r " +
            "JOIN costumer c ON r.CustomerId = c.CostumerId " +
            "WHERE r.Status = 'ACTIVE' AND r.arrivalTime IS NOT NULL AND r.leaveTime IS NULL " +
            "ORDER BY r.arrivalTime DESC";

        ArrayList<CurrentDinerRow> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new CurrentDinerRow(
                    rs.getInt("ResId"),
                    rs.getTimestamp("reservationTime"),
                    rs.getInt("NumOfDin"),
                    rs.getString("Status"),
                    rs.getInt("CustomerId"),
                    rs.getTimestamp("arrivalTime"),
                    rs.getString("PhoneNum"),
                    rs.getString("Email")
                ));
            }
        }
        return list;
    }
}
