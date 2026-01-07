package Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * ViewRepository contains SQL queries for "view" screens:
 * waitlist, subscribers, current diners.
 * It does NOT manage the connection pool (DBController does).
 */
public class ViewRepository {

    public ArrayList<Object[]> getWaitlist(Connection conn) throws Exception {
        String sql =
            "SELECT w.ConfirmationCode, w.timeEnterQueue, w.NumberOfDiners, w.costumerId, " +
            "       c.PhoneNum, c.Email " +
            "FROM waitinglist w " +
            "JOIN costumer c ON w.costumerId = c.CostumerId " +
            "ORDER BY w.timeEnterQueue ASC";

        ArrayList<Object[]> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("ConfirmationCode"),
                    rs.getTimestamp("timeEnterQueue"),
                    rs.getInt("NumberOfDiners"),
                    rs.getInt("costumerId"),
                    rs.getString("PhoneNum"),
                    rs.getString("Email")
                });
            }
        }
        return list;
    }

    public ArrayList<Object[]> getWaitlistByMonth(Connection conn, int year, int month) throws Exception {
        String sql =
            "SELECT w.ConfirmationCode, w.timeEnterQueue, w.NumberOfDiners, w.costumerId, " +
            "       c.PhoneNum, c.Email " +
            "FROM waitinglist w " +
            "JOIN costumer c ON w.costumerId = c.CostumerId " +
            "WHERE YEAR(w.timeEnterQueue) = ? AND MONTH(w.timeEnterQueue) = ? " +
            "ORDER BY w.timeEnterQueue ASC";

        ArrayList<Object[]> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("ConfirmationCode"),
                        rs.getTimestamp("timeEnterQueue"),
                        rs.getInt("NumberOfDiners"),
                        rs.getInt("costumerId"),
                        rs.getString("PhoneNum"),
                        rs.getString("Email")
                    });
                }
            }
        }
        return list;
    }

    public ArrayList<Object[]> getSubscribers(Connection conn) throws Exception {
        String sql =
            "SELECT s.subscriberId, s.Name, s.Personalinfo, s.CostumerId, " +
            "       c.PhoneNum, c.Email " +
            "FROM subscriber s " +
            "JOIN costumer c ON s.CostumerId = c.CostumerId " +
            "ORDER BY s.subscriberId ASC";

        ArrayList<Object[]> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("subscriberId"),
                    rs.getString("Name"),
                    rs.getString("Personalinfo"),
                    rs.getInt("CostumerId"),
                    rs.getString("PhoneNum"),
                    rs.getString("Email")
                });
            }
        }
        return list;
    }

    /**
     * "Current diners" = ACTIVE reservations with arrivalTime and without leaveTime.
     */
    public ArrayList<Object[]> getCurrentDiners(Connection conn) throws Exception {
        String sql =
            "SELECT r.ResId, r.reservationTime, r.NumOfDin, r.Status, r.CostumerId, " +
            "       r.arrivalTime, r.leaveTime, c.PhoneNum, c.Email " +
            "FROM reservation r " +
            "JOIN costumer c ON r.CostumerId = c.CostumerId " +
            "WHERE r.Status = 'ACTIVE' AND r.arrivalTime IS NOT NULL AND r.leaveTime IS NULL " +
            "ORDER BY r.arrivalTime DESC";

        ArrayList<Object[]> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("ResId"),
                    rs.getTimestamp("reservationTime"),
                    rs.getInt("NumOfDin"),
                    rs.getString("Status"),
                    rs.getInt("CostumerId"),
                    rs.getTimestamp("arrivalTime"),
                    rs.getTimestamp("leaveTime"),
                    rs.getString("PhoneNum"),
                    rs.getString("Email")
                });
            }
        }
        return list;
    }
}
