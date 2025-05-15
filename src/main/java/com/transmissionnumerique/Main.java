package com.transmissionnumerique;

import com.transmissionnumerique.view.SwingTransmissionApp;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SwingTransmissionApp().setVisible(true);
        });
    }
}