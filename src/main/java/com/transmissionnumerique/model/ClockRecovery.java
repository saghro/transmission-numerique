package com.transmissionnumerique.model;

public class ClockRecovery {
    private int samplesPerSymbol;
    
    public ClockRecovery(int samplesPerSymbol) {
        this.samplesPerSymbol = 4; // Fixe, cohérent avec TransmissionFilter
    }

    public double[] recover(double[] signal) {
        if (signal.length < samplesPerSymbol) {
            return signal;
        }
        
        int numSymbols = signal.length / samplesPerSymbol;
        double[] recoveredSignal = new double[numSymbols];
        
        // AMÉLIORATION : Trouver le meilleur instant d'échantillonnage
        // en utilisant la valeur maximale absolue dans chaque symbole
        for (int i = 0; i < numSymbols; i++) {
            double maxAbsValue = 0;
            int bestSampleIndex = i * samplesPerSymbol;
            
            // Chercher le meilleur échantillon dans la fenêtre du symbole
            for (int j = 0; j < samplesPerSymbol; j++) {
                int index = i * samplesPerSymbol + j;
                if (index < signal.length) {
                    double absValue = Math.abs(signal[index]);
                    if (absValue > maxAbsValue) {
                        maxAbsValue = absValue;
                        bestSampleIndex = index;
                    }
                }
            }
            
            // Utiliser l'échantillon avec la valeur absolue maximale
            if (bestSampleIndex < signal.length) {
                recoveredSignal[i] = signal[bestSampleIndex];
            }
        }
        
        System.out.println("Récupération d'horloge adaptative : " + numSymbols + " symboles récupérés");
        
        return recoveredSignal;
    }
}