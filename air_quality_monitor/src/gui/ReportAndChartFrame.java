package gui;

import air_quality.AirQualityStats;
import service.ReportService;
import sql.SqlLiteConnection;
import utility.CsvReader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ReportAndChartFrame extends JFrame {

    private JSpinner startYearSpinner;
    private JSpinner endYearSpinner;
    private JTextField titleField;
    private JTextArea descriptionArea;
    private DefaultListModel<File> fileListModel;
    private JList<File> fileList;

    public ReportAndChartFrame() {
        setTitle("Generazione Report e Grafici Qualità Aria");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        // Pannello principale
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Spinner per START_YEAR
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Anno Inizio:"), gbc);
        startYearSpinner = new JSpinner(new SpinnerNumberModel(2016, 2000, 2050, 1));
        gbc.gridx = 1;
        inputPanel.add(startYearSpinner, gbc);

        // Spinner per END_YEAR
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Anno Fine:"), gbc);
        endYearSpinner = new JSpinner(new SpinnerNumberModel(2022, 2000, 2050, 1));
        gbc.gridx = 1;
        inputPanel.add(endYearSpinner, gbc);

        // Campo titolo PDF
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Titolo PDF:"), gbc);
        titleField = new JTextField("Rapporto Ambientale e Sanitario");
        gbc.gridx = 1; gbc.gridwidth = 2;
        inputPanel.add(titleField, gbc);
        gbc.gridwidth = 1;

        // Area descrizione PDF
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTH;
        inputPanel.add(new JLabel("Descrizione PDF:"), gbc);
        descriptionArea = new JTextArea(5, 30);
        descriptionArea.setText("Questo documento analizza la correlazione tra la concentrazione di inquinanti atmosferici e i tassi di mortalità.");
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        gbc.gridx = 1;
        inputPanel.add(descScroll, gbc);

        // Lista file CSV
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane fileScroll = new JScrollPane(fileList);

        JButton loadButton = new JButton("Carica CSV");
        JButton pdfButton = new JButton("Genera PDF");
        JButton chartButton = new JButton("Visualizza Grafico");

        loadButton.addActionListener(e -> loadCsvFiles());
        pdfButton.addActionListener(e -> generatePdf());
        chartButton.addActionListener(e -> generateChart());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loadButton);
        buttonPanel.add(pdfButton);
        buttonPanel.add(chartButton);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(new JLabel("Dataset caricati:"), BorderLayout.CENTER);
        mainPanel.add(fileScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadCsvFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("File CSV", "csv"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            for (File file : chooser.getSelectedFiles()) {
                if (!fileListModel.contains(file)) {
                    fileListModel.addElement(file);
                }
            }
        }
    }

    private void generatePdf() {
        File selectedFile = fileList.getSelectedValue();
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un file CSV", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            SqlLiteConnection.initDatabase();
            CsvReader reader = new CsvReader();
            reader.importAirQualityToDb(selectedFile.getAbsolutePath());

            int startYear = (Integer) startYearSpinner.getValue();
            int endYear = (Integer) endYearSpinner.getValue();
            String title = titleField.getText();
            String description = descriptionArea.getText();

            ReportService pdfService = new ReportService();
            pdfService.generatePdf(startYear, endYear, title, description);

            JOptionPane.showMessageDialog(this, "PDF generato correttamente!", "Successo", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Errore I/O: " + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void generateChart() {
        File selectedFile = fileList.getSelectedValue();
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un file CSV", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            SqlLiteConnection.initDatabase();
            CsvReader reader = new CsvReader();
            reader.importAirQualityToDb(selectedFile.getAbsolutePath());

            AirQualityStats stats = new AirQualityStats();
            int year = extractYearFromFilename(selectedFile.getName());

            gui.AirQualityChart chart = new gui.AirQualityChart(stats, year);
            chart.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore generazione grafico: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private int extractYearFromFilename(String filename) {
        try {
            String digits = filename.replaceAll("\\D+", "");
            return Integer.parseInt(digits);
        } catch (Exception e) {
            return 0;
        }
    }
}
