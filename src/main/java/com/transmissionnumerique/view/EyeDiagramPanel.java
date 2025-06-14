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
 * Panneau spécialisé pour l'affichage du diagramme de l'œil
 */
public class EyeDiagramPanel extends JPanel {
    
    private ChartPanel chartPanel;
    private JLabel marginLabel;
    private JLabel jitterLabel;
    private JLabel openingLabel;
    private JLabel bestSamplingLabel;
    
    // Paramètres du diagramme
    private int samplesPerSymbol;
    private int numSymbolsToDisplay = 100; // Nombre de symboles à superposer
    
    public EyeDiagramPanel() {
        setLayout(new BorderLayout());
        
        // Créer le panneau du graphique
        createChartPanel();
        
        // Créer le panneau d'informations
        JPanel infoPanel = createInfoPanel();
        
        // Ajouter les composants
        add(chartPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);
    }
    
    private void createChartPanel() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Diagramme de l'œil",
            "Temps (échantillons)",
            "Amplitude",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        );
        
        // Personnaliser l'apparence
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Métriques du diagramme"));
        panel.setPreferredSize(new Dimension(250, 400));
        
        // Labels pour les métriques
        marginLabel = new JLabel("Marge d'ouverture: --");
        jitterLabel = new JLabel("Jitter: --");
        openingLabel = new JLabel("Ouverture verticale: --");
        bestSamplingLabel = new JLabel("Instant optimal: --");
        
        // Style des labels
        Font font = new Font("Arial", Font.BOLD, 12);
        marginLabel.setFont(font);
        jitterLabel.setFont(font);
        openingLabel.setFont(font);
        bestSamplingLabel.setFont(font);
        
        // Ajouter les labels
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(marginLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(jitterLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(openingLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(bestSamplingLabel);
        panel.add(Box.createVerticalGlue());
        
        // Ajouter une légende
        JTextArea legend = new JTextArea();
        legend.setText("Le diagramme de l'œil permet de:\n" +
                      "• Visualiser la qualité du signal\n" +
                      "• Mesurer la marge de bruit\n" +
                      "• Évaluer le jitter temporel\n" +
                      "• Déterminer l'instant d'échantillonnage optimal");
        legend.setEditable(false);
        legend.setLineWrap(true);
        legend.setWrapStyleWord(true);
        legend.setBackground(panel.getBackground());
        legend.setFont(new Font("Arial", Font.PLAIN, 11));
        
        panel.add(legend);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        return panel;
    }
    
    /**
     * Met à jour le diagramme de l'œil avec le signal donné
     * @param signal Signal à analyser
     * @param samplesPerSymbol Nombre d'échantillons par symbole
     */
    public void updateEyeDiagram(double[] signal, int samplesPerSymbol) {
        this.samplesPerSymbol = samplesPerSymbol;
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        // Calculer le nombre de symboles complets dans le signal
        int totalSymbols = signal.length / samplesPerSymbol;
        int symbolsToPlot = Math.min(totalSymbols, numSymbolsToDisplay);
        
        // Superposer plusieurs périodes de symboles
        for (int symbol = 0; symbol < symbolsToPlot; symbol++) {
            XYSeries trace = new XYSeries("Trace " + symbol);
            
            // Tracer 2 périodes de symboles pour voir les transitions
            for (int i = 0; i < 2 * samplesPerSymbol; i++) {
                int index = symbol * samplesPerSymbol + i;
                if (index < signal.length) {
                    trace.add(i, signal[index]);
                }
            }
            
            dataset.addSeries(trace);
        }
        
        // Mettre à jour le graphique
        JFreeChart chart = chartPanel.getChart();
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(dataset);
        
        // Personnaliser le rendu pour avoir des lignes fines
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, new Color(0, 255, 0, 100)); // Vert semi-transparent
            renderer.setSeriesStroke(i, new BasicStroke(0.5f));
        }
        plot.setRenderer(renderer);
        
        // Calculer et afficher les métriques
        calculateAndDisplayMetrics(signal, samplesPerSymbol);
    }
    
    /**
     * Calcule et affiche les métriques du diagramme de l'œil
     */
    private void calculateAndDisplayMetrics(double[] signal, int samplesPerSymbol) {
        // Analyse pour trouver l'instant d'échantillonnage optimal
        double maxOpening = 0;
        int bestSamplingPoint = 0;
        double minHigh = Double.MAX_VALUE;
        double maxLow = Double.MIN_VALUE;
        
        // Analyser chaque position d'échantillonnage possible
        for (int offset = 0; offset < samplesPerSymbol; offset++) {
            double localMinHigh = Double.MAX_VALUE;
            double localMaxLow = Double.MIN_VALUE;
            double sumHigh = 0;
            double sumLow = 0;
            int countHigh = 0;
            int countLow = 0;
            
            // Parcourir tous les symboles à cette position d'échantillonnage
            for (int i = offset; i < signal.length; i += samplesPerSymbol) {
                if (signal[i] > 0) {
                    // Niveau haut
                    if (signal[i] < localMinHigh) localMinHigh = signal[i];
                    sumHigh += signal[i];
                    countHigh++;
                } else {
                    // Niveau bas
                    if (signal[i] > localMaxLow) localMaxLow = signal[i];
                    sumLow += signal[i];
                    countLow++;
                }
            }
            
            // Calculer l'ouverture à cette position
            double opening = localMinHigh - localMaxLow;
            if (opening > maxOpening) {
                maxOpening = opening;
                bestSamplingPoint = offset;
                minHigh = localMinHigh;
                maxLow = localMaxLow;
            }
        }
        
        // Calculer le jitter (variation temporelle)
        double jitter = calculateJitter(signal, samplesPerSymbol, bestSamplingPoint);
        
        // Calculer la marge de bruit
        double threshold = 0.0; // Seuil de décision
        double noiseMargin = Math.min(minHigh - threshold, threshold - maxLow);
        
        // Mettre à jour les labels
        marginLabel.setText(String.format("Marge d'ouverture: %.3f", noiseMargin));
        jitterLabel.setText(String.format("Jitter: %.3f %%", jitter * 100));
        openingLabel.setText(String.format("Ouverture verticale: %.3f", maxOpening));
        bestSamplingLabel.setText(String.format("Instant optimal: %d/%d", bestSamplingPoint, samplesPerSymbol));
        
        // Colorer les labels selon la qualité
        Color qualityColor = getQualityColor(maxOpening);
        marginLabel.setForeground(qualityColor);
        openingLabel.setForeground(qualityColor);
    }
    
    /**
     * Calcule le jitter temporel
     */
    private double calculateJitter(double[] signal, int samplesPerSymbol, int optimalPoint) {
        // Mesurer la variation des passages par zéro
        double totalVariation = 0;
        int transitionCount = 0;
        
        for (int symbol = 1; symbol < signal.length / samplesPerSymbol; symbol++) {
            int prevIndex = (symbol - 1) * samplesPerSymbol + optimalPoint;
            int currIndex = symbol * samplesPerSymbol + optimalPoint;
            
            if (prevIndex < signal.length && currIndex < signal.length) {
                // Détecter une transition
                if (Math.signum(signal[prevIndex]) != Math.signum(signal[currIndex])) {
                    // Chercher le point exact de passage par zéro
                    for (int i = 0; i < samplesPerSymbol; i++) {
                        int idx = (symbol - 1) * samplesPerSymbol + i;
                        int nextIdx = idx + 1;
                        if (nextIdx < signal.length && 
                            Math.signum(signal[idx]) != Math.signum(signal[nextIdx])) {
                            totalVariation += Math.abs(i - samplesPerSymbol/2.0);
                            transitionCount++;
                            break;
                        }
                    }
                }
            }
        }
        
        return transitionCount > 0 ? totalVariation / (transitionCount * samplesPerSymbol) : 0;
    }
    
    /**
     * Retourne une couleur selon la qualité du signal
     */
    private Color getQualityColor(double opening) {
        if (opening > 1.5) return new Color(0, 200, 0); // Vert - Excellent
        else if (opening > 1.0) return new Color(255, 200, 0); // Orange - Bon
        else if (opening > 0.5) return new Color(255, 100, 0); // Orange foncé - Moyen
        else return new Color(255, 0, 0); // Rouge - Mauvais
    }
    
    /**
     * Configure le nombre de symboles à afficher
     */
    public void setNumSymbolsToDisplay(int num) {
        this.numSymbolsToDisplay = num;
    }
}