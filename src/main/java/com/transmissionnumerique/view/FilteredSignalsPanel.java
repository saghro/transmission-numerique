package com.transmissionnumerique.view;

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
 * Panneau pour visualiser les signaux avant et après filtrage
 */
public class FilteredSignalsPanel extends JPanel {
    
    private ChartPanel txFilteredPanel;
    private ChartPanel rxFilteredPanel;
    private JTextArea infoArea;
    
    public FilteredSignalsPanel() {
        setLayout(new BorderLayout());
        
        // Panneau principal avec deux graphiques
        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Graphique du signal après filtre d'émission
        txFilteredPanel = createEmptyChart("Signal après filtre d'émission", "Échantillons", "Amplitude");
        chartsPanel.add(txFilteredPanel);
        
        // Graphique du signal après filtre de réception
        rxFilteredPanel = createEmptyChart("Signal après filtre de réception", "Échantillons", "Amplitude");
        chartsPanel.add(rxFilteredPanel);
        
        add(chartsPanel, BorderLayout.CENTER);
        
        // Panneau d'information
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informations sur le filtrage"));
        infoPanel.setPreferredSize(new Dimension(300, 0));
        
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(infoArea);
        infoPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(infoPanel, BorderLayout.EAST);
    }
    
    private ChartPanel createEmptyChart(String title, String xLabel, String yLabel) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(
            title, xLabel, yLabel, dataset,
            PlotOrientation.VERTICAL, true, true, false
        );
        
        // Personnaliser l'apparence
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        return new ChartPanel(chart);
    }
    
    /**
     * Met à jour l'affichage avec les signaux filtrés
     */
    public void updateFilteredSignals(double[] encodedSignal, double[] txFiltered, 
                                     double[] rxFiltered, String filterType) {
        // Mise à jour du signal après filtre d'émission
        updateTxFilteredChart(encodedSignal, txFiltered);
        
        // Mise à jour du signal après filtre de réception
        updateRxFilteredChart(rxFiltered);
        
        // Mise à jour des informations
        updateFilterInfo(encodedSignal, txFiltered, rxFiltered, filterType);
    }
    
    private void updateTxFilteredChart(double[] original, double[] filtered) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        // Signal original (encodé)
        XYSeries originalSeries = new XYSeries("Signal encodé");
        for (int i = 0; i < Math.min(original.length * 8, 200); i++) {
            int symbolIndex = i / 8;
            if (symbolIndex < original.length) {
                originalSeries.add(i, original[symbolIndex]);
            }
        }
        dataset.addSeries(originalSeries);
        
        // Signal filtré
        XYSeries filteredSeries = new XYSeries("Signal filtré TX");
        for (int i = 0; i < Math.min(filtered.length, 200); i++) {
            filteredSeries.add(i, filtered[i]);
        }
        dataset.addSeries(filteredSeries);
        
        // Mettre à jour le graphique
        JFreeChart chart = txFilteredPanel.getChart();
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(dataset);
        
        // Personnaliser le rendu
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, new Color(200, 200, 200));
        renderer.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, 
                                                    BasicStroke.JOIN_MITER, 10.0f, 
                                                    new float[]{5.0f}, 0.0f));
        renderer.setSeriesPaint(1, Color.BLUE);
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        plot.setRenderer(renderer);
    }
    
    private void updateRxFilteredChart(double[] filtered) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        // Signal filtré en réception
        XYSeries series = new XYSeries("Signal filtré RX");
        for (int i = 0; i < Math.min(filtered.length, 200); i++) {
            series.add(i, filtered[i]);
        }
        dataset.addSeries(series);
        
        // Ajouter des marqueurs pour les instants d'échantillonnage optimaux
        XYSeries samplingPoints = new XYSeries("Points d'échantillonnage");
        for (int i = 4; i < Math.min(filtered.length, 200); i += 8) {
            samplingPoints.add(i, filtered[i]);
        }
        dataset.addSeries(samplingPoints);
        
        // Mettre à jour le graphique
        JFreeChart chart = rxFilteredPanel.getChart();
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(dataset);
        
        // Personnaliser le rendu
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        
        renderer.setSeriesLinesVisible(1, false);
        renderer.setSeriesShapesVisible(1, true);
        renderer.setSeriesPaint(1, Color.BLACK);
        renderer.setSeriesShape(1, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6));
        
        plot.setRenderer(renderer);
    }
    
    private void updateFilterInfo(double[] encoded, double[] txFiltered, 
                                 double[] rxFiltered, String filterType) {
        StringBuilder info = new StringBuilder();
        
        info.append("=== INFORMATIONS FILTRAGE ===\n\n");
        info.append("Type de filtre: ").append(filterType).append("\n\n");
        
        // Analyse du filtre d'émission
        info.append("FILTRE D'ÉMISSION:\n");
        info.append("- Longueur signal encodé: ").append(encoded.length).append("\n");
        info.append("- Longueur après filtrage: ").append(txFiltered.length).append("\n");
        info.append("- Facteur de suréchantillonnage: ").append(txFiltered.length / encoded.length).append("\n");
        
        // Calcul de l'énergie
        double energyBefore = calculateEnergy(encoded);
        double energyAfterTx = calculateEnergy(txFiltered);
        info.append("- Énergie avant: ").append(String.format("%.3f", energyBefore)).append("\n");
        info.append("- Énergie après: ").append(String.format("%.3f", energyAfterTx)).append("\n");
        
        // Analyse du filtre de réception
        info.append("\nFILTRE DE RÉCEPTION:\n");
        info.append("- Longueur signal reçu: ").append(rxFiltered.length).append("\n");
        
        // Analyse de la qualité
        double snrImprovement = analyzeFilterQuality(txFiltered, rxFiltered);
        info.append("- Amélioration SNR: ").append(String.format("%.1f dB", snrImprovement)).append("\n");
        
        // Caractéristiques spectrales
        info.append("\nCARACTÉRISTIQUES:\n");
        switch (filterType) {
            case "RECTANGULAR":
                info.append("- Bande passante: Infinie\n");
                info.append("- ISI: Élevée\n");
                info.append("- Complexité: Faible\n");
                break;
            case "RAISED_COSINE":
                info.append("- Bande passante: (1+α)/2T\n");
                info.append("- ISI: Nulle (théorique)\n");
                info.append("- Complexité: Moyenne\n");
                break;
            case "ROOT_RAISED_COSINE":
                info.append("- Bande passante: (1+α)/2T\n");
                info.append("- ISI: Nulle (avec RRC en RX)\n");
                info.append("- Complexité: Élevée\n");
                info.append("- Usage: Standard 4G/5G\n");
                break;
        }
        
        infoArea.setText(info.toString());
    }
    
    private double calculateEnergy(double[] signal) {
        double energy = 0;
        for (double sample : signal) {
            energy += sample * sample;
        }
        return energy / signal.length;
    }
    
    private double analyzeFilterQuality(double[] txSignal, double[] rxSignal) {
        // Estimation simplifiée de l'amélioration du SNR
        // En pratique, cela dépend du type de filtre et du bruit
        return 3.0; // Amélioration typique de 3 dB pour un filtre adapté
    }
}