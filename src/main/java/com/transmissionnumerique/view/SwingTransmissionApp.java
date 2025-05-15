package com.transmissionnumerique.view;

import com.transmissionnumerique.controller.TransmissionController;
import com.transmissionnumerique.model.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SwingTransmissionApp extends JFrame {

    private TransmissionController controller;

    // UI Components
    private JTextField sequenceLengthField;
    private JButton generateSequenceButton;
    private JComboBox<LineEncoder.EncodingType> encodingTypeComboBox;
    private JComboBox<TransmissionFilter.FilterType> filterTypeComboBox;
    private JComboBox<Modulator.ModulationType> modulationTypeComboBox;
    private JSlider snrSlider;
    private JButton startSimulationButton;
    private JTextArea resultTextArea;

    // Graphiques
    private JTabbedPane chartPane;
    private ChartPanel inputSignalPanel;
    private ChartPanel encodedSignalPanel;
    private ChartPanel modulatedSignalPanel;
    private ChartPanel noisySignalPanel;
    private ChartPanel demodulatedSignalPanel;
    private ChartPanel outputSignalPanel;

    public SwingTransmissionApp() {
        controller = new TransmissionController();

        // Configuration de la fenêtre
        setTitle("Simulation de chaîne de transmission numérique");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Création des panneaux
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel controlPanel = createControlPanel();
        chartPane = createChartPane();
        JPanel resultPanel = createResultPanel();

        mainPanel.add(controlPanel, BorderLayout.WEST);
        mainPanel.add(chartPane, BorderLayout.CENTER);
        mainPanel.add(resultPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(300, 600));

        // Section séquence binaire
        JPanel sequencePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        sequencePanel.setBorder(BorderFactory.createTitledBorder("Séquence binaire"));

        sequencePanel.add(new JLabel("Longueur:"));
        sequenceLengthField = new JTextField("32");
        sequencePanel.add(sequenceLengthField);

        generateSequenceButton = new JButton("Générer une séquence");
        generateSequenceButton.addActionListener(e ->
                controller.generateRandomSequence(Integer.parseInt(sequenceLengthField.getText())));
        sequencePanel.add(generateSequenceButton);

        // Section codage en ligne
        JPanel encodingPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        encodingPanel.setBorder(BorderFactory.createTitledBorder("Codage en ligne"));

        encodingPanel.add(new JLabel("Type de codage:"));
        encodingTypeComboBox = new JComboBox<>(LineEncoder.EncodingType.values());
        encodingPanel.add(encodingTypeComboBox);

        // Section filtrage
        JPanel filterPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtrage"));

        filterPanel.add(new JLabel("Type de filtre:"));
        filterTypeComboBox = new JComboBox<>(TransmissionFilter.FilterType.values());
        filterPanel.add(filterTypeComboBox);

        // Section modulation
        JPanel modulationPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        modulationPanel.setBorder(BorderFactory.createTitledBorder("Modulation"));

        modulationPanel.add(new JLabel("Type de modulation:"));
        modulationTypeComboBox = new JComboBox<>(Modulator.ModulationType.values());
        modulationPanel.add(modulationTypeComboBox);

        // Section canal
        JPanel channelPanel = new JPanel(new BorderLayout(5, 5));
        channelPanel.setBorder(BorderFactory.createTitledBorder("Canal de propagation"));

        channelPanel.add(new JLabel("SNR (dB):"), BorderLayout.NORTH);
        snrSlider = new JSlider(0, 30, 15);
        snrSlider.setMajorTickSpacing(5);
        snrSlider.setMinorTickSpacing(1);
        snrSlider.setPaintTicks(true);
        snrSlider.setPaintLabels(true);
        channelPanel.add(snrSlider, BorderLayout.CENTER);

        // Bouton de démarrage
        startSimulationButton = new JButton("Démarrer la simulation");
        startSimulationButton.addActionListener(e -> runSimulation());

        // Ajout des panneaux au panneau de contrôle
        controlPanel.add(sequencePanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(encodingPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(filterPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(modulationPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(channelPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(startSimulationButton);

        return controlPanel;
    }

    private JTabbedPane createChartPane() {
        JTabbedPane chartPane = new JTabbedPane();

        // Création des panneaux graphiques vides
        inputSignalPanel = createEmptyChartPanel("Séquence binaire d'entrée");
        encodedSignalPanel = createEmptyChartPanel("Signal après codage en ligne");
        modulatedSignalPanel = createEmptyChartPanel("Signal après modulation");
        noisySignalPanel = createEmptyChartPanel("Signal après passage dans le canal");
        demodulatedSignalPanel = createEmptyChartPanel("Signal après démodulation");
        outputSignalPanel = createEmptyChartPanel("Séquence binaire de sortie");

        chartPane.addTab("Séquence d'entrée", inputSignalPanel);
        chartPane.addTab("Signal encodé", encodedSignalPanel);
        chartPane.addTab("Signal modulé", modulatedSignalPanel);
        chartPane.addTab("Signal bruité", noisySignalPanel);
        chartPane.addTab("Signal démodulé", demodulatedSignalPanel);
        chartPane.addTab("Séquence de sortie", outputSignalPanel);

        return chartPane;
    }

    private ChartPanel createEmptyChartPanel(String title) {
        XYSeries series = new XYSeries("Données");
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "Échantillons",
                "Amplitude",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        return new ChartPanel(chart);
    }

    private JPanel createResultPanel() {
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        resultPanel.add(new JLabel("Résultats:"), BorderLayout.NORTH);
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setRows(5);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        return resultPanel;
    }

    private void runSimulation() {
        try {
            // Configuration des paramètres de la simulation
            int sequenceLength = Integer.parseInt(sequenceLengthField.getText());
            LineEncoder.EncodingType encodingType = (LineEncoder.EncodingType) encodingTypeComboBox.getSelectedItem();
            TransmissionFilter.FilterType filterType = (TransmissionFilter.FilterType) filterTypeComboBox.getSelectedItem();
            Modulator.ModulationType modulationType = (Modulator.ModulationType) modulationTypeComboBox.getSelectedItem();
            double snr = snrSlider.getValue();

            // Lancement de la simulation via le contrôleur
            controller.runSimulation(sequenceLength, encodingType, filterType, modulationType, snr);

            // Mise à jour des graphiques
            updateCharts();

            // Calcul BER sécurisé
            double ber = 0;
            try {
                ber = controller.calculateBER();
            } catch (IllegalArgumentException e) {
                // Afficher un message plus détaillé sur le problème
                JOptionPane.showMessageDialog(this,
                        "Erreur dans le calcul du BER: " + e.getMessage() +
                                "\nLes séquences d'entrée et de sortie ont des longueurs différentes.",
                        "Avertissement", JOptionPane.WARNING_MESSAGE);

                ber = 0.0; // Valeur par défaut
            }

            resultTextArea.setText(
                    "Simulation terminée!\n" +
                            "- Longueur de la séquence: " + sequenceLength + " bits\n" +
                            "- Taux d'erreur binaire (BER): " + String.format("%.6f", ber) + "\n" +
                            "- Nombre de bits erronés: " + (int)(ber * sequenceLength) + "/" + sequenceLength
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la simulation: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateCharts() {
        // Mise à jour du graphique de la séquence d'entrée
        updateChart(inputSignalPanel, "Bits d'entrée", controller.getInputSequence());

        // Mise à jour du graphique du signal encodé
        updateDoubleChart(encodedSignalPanel, "Signal encodé", controller.getEncodedSignal());

        // Mise à jour du graphique du signal modulé
        updateDoubleChart(modulatedSignalPanel, "Signal modulé", controller.getModulatedSignal());

        // Mise à jour du graphique du signal bruité
        updateDoubleChart(noisySignalPanel, "Signal bruité", controller.getNoisySignal());

        // Mise à jour du graphique du signal démodulé
        updateDoubleChart(demodulatedSignalPanel, "Signal démodulé", controller.getDemodulatedSignal());

        // Mise à jour du graphique de la séquence de sortie
        updateChart(outputSignalPanel, "Bits de sortie", controller.getOutputSequence());
    }

    private void updateChart(ChartPanel panel, String seriesName, boolean[] data) {
        XYSeries series = new XYSeries(seriesName);

        for (int i = 0; i < data.length; i++) {
            series.add(i, data[i] ? 1.0 : 0.0);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                panel.getChart().getTitle().getText(),
                "Échantillons",
                "Amplitude",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        panel.setChart(chart);
    }

    private void updateDoubleChart(ChartPanel panel, String seriesName, double[] data) {
        XYSeries series = new XYSeries(seriesName);

        for (int i = 0; i < data.length; i++) {
            series.add(i, data[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                panel.getChart().getTitle().getText(),
                "Échantillons",
                "Amplitude",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        panel.setChart(chart);
    }
}