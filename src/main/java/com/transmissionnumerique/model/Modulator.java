package com.transmissionnumerique.model;

public class Modulator {
    
    // ENUM REQUIS POUR L'INTERFACE
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
                return modulateImprovedASK(signal);
            case FSK:
                return modulateImprovedFSK(signal);
            case PSK:
                return modulateImprovedPSK(signal);
            default:
                return modulateImprovedASK(signal);
        }
    }

    // MODULATION ASK AMÉLIORÉE avec meilleure distinction
    private double[] modulateImprovedASK(double[] signal) {
        double[] modulatedSignal = new double[signal.length];
        
        // Détecter si on a un signal AMI (présence de valeurs proches de 0)
        boolean isAMI = false;
        int zeroCount = 0;
        for (double value : signal) {
            if (Math.abs(value) < 0.1) {
                zeroCount++;
            }
        }
        isAMI = zeroCount > signal.length / 10; // Plus de 10% de zéros
        
        if (isAMI) {
            // Modulation ASK à 3 niveaux pour AMI
            System.out.println("Modulation ASK pour signal AMI détectée");
            
            for (int i = 0; i < signal.length; i++) {
                if (Math.abs(signal[i]) < 0.1) {
                    // Niveau 0 (bit 0) -> amplitude faible
                    modulatedSignal[i] = 0.0;
                } else if (signal[i] > 0) {
                    // Niveau +1 (bit 1, polarité positive) -> amplitude haute
                    modulatedSignal[i] = 0.8;
                } else {
                    // Niveau -1 (bit 1, polarité négative) -> amplitude basse
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
            
            System.out.println("Modulation ASK standard avec amplitudes : " + lowAmplitude + " et " + highAmplitude);
        }
        
        return modulatedSignal;
    }

    // MODULATION PSK AMÉLIORÉE
    private double[] modulateImprovedPSK(double[] signal) {
        double[] modulatedSignal = new double[signal.length];
        
        // Amplitudes fixes mais phases opposées
        double amplitude = 1.0;
        
        for (int i = 0; i < signal.length; i++) {
            // Phase 0° pour bit 1, phase 180° pour bit 0
            modulatedSignal[i] = signal[i] > 0 ? amplitude : -amplitude;
        }
        
        return modulatedSignal;
    }

    // MODULATION FSK AMÉLIORÉE
    private double[] modulateImprovedFSK(double[] signal) {
        double[] modulatedSignal = new double[signal.length];
        
        // Valeurs représentant différentes fréquences
        double freq1 = 0.7;  // Fréquence pour bit 1
        double freq0 = -0.7; // Fréquence pour bit 0
        
        for (int i = 0; i < signal.length; i++) {
            modulatedSignal[i] = signal[i] > 0 ? freq1 : freq0;
        }
        
        return modulatedSignal;
    }

    public ModulationType getModulationType() {
        return modulationType;
    }
}