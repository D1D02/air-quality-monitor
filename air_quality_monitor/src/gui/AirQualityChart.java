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

    public AirQualityChart(AirQualityStats stats, int anno) {
        super("Analisi Qualità Aria - " + anno);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int mese = 1; mese <= 12; mese++) {
            Map<String, Double> medieMensili = stats.getMonthlyStats(anno, mese);

            for (Map.Entry<String, Double> entry : medieMensili.entrySet()) {
                String inquinante = entry.getKey();
                Double valore = entry.getValue();
                
                dataset.addValue(valore, inquinante, String.valueOf(mese));
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Andamento Mensile Inquinanti (" + anno + ")",
                "Mese dell'anno",               
                "Valore Medio (µg/m3)",         
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 600));
        chartPanel.setMouseWheelEnabled(true); 

        setContentPane(chartPanel);
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        pack();
        setLocationRelativeTo(null);
    }
}