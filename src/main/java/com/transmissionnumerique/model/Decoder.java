package com.transmissionnumerique.model;

public class Decoder {
    private double threshold;
    private Modulator.ModulationType modulationType;
    private LineEncoder.EncodingType encodingType;
   
    public Decoder(double threshold) {
        this.threshold = threshold;
        this.modulationType = Modulator.ModulationType.PSK;
        this.encodingType = LineEncoder.EncodingType.NRZ;
    }
    
    public boolean[] decode(double[] signal) {
        // DIAGNOSTIC
        System.out.println("=== DÉCODAGE ===");
        System.out.println("Longueur du signal à décoder : " + signal.length);
        System.out.println("Type d'encodage : " + encodingType);
        System.out.println("Type de modulation : " + modulationType);
        System.out.println("Premières valeurs du signal :");
        for (int i = 0; i < Math.min(10, signal.length); i++) {
            System.out.println("  Signal[" + i + "] = " + signal[i]);
        }
        
        // Décodage spécialisé selon le type d'encodage
        boolean[] decodedBits;
        
        switch (encodingType) {
            case AMI:
                decodedBits = decodeAMI(signal);
                break;
                
            case MANCHESTER:
                // D'abord décoder les symboles, puis les convertir
                boolean[] symbols = decodeSymbols(signal);
                decodedBits = decodeManchesterSymbols(symbols);
                break;
                
            case HDB3:
                // HDB3 est similaire à AMI
                decodedBits = decodeAMI(signal);
                break;
                
            case NRZ:
            default:
                // Décodage standard pour NRZ
                decodedBits = decodeSymbols(signal);
                break;
        }
        
        return decodedBits;
    }
    
    // Décodage spécifique pour AMI
    private boolean[] decodeAMI(double[] signal) {
        System.out.println("\nDécodage AMI spécialisé");
        boolean[] bits = new boolean[signal.length];
        
        // Pour AMI : niveau proche de 0 = bit 0, niveau éloigné de 0 = bit 1
        // On utilise un seuil sur la valeur absolue
        double noiseMargin = 0.3; // Marge pour le bruit autour de 0
        
        // Calculer le niveau moyen des "1" pour adapter le seuil
        double sumAbs = 0;
        int count = 0;
        for (double value : signal) {
            if (Math.abs(value) > noiseMargin) {
                sumAbs += Math.abs(value);
                count++;
            }
        }
        
        double avgLevel = count > 0 ? sumAbs / count : 0.5;
        double amiThreshold = avgLevel * 0.5; // Seuil à mi-chemin
        
        System.out.println("Seuil AMI calculé : " + amiThreshold);
        
        // Décodage
        for (int i = 0; i < signal.length; i++) {
            // Bit 1 si la valeur absolue dépasse le seuil
            bits[i] = Math.abs(signal[i]) > amiThreshold;
            
            if (i < 10) {
                System.out.println("  |Signal[" + i + "]| = " + Math.abs(signal[i]) + 
                                 " > " + amiThreshold + " = " + bits[i]);
            }
        }
        
        return bits;
    }
    
    // Décodage standard des symboles
    private boolean[] decodeSymbols(double[] signal) {
        boolean[] symbols = new boolean[signal.length];
        
        // Calcul du seuil adaptatif
        double adaptiveThreshold = calculateAdaptiveThreshold(signal);
        
        System.out.println("Décodage avec seuil adaptatif : " + adaptiveThreshold);
        
        // Décodage selon le type de modulation
        switch (modulationType) {
            case ASK:
                double askThreshold = calculateASKThreshold(signal);
                System.out.println("Seuil ASK calculé : " + askThreshold);
                
                for (int i = 0; i < signal.length; i++) {
                    symbols[i] = signal[i] > askThreshold;
                }
                break;
                
            case PSK:
            case FSK:
                // Pour PSK et FSK, le seuil est 0
                for (int i = 0; i < signal.length; i++) {
                    symbols[i] = signal[i] > 0.0;
                }
                break;
                
            default:
                for (int i = 0; i < signal.length; i++) {
                    symbols[i] = signal[i] > adaptiveThreshold;
                }
        }
        
        return symbols;
    }
    
    // Décodage des symboles Manchester en bits
    private boolean[] decodeManchesterSymbols(boolean[] symbols) {
        if (symbols.length % 2 != 0) {
            System.out.println("ATTENTION: Nombre impair de symboles Manchester!");
        }
        
        int numBits = symbols.length / 2;
        boolean[] bits = new boolean[numBits];
        
        for (int i = 0; i < numBits; i++) {
            int idx = i * 2;
            if (idx + 1 < symbols.length) {
                boolean firstSymbol = symbols[idx];
                boolean secondSymbol = symbols[idx + 1];
                
                // Bit 1 si transition descendante, bit 0 si transition montante
                if (firstSymbol && !secondSymbol) {
                    bits[i] = true;
                } else if (!firstSymbol && secondSymbol) {
                    bits[i] = false;
                } else {
                    // Pas de transition valide - utiliser le premier symbole
                    System.out.println("Erreur Manchester à la position " + i);
                    bits[i] = firstSymbol;
                }
            }
        }
        
        System.out.println("Décodage Manchester : " + symbols.length + " symboles -> " + bits.length + " bits");
        return bits;
    }
    
    private double calculateASKThreshold(double[] signal) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (double value : signal) {
            if (value < min) min = value;
            if (value > max) max = value;
        }
        
        double threshold = (min + max) / 2.0;
        System.out.println("ASK - Min: " + min + ", Max: " + max + ", Seuil: " + threshold);
        
        return threshold;
    }
    
    private double calculateAdaptiveThreshold(double[] signal) {
        double sum = 0;
        for (double value : signal) {
            sum += value;
        }
        return sum / signal.length;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setModulationType(Modulator.ModulationType modulationType) {
        this.modulationType = modulationType;
    }
    
    public void setEncodingType(LineEncoder.EncodingType encodingType) {
        this.encodingType = encodingType;
    }
}