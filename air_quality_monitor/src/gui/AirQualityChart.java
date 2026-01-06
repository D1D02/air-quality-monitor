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
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AirQualityChart extends JFrame {

    /* ======================= THEME ======================= */
    private static final Color BG_DARK = new Color(28, 28, 30);
    private static final Color BG_PANEL = new Color(40, 40, 42);
    private static final Color FG_TEXT = new Color(230, 230, 235);
    private static final Color FG_MUTED = new Color(170, 170, 175);
    private static final Color ACCENT = new Color(56, 139, 253);

    private final AirQualityStats airStats = new AirQualityStats();
    private final MortalityStats mortStats = new MortalityStats();

    private int startYear, endYear;

    private Set<String> selectedPollutants = new HashSet<>();
    private Set<String> selectedCauses = new HashSet<>();

    private TimeSeriesCollection airDataset = new TimeSeriesCollection();
    private TimeSeriesCollection mortDataset = new TimeSeriesCollection();

    private JFreeChart chart;
    private XYPlot plot;

    private JPanel controlPanel;

    public AirQualityChart(int startYear, int endYear) {
        this.startYear = startYear;
        this.endYear = endYear;

        setTitle("Andamento Qualità Aria e Mortalità");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        setIconImage(new ImageIcon(
                getClass().getResource("/icons/app.png")
         ).getImage());
        
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(10, 10));

        chart = ChartFactory.createTimeSeriesChart(
                "Qualità Aria e Mortalità (" + startYear + " - " + endYear + ")",
                "Anno",
                "Valore",
                airDataset,
                true,
                true,
                false
        );

        plot = chart.getXYPlot();

        // Asse secondario per mortalità
        NumberAxis mortalityAxis = new NumberAxis("Mortalità");
        plot.setRangeAxis(1, mortalityAxis);
        plot.setDataset(1, mortDataset);
        plot.mapDatasetToRangeAxis(1, 1);

        // Renderer
        XYLineAndShapeRenderer airRenderer = new XYLineAndShapeRenderer(true, false);
        XYLineAndShapeRenderer mortRenderer = new XYLineAndShapeRenderer(true, false);
        plot.setRenderer(0, airRenderer);
        plot.setRenderer(1, mortRenderer);

        // Stile base
        plot.setBackgroundPaint(BG_PANEL);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        // ChartPanel interattivo
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true); // zoom con rotellina
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        chartPanel.setBackground(BG_PANEL);

        // Pannello di selezione inquinanti/malattie
        controlPanel = createControlPanel();

        add(controlPanel, BorderLayout.WEST);
        add(chartPanel, BorderLayout.CENTER);

        loadAllOptions();
        updateChart();
    }

    /* ======================= CONTROL PANEL ======================= */
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

        // Scroll pane per checkboxes
        panel.add(new JScrollPane(new JPanel())); // placeholder, riempito in loadAllOptions()

        return panel;
    }

    /* ======================= LOAD OPTIONS ======================= */
    private void loadAllOptions() {
        // Pollutants
        Map<String, Double> airData = airStats.getStatsByYears(startYear, endYear);
        Set<String> pollutants = airData.keySet().stream()
                .map(k -> k.split("\\|")[1])
                .collect(Collectors.toSet());

        // Mortality causes
        Set<String> causes = new HashSet<>();
        for (int y = startYear; y <= endYear; y++) {
            causes.addAll(mortStats.getStatsByYear(y).keySet());
        }

        // Panel con checkbox
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        checkboxPanel.setBackground(BG_PANEL);

        JLabel l1 = new JLabel("Inquinanti:");
        l1.setForeground(FG_TEXT);
        checkboxPanel.add(l1);

        for (String pollutant : pollutants) {
            JCheckBox cb = new JCheckBox(pollutant);
            cb.setForeground(FG_TEXT);
            cb.setBackground(BG_PANEL);
            cb.addActionListener(e -> {
                if (cb.isSelected()) selectedPollutants.add(pollutant);
                else selectedPollutants.remove(pollutant);
                updateChart();
            });
            checkboxPanel.add(cb);
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
        scroll.setPreferredSize(new Dimension(200, 500));
        controlPanel.add(scroll);
        controlPanel.revalidate();
        controlPanel.repaint();
    }

    /* ======================= UPDATE CHART ======================= */
    private void updateChart() {
        airDataset.removeAllSeries();
        mortDataset.removeAllSeries();

        // Inquinanti
        Map<String, Double> airData = airStats.getStatsByYears(startYear, endYear);
        for (String pollutant : selectedPollutants) {
            TimeSeries series = new TimeSeries(pollutant);
            for (int y = startYear; y <= endYear; y++) {
                Double val = airData.get(y + "|" + pollutant);
                if (val != null) series.add(new Year(y), val);
            }
            if (!series.isEmpty()) airDataset.addSeries(series);
        }

        // Mortalità
        for (String cause : selectedCauses) {
            TimeSeries series = new TimeSeries(cause);
            for (int y = startYear; y <= endYear; y++) {
                Integer val = mortStats.getStatsByYear(y).get(cause);
                if (val != null) series.add(new Year(y), val);
            }
            if (!series.isEmpty()) mortDataset.addSeries(series);
        }
    }
}
