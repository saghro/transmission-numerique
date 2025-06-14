package com.transmissionnumerique;

import com.transmissionnumerique.view.SwingTransmissionApp;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Lancement direct de l'interface graphique
        SwingUtilities.invokeLater(() -> {
            SwingTransmissionApp app = new SwingTransmissionApp();
            app.setVisible(true);
        });
    }
}