package com.transmissionnumerique.model;

public class TransmissionFilter {
    
    public enum FilterType {
        RECTANGULAR, RAISED_COSINE, ROOT_RAISED_COSINE
    }

    private FilterType filterType;
    private int samplesPerSymbol;
    private double rollOff;

    public TransmissionFilter(FilterType filterType, int samplesPerSymbol, double rollOff) {
        this.filterType = filterType;
        this.samplesPerSymbol = samplesPerSymbol;
        this.rollOff = rollOff;
    }

    public double[] filter(double[] signal) {
        switch (filterType) {
            case RECTANGULAR:
                return rectangularFilter(signal);
            case RAISED_COSINE:
                return raisedCosineFilter(signal);
            case ROOT_RAISED_COSINE:
                return rootRaisedCosineFilter(signal);
            default:
                return rectangularFilter(signal);
        }
    }

    private double[] rectangularFilter(double[] signal) {
        double[] output = new double[signal.length * samplesPerSymbol];
        
        for (int i = 0; i < signal.length; i++) {
            for (int j = 0; j < samplesPerSymbol; j++) {
                output[i * samplesPerSymbol + j] = signal[i];
            }
        }
        
        return output;
    }

    private double[] raisedCosineFilter(double[] signal) {
        double[] upsampled = new double[signal.length * samplesPerSymbol];
        for (int i = 0; i < signal.length; i++) {
            upsampled[i * samplesPerSymbol] = signal[i];
        }
        
        int filterLength = 6 * samplesPerSymbol + 1;
        double[] h = generateRaisedCosineImpulseResponse(filterLength);
        
        return convolve(upsampled, h);
    }

    private double[] rootRaisedCosineFilter(double[] signal) {
        double[] upsampled = new double[signal.length * samplesPerSymbol];
        for (int i = 0; i < signal.length; i++) {
            upsampled[i * samplesPerSymbol] = signal[i];
        }
        
        int filterLength = 6 * samplesPerSymbol + 1;
        double[] h = generateRootRaisedCosineImpulseResponse(filterLength);
        
        return convolve(upsampled, h);
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
        
        if (filterType != FilterType.RECTANGULAR) {
            int delay = filter.length / 2;
            double[] trimmedOutput = new double[signal.length];
            
            for (int i = 0; i < signal.length; i++) {
                if (i + delay < output.length) {
                    trimmedOutput[i] = output[i + delay];
                }
            }
            
            return trimmedOutput;
        }
        
        return output;
    }

    private double[] normalize(double[] filter) {
        double sumSquares = 0;
        for (double value : filter) {
            sumSquares += value * value;
        }
        
        double normFactor = Math.sqrt(sumSquares);
        
        if (normFactor > 0) {
            for (int i = 0; i < filter.length; i++) {
                filter[i] /= normFactor;
            }
        }
        
        double gainCompensation = Math.sqrt(samplesPerSymbol);
        for (int i = 0; i < filter.length; i++) {
            filter[i] *= gainCompensation;
        }
        
        return filter;
    }

    public double[] getFrequencyResponse(int numPoints) {
        double[] h;
        int filterLength = 6 * samplesPerSymbol + 1;
        
        switch (filterType) {
            case RAISED_COSINE:
                h = generateRaisedCosineImpulseResponse(filterLength);
                break;
            case ROOT_RAISED_COSINE:
                h = generateRootRaisedCosineImpulseResponse(filterLength);
                break;
            default:
                h = new double[samplesPerSymbol];
                for (int i = 0; i < samplesPerSymbol; i++) {
                    h[i] = 1.0 / samplesPerSymbol;
                }
        }
        
        double[] magnitude = new double[numPoints];
        for (int k = 0; k < numPoints; k++) {
            double real = 0, imag = 0;
            double omega = 2 * Math.PI * k / numPoints;
            
            for (int n = 0; n < h.length; n++) {
                real += h[n] * Math.cos(omega * n);
                imag -= h[n] * Math.sin(omega * n);
            }
            
            magnitude[k] = Math.sqrt(real * real + imag * imag);
        }
        
        return magnitude;
    }
}