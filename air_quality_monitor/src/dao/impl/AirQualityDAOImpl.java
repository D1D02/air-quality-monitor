package dao.impl;

import air_quality.AirQuality;
import java.sql.*;
import java.util.*;
import dao.AirQualityDAO;
import sql.SqlLiteConnection;

public class AirQualityDAOImpl implements AirQualityDAO {

    @Override
    public void insertBatch(List<AirQuality> records, String region) throws SQLException {
        String sql = "INSERT INTO air_quality (data_ora, inquinante, unita, valore, regione) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            for (AirQuality aq : records) {
                pstmt.setString(1, aq.getdate().toString());
                pstmt.setString(2, aq.getcodPolluting());
                pstmt.setString(3, aq.getmeasurementUnit());
                pstmt.setDouble(4, aq.getvaluePolluting());
                pstmt.setString(5, region);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        }
    }

    @Override
    public List<AirQuality> getAll(String region) throws SQLException {
        List<AirQuality> list = new ArrayList<>();
        String sql = "SELECT data_ora, inquinante, unita, valore FROM air_quality WHERE regione = ?";
        
        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, region);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new AirQuality(
                        rs.getString("data_ora"),
                        null, 
                        rs.getString("inquinante"),
                        rs.getString("unita"),
                        String.valueOf(rs.getDouble("valore"))
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public Map<String, Double> getAverageByPollutant(String region) throws SQLException {
        Map<String, Double> stats = new HashMap<>();
        String sql = "SELECT inquinante, AVG(valore) as media FROM air_quality WHERE regione = ? GROUP BY inquinante";

        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, region);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getString("inquinante"), rs.getDouble("media"));
                }
            }
        }
        return stats;
    }

    @Override
    public Map<String, Double> getAnnualAverageByPollutant(String region) throws SQLException {
        Map<String, Double> stats = new HashMap<>();
        String sql = "SELECT strftime('%Y', data_ora) as anno, inquinante, AVG(valore) as media " +
                     "FROM air_quality WHERE regione = ? GROUP BY anno, inquinante";

        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, region);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("anno") + "|" + rs.getString("inquinante");
                    stats.put(key, rs.getDouble("media"));
                }
            }
        }
        return stats;
    }

    @Override
    public Map<String, Double> getAnnualAveragesForPeriod(int startYear, int endYear, String region)
            throws SQLException {

        Map<String, Double> stats = new HashMap<>();

        String sql =
            "SELECT strftime('%Y', data_ora) AS anno, inquinante, AVG(valore) AS media " +
            "FROM air_quality " +
            "WHERE regione = ? " +
            "AND strftime('%Y', data_ora) BETWEEN ? AND ? " +
            "GROUP BY anno, inquinante";

        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, region);
            pstmt.setString(2, String.valueOf(startYear));
            pstmt.setString(3, String.valueOf(endYear));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("anno") + "|" + rs.getString("inquinante");
                    stats.put(key, rs.getDouble("media"));
                }
            }
        }
        return stats;
    }


    @Override
    public Map<String, Double> getAverageByMonth(int year, int month, String region) throws SQLException {
        Map<String, Double> stats = new HashMap<>();
        String sql = "SELECT inquinante, AVG(valore) as media FROM air_quality " +
                     "WHERE strftime('%Y', data_ora) = ? AND strftime('%m', data_ora) = ? " + 
                     "AND regione = ? " +
                     "GROUP BY inquinante";

        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, String.valueOf(year));
            pstmt.setString(2, String.format("%02d", month));
            pstmt.setString(3, region);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getString("inquinante"), rs.getDouble("media"));
                }
            }
        }
        return stats;
    }

    @Override
    public String getMostPollutedMonth(int year, String region) throws SQLException {
        String sql = "SELECT strftime('%m', data_ora) as mese, AVG(valore) as media " +
                     "FROM air_quality WHERE strftime('%Y', data_ora) = ? " +
                     "AND regione = ? " +
                     "GROUP BY mese ORDER BY media DESC LIMIT 1";
        
        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, String.valueOf(year));
            pstmt.setString(2, region);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("mese") : "N/D";
            }
        }
    }
}