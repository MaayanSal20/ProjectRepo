package server_repositries;

import java.sql.*;

public class WaitlistRepository {

    private WaitlistRepository() {}

    // --- JOIN ---

    public static String joinSubscriber(Connection con, int subscriberId, int diners) {
        try {
            Integer costumerId = getCostumerIdBySubscriber(con, subscriberId);
            if (costumerId == null) return "Subscriber has no linked customer.";

            int confCode = allocateConfCode(con);
            if (confCode == 0) return "No confirmation codes available.";

            String sql =
                "INSERT INTO schema_for_project.waitinglist " +
                "(ConfirmationCode, timeEnterQueue, NumberOfDiners, costumerId, status) " +
                "VALUES (?, NOW(), ?, ?, 'WAITING')";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, confCode);
                ps.setInt(2, diners);
                ps.setInt(3, costumerId);
                ps.executeUpdate();
            }

            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String joinNonSubscriber(Connection con, String email, String phone, int diners) {
        try {
            int costumerId = getOrCreateCostumerId(con, email, phone);

            int confCode = allocateConfCode(con);
            if (confCode == 0) return "No confirmation codes available.";

            String sql =
                "INSERT INTO schema_for_project.waitinglist " +
                "(ConfirmationCode, timeEnterQueue, NumberOfDiners, costumerId, status) " +
                "VALUES (?, NOW(), ?, ?, 'WAITING')";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, confCode);
                ps.setInt(2, diners);
                ps.setInt(3, costumerId);
                ps.executeUpdate();
            }

            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    // --- LEAVE ---

    public static String leaveSubscriber(Connection con, int subscriberId) {
        try {
            Integer costumerId = getCostumerIdBySubscriber(con, subscriberId);
            if (costumerId == null) return "Subscriber has no linked customer.";

            Integer confCode = findLatestWaitingConfCodeByCostumer(con, costumerId);
            if (confCode == null) return "No active WAITING entry found for subscriber.";

            String upd =
                "UPDATE schema_for_project.waitinglist " +
                "SET status='CANCELLED' " +
                "WHERE ConfirmationCode=? AND status='WAITING'";

            try (PreparedStatement ps = con.prepareStatement(upd)) {
                ps.setInt(1, confCode);
                if (ps.executeUpdate() != 1) return "No active WAITING entry found for subscriber.";
            }

            freeConfCode(con, confCode);
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String leaveNonSubscriber(Connection con, String email, String phone) {
        try {
            Integer costumerId = getCostumerIdByEmailPhone(con, email, phone);
            if (costumerId == null) return "Customer not found.";

            Integer confCode = findLatestWaitingConfCodeByCostumer(con, costumerId);
            if (confCode == null) return "No active WAITING entry found for this customer.";

            String upd =
                "UPDATE schema_for_project.waitinglist " +
                "SET status='CANCELLED' " +
                "WHERE ConfirmationCode=? AND status='WAITING'";

            try (PreparedStatement ps = con.prepareStatement(upd)) {
                ps.setInt(1, confCode);
                if (ps.executeUpdate() != 1) return "No active WAITING entry found for this customer.";
            }

            freeConfCode(con, confCode);
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static boolean hasActiveWait(Connection con, int subscriberId) {
        try {
            Integer costumerId = getCostumerIdBySubscriber(con, subscriberId);
            if (costumerId == null) return false;

            String sql =
                "SELECT 1 FROM schema_for_project.waitinglist " +
                "WHERE costumerId=? AND status='WAITING' " +
                "LIMIT 1";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, costumerId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    // ----------------- helpers -----------------

    private static Integer getCostumerIdBySubscriber(Connection con, int subscriberId) throws SQLException {
        String sql =
            "SELECT CostumerId FROM schema_for_project.subscriber " +
            "WHERE subscriberId=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subscriberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Object v = rs.getObject("CostumerId");
                return (v == null) ? null : rs.getInt("CostumerId");
            }
        }
    }

    private static Integer getCostumerIdByEmailPhone(Connection con, String email, String phone) throws SQLException {
        String sql =
            "SELECT CostumerId FROM schema_for_project.costumer " +
            "WHERE Email=? AND PhoneNum=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt("CostumerId");
            }
        }
    }

    private static int getOrCreateCostumerId(Connection con, String email, String phone) throws SQLException {
        Integer existing = getCostumerIdByEmailPhone(con, email, phone);
        if (existing != null) return existing;

        String ins =
            "INSERT INTO schema_for_project.costumer (PhoneNum, Email) " +
            "VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, phone);
            ps.setString(2, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Failed to create customer.");
                return keys.getInt(1);
            }
        }
    }

    private static Integer findLatestWaitingConfCodeByCostumer(Connection con, int costumerId) throws SQLException {
        String sql =
            "SELECT ConfirmationCode FROM schema_for_project.waitinglist " +
            "WHERE costumerId=? AND status='WAITING' " +
            "ORDER BY timeEnterQueue DESC " +
            "LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, costumerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt("ConfirmationCode");
            }
        }
    }

    private static int allocateConfCode(Connection con) throws SQLException {
        String pick =
            "SELECT code FROM schema_for_project.conf_codes " +
            "WHERE in_use=0 " +
            "LIMIT 1 FOR UPDATE";

        Integer code = null;
        try (PreparedStatement ps = con.prepareStatement(pick);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return 0;
            code = rs.getInt("code");
        }

        String mark =
            "UPDATE schema_for_project.conf_codes " +
            "SET in_use=1 " +
            "WHERE code=? AND in_use=0";

        try (PreparedStatement ps2 = con.prepareStatement(mark)) {
            ps2.setInt(1, code);
            if (ps2.executeUpdate() != 1) return 0;
        }

        return code;
    }

    private static void freeConfCode(Connection con, int confCode) throws SQLException {
        String sql =
            "UPDATE schema_for_project.conf_codes " +
            "SET in_use=0 " +
            "WHERE code=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, confCode);
            ps.executeUpdate();
        }
    }
}
