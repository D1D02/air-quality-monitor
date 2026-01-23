package gui;

import com.fazecast.jSerialComm.SerialPort;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerialUARTChartFrame extends JFrame {

    /* ======================= THEME ======================= */
    private static final Color BG_DARK = new Color(28, 28, 30);
    private static final Color BG_PANEL = new Color(40, 40, 42);

    /* ======================= SERIAL ======================= */
    private SerialPort serialPort;
    private Thread readerThread;

    /* ======================= CHART ======================= */
    private final TimeSeries series = new TimeSeries("Valore UART");
    private final TimeSeriesCollection dataset = new TimeSeriesCollection(series);

    /* ======================= UI ======================= */
    private JComboBox<SerialPort> portCombo;
    private JButton connectBtn;
    private ChartPanel chartPanel;


    /* ======================= PARSER ======================= */
    private static final Pattern DATA_PATTERN =
            Pattern.compile("\\{[^:]+:\\s*([0-9.]+)}");

    /* ======================= COSTRUTTORE ======================= */
    public SerialUARTChartFrame() {
        setTitle("Monitor UART – Dati in tempo reale");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(10, 10));
 
        try {
            setIconImage(new ImageIcon(
                   getClass().getResource("/icons/app.png")
            ).getImage());
		} catch (Exception e) {
			e.printStackTrace();
		}

        add(createTopPanel(), BorderLayout.NORTH);
        add(createChartPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
        
        series.setMaximumItemCount(500); // ~ ultimi 500 campioni
        
    }

    /* ======================= METODO PUBBLICO ======================= */
    public static void open() {
        SwingUtilities.invokeLater(() ->
                new SerialUARTChartFrame().setVisible(true)
        );
    }

    /* ======================= TOP ======================= */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(BG_PANEL);

        portCombo = new JComboBox<>(SerialPort.getCommPorts());
        portCombo.setPreferredSize(new Dimension(250, 28));

        connectBtn = new JButton("Connetti");
        connectBtn.addActionListener(e -> toggleConnection());

        panel.add(new JLabel("Dispositivo:"));
        panel.add(portCombo);
        panel.add(connectBtn);

        return panel;
    }

    /* ======================= CHART ======================= */
    private ChartPanel createChartPanel() {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Dati CO in tempo reale",
                "Tempo",
                "Valore",
                dataset,
                false,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(BG_PANEL);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setDomainGridlinePaint(Color.GRAY);

        plot.getDomainAxis().setAutoRange(true);         // scroll automatico
        plot.getDomainAxis().setFixedAutoRange(10_000);  // ultimi 10 secondi

        plot.getRenderer().setDefaultStroke(new BasicStroke(2.0f));

        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setBackground(BG_PANEL);

        return chartPanel;
    }


    /* ======================= BOTTOM ======================= */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_DARK);

        return panel;
    }

    /* ======================= SERIAL ======================= */
    private void toggleConnection() {
        if (serialPort != null && serialPort.isOpen()) {
            closeSerial();
        } else {
            openSerial();
        }
    }

    private void openSerial() {
        serialPort = (SerialPort) portCombo.getSelectedItem();
        if (serialPort == null) return;

        serialPort.setBaudRate(115200);
        serialPort.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING,
                1000,
                0
        );

        if (!serialPort.openPort()) {
            JOptionPane.showMessageDialog(this, "Impossibile aprire la porta");
            return;
        }

        connectBtn.setText("Disconnetti");

        readerThread = new Thread(this::readLoop, "UART-Reader");
        readerThread.start();
    }


    private void closeSerial() {
        try {
            if (readerThread != null) readerThread.interrupt();
            if (serialPort != null) serialPort.closePort();
        } catch (Exception ignored) {}

        connectBtn.setText("Connetti");
    }

    private void readLoop() {
        try {
            InputStreamReader isr = new InputStreamReader(serialPort.getInputStream());
            StringBuilder buffer = new StringBuilder();

            while (!Thread.currentThread().isInterrupted()) {
                int c = isr.read();  // legge un singolo char
                if (c == -1) continue;

                if (c == '\n' || c == '\r') {
                    String line = buffer.toString().trim();
                    buffer.setLength(0);
                    if (!line.isEmpty()) {
                        parseAndAdd(line);
                        System.out.println(line);
                    }
                } else {
                    buffer.append((char) c);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /* ======================= PARSE ======================= */
    private void parseAndAdd(String line) {
        Matcher m = DATA_PATTERN.matcher(line);
        if (!m.find()) return;

        double value = Double.parseDouble(m.group(1));
        Second now = new Second();

        SwingUtilities.invokeLater(() ->
            series.addOrUpdate(now, value)
        );
    }



}
