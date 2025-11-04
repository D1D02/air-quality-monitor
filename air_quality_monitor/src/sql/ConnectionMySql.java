package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import utility.EnvLoader;

public class ConnectionMySql {

    private static final String DB_HOST = EnvLoader.get("DB_HOST");
    private static final String DB_PORT = EnvLoader.get("DB_PORT");
    private static final String DB_NAME = EnvLoader.get("DB_NAME");
    private static final String DB_USER = EnvLoader.get("DB_USER"); 
    private static final String DB_PASSWORD = EnvLoader.get("DB_PASSWORD");
    
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;

    public static Connection getConnection() {
        
        if (DB_USER == null || DB_PASSWORD == null) {
            System.err.println("❌ Impossibile connettersi: Credenziali mancanti dal file .env.");
            return null;
        }
        
        Connection connection = null;

        try {
            System.out.println("Tentativo di connessione a: " + DB_URL);
            
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            System.out.println("✅ Connessione al database stabilita con successo!");
            
        } catch (SQLException e) {
            System.err.println("❌ Errore durante la connessione al database!");
            System.err.println("Dettagli: Controlla che MySQL sia attivo e che la URL/Credenziali siano corrette.");
            e.printStackTrace();
            
        }

        return connection;
    }
 
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connessione al database chiusa.");
            } catch (SQLException e) {
                System.err.println("Errore durante la chiusura della connessione.");
                e.printStackTrace();
            }
        }
    }
}