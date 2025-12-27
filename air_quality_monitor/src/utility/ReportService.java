package utility;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import air_quality.AirQualityStats;
import air_quality.MortalityStats;
import gui.AirQualityChart;
import org.jfree.chart.ChartUtils;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Map;

public class ReportService {

    private final AirQualityStats aqStats = new AirQualityStats();
    private final MortalityStats mStats = new MortalityStats();

    public void generatePdf(int year, String title, String description) {
        Document document = new Document(PageSize.A4);
        try {
            String fileName = "Report_Analisi_" + year + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // 1. TITOLO
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
            Paragraph mainTitle = new Paragraph(title, titleFont);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(mainTitle);

            document.add(new Paragraph("Anno di analisi: " + year));
            document.add(Chunk.NEWLINE);

            // 2. DESCRIZIONE
            document.add(new Paragraph("Descrizione:", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            document.add(new Paragraph(description));
            document.add(Chunk.NEWLINE);

            // 3. INSIGHT AUTOMATICI (Dati dall'Aria)
            document.add(new Paragraph("Statistiche Qualità Aria", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            Map<String, Double> medieAria = aqStats.getMonthlyStats(year, 1); 
            document.add(new Paragraph("Media inquinanti rilevata nel periodo di riferimento (Esempio Gennaio):"));
            for (Map.Entry<String, Double> entry : medieAria.entrySet()) {
                document.add(new Paragraph("- " + entry.getKey() + ": " + String.format("%.2f", entry.getValue()) + " µg/m³"));
            }
            document.add(Chunk.NEWLINE);

            // 4. INSIGHT MORTALITÀ
            document.add(new Paragraph("Statistiche Mortalità", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            Map<String, Integer> morti = mStats.getStatsByYear(year);
            morti.entrySet().stream()
                 .max(Map.Entry.comparingByValue())
                 .ifPresent(e -> {
                     try {
                         document.add(new Paragraph("Causa principale di decesso: " + e.getKey()));
                         document.add(new Paragraph("Totale decessi per questa causa: " + e.getValue()));
                     } catch (Exception ex) { ex.printStackTrace(); }
                 });

            document.add(Chunk.NEWLINE);

            // 5. INSERIMENTO GRAFICO
            document.add(new Paragraph("Andamento Grafico Inquinanti", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            
            // Generiamo l'immagine dal grafico JFreeChart
            AirQualityChart chartFrame = new AirQualityChart(aqStats, year);
            File chartFile = new File("temp_chart.png");
            ChartUtils.saveChartAsPNG(chartFile, chartFrame.getChart(), 800, 500);
            
            Image chartImage = Image.getInstance(chartFile.getAbsolutePath());
            chartImage.scaleToFit(500, 400);
            chartImage.setAlignment(Element.ALIGN_CENTER);
            document.add(chartImage);

            document.close();
            chartFile.delete(); // Pulizia file temporaneo
            
            System.out.println("✅ PDF Generato con successo: " + fileName);

        } catch (Exception e) {
            System.err.println("❌ Errore generazione PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}