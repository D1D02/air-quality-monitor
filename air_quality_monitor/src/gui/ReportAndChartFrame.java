package gui;

import service.ReportService;
import sql.SqlLiteConnection;
import utility.CsvReader;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Year;

import air_quality.AirQualityStats;
import air_quality.MortalityStats;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ReportAndChartFrame extends JFrame {
	
	private static final Color BG_DARK = new Color(32, 32, 32);
	private static final Color BG_PANEL = new Color(45, 45, 45);
	private static final Color FG_TEXT = new Color(230, 230, 230);
	private static final Color ACCENT = new Color(38, 142, 255);

	private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 13);


    private DefaultListModel<File> airQualityModel = new DefaultListModel<>();
    private DefaultListModel<File> mortalityModel = new DefaultListModel<>();

    private JSpinner startYearSpinner;
    private JSpinner endYearSpinner;
    private JTextField titleField;
    private JTextArea descriptionArea;

    private CsvReader estrattore;

    public ReportAndChartFrame() {
        setTitle("Report Qualità Aria e Mortalità");
        setSize(800, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        SqlLiteConnection.initDatabase();
        estrattore = new CsvReader();
        
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}
        getContentPane().setBackground(BG_DARK);


        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        /* =======================
           SEZIONE INPUT REPORT
         ======================= */
        JPanel reportPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        reportPanel.add(new JLabel("Anno Inizio:"), gbc);
        startYearSpinner = new JSpinner(new SpinnerNumberModel(2020, 2000, 2100, 1));
        JSpinner.NumberEditor startEditor =
                new JSpinner.NumberEditor(startYearSpinner, "####");
        startYearSpinner.setEditor(startEditor);
        gbc.gridx = 1;
        reportPanel.add(startYearSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        reportPanel.add(new JLabel("Anno Fine:"), gbc);
        endYearSpinner = new JSpinner(new SpinnerNumberModel(2022, 2000, 2100, 1));
        JSpinner.NumberEditor endEditor =
                new JSpinner.NumberEditor(endYearSpinner, "####");
        endYearSpinner.setEditor(endEditor);        gbc.gridx = 1;
        reportPanel.add(endYearSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        reportPanel.add(new JLabel("Titolo Report:"), gbc);
        titleField = new JTextField("Rapporto Ambientale e Sanitario");
        gbc.gridx = 1; gbc.gridwidth = 2;
        reportPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        reportPanel.add(new JLabel("Descrizione:"), gbc);
        descriptionArea = new JTextArea(4, 30);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        gbc.gridx = 1; gbc.gridwidth = 2;
        reportPanel.add(descScroll, gbc);
        
        styleComponent(titleField);
        styleComponent(descriptionArea);
        styleComponent(startYearSpinner);
        styleComponent(endYearSpinner);


        add(reportPanel, BorderLayout.NORTH);

        /* =======================
           SEZIONE FILE CSV
         ======================= */
        JPanel filePanel = new JPanel(new GridLayout(1, 2, 10, 10));

        filePanel.add(createCsvPanel(
                "File Qualità Aria",
                airQualityModel,
                this::loadAirQualityCsv
        ));

        filePanel.add(createCsvPanel(
                "File Mortalità",
                mortalityModel,
                this::loadMortalityCsv
        ));
        
        add(filePanel, BorderLayout.CENTER);

        /* =======================
           SEZIONE PULSANTI
         ======================= */
        JButton pdfButton = createAccentButton("Genera PDF");
        JButton chartButton = createAccentButton("Genera Grafico");
        pdfButton.addActionListener(e -> generatePdf());

        chartButton.addActionListener(e -> {
            int startYear = (Integer) startYearSpinner.getValue();
            int endYear = (Integer) endYearSpinner.getValue();

            AirQualityChart chart = new AirQualityChart(startYear, endYear);
            chart.setVisible(true);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(pdfButton);
        buttonPanel.add(chartButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /* =======================
       CARICAMENTO CSV
     ======================= */
    private JPanel createCsvPanel(String title,
                                  DefaultListModel<File> model,
                                  Runnable loaderAction) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        JList<File> list = new JList<>(model);
        JScrollPane scroll = new JScrollPane(list);

        JButton loadButton = new JButton("Carica CSV");
        loadButton.addActionListener(e -> loaderAction.run());

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(loadButton, BorderLayout.SOUTH);
        return panel;
    }

    private void loadAirQualityCsv() {
        File[] files = chooseCsvFiles();
        if (files == null) return;

        for (File file : files) {
            airQualityModel.addElement(file);
            try {
                estrattore.importAirQualityToDb(file.getAbsolutePath());
            } catch (IOException e) {
                showError("Errore caricamento Qualità Aria:\n" + e.getMessage());
            } catch (Exception e) {
            	showError("Errore:\n" + e.getMessage());
            }
        }
    }

    private void loadMortalityCsv() {
        File[] files = chooseCsvFiles();
        if (files == null) return;

        for (File file : files) {
            mortalityModel.addElement(file);
            try {
                estrattore.importMortalityToDb(file.getAbsolutePath());
            } catch (IOException e) {
                showError("Errore caricamento Mortalità:\n" + e.getMessage());
            } catch (Exception e) {
            	showError("Errore:\n" + e.getMessage());
            }
        }
    }

    private File[] chooseCsvFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("File CSV", "csv"));

        int result = chooser.showOpenDialog(this);
        return (result == JFileChooser.APPROVE_OPTION)
                ? chooser.getSelectedFiles()
                : null;
    }

    /* =======================
       GENERAZIONE PDF
     ======================= */
    private void generatePdf() {
        try {
            int startYear = (Integer) startYearSpinner.getValue();
            int endYear = (Integer) endYearSpinner.getValue();
            String title = titleField.getText();
            String description = descriptionArea.getText();

            ReportService pdfService = new ReportService();
            pdfService.generatePdf(startYear, endYear, title, description);

            JOptionPane.showMessageDialog(this,
                    "PDF generato correttamente!",
                    "Successo",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            showError("Errore durante la generazione PDF:\n" + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    private void styleComponent(JComponent c) {
        c.setFont(UI_FONT);
        c.setForeground(FG_TEXT);
        c.setBackground(BG_PANEL);
    }
    
    private JButton createAccentButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UI_FONT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }
}
