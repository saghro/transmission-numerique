package com.transmissionnumerique.model;

public class SignalAnalyzer {
    
    // Calcul du SNR effectif du signal reçu
    public static double calculateEffectiveSNR(double[] cleanSignal, double[] noisySignal) {
        if (cleanSignal.length != noisySignal.length) {
            return -1; // Erreur
        }
        
        double signalPower = 0;
        double noisePower = 0;
        
        for (int i = 0; i < cleanSignal.length; i++) {
            signalPower += cleanSignal[i] * cleanSignal[i];
            double noise = noisySignal[i] - cleanSignal[i];
            noisePower += noise * noise;
        }
        
        signalPower /= cleanSignal.length;
        noisePower /= cleanSignal.length;
        
        if (noisePower == 0) {
            return Double.POSITIVE_INFINITY; // Pas de bruit
        }
        
        double snrLinear = signalPower / noisePower;
        double snrDB = 10 * Math.log10(snrLinear);
        
        return snrDB;
    }
    
    // Analyse de la distribution du signal
    public static void analyzeSignalDistribution(double[] signal, String signalName) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;
        
        for (double value : signal) {
            if (value < min) min = value;
            if (value > max) max = value;
            sum += value;
        }
        
        double mean = sum / signal.length;
        
        // Calcul de la variance
        double variance = 0;
        for (double value : signal) {
            variance += (value - mean) * (value - mean);
        }
        variance /= signal.length;
        double stdDev = Math.sqrt(variance);
        
