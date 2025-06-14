package com.transmissionnumerique.model;

public class SimplifiedRaisedCosineFilter {
    
    private int samplesPerSymbol;
    private double rollOff;
    
    public SimplifiedRaisedCosineFilter(int samplesPerSymbol, double rollOff) {
        this.samplesPerSymbol = samplesPerSymbol;
        this.rollOff = rollOff;
    }
    
    public double[] filterWithEnergyPreservation(double[] symbols) {
        double[] upsampled = upsampleWithInterpolation(symbols);
        double[] filtered = applyRaisedCosineFilter(upsampled);
        filtered = compensateEnergy(filtered, symbols);
        
        return filtered;
    }
    
    private double[] upsampleWithInterpolation(double[] symbols) {
        double[] upsampled = new double[symbols.length * samplesPerSymbol];
        
        for (int i = 0; i < symbols.length; i++) {
            int baseIndex = i * samplesPerSymbol;
            upsampled[baseIndex + samplesPerSymbol/2] = symbols[i];
            
            if (i < symbols.length - 1) {
                double slope = (symbols[i+1] - symbols[i]) / samplesPerSymbol;
                for (int j = 0; j < samplesPerSymbol; j++) {
                    if (j != samplesPerSymbol/2) {
                        upsampled[baseIndex + j] = symbols[i] + slope * (j - samplesPerSymbol/2);
                    }
                }
            }
        }
        
        return upsampled;
    }
    
    private double[] applyRaisedCosineFilter(double[] signal) {
        int filterLength = 6 * samplesPerSymbol + 1;
        double[] h = createSimpleRCFilter(filterLength);
        
        double[] filtered = new double[signal.length];
        int halfFilter = filterLength / 2;
        
        for (int n = 0; n < signal.length; n++) {
            double sum = 0;
            for (int k = 0; k < filterLength; k++) {
                int idx = n - halfFilter + k;
                if (idx >= 0 && idx < signal.length) {
                    sum += signal[idx] * h[k];
                }
            }
            filtered[n] = sum;
        }
        
        return filtered;
    }
    
    private double[] createSimpleRCFilter(int length) {
        double[] h = new double[length];
        int center = length / 2;
        
        for (int i = 0; i < length; i++) {
            double t = (i - center) / (double)samplesPerSymbol;
            h[i] = raisedCosineImpulse(t);
        }
        
        double energy = 0;
        for (double val : h) {
            energy += val * val;
        }
        
        double normFactor = Math.sqrt(energy / samplesPerSymbol);
        for (int i = 0; i < h.length; i++) {
            h[i] /= normFactor;
        }
        
        return h;
    }
    
    private double raisedCosineImpulse(double t) {
        if (Math.abs(t) < 1e-10) {
            return 1.0;
        }
        
        double denominator = 1.0 - Math.pow(2.0 * rollOff * t, 2);
        if (Math.abs(denominator) < 1e-10) {
            return Math.PI / 4.0 * sinc(1.0 / (2.0 * rollOff));
        }
        
        return sinc(t) * Math.cos(Math.PI * rollOff * t) / denominator;
    }
    
    private double sinc(double x) {
        if (Math.abs(x) < 1e-10) {
            return 1.0;
        }
        return Math.sin(Math.PI * x) / (Math.PI * x);
    }
    
    private double[] compensateEnergy(double[] filtered, double[] original) {
        double originalEnergy = 0;
        for (double val : original) {
            originalEnergy += val * val;
        }
        originalEnergy /= original.length;
        
        double filteredEnergy = 0;
        for (double val : filtered) {
            filteredEnergy += val * val;
        }
        filteredEnergy /= filtered.length;
        
        double compensationFactor = 1.0;
        if (filteredEnergy > 0) {
            compensationFactor = Math.sqrt(originalEnergy / filteredEnergy);
        }
        
        double[] compensated = new double[filtered.length];
        for (int i = 0; i < filtered.length; i++) {
            compensated[i] = filtered[i] * compensationFactor;
        }
        
        return compensated;
    }
}