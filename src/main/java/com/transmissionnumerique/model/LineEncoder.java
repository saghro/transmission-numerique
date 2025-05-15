package com.transmissionnumerique.model;

public class LineEncoder {

    public enum EncodingType {
        NRZ, MANCHESTER, AMI, HDB3
    }

    private EncodingType encodingType;

    public LineEncoder(EncodingType encodingType) {
        this.encodingType = encodingType;
    }

    public double[] encode(boolean[] bits) {
        switch (encodingType) {
            case NRZ:
                return encodeNRZ(bits);
            case MANCHESTER:
                return encodeManchester(bits);
            case AMI:
                return encodeAMI(bits);
            case HDB3:
                return encodeHDB3(bits);
            default:
                return encodeNRZ(bits);
        }
    }

    private double[] encodeNRZ(boolean[] bits) {
        double[] signal = new double[bits.length];
        for (int i = 0; i < bits.length; i++) {
            signal[i] = bits[i] ? 1.0 : -1.0;
        }
        return signal;
    }

    private double[] encodeManchester(boolean[] bits) {
        double[] signal = new double[bits.length * 2];
        for (int i = 0; i < bits.length; i++) {
            if (bits[i]) {
                signal[2*i] = 1.0;
                signal[2*i+1] = -1.0;
            } else {
                signal[2*i] = -1.0;
                signal[2*i+1] = 1.0;
            }
        }
        return signal;
    }

    private double[] encodeAMI(boolean[] bits) {
        double[] signal = new double[bits.length];
        int lastPolarity = 1;

        for (int i = 0; i < bits.length; i++) {
            if (!bits[i]) {
                signal[i] = 0.0;
            } else {
                signal[i] = lastPolarity * 1.0;
                lastPolarity = -lastPolarity;
            }
        }
        return signal;
    }

    private double[] encodeHDB3(boolean[] bits) {
        // Implémentation plus complexe du codage HDB3
        // (Pour simplifier, nous ne l'implémentons pas complètement ici)
        return encodeAMI(bits);
    }
}