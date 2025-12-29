package air_quality_monitor;

import java.io.IOException;
import java.util.Map;

import gui.AirQualityChart;
import service.ReportService;
import sql.SqlLiteConnection;
import utility.CsvReader;
import air_quality.AirQualityStats;

public class Main {
	private static final String FILE_PATTERN_AQ = "Datasets/QualitàAria_%d.csv";
	private static final String FILE_PATTERN_M = "Datasets/MorteCampania_2006-2022.CSV";
    private static final int START_YEAR = 2020;
    private static final int END_YEAR = 2022;

public static void main(String[] args) {
		
		SqlLiteConnection.initDatabase();
	
		CsvReader estrattore = new CsvReader();
	
	    System.out.println("## ⏳ Caricamento dati Qualità Aria da " + START_YEAR + " a " + END_YEAR + "...");
	
	    for (int year = START_YEAR; year <= END_YEAR; year++) {
	        String filePath = String.format(FILE_PATTERN_AQ, year);
	        try {
	            estrattore.importAirQualityToDb(filePath);
	        } catch (IOException e) {
	            System.err.printf("❌ Errore di I/O, file non trovato o non leggibile: %s. %s%n", filePath, e.getMessage());
	        } catch (Exception e) {
	             System.err.printf("❌ Errore generico durante l'elaborazione di %s: %s%n", filePath, e.getMessage());
	        }
	    }
	    
	    try {
	    	estrattore.importMortalityToDb(FILE_PATTERN_M);
	    } catch (IOException e) {
	    	System.err.printf("❌ Errore di I/O, file non trovato o non leggibile: %s. %s%n", FILE_PATTERN_M, e.getMessage());
	    } catch (Exception e) {
            System.err.printf("❌ Errore generico durante l'elaborazione di %s: %s%n", FILE_PATTERN_M, e.getMessage());
       }

	   try {
		   System.out.println("\n## 📄 Generazione Report Finale...");
		   ReportService pdfService = new ReportService();
		   pdfService.generatePdf(
			   START_YEAR,
			   END_YEAR,
		       "Rapporto Ambientale e Sanitario", 
		       "Questo documento analizza la correlazione tra la concentrazione di inquinanti atmosferici " +
		       "e i tassi di mortalità per cause respiratorie e circolatorie nella regione Campania."
		   );
	   } catch (Exception e) {
           System.err.println("Errore di I/O durante la lettura dei file: " + e.getMessage());
       }
	    
       try {
        	AirQualityStats stats = new AirQualityStats();
        	
            System.out.println("\n## 💨 Media Annuale Qualità Aria per Inquinante (Globale):");
            Map<String, Double> medieAnnuali = stats.getAnnualAverageByPollutant();
            medieAnnuali.forEach((chiave, media) -> {
                 String[] parti = chiave.split("\\|");
                 System.out.printf("Anno: %s, Inquinante: %s, Media Valore: %.4f%n", 
                                   parti[0], parti[1], media);
            });
            
            AirQualityChart frame = new AirQualityChart(stats, 2016);
            frame.setVisible(true);

        } catch (Exception e) {
            System.err.println("Errore di I/O durante la lettura dei file: " + e.getMessage());
        }
    }



}
