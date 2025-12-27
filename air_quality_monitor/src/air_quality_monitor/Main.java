package air_quality_monitor;

import gui.CsvSelectionFrame;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CsvSelectionFrame frame = new CsvSelectionFrame();
            frame.setVisible(true);
        });
    }
}
