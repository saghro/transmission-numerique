package com.transmissionnumerique.model;

public class Demodulator {

    private Modulator.ModulationType modulationType;
    private double carrierFrequency;
    private double sampleRate;

    public Demodulator(Modulator.ModulationType modulationType, double carrierFrequency, double sampleRate) {
        this.modulationType = modulationType;
        this.carrierFrequency = carrierFrequency;
        this.sampleRate = sampleRate;
    }

    public double[] demodulate(double[] signal) {
        switch (modulationType) {
            case ASK:
                return demodulateASK(signal);
            case FSK:
                return demodulateFSK(signal);
            case PSK:
                return demodulateNonCoherentPSK(signal);
            case QPSK:
                return demodulateQPSK(signal);
            case QAM:
                return demodulateQAM(signal);
            default:
                return demodulateASK(signal);
        }
    }

    private double[] demodulateASK(double[] signal) {
        double[] demodulatedSignal = new double[signal.length];
        double timeStep = 1.0 / sampleRate;

        for (int i = 0; i < signal.length; i++) {
            double time = i * timeStep;
            // Démodulation par détection d'enveloppe simplifiée
            double referenceSignal = Math.sin(2 * Math.PI * carrierFrequency * time);
            demodulatedSignal[i] = signal[i] * referenceSignal;
        }

        // Filtrage passe-bas simplifié (moyenne mobile)
        int windowSize = (int) (sampleRate / carrierFrequency);
        double[] filteredSignal = new double[signal.length];

        for (int i = 0; i < signal.length; i++) {
            double sum = 0;
            int count = 0;

            for (int j = Math.max(0, i - windowSize / 2); j < Math.min(signal.length, i + windowSize / 2); j++) {
                sum += demodulatedSignal[j];
                count++;
            }

            filteredSignal[i] = sum / count;
        }

        return filteredSignal;
    }

    private double[] demodulateFSK(double[] signal) {
        // Démodulation FSK simplifiée
        return demodulateASK(signal);
    }

    private double[] demodulateNonCoherentPSK(double[] signal) {
        double[] demodulatedSignal = new double[signal.length];
        double timeStep = 1.0 / sampleRate;

        for (int i = 0; i < signal.length; i++) {
            double time = i * timeStep;
            // Démodulation PSK non-cohérente simplifiée
            double inPhase = signal[i] * Math.cos(2 * Math.PI * carrierFrequency * time);
            double quadrature = signal[i] * Math.sin(2 * Math.PI * carrierFrequency * time);
            demodulatedSignal[i] = inPhase;
        }

        return demodulatedSignal;
    }

    private double[] demodulateQPSK(double[] signal) {
        // Simplification
        return demodulateNonCoherentPSK(signal);
    }

    private double[] demodulateQAM(double[] signal) {
        // Simplification
        return demodulateNonCoherentPSK(signal);
    }
}