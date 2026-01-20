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

    public void importMortalityToDb(String path, String region) throws IOException, SQLException {
        List<Mortality> batch = new ArrayList<>();
        
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setTrim(true)
                .build();

        try (Reader reader = Files.newBufferedReader(Path.of(path));
             CSVParser parser = format.parse(reader)) {
            
            List<CSVRecord> records = parser.getRecords();
            if (records.isEmpty()) return;

            CSVRecord headerYear = records.get(0);
            List<Integer> years = new ArrayList<>();
            for (int i = 1; i < headerYear.size(); i++) {
                try {
                    years.add(Integer.parseInt(headerYear.get(i).trim()));
                } catch (NumberFormatException e) {
                    years.add(-1); 
                }
            }

            for (int i = 3; i < records.size(); i++) {
                CSVRecord record = records.get(i);
                String malattia = record.get(0).trim();
                if (malattia.isEmpty()) continue;

                for (int j = 1; j < record.size(); j++) {
                    int colIdx = j - 1;
                    if (colIdx < years.size() && years.get(colIdx) != -1) {
                        try {
                            String valStr = record.get(j).trim().replace(".", "");
                            if (!valStr.equals("..")) { 
                                int morti = Integer.parseInt(valStr);
                                batch.add(new Mortality(malattia, years.get(colIdx), morti));
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
            mortalityDAO.insertBatch(batch, region);
            System.out.println("✅ Importazione mortalità (matrice) completata.");
        }
    }

    public void importAirQualityToDb(String path, String region) throws IOException, SQLException {
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

                    if (batch.size() >= 5000) {
                        airQualityDAO.insertBatch(batch, region);
                        batch.clear();
                    }
                } catch (Exception ignored) {}
            }
            
            if (!batch.isEmpty()) {
                airQualityDAO.insertBatch(batch, region);
            }
            System.out.println("✅ Importati " + count + " record da: " + path);
        }
    }
}