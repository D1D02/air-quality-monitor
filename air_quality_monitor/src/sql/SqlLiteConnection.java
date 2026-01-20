package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlLiteConnection {
    private static final String URL = "jdbc:sqlite:air_quality_data.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initDatabase() {
    	
    	//String dropAir = "DROP TABLE IF EXISTS air_quality";
        
    	//String dropMortality = "DROP TABLE IF EXISTS mortality";
        
        String sqlAir = "CREATE TABLE IF NOT EXISTS air_quality (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "data_ora TEXT, " +
                        "inquinante TEXT, " +
                        "unita TEXT, " +
                        "valore REAL," + 
                        "regione TEXT)";

        String sqlMortality = "CREATE TABLE IF NOT EXISTS mortality (" +
                              "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                              "malattia TEXT, " +
                              "anno INTEGER, " +
                              "decessi INTEGER," + 
                              "regione TEXT)";

        try (Connection conn = getConnection(); 
             Statement stmt = conn.createStatement()) {
        	//stmt.execute(dropAir);
            //stmt.execute(dropMortality);
            stmt.execute(sqlAir);
            stmt.execute(sqlMortality);
            System.out.println("✅ Database inizializzato correttamente.");
        } catch (SQLException e) {
            System.err.println("❌ Errore inizializzazione DB: " + e.getMessage());
        }
    }
}