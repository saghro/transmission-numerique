package com.transmissionnumerique.model;

public class ClockRecovery {

    private int samplesPerSymbol;

    public ClockRecovery(int samplesPerSymbol) {
        this.samplesPerSymbol = samplesPerSymbol;
    }

    public double[] recover(double[] signal) {
        // Méthode améliorée de récupération d'horloge
        // Nous devons garantir que le nombre de symboles correspond à la séquence d'origine

        int numSymbols = signal.length / samplesPerSymbol;
        double[] recoveredSignal = new double[numSymbols];

        for (int i = 0; i < numSymbols; i++) {
            // Prendre l'échantillon au milieu de chaque symbole
            int sampleIndex = i * samplesPerSymbol + samplesPerSymbol / 2;
            if (sampleIndex < signal.length) {
                recoveredSignal[i] = signal[sampleIndex];
            }
        }

        return recoveredSignal;
    }
}