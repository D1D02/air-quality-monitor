package utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {

    private static final Map<String, String> ENV_VARS = new HashMap<>();
    private static final String ENV_FILE_PATH = ".env"; 

    static {
        loadEnvFile();
    }

    private static void loadEnvFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ENV_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String key = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    
                    if (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    ENV_VARS.put(key, value);
                }
            }
            System.out.println("✅ File .env caricato con successo senza librerie esterne.");
        } catch (IOException e) {
            System.err.println("❌ Errore durante il caricamento del file .env: " + ENV_FILE_PATH);
            System.err.println("Assicurati che il file esista nella directory principale del progetto.");
        }
    }

    public static String get(String key) {
        return ENV_VARS.get(key);
    }
}