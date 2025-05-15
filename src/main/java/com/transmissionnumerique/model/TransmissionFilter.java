package com.transmissionnumerique.model;

public class TransmissionFilter {

    public enum FilterType {
        RECTANGULAR, RAISED_COSINE, ROOT_RAISED_COSINE
    }

    private FilterType filterType;
    private int sampleRate;
    private double rollOff;

    public TransmissionFilter(FilterType filterType, int sampleRate, double rollOff) {
        this.filterType = filterType;
        this.sampleRate = sampleRate;
        this.rollOff = rollOff;
    }

    public double[] filter(double[] signal) {
        switch (filterType) {
            case RECTANGULAR:
                return rectangularFilter(signal);
            case RAISED_COSINE:
                return raisedCosineFilter(signal);
            case ROOT_RAISED_COSINE:
                return rootRaisedCosineFilter(signal);
            default:
                return rectangularFilter(signal);
        }
    }

    private double[] rectangularFilter(double[] signal) {
        // Pour simplifier, on renvoie le signal sans modification
        return signal.clone();
    }

    private double[] raisedCosineFilter(double[] signal) {
        // Implémentation d'un filtre en cosinus surélevé
        // Cela nécessite une convolution avec la réponse impulsionnelle du filtre

        // Version simplifiée
        double[] output = new double[signal.length];
        System.arraycopy(signal, 0, output, 0, signal.length);
        return output;
    }

    private double[] rootRaisedCosineFilter(double[] signal) {
        // Implémentation d'un filtre en racine de cosinus surélevé

        // Version simplifiée
        double[] output = new double[signal.length];
        System.arraycopy(signal, 0, output, 0, signal.length);
        return output;
    }
}