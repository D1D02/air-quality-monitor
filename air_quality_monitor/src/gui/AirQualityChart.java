package gui;

import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import air_quality.AirQualityStats;

import java.util.Map;

public class AirQualityChart extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final int ANNO = 2022; // anno fisso

    public AirQualityChart(AirQualityStats stats) {
        super("Medie mensili inquinanti - " + ANNO);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Ciclo sui mesi da 1 a 12
        for (int mese = 1; mese <= 12; mese++) {
            AirQualityStats statsFiltrate = stats.filterByMonth(ANNO, mese);
            Map<String, Double> medieMensili = statsFiltrate.getAverageByPollutant();

            // aggiungo ogni inquinante come serie
            for (Map.Entry<String, Double> entry : medieMensili.entrySet()) {
                String inquinante = entry.getKey();
                Double valore = entry.getValue();
                dataset.addValue(valore, inquinante, String.valueOf(mese));
            }
        }

        // Creo il grafico a linee
        JFreeChart chart = ChartFactory.createLineChart(
                "Medie mensili inquinanti",
                "Mese",
                "Valore medio",
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(900, 600));

        setContentPane(chartPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null); // centra la finestra
    }
}
