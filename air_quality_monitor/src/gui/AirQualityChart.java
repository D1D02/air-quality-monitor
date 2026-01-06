package gui;

import air_quality.AirQualityStats;
import air_quality.MortalityStats;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Year;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Set;

public class AirQualityChart extends JFrame {

    public AirQualityChart(int startYear, int endYear) {
        setTitle("Andamento Qualità Aria e Mortalità");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JFreeChart chart = createChart(startYear, endYear);
        ChartPanel chartPanel = new ChartPanel(chart);

        setContentPane(chartPanel);
    }

    private JFreeChart createChart(int startYear, int endYear) {

        AirQualityStats airStats = new AirQualityStats();
        MortalityStats mortStats = new MortalityStats();

        /* ================= DATASET INQUINANTI ================= */

        TimeSeriesCollection airDataset = new TimeSeriesCollection();
        Map<String, Double> airData =
                airStats.getStatsByYears(startYear, endYear);

        // Ricavo automaticamente tutti gli inquinanti
        Set<String> pollutants = airData.keySet().stream()
                .map(k -> k.split("\\|")[1])
                .collect(java.util.stream.Collectors.toSet());

        for (String pollutant : pollutants) {
            TimeSeries series = new TimeSeries(pollutant);

            for (int year = startYear; year <= endYear; year++) {
                Double value = airData.get(year + "|" + pollutant);
                if (value != null) {
                    series.add(new Year(year), value);
                }
            }

            if (!series.isEmpty()) {
                airDataset.addSeries(series);
            }
        }

        /* ================= DATASET MORTALITÀ ================= */

        TimeSeriesCollection mortDataset = new TimeSeriesCollection();

        // Prendo le cause reali dal DB
        Set<String> causes =
                mortStats.getStatsByYear(startYear).keySet();

        for (String cause : causes) {
            TimeSeries series = new TimeSeries(cause);

            for (int year = startYear; year <= endYear; year++) {
                Map<String, Integer> yearData =
                        mortStats.getStatsByYear(year);

                Integer value = yearData.get(cause);
                if (value != null) {
                    series.add(new Year(year), value);
                }
            }

            if (!series.isEmpty()) {
                mortDataset.addSeries(series);
            }
        }

        /* ================= CREAZIONE GRAFICO ================= */

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Qualità Aria e Mortalità (" + startYear + " - " + endYear + ")",
                "Anno",
                "Inquinanti",
                airDataset,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();

        /* ===== Asse secondario ===== */
        NumberAxis mortalityAxis = new NumberAxis("Mortalità");
        plot.setRangeAxis(1, mortalityAxis);

        plot.setDataset(1, mortDataset);
        plot.mapDatasetToRangeAxis(1, 1);

        /* ===== Renderer ===== */
        XYLineAndShapeRenderer airRenderer =
                new XYLineAndShapeRenderer(true, false);
        XYLineAndShapeRenderer mortRenderer =
                new XYLineAndShapeRenderer(true, false);

        plot.setRenderer(0, airRenderer);
        plot.setRenderer(1, mortRenderer);

        /* ===== Stile base ===== */
        plot.setBackgroundPaint(new Color(245, 245, 245));
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        return chart;
    }
}
