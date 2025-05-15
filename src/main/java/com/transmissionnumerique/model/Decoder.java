package com.transmissionnumerique.model;

public class Decoder {

    private double threshold;

    public Decoder(double threshold) {
        this.threshold = threshold;
    }

    public boolean[] decode(double[] signal) {
        boolean[] decodedBits = new boolean[signal.length];

        for (int i = 0; i < signal.length; i++) {
            decodedBits[i] = signal[i] > threshold;
        }

        return decodedBits;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}