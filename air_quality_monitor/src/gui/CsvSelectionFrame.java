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

    private JComboBox<String> regionCombo;
    private String selectedRegion;

    private static final String[] REGIONS = {
    	    "Abruzzo", "Basilicata", "Calabria", "Campania",
    	    "Emilia-Romagna", "Friuli-Venezia Giulia", "Lazio",
    	    "Liguria", "Lombardia", "Marche", "Molise",
    	    "Piemonte", "Puglia", "Sardegna", "Sicilia",
    	    "Toscana", "Trentino-Alto Adige", "Umbria",
    	    "Valle d'Aosta", "Veneto"
    	};

    
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

        regionCombo = new JComboBox<>(REGIONS);
        regionCombo.setSelectedIndex(-1);

        regionCombo.addActionListener(e ->
                selectedRegion = (String) regionCombo.getSelectedItem()
        );

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Regione:"), BorderLayout.WEST);
        topPanel.add(regionCombo, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

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
        if (selectedRegion == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleziona una regione",
                    "Errore",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

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
            reader.importAirQualityToDb(
                    selectedFile.getAbsolutePath(),
                    selectedRegion
            );

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