package gui;

import service.ReportService;
import sql.SqlLiteConnection;
import utility.CsvReader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class ReportAndChartFrame extends JFrame {

    /* =======================
       THEME
     ======================= */
    private static final Color BG_DARK = new Color(28, 28, 30);
    private static final Color BG_PANEL = new Color(40, 40, 42);
    private static final Color FG_TEXT = new Color(230, 230, 235);
    private static final Color FG_MUTED = new Color(170, 170, 175);
    private static final Color ACCENT = new Color(56, 139, 253);

    private static final Color BTN_BG = new Color(55, 55, 58);
    private static final Color BTN_HOVER = new Color(70, 70, 74);
    private static final Color BTN_BORDER = new Color(90, 90, 95);
    private static final Color BTN_TEXT = new Color(235, 235, 240);
    
    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    
    /* =======================
       REGIONI
     ======================= */
    private static final String[] REGIONI = {
            "Abruzzo", "Basilicata", "Calabria", "Campania", "Emilia-Romagna",
            "Friuli-Venezia Giulia", "Lazio", "Liguria", "Lombardia",
            "Marche", "Molise", "Piemonte", "Puglia", "Sardegna",
            "Sicilia", "Toscana", "Trentino-Alto Adige", "Umbria",
            "Valle d'Aosta", "Veneto"
    };
	
    /* =======================
       MODEL
     ======================= */
    private final DefaultListModel<File> airQualityModel = new DefaultListModel<>();
    private final DefaultListModel<File> mortalityModel = new DefaultListModel<>();

    private JSpinner startYearSpinner;
    private JSpinner endYearSpinner;
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> regionComboGlobal;

    private CsvReader estrattore;

    public ReportAndChartFrame() {
        setTitle("Air Quality Monitor");
        setSize(950, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        SqlLiteConnection.initDatabase();
        estrattore = new CsvReader();

        getContentPane().setBackground(BG_DARK);
        initUI();
    }

    /* =======================
       UI
     ======================= */
    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        add(createHeader(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Regione globale
        gbc.gridx = 0; gbc.gridy = 0;
        header.add(createLabel("Regione"), gbc);

        regionComboGlobal = new JComboBox<>(REGIONI);
        styleComponent(regionComboGlobal);
        gbc.gridx = 1;
        header.add(regionComboGlobal, gbc);

        // Anni
        gbc.gridx = 2;
        header.add(createLabel("Anno Inizio"), gbc);
        startYearSpinner = createYearSpinner(2020);
        gbc.gridx = 3;
        header.add(startYearSpinner, gbc);

        gbc.gridx = 4;
        header.add(createLabel("Anno Fine"), gbc);
        endYearSpinner = createYearSpinner(2022);
        gbc.gridx = 5;
        header.add(endYearSpinner, gbc);

        // Titolo
        gbc.gridx = 0; gbc.gridy = 1;
        header.add(createLabel("Titolo report"), gbc);
        titleField = createTextField("Rapporto Ambientale e Sanitario");
        gbc.gridx = 1; gbc.gridwidth = 5;
        header.add(titleField, gbc);

        // Descrizione
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        header.add(createLabel("Descrizione"), gbc);
        descriptionArea = new JTextArea(4, 30);
        styleComponent(descriptionArea);
        JScrollPane scroll = new JScrollPane(descriptionArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_PANEL);
        gbc.gridx = 1; gbc.gridwidth = 5;
        header.add(scroll, gbc);

        return header;
    }

    private JPanel createCenter() {
        JPanel center = new JPanel(new GridLayout(1, 2, 15, 15));
        center.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        center.setBackground(BG_DARK);

        center.add(createCsvCard(
                "Qualità Aria",
                "Dataset ambientali",
                airQualityModel,
                true
        ));

        center.add(createCsvCard(
                "Mortalità",
                "Dati sanitari",
                mortalityModel,
                false
        ));

        return center;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(BG_DARK);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));

        JButton pdfButton = createFlatButton("Genera PDF");
        JButton chartButton = createFlatButton("Genera Grafico");

        pdfButton.addActionListener(e -> generatePdf());
        chartButton.addActionListener(e -> {
            String region = (String) regionComboGlobal.getSelectedItem();
            new AirQualityChart(
                    (Integer) startYearSpinner.getValue(),
                    (Integer) endYearSpinner.getValue(),
                    region
            ).setVisible(true);
        });

        footer.add(pdfButton);
        footer.add(chartButton);
        return footer;
    }

    /* =======================
       CSV CARD
     ======================= */
    private JPanel createCsvCard(String title, String subtitle,
                                 DefaultListModel<File> model,
                                 boolean air) {

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(65, 65, 70)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(FG_TEXT);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setForeground(FG_MUTED);

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        header.add(titleLabel);
        header.add(subLabel);

        JList<File> list = new JList<>(model);
        styleList(list);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_PANEL);

        JButton loadButton = createFlatButton("＋ Aggiungi CSV");
        loadButton.addActionListener(e -> loadCsvWithRegion(model, air));

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(loadButton, BorderLayout.SOUTH);

        return card;
    }

    /* =======================
       CSV LOAD CON REGIONE
     ======================= */
    private void loadCsvWithRegion(DefaultListModel<File> model, boolean air) {
        String region = askRegion();
        if (region == null) return;

        File[] files = chooseCsvFiles();
        if (files == null) return;

        LoadingDialog dialog = new LoadingDialog(this, files.length);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            protected Void doInBackground() throws Exception {
                for (int i = 0; i < files.length; i++) {
                    if (dialog.isCancelled()) break;
                    if (air)
                        estrattore.importAirQualityToDb(files[i].getAbsolutePath(), region);
                    else
                        estrattore.importMortalityToDb(files[i].getAbsolutePath(), region);
                    publish(i + 1);
                }
                return null;
            }

            protected void process(java.util.List<Integer> c) {
                int v = c.get(c.size() - 1);
                dialog.update(v, files[v - 1].getName());
            }

            protected void done() {
                dialog.dispose();
                filesToModel(model, files);
            }
        };

        worker.execute();
        dialog.setVisible(true);
    }

    private void filesToModel(DefaultListModel<File> model, File[] files) {
        for (File f : files) model.addElement(f);
    }

    /* =======================
       UTILS
     ======================= */
    private String askRegion() {
        JComboBox<String> combo = new JComboBox<>(REGIONI);
        styleComponent(combo);

        int res = JOptionPane.showConfirmDialog(
                this, combo, "Seleziona Regione",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        return res == JOptionPane.OK_OPTION
                ? (String) combo.getSelectedItem()
                : null;
    }

    private File[] chooseCsvFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("File CSV", "csv"));
        return chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION
                ? chooser.getSelectedFiles()
                : null;
    }

    private void generatePdf() {
        try {
            new ReportService().generatePdf(
                    (Integer) startYearSpinner.getValue(),
                    (Integer) endYearSpinner.getValue(),
                    titleField.getText(),
                    descriptionArea.getText(),
                    (String) regionComboGlobal.getSelectedItem()
            );
            JOptionPane.showMessageDialog(this, "PDF generato correttamente");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    /* =======================
       STYLE HELPERS
     ======================= */
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UI_FONT);
        l.setForeground(FG_MUTED);
        return l;
    }

    private JTextField createTextField(String text) {
        JTextField f = new JTextField(text);
        styleComponent(f);
        return f;
    }

    private JSpinner createYearSpinner(int value) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(value, 2000, 2100, 1));
        styleComponent(s);
        return s;
    }

    private void styleComponent(JComponent c) {
        c.setFont(UI_FONT);
        c.setForeground(FG_TEXT);
        c.setBackground(BG_PANEL);
        c.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    }

    private void styleList(JList<?> list) {
        list.setFont(UI_FONT);
        list.setBackground(BG_PANEL);
        list.setForeground(FG_TEXT);
        list.setSelectionBackground(ACCENT);
        list.setSelectionForeground(Color.WHITE);
    }

    private JButton createPrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(UI_FONT.deriveFont(Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setBackground(ACCENT);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(ACCENT.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(ACCENT);
            }
        });

        return b;
    }

    private JButton createFlatButton(String text) {
        JButton b = new JButton(text);
        b.setFont(UI_FONT);
        b.setForeground(BTN_TEXT);
        b.setBackground(BTN_BG);
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BTN_BORDER),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(BTN_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(BTN_BG);
            }
        });

        return b;
    }

}
