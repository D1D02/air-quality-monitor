package service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.*;
import java.util.List;

import air_quality.AirQualityStats;
import air_quality.MortalityStats;

public class ReportService {

    private final AirQualityStats aqStats = new AirQualityStats();
    private final MortalityStats mStats = new MortalityStats();

    private static class CorrelationResult {
        String pollutant;
        String illness;
        double value;

        CorrelationResult(String p, String i, double v) {
            pollutant = p;
            illness = i;
            value = v;
        }
    }

    public void generatePdf(int startYear, int endYear, String title, String description, String region) {

        Document document = new Document(PageSize.A4.rotate());
        List<CorrelationResult> allResults = new ArrayList<>();

        try {
            PdfWriter.getInstance(document, new FileOutputStream("Report_Finale_Analisi.pdf"));
            document.open();

            /* =======================
               1️⃣ PRECARICAMENTO DATI
               ======================= */

            Map<String, Double> airQualityData =
                    aqStats.getStatsByYears(startYear, endYear, region);

            Map<Integer, Map<String, Integer>> mortalityData =
                    mStats.getStatsByYears(startYear, endYear, region);

            Set<String> pollutants = extractPollutants(airQualityData);
            Set<String> illnesses = extractIllnesses(mortalityData);

            CorrelationService corrService =
                    new CorrelationService(airQualityData, mortalityData);

            /* =======================
               2️⃣ INTESTAZIONE
               ======================= */

            document.add(new Paragraph(title,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22)));
            document.add(new Paragraph(
                    "Periodo: " + startYear + " - " + endYear));
            document.add(new Paragraph(description));
            document.add(Chunk.NEWLINE);

            /* =======================
               3️⃣ MATRICE CORRELAZIONE
               ======================= */

            PdfPTable table = new PdfPTable(pollutants.size() + 1);
            table.setWidthPercentage(100);

            table.addCell(createStyledCell(
                    "Patologia / Inquinante", true, Color.LIGHT_GRAY));

            for (String p : pollutants) {
                table.addCell(createStyledCell(p, true, Color.LIGHT_GRAY));
            }

            for (String illness : illnesses) {
                table.addCell(new Phrase(
                        illness,
                        FontFactory.getFont(FontFactory.HELVETICA, 8)));

                for (String pollutant : pollutants) {
                    double r = corrService.getCorrelation(
                            pollutant, illness, startYear, endYear);

                    if (!Double.isNaN(r)) {
                        allResults.add(
                                new CorrelationResult(pollutant, illness, r));
                    }

                    table.addCell(createCorrelationCell(r));
                }
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            /* =======================
               4️⃣ LEGENDA
               ======================= */

            addLegend(document);
            document.add(Chunk.NEWLINE);

            /* =======================
               5️⃣ CONCLUSIONI
               ======================= */

            document.add(new Paragraph(
                    "Conclusioni e Risultati Critici",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));

            document.add(new Paragraph(
                    "Le correlazioni più forti individuate sono:"));

            allResults.sort(
                    (a, b) -> Double.compare(b.value, a.value));

            int shown = 0;
            for (CorrelationResult r : allResults) {
                if (shown == 3) break;
                if (Math.abs(r.value) >= 0.5) {
                    document.add(new Paragraph(String.format(
                            "%d. %s vs %s → r = %.4f",
                            shown + 1,
                            r.pollutant,
                            r.illness,
                            r.value
                    )));
                    shown++;
                }
            }

            document.close();
            System.out.println("✅ Report generato correttamente");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* =======================
       METODI DI SUPPORTO
       ======================= */

    private Set<String> extractPollutants(Map<String, Double> data) {
        Set<String> set = new TreeSet<>();
        for (String key : data.keySet()) {
            if (key.contains("|")) {
                set.add(key.split("\\|")[1]);
            }
        }
        return set;
    }

    private Set<String> extractIllnesses(
            Map<Integer, Map<String, Integer>> data) {

        Set<String> set = new TreeSet<>();
        for (Map<String, Integer> m : data.values()) {
            set.addAll(m.keySet());
        }
        return set;
    }

    private void addLegend(Document document) throws DocumentException {
        PdfPTable legend = new PdfPTable(2);
        legend.setWidthPercentage(30);
        legend.setHorizontalAlignment(Element.ALIGN_LEFT);

        legend.addCell(createCorrelationCell(0.85));
        legend.addCell("Forte correlazione");

        legend.addCell(createCorrelationCell(-0.65));
        legend.addCell("Correlazione inversa");

        document.add(legend);
    }

    private PdfPCell createCorrelationCell(double r) {
        String text = Double.isNaN(r) ? "N/D" : String.format("%.2f", r);

        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Color bg = Color.WHITE;

        if (!Double.isNaN(r) && Math.abs(r) >= 0.7) {
            font = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 9, Color.RED);
            bg = new Color(255, 230, 230);
        }

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell createStyledCell(String text, boolean bold, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(
                text,
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        10,
                        bold ? Font.BOLD : Font.NORMAL
                )));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}
