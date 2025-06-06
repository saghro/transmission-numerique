package com.transmissionnumerique.model;

public class Demodulator {
    private Modulator.ModulationType modulationType;

    public Demodulator(Modulator.ModulationType modulationType, double carrierFrequency, double sampleRate) {
        this.modulationType = modulationType;
    }

    public double[] demodulate(double[] signal) {
        switch (modulationType) {
            case ASK:
                return demodulateSimpleASK(signal);
            case FSK:
                return demodulateSimpleFSK(signal);
            case PSK:
                return demodulateSimplePSK(signal);
            default:
                return demodulateSimpleASK(signal);
        }
    }

    // DÉMODULATION ASK TRÈS SIMPLE
    private double[] demodulateSimpleASK(double[] signal) {
        // Retourne le signal tel quel - la démodulation se fera dans le décodeur
        return signal.clone();
    }

    // DÉMODULATION PSK TRÈS SIMPLE
    private double[] demodulateSimplePSK(double[] signal) {
        // Retourne le signal tel quel
        return signal.clone();
    }

    // DÉMODULATION FSK TRÈS SIMPLE
    private double[] demodulateSimpleFSK(double[] signal) {
        // Retourne le signal tel quel
        return signal.clone();
    }
}