package com.transmissionnumerique.view;

import com.transmissionnumerique.controller.TransmissionController;
import com.transmissionnumerique.model.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
    private JLabel snrValueLabel;
    private JButton startSimulationButton;
    private JButton performanceTestButton;
    private JTextArea resultTextArea;
    private JTextArea consoleTextArea;

    // Graphiques
    private JTabbedPane chartPane;
    private ChartPanel inputSignalPanel;
    private ChartPanel encodedSignalPanel;
    private ChartPanel modulatedSignalPanel;
    private ChartPanel noisySignalPanel;
    private ChartPanel demodulatedSignalPanel;
    private ChartPanel outputSignalPanel;
    private ChartPanel analysisPanel;
    private EyeDiagramPanel eyeDiagramPanel; // Nouveau panneau pour le diagramme de l'œil
    private FilterVisualizationPanel filterPanel; // Nouveau panneau pour les filtres
    private FilteredSignalsPanel filteredSignalsPanel; // Nouveau panneau pour les signaux filtrés

    public SwingTransmissionApp() {
        controller = new TransmissionController();

        // Configuration de la fenêtre
        setTitle("Simulation de chaîne de transmission numérique - Avec diagramme de l'œil");
        setSize(1400, 950);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Création des panneaux
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel controlPanel = createControlPanel();
        chartPane = createChartPane();
        JPanel bottomPanel = createBottomPanel();

        mainPanel.add(controlPanel, BorderLayout.WEST);
        mainPanel.add(chartPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(350, 600));

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

        JPanel snrLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        snrLabelPanel.add(new JLabel("SNR (dB):"));
        snrValueLabel = new JLabel("15");
        snrValueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        snrLabelPanel.add(snrValueLabel);
        channelPanel.add(snrLabelPanel, BorderLayout.NORTH);

        snrSlider = new JSlider(-5, 50, 15);
        snrSlider.setMajorTickSpacing(10);
        snrSlider.setMinorTickSpacing(5);
        snrSlider.setPaintTicks(true);
        snrSlider.setPaintLabels(true);
        snrSlider.addChangeListener(e -> snrValueLabel.setText(String.valueOf(snrSlider.getValue())));
        channelPanel.add(snrSlider, BorderLayout.CENTER);

        // Boutons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        startSimulationButton = new JButton("Démarrer la simulation");
        startSimulationButton.addActionListener(e -> runSimulation());
        startSimulationButton.setBackground(new Color(0, 120, 215));
        startSimulationButton.setForeground(Color.WHITE);

        performanceTestButton = new JButton("Tests de performance");
        performanceTestButton.addActionListener(e -> runPerformanceTests());
        performanceTestButton.setBackground(new Color(0, 150, 100));
        performanceTestButton.setForeground(Color.WHITE);

        buttonPanel.add(startSimulationButton);
        buttonPanel.add(performanceTestButton);

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
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(buttonPanel);

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
        analysisPanel = createEmptyChartPanel("Analyse du signal");
        
        // Créer le panneau du diagramme de l'œil
        eyeDiagramPanel = new EyeDiagramPanel();
        
        // Créer le panneau de visualisation des filtres
        filterPanel = new FilterVisualizationPanel();
        
        // Créer le panneau des signaux filtrés
        filteredSignalsPanel = new FilteredSignalsPanel();

        chartPane.addTab("Séquence d'entrée", inputSignalPanel);
        chartPane.addTab("Signal encodé", encodedSignalPanel);
        chartPane.addTab("Filtres", filterPanel); // Nouvel onglet pour les filtres
        chartPane.addTab("Signaux filtrés", filteredSignalsPanel); // Nouvel onglet
        chartPane.addTab("Signal modulé", modulatedSignalPanel);
        chartPane.addTab("Signal bruité", noisySignalPanel);
        chartPane.addTab("Signal démodulé", demodulatedSignalPanel);
        chartPane.addTab("Séquence de sortie", outputSignalPanel);
        chartPane.addTab("Diagramme de l'œil", eyeDiagramPanel);
        chartPane.addTab("Analyse", analysisPanel);

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

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Onglets pour résultats et console
        JTabbedPane bottomTabs = new JTabbedPane();
        
        // Panneau de résultats
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setRows(8);
        resultTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane resultScrollPane = new JScrollPane(resultTextArea);
        resultPanel.add(resultScrollPane, BorderLayout.CENTER);
        
        // Panneau console
        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);
        consoleTextArea.setRows(8);
        consoleTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        consoleTextArea.setBackground(new Color(30, 30, 30));
        consoleTextArea.setForeground(new Color(0, 255, 0));
        JScrollPane consoleScrollPane = new JScrollPane(consoleTextArea);
        consolePanel.add(consoleScrollPane, BorderLayout.CENTER);
        
        // Bouton pour effacer la console
        JButton clearConsoleButton = new JButton("Effacer");
        clearConsoleButton.addActionListener(e -> consoleTextArea.setText(""));
        consolePanel.add(clearConsoleButton, BorderLayout.SOUTH);
        
        bottomTabs.addTab("Résultats", resultPanel);
        bottomTabs.addTab("Console d'analyse", consolePanel);
        
        bottomPanel.add(bottomTabs, BorderLayout.CENTER);
        bottomPanel.setPreferredSize(new Dimension(800, 200));
        
        return bottomPanel;
    }

    private void runSimulation() {
        try {
            // Rediriger la sortie console vers notre JTextArea
            redirectConsoleOutput();
            
            // Configuration des paramètres de la simulation
            int sequenceLength = Integer.parseInt(sequenceLengthField.getText());
            LineEncoder.EncodingType encodingType = (LineEncoder.EncodingType) encodingTypeComboBox.getSelectedItem();
            TransmissionFilter.FilterType filterType = (TransmissionFilter.FilterType) filterTypeComboBox.getSelectedItem();
            Modulator.ModulationType modulationType = (Modulator.ModulationType) modulationTypeComboBox.getSelectedItem();
            double snr = snrSlider.getValue();

            // Effacer les résultats précédents
            resultTextArea.setText("");
            consoleTextArea.append("\n=== NOUVELLE SIMULATION ===\n");

            // Lancement de la simulation via le contrôleur
            controller.runSimulation(sequenceLength, encodingType, filterType, modulationType, snr);

            // Mise à jour des graphiques
            updateCharts();
            
            // Mise à jour du diagramme de l'œil
            updateEyeDiagram();
            
            // Mise à jour des signaux filtrés
            updateFilteredSignals();

            // Calcul BER sécurisé
            double ber = 0;
            try {
                ber = controller.calculateBER();
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur dans le calcul du BER: " + e.getMessage(),
                        "Avertissement", JOptionPane.WARNING_MESSAGE);
                ber = 0.0;
            }

            // Affichage des résultats détaillés
            StringBuilder results = new StringBuilder();
            results.append("=== RÉSULTATS DE LA SIMULATION ===\n\n");
            results.append("Paramètres:\n");
            results.append("- Longueur de la séquence: ").append(sequenceLength).append(" bits\n");
            results.append("- Codage: ").append(encodingType).append("\n");
            results.append("- Filtre: ").append(filterType).append("\n");
            results.append("- Modulation: ").append(modulationType).append("\n");
            results.append("- SNR théorique: ").append(snr).append(" dB\n\n");
            
            results.append("Performances:\n");
            results.append("- Taux d'erreur binaire (BER): ").append(String.format("%.6f", ber)).append("\n");
            results.append("- Nombre de bits erronés: ").append((int)(ber * sequenceLength))
                   .append("/").append(sequenceLength).append("\n\n");
            
            results.append("Métriques avancées:\n");
            results.append("- SNR effectif: ").append(String.format("%.2f", controller.getEffectiveSNR())).append(" dB\n");
            results.append("- Marge de bruit: ").append(String.format("%.3f", controller.getNoiseMargin())).append("\n");
            
            // Ajouter les métriques du diagramme de l'œil
            SignalAnalyzer.EyePatternMetrics eyeMetrics = controller.getEyeMetrics();
            if (eyeMetrics != null) {
                results.append("\nDiagramme de l'œil:\n");
                results.append("- Ouverture maximale: ").append(String.format("%.3f", eyeMetrics.maxOpening)).append("\n");
                results.append("- Jitter temporel: ").append(String.format("%.1f%%", eyeMetrics.jitter * 100)).append("\n");
                results.append("- Instant d'échantillonnage optimal: ").append(eyeMetrics.bestSamplingPoint).append("\n");
                results.append("- Qualité du signal: ").append(eyeMetrics.getQualityAssessment()).append("\n\n");
            }
            
            // Évaluation de la qualité
            results.append("Évaluation de la qualité:\n");
            if (ber == 0) {
                results.append("✓ Transmission PARFAITE - Aucune erreur détectée\n");
            } else if (ber < 0.001) {
                results.append("✓ Excellente qualité - BER < 10^-3\n");
            } else if (ber < 0.01) {
                results.append("⚠ Bonne qualité - BER < 10^-2\n");
            } else if (ber < 0.1) {
                results.append("⚠ Qualité moyenne - BER < 10^-1\n");
            } else {
                results.append("✗ Mauvaise qualité - BER ≥ 10^-1\n");
            }
            
            resultTextArea.setText(results.toString());
            
            // Mise à jour du graphique d'analyse
            updateAnalysisChart();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la simulation: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void runPerformanceTests() {
        // Désactiver les boutons pendant le test
        startSimulationButton.setEnabled(false);
        performanceTestButton.setEnabled(false);
        
        // Effacer les zones de texte
        resultTextArea.setText("Tests de performance en cours...\n");
        consoleTextArea.setText("");
        
        // Exécuter dans un thread séparé
        SwingWorker<double[][], Void> worker = new SwingWorker<double[][], Void>() {
            @Override
            protected double[][] doInBackground() throws Exception {
                Modulator.ModulationType modType = (Modulator.ModulationType) modulationTypeComboBox.getSelectedItem();
                return controller.runPerformanceTest(modType, 1000);
            }
            
            @Override
            protected void done() {
                try {
                    double[][] results = get();
                    displayPerformanceResults(results);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SwingTransmissionApp.this,
                            "Erreur lors des tests: " + e.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                } finally {
                    startSimulationButton.setEnabled(true);
                    performanceTestButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }

    private void displayPerformanceResults(double[][] results) {
        // Créer un graphique BER vs SNR
        XYSeries series = new XYSeries("BER vs SNR");
        
        StringBuilder text = new StringBuilder();
        text.append("=== RÉSULTATS DES TESTS DE PERFORMANCE ===\n\n");
        text.append("SNR (dB) | BER\n");
        text.append("---------|------------\n");
        
        for (double[] result : results) {
            double snr = result[0];
            double ber = result[1];
            series.add(snr, ber > 0 ? Math.log10(ber) : -10); // Log scale pour BER
            text.append(String.format("%8.0f | %.6e\n", snr, ber));
        }
        
        resultTextArea.setText(text.toString());
        
        // Créer le graphique
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Courbe BER vs SNR",
                "SNR (dB)",
                "log10(BER)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        analysisPanel.setChart(chart);
        chartPane.setSelectedComponent(analysisPanel);
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
    
    private void updateEyeDiagram() {
        // Obtenir le signal filtré en réception
        double[] rxFilteredSignal = controller.getRxFilteredSignal();
        int samplesPerSymbol = controller.getSamplesPerSymbol();
        
        // Mettre à jour le diagramme de l'œil
        if (rxFilteredSignal != null && rxFilteredSignal.length > 0) {
            eyeDiagramPanel.updateEyeDiagram(rxFilteredSignal, samplesPerSymbol);
        }
    }
    
    private void updateFilteredSignals() {
        // Obtenir les signaux
        double[] encodedSignal = controller.getEncodedSignal();
        double[] txFilteredSignal = controller.getFilteredSignal();
        double[] rxFilteredSignal = controller.getRxFilteredSignal();
        
        // Obtenir le type de filtre
        TransmissionFilter.FilterType filterType = (TransmissionFilter.FilterType) filterTypeComboBox.getSelectedItem();
        
        // Mettre à jour le panneau
        if (encodedSignal != null && txFilteredSignal != null && rxFilteredSignal != null) {
            filteredSignalsPanel.updateFilteredSignals(encodedSignal, txFilteredSignal, 
                                                      rxFilteredSignal, filterType.toString());
        }
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

    private void updateAnalysisChart() {
        // Créer un histogramme simple pour visualiser la distribution du signal
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Calculer un histogramme simple
        double[] signal = controller.getModulatedSignal();
        int numBins = 10;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (double v : signal) {
            if (v < min) min = v;
            if (v > max) max = v;
        }
        
        int[] histogram = new int[numBins];
        double binWidth = (max - min) / numBins;
        
        for (double v : signal) {
            int bin = (int)((v - min) / binWidth);
            if (bin >= numBins) bin = numBins - 1;
            histogram[bin]++;
        }
        
        for (int i = 0; i < numBins; i++) {
            double binCenter = min + (i + 0.5) * binWidth;
            dataset.addValue(histogram[i], "Distribution", String.format("%.2f", binCenter));
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                "Distribution du signal modulé",
                "Amplitude",
                "Nombre d'échantillons",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        
        analysisPanel.setChart(chart);
    }

    private void redirectConsoleOutput() {
        // Créer un PrintStream personnalisé qui écrit dans notre JTextArea
        java.io.PrintStream printStream = new java.io.PrintStream(new java.io.OutputStream() {
            @Override
            public void write(int b) {
                SwingUtilities.invokeLater(() -> {
                    consoleTextArea.append(String.valueOf((char) b));
                    consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
                });
            }
        });
        
        // Rediriger System.out vers notre PrintStream
        System.setOut(printStream);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingTransmissionApp app = new SwingTransmissionApp();
            app.setVisible(true);
        });
    }
}