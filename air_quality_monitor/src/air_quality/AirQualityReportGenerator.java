package report;

import java.io.IOException;
import java.util.Map;

// Importazioni della libreria iText
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.properties.TextAlignment;

import air_quality.AirQualityStats;

public class AirQualityReportGenerator {

    private final AirQualityStats stats;

    public AirQualityReportGenerator(AirQualityStats stats) {
        this.stats = stats;
    }

    /**
     * Genera e salva il report in formato PDF.
     * @param destPath Il percorso dove salvare il file PDF.
     * @param reportTitle Il titolo del report.
     * @throws IOException Se c'è un errore di scrittura del file.
     */
    public void generateReport(String destPath, String reportTitle) throws IOException {
        
        // 1. Inizializzazione di iText
        PdfWriter writer = new PdfWriter(destPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            // Aggiungi Titolo
            document.add(new Paragraph(reportTitle)
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));

            // Aggiungi data
            document.add(new Paragraph("Data di Generazione: " + java.time.LocalDate.now().toString())
                .setTextAlignment(TextAlignment.RIGHT)
                .setItalic());
            
            // --- 2. Abstract ---
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("## Abstract")
                .setFontSize(18)
                .setBold());
            
            // Un abstract di esempio basato sui dati annuali
            Map<String, Double> annualAverages = stats.getAnnualAverageByPollutant();
            String abstractText = generateAbstractText(annualAverages);
            
            document.add(new Paragraph(abstractText));
            
            // --- 3. Dati Statistici ---
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("## Dati Statistici: Medie Annuali per Inquinante")
                .setFontSize(18)
                .setBold());

            // Aggiungi la lista delle statistiche
            document.add(createStatisticsList(annualAverages));
            
        } finally {
            // Chiude il documento
            document.close();
        }
    }

    private String generateAbstractText(Map<String, Double> annualAverages) {
        StringBuilder sb = new StringBuilder();
        sb.append("Questo report presenta un'analisi delle misurazioni di qualità dell'aria, " + 
                  "basata sui dati forniti. Le statistiche sono calcolate come media annuale " + 
                  "per i vari inquinanti rilevati.\n\n");
        
        // Trova l'inquinante con la media più alta per un abstract dinamico
        if (!annualAverages.isEmpty()) {
            Map.Entry<String, Double> maxEntry = annualAverages.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
            
            if (maxEntry != null) {
                String[] parts = maxEntry.getKey().split("\\|");
                sb.append(String.format("L'analisi mostra che l'inquinante con la media annuale più alta " + 
                                        "è stato il **%s** nell'anno **%s**, con un valore medio di %.4f.", 
                                        parts[1], parts[0], maxEntry.getValue()));
            }
        } else {
             sb.append("Non sono stati rilevati dati validi per il calcolo delle medie annuali.");
        }
        return sb.toString();
    }
    
    private List createStatisticsList(Map<String, Double> annualAverages) {
        List list = new List();
        
        annualAverages.forEach((chiave, media) -> {
            String[] parti = chiave.split("\\|");
            String listItemText = String.format("Anno: %s, Inquinante: %s, Media Valore: %.4f", 
                                                parti[0], parti[1], media);
            list.add(new ListItem(listItemText));
        });
        
        return list;
    }
}