package com.transmissionnumerique.model;

public class ReceptionFilter {

    private TransmissionFilter.FilterType filterType;
    private int samplesPerSymbol;
    private double rollOff;
    private LineEncoder.EncodingType encodingType = LineEncoder.EncodingType.NRZ;

    public ReceptionFilter(TransmissionFilter.FilterType filterType, int samplesPerSymbol, double rollOff) {
        this.filterType = filterType;
        this.samplesPerSymbol = samplesPerSymbol;
        this.rollOff = rollOff;
    }

    public double[] filter(double[] signal) {
        switch (filterType) {
            case RECTANGULAR:
                return matchedRectangularFilter(signal);
            case RAISED_COSINE:
                return matchedRaisedCosineFilter(signal);
            case ROOT_RAISED_COSINE:
                return matchedRootRaisedCosineFilter(signal);
            default:
                return matchedRectangularFilter(signal);
        }
    }
    
    private double[] matchedRectangularFilter(double[] signal) {
        if (encodingType == LineEncoder.EncodingType.AMI || 
            encodingType == LineEncoder.EncodingType.HDB3) {
            return applyMinimalFiltering(signal);
        }
        
        double[] filtered = new double[signal.length];
        int windowSize = samplesPerSymbol / 2;
        
        for (int i = 0; i < signal.length; i++) {
            double sum = 0;
            int count = 0;
            
            for (int j = -windowSize/2; j <= windowSize/2; j++) {
                int index = i + j;
                if (index >= 0 && index < signal.length) {
                    sum += signal[index];
                    count++;
                }
            }
            
            filtered[i] = sum / count;
        }
        
        return filtered;
    }

    private double[] matchedRaisedCosineFilter(double[] signal) {
        int filterLength = 6 * samplesPerSymbol + 1;
        double[] h = generateRaisedCosineImpulseResponse(filterLength);
        
        double[] matchedFilter = new double[h.length];
        for (int i = 0; i < h.length; i++) {
            matchedFilter[i] = h[h.length - 1 - i];
        }
        
        return convolve(signal, matchedFilter);
    }

    private double[] matchedRootRaisedCosineFilter(double[] signal) {
        int filterLength = 6 * samplesPerSymbol + 1;
        double[] h = generateRootRaisedCosineImpulseResponse(filterLength);
        
        return convolve(signal, h);
    }

    private double[] applyMinimalFiltering(double[] signal) {
        double[] filtered = new double[signal.length];
        double alpha = 0.8;
        
        filtered[0] = signal[0];
        for (int i = 1; i < signal.length; i++) {
            filtered[i] = alpha * signal[i] + (1 - alpha) * filtered[i-1];
        }
        
        return filtered;
    }

    private double[] generateRaisedCosineImpulseResponse(int length) {
        double[] h = new double[length];
        int center = length / 2;
        
        for (int i = 0; i < length; i++) {
            double t = (i - center) / (double)samplesPerSymbol;
            
            if (Math.abs(t) < 1e-10) {
                h[i] = 1.0;
            } else if (Math.abs(Math.abs(t) - 1.0/(2.0*rollOff)) < 1e-10) {
                h[i] = (Math.PI/4.0) * sinc(1.0/(2.0*rollOff));
            } else {
                double numerator = sinc(t) * Math.cos(Math.PI * rollOff * t);
                double denominator = 1.0 - Math.pow(2.0 * rollOff * t, 2);
                h[i] = numerator / denominator;
            }
        }
        
        return normalize(h);
    }

    private double[] generateRootRaisedCosineImpulseResponse(int length) {
        double[] h = new double[length];
        int center = length / 2;
        
        for (int i = 0; i < length; i++) {
            double t = (i - center) / (double)samplesPerSymbol;
            
            if (Math.abs(t) < 1e-10) {
                h[i] = 1.0 - rollOff + 4.0 * rollOff / Math.PI;
            } else if (Math.abs(Math.abs(t) - 1.0/(4.0*rollOff)) < 1e-10) {
                double factor = rollOff / Math.sqrt(2.0);
                h[i] = factor * ((1.0 + 2.0/Math.PI) * Math.sin(Math.PI/(4.0*rollOff)) + 
                               (1.0 - 2.0/Math.PI) * Math.cos(Math.PI/(4.0*rollOff)));
            } else {
                double sin_part = Math.sin(Math.PI * t * (1.0 - rollOff));
                double cos_part = Math.cos(Math.PI * t * (1.0 + rollOff));
                double numerator = sin_part + 4.0 * rollOff * t * cos_part;
                double denominator = Math.PI * t * (1.0 - Math.pow(4.0 * rollOff * t, 2));
                h[i] = numerator / denominator;
            }
        }
        
        return normalize(h);
    }

    private double sinc(double x) {
        if (Math.abs(x) < 1e-10) {
            return 1.0;
        }
        return Math.sin(Math.PI * x) / (Math.PI * x);
    }

    private double[] convolve(double[] signal, double[] filter) {
        int outputLength = signal.length + filter.length - 1;
        double[] output = new double[outputLength];
        
        for (int n = 0; n < outputLength; n++) {
            output[n] = 0;
            for (int k = 0; k < filter.length; k++) {
                if (n - k >= 0 && n - k < signal.length) {
                    output[n] += signal[n - k] * filter[k];
                }
            }
        }
        
        int delay = filter.length / 2;
        double[] compensatedOutput = new double[signal.length];
        
        for (int i = 0; i < signal.length; i++) {
            if (i + delay < outputLength) {
                compensatedOutput[i] = output[i + delay];
            }
        }
        
        return compensatedOutput;
    }

    private double[] normalize(double[] filter) {
        double energy = 0;
        for (double value : filter) {
            energy += value * value;  // Calcul de l'énergie
        }
        
        double norm = Math.sqrt(energy);
        if (norm > 0) {
            for (int i = 0; i < filter.length; i++) {
                filter[i] /= norm;
            }
        }
        return filter;
    }

    public void setEncodingType(LineEncoder.EncodingType encodingType) {
        this.encodingType = encodingType;
    }
}