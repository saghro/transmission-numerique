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
            default:
                return modulateASK(signal);
        }
    }

    private double[] modulateASK(double[] signal) {
        double[] modulatedSignal = new double[signal.length];
        
        // Détecter si on a un signal AMI (présence de valeurs proches de 0)
        boolean isAMI = false;
        int zeroCount = 0;
        for (double value : signal) {
            if (Math.abs(value) < 0.1) {
                zeroCount++;
            }
        }
        isAMI = zeroCount > signal.length / 10;
        
        if (isAMI) {
            // Modulation ASK à 3 niveaux pour AMI
            for (int i = 0; i < signal.length; i++) {
                if (Math.abs(signal[i]) < 0.1) {
                    modulatedSignal[i] = 0.0;
                } else if (signal[i] > 0) {
                    modulatedSignal[i] = 0.8;
                } else {
                    modulatedSignal[i] = -0.8;
                }
            }
        } else {
            // Modulation ASK standard pour NRZ
            double lowAmplitude = -0.8;
            double highAmplitude = 0.8;
            
            for (int i = 0; i < signal.length; i++) {
                if (signal[i] > 0) {
                    modulatedSignal[i] = highAmplitude;
                } else {
                    modulatedSignal[i] = lowAmplitude;
                }
            }
        }
        
        return modulatedSignal;
    }

    private double[] modulatePSK(double[] signal) {
        double[] modulatedSignal = new double[signal.length];
        double amplitude = 1.0;
        
        for (int i = 0; i < signal.length; i++) {
            modulatedSignal[i] = signal[i] > 0 ? amplitude : -amplitude;
        }
        
        return modulatedSignal;
    }

    private double[] modulateFSK(double[] signal) {
        double[] modulatedSignal = new double[signal.length];
        double freq1 = 0.7;
        double freq0 = -0.7;
        
        for (int i = 0; i < signal.length; i++) {
            modulatedSignal[i] = signal[i] > 0 ? freq1 : freq0;
        }
        
        return modulatedSignal;
    }

    public ModulationType getModulationType() {
        return modulationType;
    }
}