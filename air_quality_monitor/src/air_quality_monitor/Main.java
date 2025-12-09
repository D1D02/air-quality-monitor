package air_quality_monitor;

import java.util.List;
import java.io.IOException;
import java.util.Map;

import gui.AirQualityChart;
import gui.AirQualityUI;
import utility.CsvReader;
import air_quality.Mortality;
import air_quality.AirQuality;
import air_quality.AirQualityStats;

public class Main {
	private static final String FILE_MORTALITA = "mortality.csv";
    private static final String FILE_QUALITA_ARIA = "air_quality.csv";

public static void main(String[] args) {
        
        CsvReader estrattore = new CsvReader();

        try {
            /*System.out.println("## 📉 Dati di Mortality Estratti:");
            List<Mortality> listaMorti = estrattore.extractMortality(FILE_MORTALITA);
            listaMorti.forEach(System.out::println);
            
            System.out.println("\n" + "---" + "\n");

            List<AirQuality> listaAria = estrattore.extractAirQuality(FILE_QUALITA_ARIA);
            
            AirQualityStats stats = new AirQualityStats(listaAria);
            
            System.out.println("## 💨 Media Annuale Qualità Aria per Inquinante (Globale):");
            Map<String, Double> medieAnnuali = stats.getAnnualAverageByPollutant();
            medieAnnuali.forEach((chiave, media) -> {
                 String[] parti = chiave.split("\\|");
                 System.out.printf("Anno: %s, Inquinante: %s, Media Valore: %.4f%n", 
                                   parti[0], parti[1], media);
            });
            
            System.out.println("\n" + "---" + "\n");
            
            int ANNO_DA_FILTRARE = 2022;
            int MESE_DA_FILTRARE = 1;
            
            System.out.printf("## 🔬 Media Mensile Qualità Aria per Inquinante (Mese %d/%d):%n", MESE_DA_FILTRARE, ANNO_DA_FILTRARE);
            
            AirQualityStats statsFiltrate = stats.filterByMonth(ANNO_DA_FILTRARE, MESE_DA_FILTRARE);
            
            Map<String, Double> medieMensili = statsFiltrate.getAverageByPollutant();
            
            medieMensili.forEach((inquinante, media) -> {
                 System.out.printf("Mese: %d/%d, Inquinante: %s, Media Valore: %.4f%n", 
                                   MESE_DA_FILTRARE, ANNO_DA_FILTRARE, inquinante, media);
            });*/
            AirQualityStats stats = new AirQualityStats(estrattore.extractAirQuality("air_quality.csv"));

            // Creo e mostro il grafico
            AirQualityChart frame = new AirQualityChart(stats);
            frame.setVisible(true);

        } catch (IOException e) {
            System.err.println("Errore di I/O durante la lettura dei file: " + e.getMessage());
        }
    }



}
