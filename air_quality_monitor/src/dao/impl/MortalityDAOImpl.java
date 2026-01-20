package dao.impl;

import air_quality.Mortality;
import java.sql.*;
import java.util.*;
import dao.MortalityDAO;
import sql.SqlLiteConnection;

public class MortalityDAOImpl implements MortalityDAO {

    @Override
    public void insertBatch(List<Mortality> list, String region) throws SQLException {
        String sql = "INSERT INTO mortality (malattia, anno, decessi, regione) VALUES (?, ?, ?, ?);";
        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            for (Mortality m : list) {
                pstmt.setString(1, m.getIllness());
                pstmt.setInt(2, m.getYear());
                pstmt.setInt(3, m.getDeathNumber());
                pstmt.setString(4, region);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        }
    }

    @Override
    public Map<String, Integer> getDeathsByYear(int year, String region) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT malattia, decessi FROM mortality WHERE anno = ? AND regione = ?;";
        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, year);
            pstmt.setString(2, region); 
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getString("malattia"), rs.getInt("decessi"));
                }
            }
        }
        return stats;
    }

    @Override
    public Map<Integer, Map<String, Integer>> getDeathsByYears(int startYear, int endYear, String region) throws SQLException {
        Map<Integer, Map<String, Integer>> result = new HashMap<>();
        String sql = "SELECT anno, malattia, decessi FROM mortality WHERE regione = ? AND anno BETWEEN ? AND ?;";

        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, region);
            pstmt.setInt(2, startYear);
            pstmt.setInt(3, endYear);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int year = rs.getInt("anno");
                    String illness = rs.getString("malattia");
                    int deaths = rs.getInt("decessi");

                    result.computeIfAbsent(year, y -> new HashMap<>())
                          .put(illness, deaths);
                }
            }
        }
        return result;
    }

    @Override
    public Map<String, Integer> getTotalDeathsByIllness(String region) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT malattia, SUM(decessi) as totale FROM mortality WHERE regione = ? GROUP BY malattia;";

        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, region);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getString("malattia"), rs.getInt("totale"));
                }
            }
        }
        return stats;
    }

    @Override
    public List<Mortality> getAll(String region) throws SQLException {
        List<Mortality> list = new ArrayList<>();
        String sql = "SELECT malattia, anno, decessi FROM mortality WHERE regione = ?;";
        
        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, region);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Mortality(
                        rs.getString("malattia"), 
                        rs.getInt("anno"), 
                        rs.getInt("decessi")
                    ));
                }
            }
        }
        return list;
    }
}