package Server;

import java.sql.*;

import entities.Subscriber;

public class SubscribersRepository {

    public Integer findCostumerId(Connection conn, String phone, String email) throws SQLException {
        String sql = "SELECT CostumerId FROM costumer WHERE PhoneNum=? OR Email=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("CostumerId") : null;
            }
        }
    }

    public int insertCostumer(Connection conn, String phone, String email) throws SQLException {
        String sql = "INSERT INTO costumer (PhoneNum, Email) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, phone);
            ps.setString(2, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("Failed to get generated CostumerId");
            }
        }
    }

    public Integer findSubscriberCodeByCostumerId(Connection conn, int costumerId) throws SQLException {
        String sql = "SELECT subscriberId FROM subscriber WHERE CostumerId=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, costumerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("subscriberId") : null;
            }
        }
    }

    public int insertSubscriber(Connection conn, String name, String personalInfo, int costumerId) throws SQLException {
        
        String sql = "INSERT INTO subscriber (Personalinfo, Name, CostumerId) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, personalInfo);
            ps.setString(2, name);
            ps.setInt(3, costumerId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1); // SubscribtionCode
                throw new SQLException("Failed to get generated SubscribtionCode");
            }
        }
    }
    
    /*hala 05/01/2026*/
      public boolean phoneExists(Connection conn, String phone) throws SQLException {
        String sql = "SELECT 1 FROM costumer WHERE PhoneNum=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public boolean emailExists(Connection conn, String email) throws SQLException {
        String sql = "SELECT 1 FROM costumer WHERE Email=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
    
    public static Subscriber checkSubscriberById(Connection conn, int subscriberId) {
        String sql =
            "SELECT s.subscriberId, s.name, s.personalInfo, c.email " +
            "FROM subscriber s " +
            "JOIN costumer c ON s.costumerId = c.costumerId " +
            "WHERE s.subscriberId = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subscriberId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return null;

            return new Subscriber(
                rs.getInt("subscriberId"),
                rs.getString("name"),
                rs.getString("personalInfo"),
                rs.getString("email")
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


 


}
