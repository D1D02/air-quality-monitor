package air_quality_monitor;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.Map;

import gui.AirQualityChart;
import gui.AirQualityUI;
import sql.SqlLiteConnection;
import utility.CsvReader;
import air_quality.Mortality;
import air_quality.AirQuality;
import air_quality.AirQualityStats;

public class Main {
	private static final String FILE_PATTERN = "Datasets/QualitàAria_%d.csv";
    private static final int START_YEAR = 2006;
    private static final int END_YEAR = 2022;

public static void main(String[] args) {
		
		SqlLiteConnection.initDatabase();
	
		CsvReader estrattore = new CsvReader();
	    List<AirQuality> datiAriaTotali = new ArrayList<>();
	
	    System.out.println("## ⏳ Caricamento dati Qualità Aria da " + START_YEAR + " a " + END_YEAR + "...");
	
	    for (int year = START_YEAR; year <= END_YEAR; year++) {
	        String filePath = String.format(FILE_PATTERN, year);
	        try {
	            estrattore.importAirQualityToDb(filePath);
	        } catch (IOException e) {
	            System.err.printf("❌ Errore di I/O, file non trovato o non leggibile: %s. %s%n", filePath, e.getMessage());
	        } catch (Exception e) {
	             System.err.printf("❌ Errore generico durante l'elaborazione di %s: %s%n", filePath, e.getMessage());
	        }
	    }
	    
	    System.out.println("\n---");
	    System.out.printf("## ✨ Caricamento Completato. Totale record: %d%n", datiAriaTotali.size());
	    
	    if (datiAriaTotali.isEmpty()) {
	        System.out.println("Nessun dato valido caricato. Terminazione.");
	        return;
	    }

        try {
        	AirQualityStats stats = new AirQualityStats(datiAriaTotali);

            System.out.println("\n## 💨 Media Annuale Qualità Aria per Inquinante (Globale):");
            Map<String, Double> medieAnnuali = stats.getAnnualAverageByPollutant();
            medieAnnuali.forEach((chiave, media) -> {
                 String[] parti = chiave.split("\\|");
                 System.out.printf("Anno: %s, Inquinante: %s, Media Valore: %.4f%n", 
                                   parti[0], parti[1], media);
            });
            
            // Creo e mostro il grafico
            AirQualityChart frame = new AirQualityChart(stats);
            frame.setVisible(true);

        } catch (Exception e) {
            System.err.println("Errore di I/O durante la lettura dei file: " + e.getMessage());
        }
    }



}
