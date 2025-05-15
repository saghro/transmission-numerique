package com.transmissionnumerique.model;

public class ReceptionFilter {

    private TransmissionFilter.FilterType filterType;
    private int sampleRate;
    private double rollOff;

    public ReceptionFilter(TransmissionFilter.FilterType filterType, int sampleRate, double rollOff) {
        this.filterType = filterType;
        this.sampleRate = sampleRate;
        this.rollOff = rollOff;
    }

    public double[] filter(double[] signal) {
        // Le filtre de réception est souvent adapté au filtre d'émission
        TransmissionFilter matchedFilter = new TransmissionFilter(filterType, sampleRate, rollOff);
        return matchedFilter.filter(signal);
    }
}