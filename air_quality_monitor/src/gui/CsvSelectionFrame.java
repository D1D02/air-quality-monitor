package gui;

import air_quality.AirQualityStats;
import sql.SqlLiteConnection;
import utility.CsvReader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class CsvSelectionFrame extends JFrame {

    private DefaultListModel<File> fileListModel;
    private JList<File> fileList;

    public CsvSelectionFrame() {
        setTitle("Selezione Dataset Qualità Aria");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(fileList);

        JButton loadButton = new JButton("Carica CSV");
        JButton chartButton = new JButton("Genera Grafico");

        loadButton.addActionListener(e -> loadCsvFiles());
        chartButton.addActionListener(e -> generateChart());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loadButton);
        buttonPanel.add(chartButton);

        add(new JLabel("Dataset caricati:"), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCsvFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("File CSV", "csv"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            for (File file : chooser.getSelectedFiles()) {
                if (!fileListModel.contains(file)) {
                    fileListModel.addElement(file);
                }
            }
        }
    }

    private void generateChart() {
        File selectedFile = fileList.getSelectedValue();
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleziona un file CSV",
                    "Errore",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Reset / init DB
            SqlLiteConnection.initDatabase();

            CsvReader reader = new CsvReader();
            reader.importAirQualityToDb(selectedFile.getAbsolutePath());

            AirQualityStats stats = new AirQualityStats();

            int year = extractYearFromFilename(selectedFile.getName());

            //AirQualityChart chart = new AirQualityChart(stats, year);
            //chart.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Errore durante la generazione del grafico:\n" + ex.getMessage(),
                    "Errore",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Estrae l'anno dal nome file (es: QualitàAria_2018.csv)
     */
    private int extractYearFromFilename(String filename) {
        try {
            String digits = filename.replaceAll("\\D+", "");
            return Integer.parseInt(digits);
        } catch (Exception e) {
            return 0; // fallback
        }
    }
}