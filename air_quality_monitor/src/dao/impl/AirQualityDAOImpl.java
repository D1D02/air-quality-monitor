package dao.impl;

import air_quality.AirQuality;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.AirQualityDAO;
import sql.SqlLiteConnection;

public class AirQualityDAOImpl implements AirQualityDAO {
	@Override
	public void insertBatch(List<AirQuality> records) throws SQLException {
        String sql = "INSERT INTO air_quality (data_ora, inquinante, unita, valore) VALUES (?, ?, ?, ?)";
        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            for (AirQuality aq : records) {
                pstmt.setString(1, aq.getdate().toString());
                pstmt.setString(2, aq.getcodPolluting());
                pstmt.setString(3, aq.getmeasurementUnit());
                pstmt.setDouble(4, aq.getvaluePolluting());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        }
    }
	
	@Override
    public List<AirQuality> getAll() throws SQLException {
        List<AirQuality> list = new ArrayList<>();
        String sql = "SELECT data_ora, inquinante, unita, valore FROM air_quality";
        
        try (Connection conn = SqlLiteConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
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
        return list;
    }
	
	@Override
	public Map<String, Double> getAverageByPollutant() throws SQLException {
        Map<String, Double> stats = new HashMap<>();
        String sql = "SELECT inquinante, AVG(valore) as media FROM air_quality GROUP BY inquinante";

        try (Connection conn = SqlLiteConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("inquinante"), rs.getDouble("media"));
            }
        }
        return stats;
    }

	@Override
    public Map<String, Double> getAnnualAverageByPollutant() throws SQLException {
        Map<String, Double> stats = new HashMap<>();
        String sql = "SELECT strftime('%Y', data_ora) as anno, inquinante, AVG(valore) as media " +
                     "FROM air_quality GROUP BY anno, inquinante";

        try (Connection conn = SqlLiteConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String key = rs.getString("anno") + "|" + rs.getString("inquinante");
                stats.put(key, rs.getDouble("media"));
            }
        }
        return stats;
    }
	
	@Override
    public Map<String, Double> getAverageByMonth(int year, int month) throws SQLException {
        Map<String, Double> stats = new HashMap<>();
        String monthStr = String.format("%02d", month); 
        String yearStr = String.valueOf(year);

        String sql = "SELECT inquinante, AVG(valore) as media FROM air_quality " +
                     "WHERE strftime('%Y', data_ora) = ? AND strftime('%m', data_ora) = ? " +
                     "GROUP BY inquinante";

        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, yearStr);
            pstmt.setString(2, monthStr);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getString("inquinante"), rs.getDouble("media"));
                }
            }
        }
        return stats;
    }

	
	 
}
