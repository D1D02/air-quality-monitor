package gui;

import javax.swing.*;
import java.awt.*;

public class LoadingDialog extends JDialog {

    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private boolean cancelled = false;

    public LoadingDialog(Frame parent, int totalFiles) {
        super(parent, "Importazione dataset", true);

        setSize(420, 160);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        progressBar = new JProgressBar(0, totalFiles);
        progressBar.setStringPainted(true);

        statusLabel = new JLabel("Inizializzazione...", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton cancelButton = new JButton("Annulla");
        cancelButton.addActionListener(e -> {
            cancelled = true;
            statusLabel.setText("⏹ Annullamento...");
            cancelButton.setEnabled(false);
        });

        JPanel center = new JPanel(new GridLayout(2, 1, 8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        center.add(statusLabel);
        center.add(progressBar);

        add(center, BorderLayout.CENTER);
        add(cancelButton, BorderLayout.SOUTH);
    }

    public void update(int current, String fileName) {
        progressBar.setValue(current);
        statusLabel.setText("📄 " + fileName + "  (" + current + "/" + progressBar.getMaximum() + ")");
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
