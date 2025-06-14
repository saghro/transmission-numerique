package com.transmissionnumerique.model;

/**
 * Module de récupération d'horloge amélioré
 * Gère la synchronisation et l'échantillonnage optimal du signal
 */
public class ClockRecovery {
    private int samplesPerSymbol;
    private boolean debugMode = true;
    
    public ClockRecovery(int samplesPerSymbol) {
        this.samplesPerSymbol = samplesPerSymbol;
    }

    /**
     * Récupère les symboles en trouvant l'instant d'échantillonnage optimal
     */
    public double[] recover(double[] signal) {
        if (signal.length < samplesPerSymbol) {
            return signal;
        }
        
        // Trouver l'offset optimal en analysant l'énergie du signal
        int optimalOffset = findOptimalSamplingOffset(signal);
        
        // Calculer le nombre de symboles
        int numSymbols = signal.length / samplesPerSymbol;
        double[] recoveredSignal = new double[numSymbols];
        
        if (debugMode) {
            System.out.println("=== RÉCUPÉRATION D'HORLOGE ===");
            System.out.println("Longueur du signal: " + signal.length);
            System.out.println("Échantillons par symbole: " + samplesPerSymbol);
            System.out.println("Nombre de symboles: " + numSymbols);
            System.out.println("Offset optimal trouvé: " + optimalOffset);
        }
        
        // Échantillonner au moment optimal
        for (int i = 0; i < numSymbols; i++) {
            int sampleIndex = i * samplesPerSymbol + optimalOffset;
            if (sampleIndex < signal.length) {
                recoveredSignal[i] = signal[sampleIndex];
            }
        }
        
        if (debugMode) {
            System.out.println("Premiers symboles récupérés:");
            for (int i = 0; i < Math.min(10, recoveredSignal.length); i++) {
                System.out.printf("  Symbole[%d] = %.4f\n", i, recoveredSignal[i]);
            }
        }
        
        return recoveredSignal;
    }
    
    /**
     * Trouve l'offset optimal pour l'échantillonnage en maximisant l'ouverture de l'œil
     */
    private int findOptimalSamplingOffset(double[] signal) {
        double maxEyeOpening = 0;
        int bestOffset = samplesPerSymbol / 2;
        int windowSize = samplesPerSymbol * 3;  // Fenêtre de 3 symboles

        for (int offset = 0; offset < samplesPerSymbol; offset++) {
            double minHigh = Double.MAX_VALUE;
            double maxLow = Double.MIN_VALUE;
            int validCount = 0;

            for (int i = offset; i < signal.length - windowSize; i += samplesPerSymbol) {
                // Analyser le centre de l'œil
                int center = i + samplesPerSymbol;
                if (center >= signal.length) break;

                // Vérifier la transition (spécifique à Manchester)
                double current = signal[center];
                double prev = signal[i];
                double next = signal[i + 2 * samplesPerSymbol];
                
                // Détection de transition valide
                if (Math.signum(current) != Math.signum(prev) || 
                    Math.signum(current) != Math.signum(next)) {
                    continue;
                }

                // Mise à jour des niveaux
                if (current > 0 && current < minHigh) minHigh = current;
                if (current < 0 && current > maxLow) maxLow = current;
                validCount++;
            }

            if (validCount > 0) {
                double opening = minHigh - maxLow;
                if (opening > maxEyeOpening) {
                    maxEyeOpening = opening;
                    bestOffset = offset;
                }
            }
        }
        return bestOffset;
    }
    
    /**
     * Trouve l'offset optimal par corrélation avec un signal de référence
     */
    private int findByCorrelation(double[] signal) {
        double maxCorr = 0;
        int bestOffset = 0;
        
        // Créer un signal de référence (alternance +1/-1)
        for (int offset = 0; offset < samplesPerSymbol; offset++) {
            double correlation = 0;
            int count = 0;
            
            // Calculer la corrélation avec un pattern alterné
            for (int i = offset; i < signal.length - samplesPerSymbol; i += samplesPerSymbol) {
                double diff = Math.abs(signal[i] - signal[i + samplesPerSymbol]);
                correlation += diff;
                count++;
            }
            
            if (count > 0) {
                correlation /= count;
                if (correlation > maxCorr) {
                    maxCorr = correlation;
                    bestOffset = offset;
                }
            }
        }
        
        // Pour les filtres en cosinus surélevé, l'optimal est souvent
        // légèrement décalé par rapport au maximum théorique
        if (bestOffset < samplesPerSymbol / 4 || bestOffset > 3 * samplesPerSymbol / 4) {
            bestOffset = samplesPerSymbol / 2; // Forcer au centre si aberrant
        }
        
        return bestOffset;
    }
    
    /**
     * Calcule l'ouverture de l'œil pour un offset donné
     */
    private double calculateEyeOpening(double[] signal, int offset) {
        double minHigh = Double.MAX_VALUE;
        double maxLow = Double.MIN_VALUE;
        
        // Analyser les échantillons à cet offset
        for (int i = offset; i < signal.length; i += samplesPerSymbol) {
            double value = signal[i];
            if (value > 0) {
                minHigh = Math.min(minHigh, value);
            } else {
                maxLow = Math.max(maxLow, value);
            }
        }
        
        // L'ouverture est la distance entre les niveaux
        double opening = (minHigh != Double.MAX_VALUE && maxLow != Double.MIN_VALUE) 
                        ? minHigh - maxLow : 0;
        
        return opening;
    }
    
    /**
     * Méthode alternative : récupération par détection d'énergie maximale
     */
    public double[] recoverByMaxEnergy(double[] signal) {
        int numSymbols = signal.length / samplesPerSymbol;
        double[] recoveredSignal = new double[numSymbols];
        
        for (int i = 0; i < numSymbols; i++) {
            double maxEnergy = 0;
            int bestSample = i * samplesPerSymbol;
            
            // Trouver l'échantillon avec l'énergie maximale dans chaque symbole
            for (int j = 0; j < samplesPerSymbol; j++) {
                int idx = i * samplesPerSymbol + j;
                if (idx < signal.length) {
                    double energy = signal[idx] * signal[idx];
                    if (energy > maxEnergy) {
                        maxEnergy = energy;
                        bestSample = idx;
                    }
                }
            }
            
            recoveredSignal[i] = signal[bestSample];
        }
        
        return recoveredSignal;
    }
    
    /**
     * Active ou désactive le mode debug
     */
    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }
}