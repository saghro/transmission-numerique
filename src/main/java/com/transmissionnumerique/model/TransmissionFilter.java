package com.transmissionnumerique.model;

public class TransmissionFilter {
    
    public enum FilterType {
        RECTANGULAR, RAISED_COSINE, ROOT_RAISED_COSINE
    }

    private FilterType filterType;
    private int samplesPerSymbol;

    public TransmissionFilter(FilterType filterType, int sampleRate, double rollOff) {
        this.filterType = filterType;
        this.samplesPerSymbol = 4; // Cohérent avec ClockRecovery
    }

    public double[] filter(double[] signal) {
        switch (filterType) {
            case RECTANGULAR:
                return ultraSimpleRectangularFilter(signal);
            case RAISED_COSINE:
                return ultraSimpleRectangularFilter(signal); // Même chose pour l'instant
            case ROOT_RAISED_COSINE:
                return ultraSimpleRectangularFilter(signal); // Même chose pour l'instant
            default:
                return ultraSimpleRectangularFilter(signal);
        }
    }

    // FILTRE ULTRA-SIMPLE pour éliminer les problèmes de synchronisation
    private double[] ultraSimpleRectangularFilter(double[] signal) {
        double[] output = new double[signal.length * samplesPerSymbol];
        
        // Suréchantillonnage parfait sans interpolation
        for (int i = 0; i < signal.length; i++) {
            for (int j = 0; j < samplesPerSymbol; j++) {
                output[i * samplesPerSymbol + j] = signal[i];
            }
        }
        
        return output;
    }
}