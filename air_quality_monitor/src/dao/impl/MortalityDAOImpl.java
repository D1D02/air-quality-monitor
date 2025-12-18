package dao.impl;

import air_quality.Mortality;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import dao.MortalityDAO;
import sql.SqlLiteConnection;

public class MortalityDAOImpl implements MortalityDAO {
	@Override
	public void insertBatch(List<Mortality> list) throws SQLException {
        String sql = "INSERT INTO mortality (malattia, decessi) VALUES (?, ?)";
        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            for (Mortality m : list) {
                pstmt.setString(1, m.getIllness());
                pstmt.setInt(2, m.getDeathNumber());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        }
    }

	@Override
    public List<Mortality> getAll() throws SQLException {
        List<Mortality> list = new ArrayList<>();
        String sql = "SELECT malattia, decessi FROM mortality";
        
        try (Connection conn = SqlLiteConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(new Mortality(
                    rs.getString("malattia"),
                    rs.getInt("decessi")
                ));
            }
        }
        return list;
    }
}
