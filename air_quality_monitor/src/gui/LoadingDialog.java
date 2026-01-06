package gui;

import javax.swing.*;
import java.awt.*;

public class LoadingDialog extends JDialog {

    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private boolean cancelled = false;

    public LoadingDialog(Frame parent, int totalFiles) {
        super(parent, "Importazione in corso", true);

        setSize(420, 180);
        setLocationRelativeTo(parent);
        setResizable(false);
        setUndecorated(true); // 🔥 look moderno

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(new Color(30, 30, 30));
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel title = new JLabel("Importazione dataset");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));

        statusLabel = new JLabel("Inizializzazione...");
        statusLabel.setForeground(new Color(200, 200, 200));

        progressBar = new JProgressBar(0, totalFiles);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(38, 142, 255));
        progressBar.setBackground(new Color(60, 60, 60));

        JButton cancel = new JButton("Annulla");
        cancel.addActionListener(e -> {
            cancelled = true;
            cancel.setEnabled(false);
            statusLabel.setText("Annullamento...");
        });

        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(2, 1, 8, 8));
        center.setOpaque(false);
        center.add(statusLabel);
        center.add(progressBar);

        root.add(center, BorderLayout.CENTER);
        root.add(cancel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    public void update(int current, String fileName) {
        progressBar.setValue(current);
        statusLabel.setText("📄 " + fileName + " (" + current + "/" + progressBar.getMaximum() + ")");
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
