package gui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public class AirQualityUI extends JFrame {


    public AirQualityUI() {

        setTitle("Air Quality Monitor ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200); 
        setLocationRelativeTo(null); 

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(240, 248, 255)); 

        JLabel titleLabel = new JLabel("Monitoraggio Indice Aria", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(25, 25, 112)); 
        
        JLabel statusLabel = new JLabel("Stato: Pronto per i dati...", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(statusLabel, BorderLayout.CENTER);
        
        add(mainPanel);
    }

    public static void start() {
        SwingUtilities.invokeLater(() -> {
            new AirQualityUI().setVisible(true);
        });
    }
}