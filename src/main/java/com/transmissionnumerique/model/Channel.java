package com.transmissionnumerique.model;

import java.util.Random;

public class Channel {

    public enum NoiseType {
        AWGN, RAYLEIGH, RICIAN
    }

    private NoiseType noiseType;
    private double snr; // Signal-to-Noise Ratio en dB

    public Channel(NoiseType noiseType, double snr) {
        this.noiseType = noiseType;
        this.snr = snr;
    }

    public double[] transmit(double[] signal) {
        // Si SNR > 50 dB, ne pas ajouter de bruit
        if (snr > 50.0) {
            return signal.clone();
        }
        
        switch (noiseType) {
            case AWGN:
                return addAWGN(signal);
            case RAYLEIGH:
                return addRayleighFading(signal);
            case RICIAN:
                return addRicianFading(signal);
            default:
                return addAWGN(signal);
        }
    }

    private double[] addAWGN(double[] signal) {
        double[] noisySignal = new double[signal.length];

        // Calcul de la puissance du signal
        double signalPower = 0;
        for (double v : signal) {
            signalPower += v * v;
        }
        signalPower /= signal.length;
        
        // Calcul de la puissance du bruit basée sur le SNR
        double noisePower = signalPower / Math.pow(10, snr / 10);
        double noiseAmplitude = Math.sqrt(noisePower);

        // Ajout du bruit blanc gaussien
        Random random = new Random();
        for (int i = 0; i < signal.length; i++) {
            double noise = random.nextGaussian() * noiseAmplitude;
            noisySignal[i] = signal[i] + noise;
        }

        return noisySignal;
    }

    private double[] addRayleighFading(double[] signal) {
        Random random = new Random();
        double fadingFactor = 0.5 + 0.5 * random.nextDouble();

        double[] fadedSignal = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            fadedSignal[i] = signal[i] * fadingFactor;
        }

        return addAWGN(fadedSignal);
    }

    private double[] addRicianFading(double[] signal) {
        return addRayleighFading(signal);
    }
}