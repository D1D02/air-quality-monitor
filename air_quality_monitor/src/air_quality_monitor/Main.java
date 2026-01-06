package air_quality_monitor;

import gui.ReportAndChartFrame;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ReportAndChartFrame frame = new ReportAndChartFrame();
            frame.setVisible(true);
        });
    }
}
