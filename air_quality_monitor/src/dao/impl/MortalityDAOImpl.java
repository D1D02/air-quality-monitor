package dao.impl;

import air_quality.Mortality;
import java.sql.*;
import java.util.*;
import dao.MortalityDAO;
import sql.SqlLiteConnection;

public class MortalityDAOImpl implements MortalityDAO {

    @Override
    public void insertBatch(List<Mortality> list) throws SQLException {
        String sql = "INSERT INTO mortality (malattia, anno, decessi) VALUES (?, ?, ?)";
        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Mortality m : list) {
                pstmt.setString(1, m.getIllness());
                pstmt.setInt(2, m.getYear());
                pstmt.setInt(3, m.getDeathNumber());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        }
    }
    
    @Override
    public Map<String, Integer> getDeathsByYear(int year) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT malattia, decessi FROM mortality WHERE anno = ?";
        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getString("malattia"), rs.getInt("decessi"));
                }
            }
        }
        return stats;
    }
    
    @Override
    public Map<Integer, Map<String, Integer>> getDeathsByYears(
            int startYear, int endYear) throws SQLException {

        Map<Integer, Map<String, Integer>> result = new HashMap<>();

        String sql = """
            SELECT anno, malattia, decessi
            FROM mortality
            WHERE anno BETWEEN ? AND ?
        """;

        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, startYear);
            pstmt.setInt(2, endYear);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int year = rs.getInt("anno");
                    String illness = rs.getString("malattia");
                    int deaths = rs.getInt("decessi");

                    result
                        .computeIfAbsent(year, y -> new HashMap<>())
                        .put(illness, deaths);
                }
            }
        }

        return result;
    }

    
    @Override
    public Map<String, Integer> getTotalDeathsByIllness() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT malattia, SUM(decessi) as totale FROM mortality GROUP BY malattia";

        try (Connection conn = SqlLiteConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("malattia"), rs.getInt("totale"));
            }
        }
        return stats;
    }

    @Override
    public List<Mortality> getAll() throws SQLException {
        List<Mortality> list = new ArrayList<>();
        String sql = "SELECT malattia, decessi FROM mortality";
        try (Connection conn = SqlLiteConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Mortality(rs.getString("malattia"), rs.getInt("anno"), rs.getInt("decessi")));
            }
        }
        return list;
    }
}