        System.out.println("\n=== Analyse du signal: " + signalName + " ===");
        System.out.println("Min: " + min);
        System.out.println("Max: " + max);
        System.out.println("Moyenne: " + mean);
        System.out.println("Écart-type: " + stdDev);
        System.out.println("Plage dynamique: " + (max - min));
    }
    
    // Calcul de la marge de bruit avec seuil adaptatif
    public static double calculateNoiseMargin(double[] signal, double threshold) {
        // Séparer les échantillons en deux groupes (haut et bas)
        java.util.List<Double> highSamples = new java.util.ArrayList<>();
        java.util.List<Double> lowSamples = new java.util.ArrayList<>();
        
        // Utiliser un algorithme de clustering simple (K-means avec K=2)
        double mean = 0;
        for (double v : signal) mean += v;
        mean /= signal.length;
        
        // Première classification
        for (double value : signal) {
            if (value > mean) {
                highSamples.add(value);
            } else {
                lowSamples.add(value);
            }
        }
        
        // Calculer les moyennes des clusters
        double meanHigh = highSamples.stream().mapToDouble(Double::doubleValue).average().orElse(1.0);
        double meanLow = lowSamples.stream().mapToDouble(Double::doubleValue).average().orElse(-1.0);
        
        // Seuil optimal entre les deux clusters
        double optimalThreshold = (meanHigh + meanLow) / 2.0;
        
        // Trouver les valeurs extrêmes près du seuil
        double minHighLevel = Double.MAX_VALUE;
        double maxLowLevel = Double.MIN_VALUE;
        
        for (double value : signal) {
            if (value > optimalThreshold && value < minHighLevel) {
                minHighLevel = value;
            }
            if (value <= optimalThreshold && value > maxLowLevel) {
                maxLowLevel = value;
            }
        }
        
        double margin = minHighLevel - maxLowLevel;
        
        System.out.println("\n=== Calcul de la marge de bruit ===");
        System.out.println("Seuil optimal calculé: " + optimalThreshold);
        System.out.println("Niveau haut moyen: " + meanHigh);
        System.out.println("Niveau bas moyen: " + meanLow);
        System.out.println("Niveau haut minimum: " + minHighLevel);
        System.out.println("Niveau bas maximum: " + maxLowLevel);
        System.out.println("Marge de bruit: " + margin);
        
        return margin;
    }
    
    // Histogramme simple du signal
    public static void plotHistogram(double[] signal, int numBins) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (double value : signal) {
            if (value < min) min = value;
            if (value > max) max = value;
        }
        
        int[] histogram = new int[numBins];
        double binWidth = (max - min) / numBins;
        
        for (double value : signal) {
            int binIndex = (int)((value - min) / binWidth);
            if (binIndex >= numBins) binIndex = numBins - 1;
            histogram[binIndex]++;
        }
        
        System.out.println("\n=== Histogramme du signal ===");
        for (int i = 0; i < numBins; i++) {
            double binCenter = min + (i + 0.5) * binWidth;
            System.out.printf("%.2f: ", binCenter);
            for (int j = 0; j < histogram[i]; j++) {
                System.out.print("*");
            }
            System.out.println(" (" + histogram[i] + ")");
        }
    }
    
    // Analyse améliorée du diagramme de l'œil avec métriques
    public static EyePatternMetrics analyzeEyePattern(double[] signal, int samplesPerSymbol) {
        System.out.println("\n=== Analyse du diagramme de l'œil ===");
        
        double maxOpening = 0;
        double minOpening = Double.MAX_VALUE;
        int bestSamplingPoint = 0;
        double minHigh = Double.MAX_VALUE;
        double maxLow = Double.MIN_VALUE;
        
        // Pour chaque position d'échantillonnage dans le symbole
        for (int offset = 0; offset < samplesPerSymbol; offset++) {
            double localMinHigh = Double.MAX_VALUE;
            double localMaxLow = Double.MIN_VALUE;
            
            // Parcourir tous les symboles
            for (int i = offset; i < signal.length; i += samplesPerSymbol) {
                if (signal[i] > 0) {
                    if (signal[i] < localMinHigh) localMinHigh = signal[i];
                } else {
                    if (signal[i] > localMaxLow) localMaxLow = signal[i];
                }
            }
            
            double opening = localMinHigh - localMaxLow;
            if (opening > maxOpening) {
                maxOpening = opening;
                bestSamplingPoint = offset;
                minHigh = localMinHigh;
                maxLow = localMaxLow;
            }
            if (opening < minOpening) minOpening = opening;
            
            System.out.println("Position " + offset + ": ouverture = " + opening);
        }
        
        // Calculer le jitter
        double jitter = calculateEyeJitter(signal, samplesPerSymbol);
        
        System.out.println("Ouverture maximale de l'œil: " + maxOpening);
        System.out.println("Ouverture minimale de l'œil: " + minOpening);
        System.out.println("Meilleur instant d'échantillonnage: " + bestSamplingPoint);
        System.out.println("Jitter estimé: " + (jitter * 100) + "%");
        
        return new EyePatternMetrics(maxOpening, minOpening, bestSamplingPoint, 
                                    minHigh, maxLow, jitter);
    }
    
    // Calcul du jitter temporel
    private static double calculateEyeJitter(double[] signal, int samplesPerSymbol) {
        double totalVariation = 0;
        int transitionCount = 0;
        
        // Analyser les transitions entre symboles
        for (int symbol = 1; symbol < signal.length / samplesPerSymbol; symbol++) {
            // Chercher les transitions
            for (int i = 0; i < samplesPerSymbol - 1; i++) {
                int idx = symbol * samplesPerSymbol + i;
                if (idx + 1 < signal.length) {
                    // Détecter un passage par zéro
                    if (signal[idx] * signal[idx + 1] < 0) {
                        // Calculer la position exacte du passage par zéro
                        double crossPoint = i + Math.abs(signal[idx]) / 
                                          (Math.abs(signal[idx]) + Math.abs(signal[idx + 1]));
                        totalVariation += Math.abs(crossPoint - samplesPerSymbol/2.0);
                        transitionCount++;
                    }
                }
            }
        }
        
        return transitionCount > 0 ? totalVariation / (transitionCount * samplesPerSymbol) : 0;
    }
    
    // Classe pour stocker les métriques du diagramme de l'œil
    public static class EyePatternMetrics {
        public final double maxOpening;
        public final double minOpening;
        public final int bestSamplingPoint;
        public final double minHighLevel;
        public final double maxLowLevel;
        public final double jitter;
        public final double noiseMargin;
        
        public EyePatternMetrics(double maxOpening, double minOpening, int bestSamplingPoint,
                                double minHighLevel, double maxLowLevel, double jitter) {
            this.maxOpening = maxOpening;
            this.minOpening = minOpening;
            this.bestSamplingPoint = bestSamplingPoint;
            this.minHighLevel = minHighLevel;
            this.maxLowLevel = maxLowLevel;
            this.jitter = jitter;
            
            // Calculer la marge de bruit (distance entre les niveaux et le seuil de décision)
            double threshold = 0.0;
            this.noiseMargin = Math.min(minHighLevel - threshold, threshold - maxLowLevel);
        }
        
        // Méthodes utilitaires
        public double getQualityScore() {
            // Score de qualité basé sur l'ouverture et le jitter
            return maxOpening * (1 - jitter);
        }
        
        public String getQualityAssessment() {
            double score = getQualityScore();
            if (score > 0.8) return "Excellent";   // Était 1.5
            else if (score > 0.6) return "Bon";    // Était 1.0
            else if (score > 0.4) return "Moyen";  // Était 0.5
            else return "Mauvais";
        }
    }
}