package com.transmissionnumerique.model;

public class Modulator {

    public enum ModulationType {
        ASK, FSK, PSK, QPSK, QAM
    }

    private ModulationType modulationType;
    private double carrierFrequency;
    private double sampleRate;

    public Modulator(ModulationType modulationType, double carrierFrequency, double sampleRate) {
        this.modulationType = modulationType;
        this.carrierFrequency = carrierFrequency;
        this.sampleRate = sampleRate;
    }

    public double[] modulate(double[] signal) {
        switch (modulationType) {
            case ASK:
                return modulateASK(signal);
            case FSK:
                return modulateFSK(signal);
            case PSK:
                return modulatePSK(signal);
            case QPSK:
                return modulateQPSK(signal);
            case QAM:
                return modulateQAM(signal);
            default:
                return modulateASK(signal);
        }
    }

    private double[] modulateASK(double[] signal) {
        double[] modulatedSignal = new double[signal.length];
        double timeStep = 1.0 / sampleRate;

        for (int i = 0; i < signal.length; i++) {
            double time = i * timeStep;
            double amplitude = 0.5 * (signal[i] + 1.0); // Normalisation entre 0 et 1
            modulatedSignal[i] = amplitude * Math.sin(2 * Math.PI * carrierFrequency * time);
        }

        return modulatedSignal;
    }

    private double[] modulateFSK(double[] signal) {
        double[] modulatedSignal = new double[signal.length];
        double timeStep = 1.0 / sampleRate;
        double frequencyDeviation = carrierFrequency * 0.1; // 10% de déviation

        for (int i = 0; i < signal.length; i++) {
            double time = i * timeStep;
            double instantFrequency = carrierFrequency + signal[i] * frequencyDeviation;
            modulatedSignal[i] = Math.sin(2 * Math.PI * instantFrequency * time);
        }

        return modulatedSignal;
    }

    private double[] modulatePSK(double[] signal) {
        double[] modulatedSignal = new double[signal.length];
        double timeStep = 1.0 / sampleRate;

        for (int i = 0; i < signal.length; i++) {
            double time = i * timeStep;
            double phase = signal[i] > 0 ? 0 : Math.PI;
            modulatedSignal[i] = Math.sin(2 * Math.PI * carrierFrequency * time + phase);
        }

        return modulatedSignal;
    }

    private double[] modulateQPSK(double[] signal) {
        // Simplification: nous ne gérons pas la conversion en symboles 2 bits
        return modulatePSK(signal);
    }

    private double[] modulateQAM(double[] signal) {
        // Simplification: implémentation basique de QAM
        return modulatePSK(signal);
    }
}