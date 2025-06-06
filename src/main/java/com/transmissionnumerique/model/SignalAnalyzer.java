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
    
    // Calcul de la marge de bruit
    public static double calculateNoiseMargin(double[] signal, double threshold) {
        double minHighLevel = Double.MAX_VALUE;
        double maxLowLevel = Double.MIN_VALUE;
        
        for (double value : signal) {
            if (value > threshold && value < minHighLevel) {
                minHighLevel = value;
            }
            if (value <= threshold && value > maxLowLevel) {
                maxLowLevel = value;
            }
        }
        
        double margin = minHighLevel - maxLowLevel;
        System.out.println("\nMarge de bruit: " + margin);
        System.out.println("Niveau haut minimum: " + minHighLevel);
        System.out.println("Niveau bas maximum: " + maxLowLevel);
        
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
    
    // Analyse de la qualité de l'œil (eye pattern)
    public static void analyzeEyePattern(double[] signal, int samplesPerSymbol) {
        System.out.println("\n=== Analyse du diagramme de l'œil ===");
        
        double maxOpening = 0;
        double minOpening = Double.MAX_VALUE;
        
        // Pour chaque position d'échantillonnage dans le symbole
        for (int offset = 0; offset < samplesPerSymbol; offset++) {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            
            // Parcourir tous les symboles
            for (int i = offset; i < signal.length; i += samplesPerSymbol) {
                if (signal[i] < min) min = signal[i];
                if (signal[i] > max) max = signal[i];
            }
            
            double opening = max - min;
            if (opening > maxOpening) maxOpening = opening;
            if (opening < minOpening) minOpening = opening;
            
            System.out.println("Position " + offset + ": ouverture = " + opening);
        }
        
        System.out.println("Ouverture maximale de l'œil: " + maxOpening);
        System.out.println("Ouverture minimale de l'œil: " + minOpening);
    }
}