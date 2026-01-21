package gui;

import air_quality.AirQualityStats;
import air_quality.MortalityStats;
import service.CorrelationService;

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
import java.util.*;
import java.util.stream.Collectors;

public class AirQualityChart extends JFrame {

    private static final Color BG_DARK = new Color(28, 28, 30);
    private static final Color BG_PANEL = new Color(40, 40, 42);
    private static final Color FG_TEXT = new Color(230, 230, 235);
    private static final Color ACCENT = new Color(56, 139, 253);

    private final AirQualityStats airStats = new AirQualityStats();
    private final MortalityStats mortStats = new MortalityStats();

    private int startYear, endYear;
    private String region;
    
    private JLabel correlationLabel;

    private String selectedPollutant = null; // solo uno
    private Set<String> selectedCauses = new HashSet<>();

    private TimeSeriesCollection airDataset = new TimeSeriesCollection();
    private TimeSeriesCollection mortDataset = new TimeSeriesCollection();

    private JFreeChart chart;
    private XYPlot plot;

    private JPanel controlPanel;

    public AirQualityChart(int startYear, int endYear, String region) {
        this.startYear = startYear;
        this.endYear = endYear;
        this.region = region;

        setTitle("Andamento Qualità Aria e Mortalità");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(10, 10));

        chart = ChartFactory.createTimeSeriesChart(
                "Qualità Aria e Mortalità (" + startYear + " - " + endYear + ")",
                "Anno",
                "Valore Inquinante",
                airDataset,
                true,
                true,
                false
        );

        plot = chart.getXYPlot();

        // Asse secondario per mortalità
        NumberAxis mortalityAxis = new NumberAxis("Mortalità");
        mortalityAxis.setAutoRangeIncludesZero(false);
        plot.setRangeAxis(1, mortalityAxis);
        plot.setDataset(1, mortDataset);
        plot.mapDatasetToRangeAxis(1, 1);

        // Renderer
        XYLineAndShapeRenderer airRenderer = new XYLineAndShapeRenderer(true, false);
        XYLineAndShapeRenderer mortRenderer = new XYLineAndShapeRenderer(true, false);

        airRenderer.setDefaultStroke(new BasicStroke(2f));
        mortRenderer.setDefaultStroke(new BasicStroke(2f));

        // Colori fissi
        airRenderer.setSeriesPaint(0, ACCENT);
        mortRenderer.setSeriesPaint(0, Color.RED);

        plot.setRenderer(0, airRenderer);
        plot.setRenderer(1, mortRenderer);

        plot.setBackgroundPaint(BG_PANEL);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        chartPanel.setBackground(BG_PANEL);

        controlPanel = createControlPanel();

        add(controlPanel, BorderLayout.WEST);
        add(chartPanel, BorderLayout.CENTER);

        loadAllOptions();
        updateChart();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Seleziona dati da visualizzare:");
        title.setForeground(FG_TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        correlationLabel = new JLabel("");
        correlationLabel.setForeground(ACCENT);
        correlationLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        correlationLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        panel.add(correlationLabel);


        panel.add(new JScrollPane(new JPanel())); // placeholder
        return panel;
    }

    private void loadAllOptions() {
        Map<String, Double> airData = airStats.getStatsByYears(startYear, endYear, region);
        Set<String> pollutants = airData.keySet().stream()
                .map(k -> k.split("\\|")[1])
                .collect(Collectors.toSet());

        Set<String> causes = new HashSet<>();
        for (int y = startYear; y <= endYear; y++) {
            causes.addAll(mortStats.getStatsByYear(y, region).keySet());
        }

        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        checkboxPanel.setBackground(BG_PANEL);

        JLabel l1 = new JLabel("Inquinanti (uno alla volta):");
        l1.setForeground(FG_TEXT);
        checkboxPanel.add(l1);

        ButtonGroup pollutantGroup = new ButtonGroup();
        for (String pollutant : pollutants) {
            JRadioButton rb = new JRadioButton(pollutant);
            rb.setForeground(FG_TEXT);
            rb.setBackground(BG_PANEL);
            rb.addActionListener(e -> {
                selectedPollutant = rb.isSelected() ? pollutant : null;
                updateChart();
            });
            pollutantGroup.add(rb);
            checkboxPanel.add(rb);
        }

        JLabel l2 = new JLabel("Cause Mortalità:");
        l2.setForeground(FG_TEXT);
        l2.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        checkboxPanel.add(l2);

        for (String cause : causes) {
            JCheckBox cb = new JCheckBox(cause);
            cb.setForeground(FG_TEXT);
            cb.setBackground(BG_PANEL);
            cb.addActionListener(e -> {
                if (cb.isSelected()) selectedCauses.add(cause);
                else selectedCauses.remove(cause);
                updateChart();
            });
            checkboxPanel.add(cb);
        }

        JScrollPane scroll = new JScrollPane(checkboxPanel);
        scroll.setPreferredSize(new Dimension(220, 500));
        controlPanel.add(scroll);
        controlPanel.revalidate();
        controlPanel.repaint();
    }

    private void updateChart() {
        airDataset.removeAllSeries();
        mortDataset.removeAllSeries();

        Map<String, Double> airData = airStats.getStatsByYears(startYear, endYear, region);
        if (selectedPollutant != null) {
            TimeSeries series = new TimeSeries(selectedPollutant);
            for (int y = startYear; y <= endYear; y++) {
                Double val = airData.get(y + "|" + selectedPollutant);
                if (val != null) series.add(new Year(y), val);
            }
            if (!series.isEmpty()) airDataset.addSeries(series);
        }

        Map<Integer, Map<String, Integer>> mortData =
                mortStats.getStatsByYears(startYear, endYear, region);

        for (String cause : selectedCauses) {
            TimeSeries series = new TimeSeries(cause);
            for (int y = startYear; y <= endYear; y++) {
                Map<String, Integer> yearData = mortData.get(y);
                if (yearData != null && yearData.containsKey(cause)) {
                    series.add(new Year(y), yearData.get(cause));
                }
            }
            if (!series.isEmpty()) mortDataset.addSeries(series);
        }

        plot.getRangeAxis(0).setAutoRange(true);
        plot.getRangeAxis(1).setAutoRange(true);
        updateCorrelationLabel();
    }
    
    private void updateCorrelationLabel() {

        // Deve esserci 1 solo inquinante e 1 sola causa
        if (selectedPollutant == null || selectedCauses.size() != 1) {
            correlationLabel.setText("");
            return;
        }

        String cause = selectedCauses.iterator().next();

        Map<String, Double> airData =
                airStats.getStatsByYears(startYear, endYear, region);

        Map<Integer, Map<String, Integer>> mortData =
                mortStats.getStatsByYears(startYear, endYear, region);

        CorrelationService correlationService =
                new CorrelationService(airData, mortData);

        double corr = correlationService.getCorrelation(
                selectedPollutant,
                cause,
                startYear,
                endYear
        );

        correlationLabel.setText(
            String.format(
                "Correlazione %s ↔ %s: %.3f",
                selectedPollutant,
                cause,
                corr
            )
        );
    }


}
