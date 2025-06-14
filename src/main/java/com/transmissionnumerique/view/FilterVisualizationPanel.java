package com.transmissionnumerique.view;

import com.transmissionnumerique.model.TransmissionFilter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

/**
 * Panneau pour visualiser les caractéristiques des filtres d'émission et de réception
 */
public class FilterVisualizationPanel extends JPanel {
    
    private ChartPanel impulseResponsePanel;
    private ChartPanel frequencyResponsePanel;
    private JComboBox<TransmissionFilter.FilterType> filterTypeCombo;
    private JSlider rollOffSlider;
    private JLabel rollOffLabel;
    
    private int samplesPerSymbol = 8;
    
    public FilterVisualizationPanel() {
        setLayout(new BorderLayout());
        
        // Panneau de contrôle
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Panneau des graphiques
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2));
        
        // Graphique de la réponse impulsionnelle
        impulseResponsePanel = createEmptyChart("Réponse impulsionnelle", "Échantillons", "Amplitude");
        chartsPanel.add(impulseResponsePanel);
        
        // Graphique de la réponse en fréquence
        frequencyResponsePanel = createEmptyChart("Réponse en fréquence", "Fréquence normalisée", "Magnitude (dB)");
        chartsPanel.add(frequencyResponsePanel);
        
        add(chartsPanel, BorderLayout.CENTER);
        
        // Mise à jour initiale
        updateFilterVisualization();
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Paramètres du filtre"));
        
        // Sélection du type de filtre
        panel.add(new JLabel("Type de filtre:"));
        filterTypeCombo = new JComboBox<>(TransmissionFilter.FilterType.values());
        filterTypeCombo.addActionListener(e -> updateFilterVisualization());
        panel.add(filterTypeCombo);
        
        // Facteur de roll-off
        panel.add(Box.createHorizontalStrut(20));
        panel.add(new JLabel("Roll-off (α):"));
        rollOffSlider = new JSlider(0, 100, 35); // 0 à 1 avec précision 0.01
        rollOffSlider.setPreferredSize(new Dimension(150, 30));
        rollOffSlider.addChangeListener(e -> {
            double rollOff = rollOffSlider.getValue() / 100.0;
            rollOffLabel.setText(String.format("%.2f", rollOff));
            if (!rollOffSlider.getValueIsAdjusting()) {
                updateFilterVisualization();
            }
        });
        panel.add(rollOffSlider);
        
        rollOffLabel = new JLabel("0.35");
        rollOffLabel.setPreferredSize(new Dimension(40, 20));
        panel.add(rollOffLabel);
        
        // Bouton d'information
        JButton infoButton = new JButton("Info");
        infoButton.addActionListener(e -> showFilterInfo());
        panel.add(Box.createHorizontalStrut(20));
        panel.add(infoButton);
        
        return panel;
    }
    
    private ChartPanel createEmptyChart(String title, String xLabel, String yLabel) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(
            title, xLabel, yLabel, dataset,
            PlotOrientation.VERTICAL, true, true, false
        );
        
        return new ChartPanel(chart);
    }
    
    private void updateFilterVisualization() {
        TransmissionFilter.FilterType filterType = (TransmissionFilter.FilterType) filterTypeCombo.getSelectedItem();
        double rollOff = rollOffSlider.getValue() / 100.0;
        
        // Activer/désactiver le slider selon le type de filtre
        boolean enableRollOff = (filterType == TransmissionFilter.FilterType.RAISED_COSINE || 
                                filterType == TransmissionFilter.FilterType.ROOT_RAISED_COSINE);
        rollOffSlider.setEnabled(enableRollOff);
        
        // Créer un filtre temporaire pour obtenir les réponses
        TransmissionFilter filter = new TransmissionFilter(filterType, samplesPerSymbol, rollOff);
        
        // Mise à jour de la réponse impulsionnelle
        updateImpulseResponse(filter, filterType, rollOff);
        
        // Mise à jour de la réponse en fréquence
        updateFrequencyResponse(filter, filterType);
    }
    
    private void updateImpulseResponse(TransmissionFilter filter, TransmissionFilter.FilterType type, double rollOff) {
        XYSeries series = new XYSeries("h(t)");
        
        // Générer une impulsion pour voir la réponse
        double[] impulse = new double[1];
        impulse[0] = 1.0;
        
        // Filtrer l'impulsion
        double[] response = filter.filter(impulse);
        
        // Ajouter les points au graphique
        for (int i = 0; i < Math.min(response.length, 100); i++) {
            series.add(i - response.length/2, response[i]);
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = impulseResponsePanel.getChart();
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(dataset);
        
        // Personnaliser le rendu
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(renderer);
        
        // Ajouter une ligne de zéro
        plot.setRangeZeroBaselineVisible(true);
        plot.setRangeZeroBaselinePaint(Color.BLACK);
    }
    
    private void updateFrequencyResponse(TransmissionFilter filter, TransmissionFilter.FilterType type) {
        XYSeries series = new XYSeries("H(f)");
        
        // Obtenir la réponse en fréquence
        int numPoints = 256;
        double[] magnitude = filter.getFrequencyResponse(numPoints);
        
        // Convertir en dB et normaliser la fréquence
        for (int i = 0; i < numPoints/2; i++) {
            double freq = i / (double)(numPoints/2); // Fréquence normalisée 0 à 1
            double magDB = 20 * Math.log10(magnitude[i] + 1e-10); // Éviter log(0)
            series.add(freq, magDB);
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = frequencyResponsePanel.getChart();
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(dataset);
        
        // Personnaliser le rendu
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(renderer);
        
        // Ajouter des lignes de référence
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setDomainGridlinePaint(Color.GRAY);
        
        // Marquer la bande passante à -3dB
        plot.addRangeMarker(new org.jfree.chart.plot.ValueMarker(-3.0, Color.GREEN, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f}, 0.0f)));
    }
    
    private void showFilterInfo() {
        TransmissionFilter.FilterType filterType = (TransmissionFilter.FilterType) filterTypeCombo.getSelectedItem();
        String info = "";
        
        switch (filterType) {
            case RECTANGULAR:
                info = "Filtre Rectangulaire (NRZ):\n\n" +
                       "• Réponse impulsionnelle constante sur Ts\n" +
                       "• Bande passante infinie théoriquement\n" +
                       "• Simple à implémenter\n" +
                       "• Forte interférence entre symboles (ISI)\n" +
                       "• Utilisé pour des transmissions simples";
                break;
                
            case RAISED_COSINE:
                info = "Filtre en Cosinus Surélevé:\n\n" +
                       "• Zéro ISI aux instants d'échantillonnage\n" +
                       "• Bande passante limitée: B = (1+α)/(2Ts)\n" +
                       "• Roll-off α contrôle la transition\n" +
                       "• α=0: filtre idéal (irréalisable)\n" +
                       "• α=1: bande passante doublée\n" +
                       "• Optimal pour minimiser l'ISI";
                break;
                
            case ROOT_RAISED_COSINE:
                info = "Filtre en Racine de Cosinus Surélevé:\n\n" +
                       "• Utilisé en émission ET réception\n" +
                       "• RRC(tx) × RRC(rx) = RC complet\n" +
                       "• Partage optimal du filtrage\n" +
                       "• Maximise le SNR en réception\n" +
                       "• Standard dans les communications\n" +
                       "• Utilisé en 4G/5G, satellite, etc.";
                break;
        }
        
        JOptionPane.showMessageDialog(this, info, "Information sur le filtre", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Méthode pour analyser les performances du filtre
     */
    public void analyzeFilterPerformance(TransmissionFilter.FilterType filterType, double rollOff) {
        TransmissionFilter filter = new TransmissionFilter(filterType, samplesPerSymbol, rollOff);
        
        // Calculer la bande passante
        double bandwidth = calculateBandwidth(filter);
        
        // Calculer l'efficacité spectrale
        double spectralEfficiency = 1.0 / bandwidth;
        
        // Afficher les résultats
        String analysis = String.format(
            "=== Analyse du filtre %s ===\n" +
            "Roll-off (α): %.2f\n" +
            "Bande passante normalisée: %.3f\n" +
            "Efficacité spectrale: %.3f bits/s/Hz\n",
            filterType, rollOff, bandwidth, spectralEfficiency
        );
        
        System.out.println(analysis);
    }
    
    private double calculateBandwidth(TransmissionFilter filter) {
        // Obtenir la réponse en fréquence
        double[] response = filter.getFrequencyResponse(512);
        
        // Trouver la fréquence de coupure à -3dB
        double maxMag = response[0];
        double cutoffMag = maxMag / Math.sqrt(2); // -3dB
        
        for (int i = 0; i < response.length/2; i++) {
            if (response[i] < cutoffMag) {
                return i / (double)(response.length/2);
            }
        }
        
        return 1.0; // Bande passante maximale
    }
}