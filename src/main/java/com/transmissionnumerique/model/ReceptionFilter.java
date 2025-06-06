package com.transmissionnumerique.model;

public class ReceptionFilter {

    private TransmissionFilter.FilterType filterType;
    private int sampleRate;
    private double rollOff;
    private int samplesPerSymbol = 4;
    private LineEncoder.EncodingType encodingType = LineEncoder.EncodingType.NRZ;

    public ReceptionFilter(TransmissionFilter.FilterType filterType, int sampleRate, double rollOff) {
        this.filterType = filterType;
        this.sampleRate = sampleRate;
        this.rollOff = rollOff;
    }

    public double[] filter(double[] signal) {
        switch (filterType) {
            case RECTANGULAR:
                return matchedRectangularFilter(signal);
            case RAISED_COSINE:
                return matchedRectangularFilter(signal); // Simplification
            case ROOT_RAISED_COSINE:
                return matchedRectangularFilter(signal); // Simplification
            default:
                return matchedRectangularFilter(signal);
        }
    }
    
    // Filtre adapté rectangulaire avec moyennage adaptatif
    private double[] matchedRectangularFilter(double[] signal) {
        // Pour AMI et HDB3, pas de moyennage car cela détruit les niveaux
        if (encodingType == LineEncoder.EncodingType.AMI || 
            encodingType == LineEncoder.EncodingType.HDB3) {
            System.out.println("Filtre de réception : pas de moyennage pour AMI/HDB3");
            return signal.clone(); // Retourner le signal sans modification
        }
        
        // Pour les autres encodages, moyennage normal
        double[] filtered = new double[signal.length];
        int windowSize = 3; // Fenêtre de moyennage
        
        for (int i = 0; i < signal.length; i++) {
            double sum = 0;
            int count = 0;
            
            // Moyennage sur une fenêtre centrée
            for (int j = -windowSize/2; j <= windowSize/2; j++) {
                int index = i + j;
                if (index >= 0 && index < signal.length) {
                    sum += signal[index];
                    count++;
                }
            }
            
            filtered[i] = sum / count;
        }
        
        System.out.println("Filtre de réception : moyennage avec fenêtre de " + windowSize);
        
        return filtered;
    }
    
    // Nouvelle méthode pour définir le type d'encodage
    public void setEncodingType(LineEncoder.EncodingType encodingType) {
        this.encodingType = encodingType;
    }
}