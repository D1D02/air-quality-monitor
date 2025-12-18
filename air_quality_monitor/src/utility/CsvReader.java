package utility;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.*;
import air_quality.AirQuality;
import air_quality.Mortality;
import dao.impl.AirQualityDAOImpl;
import dao.impl.MortalityDAOImpl;

public class CsvReader {
    
    private final AirQualityDAOImpl airQualityDAO = new AirQualityDAOImpl();
    private final MortalityDAOImpl mortalityDAO = new MortalityDAOImpl();

    public void importMortalityToDb(String path) throws IOException, SQLException {
        List<Mortality> batch = new ArrayList<>();
        
        try (Reader reader = Files.newBufferedReader(Path.of(path));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
            
            for (CSVRecord record : parser) {
                try {
                    String malattia = record.get(0).trim();
                    int morti = Integer.parseInt(record.get(1).trim().replace(".", ""));
                    batch.add(new Mortality(malattia, morti));
                } catch (Exception ignored) {}
            }
            mortalityDAO.insertBatch(batch);
            System.out.println("✅ Importazione mortalità completata.");
        }
    }

    public void importAirQualityToDb(String path) throws IOException, SQLException {
        List<AirQuality> batch = new ArrayList<>();
        int count = 0;

        try (Reader reader = Files.newBufferedReader(Path.of(path));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader().setSkipHeaderRecord(true)
                     .setTrim(true).setIgnoreSurroundingSpaces(true)
                     .build().parse(reader)) {

            Map<String, String> headerMap = new HashMap<>();
            parser.getHeaderMap().keySet().forEach(h -> headerMap.put(h.trim().toLowerCase(), h));
            boolean hasOraSeparata = headerMap.containsKey("ora_misura");

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
                        batch.add(aq);
                        count++;
                    }

                    // Per risparmiare RAM, svuotiamo il batch nel DB ogni 5000 record
                    if (batch.size() >= 5000) {
                        airQualityDAO.insertBatch(batch);
                        batch.clear();
                    }
                } catch (Exception ignored) {}
            }
            // Inseriamo gli ultimi record rimanenti
            if (!batch.isEmpty()) {
                airQualityDAO.insertBatch(batch);
            }
            System.out.println("✅ Importati " + count + " record da: " + path);
        }
    }
}