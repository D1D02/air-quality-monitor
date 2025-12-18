package utility;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.csv.*;
import air_quality.AirQuality;
import sql.SqlLiteConnection;

public class CsvReader {

    public void importMortalityToDb(String path) throws IOException, SQLException {
        String sql = "INSERT INTO mortality (malattia, decessi) VALUES (?, ?)";
        
        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false); 

            try (Reader reader = Files.newBufferedReader(Path.of(path));
                 CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
                
                for (CSVRecord record : parser) {
                    try {
                        String malattia = record.get(0).trim();
                        int morti = Integer.parseInt(record.get(1).trim().replace(".", ""));
                        
                        pstmt.setString(1, malattia);
                        pstmt.setInt(2, morti);
                        pstmt.addBatch();
                    } catch (Exception ignored) {}
                }
                pstmt.executeBatch();
                conn.commit();
                System.out.println("✅ Importazione mortalità completata.");
            }
        }
    }


    public void importAirQualityToDb(String path) throws IOException, SQLException {
        String sql = "INSERT INTO air_quality (data_ora, inquinante, unita, valore) VALUES (?, ?, ?, ?)";

        try (Connection conn = SqlLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); 

            try (Reader reader = Files.newBufferedReader(Path.of(path));
                 CSVParser parser = CSVFormat.DEFAULT.builder()
                         .setHeader()
                         .setSkipHeaderRecord(true)
                         .setTrim(true)
                         .setIgnoreSurroundingSpaces(true)
                         .build()
                         .parse(reader)) {

                Map<String, String> headerMap = new HashMap<>();
                parser.getHeaderMap().keySet().forEach(h -> headerMap.put(h.trim().toLowerCase(), h));
                boolean hasOraSeparata = headerMap.containsKey("ora_misura");

                int count = 0;
                for (CSVRecord record : parser) {
                    try {
                        String dateStr, timeStr = null, cod, unit, valStr;

                        if (hasOraSeparata) {
                            dateStr = record.get(headerMap.get("data_misura"));
                            timeStr = record.get(headerMap.get("ora_misura"));
                            cod = record.get(headerMap.get("codice_inquinante"));
                            unit = record.get(headerMap.get("unita_misura"));
                            valStr = record.get(headerMap.get("valore_inquinante"));
                        } else {
                            dateStr = record.get(headerMap.get("data_rilevazione"));
                            cod = record.get(headerMap.get("codice_inquinante"));
                            unit = record.get(headerMap.get("unita_misura"));
                            valStr = record.get(headerMap.get("valore_inquinante"));
                        }

                        AirQuality aq = new AirQuality(dateStr, timeStr, cod, unit, valStr);

                        if (aq.getvaluePolluting() >= 0) {
                            pstmt.setString(1, aq.getdate().toString()); 
                            pstmt.setString(2, aq.getcodPolluting());
                            pstmt.setString(3, aq.getmeasurementUnit());
                            pstmt.setDouble(4, aq.getvaluePolluting());
                            pstmt.addBatch();
                            count++;
                        }

                        if (count % 5000 == 0) pstmt.executeBatch();

                    } catch (Exception ignored) {}
                }
                pstmt.executeBatch();
                conn.commit(); 
                System.out.println("✅ Importati " + count + " record da: " + path);
            }
        }
    }
}