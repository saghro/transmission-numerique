package com.transmissionnumerique.model;

import java.util.*;

/**
 * Décodeur amélioré avec détection de seuil adaptatif
 */
public class Decoder {
    private double threshold;
    private Modulator.ModulationType modulationType;
    private LineEncoder.EncodingType encodingType;
    private boolean debugMode = true;
   
    public Decoder(double threshold) {
        this.threshold = threshold;
        this.modulationType = Modulator.ModulationType.PSK;
        this.encodingType = LineEncoder.EncodingType.NRZ;
    }
    
    public boolean[] decode(double[] signal) {
        if (debugMode) {
            System.out.println("\n=== DÉCODAGE ADAPTATIF ===");
            System.out.println("Longueur du signal à décoder : " + signal.length);
            System.out.println("Type d'encodage : " + encodingType);
            System.out.println("Type de modulation : " + modulationType);
        }
        
        // Calculer le seuil optimal avant décodage
        double optimalThreshold = calculateOptimalThreshold(signal);
        
        // Décodage spécialisé selon le type d'encodage
        boolean[] decodedBits;
        
        switch (encodingType) {
            case AMI:
                decodedBits = decodeAMI(signal);
                break;
                
            case MANCHESTER:
                // D'abord décoder les symboles, puis les convertir
                boolean[] symbols = decodeWithOptimalThreshold(signal, optimalThreshold);
                decodedBits = decodeManchesterSymbols(symbols);
                break;
                
            case HDB3:
                decodedBits = decodeAMI(signal); // Similaire à AMI
                break;
                
            case NRZ:
            default:
                decodedBits = decodeWithOptimalThreshold(signal, optimalThreshold);
                break;
        }
        
        if (debugMode) {
            System.out.println("Décodage terminé : " + decodedBits.length + " bits");
            System.out.println("=========================\n");
        }
        
        return decodedBits;
    }
    
    /**
     * Calcule le seuil optimal en utilisant l'algorithme de Otsu adapté
     */
    private double calculateOptimalThreshold(double[] signal) {
        // Utiliser l'algorithme K-means pour trouver deux clusters
        KMeansResult clusters = performKMeans(signal, 2);
        
        // Le seuil optimal est au milieu des deux centres
        double threshold = (clusters.centers[0] + clusters.centers[1]) / 2.0;
        
        if (debugMode) {
            System.out.println("\nAnalyse des clusters:");
            System.out.println("Centre cluster 0: " + clusters.centers[0]);
            System.out.println("Centre cluster 1: " + clusters.centers[1]);
            System.out.println("Seuil optimal calculé: " + threshold);
        }
        
        return threshold;
    }
    
    /**
     * Algorithme K-means simplifié pour 2 clusters
     */
    private KMeansResult performKMeans(double[] data, int k) {
        double[] centers = new double[k];
        
        // Initialisation : min et max
        double min = Arrays.stream(data).min().orElse(-1);
        double max = Arrays.stream(data).max().orElse(1);
        centers[0] = min + (max - min) * 0.25;
        centers[1] = min + (max - min) * 0.75;
        
        // Itérations K-means
        for (int iter = 0; iter < 10; iter++) {
            // Assigner chaque point au cluster le plus proche
            int[] assignments = new int[data.length];
            for (int i = 0; i < data.length; i++) {
                assignments[i] = Math.abs(data[i] - centers[0]) < Math.abs(data[i] - centers[1]) ? 0 : 1;
            }
            
            // Recalculer les centres
            double[] sums = new double[k];
            int[] counts = new int[k];
            
            for (int i = 0; i < data.length; i++) {
                sums[assignments[i]] += data[i];
                counts[assignments[i]]++;
            }
            
            // Mettre à jour les centres
            boolean changed = false;
            for (int j = 0; j < k; j++) {
                if (counts[j] > 0) {
                    double newCenter = sums[j] / counts[j];
                    if (Math.abs(newCenter - centers[j]) > 1e-6) {
                        changed = true;
                    }
                    centers[j] = newCenter;
                }
            }
            
            if (!changed) break;
        }
        
        // Trier les centres
        Arrays.sort(centers);
        
        return new KMeansResult(centers);
    }
    
    /**
     * Décodage avec seuil optimal
     */
    private boolean[] decodeWithOptimalThreshold(double[] signal, double threshold) {
        boolean[] bits = new boolean[signal.length];
        
        if (debugMode) {
            System.out.println("\nDécodage avec seuil: " + threshold);
            System.out.println("Premiers échantillons:");
        }
        
        for (int i = 0; i < signal.length; i++) {
            bits[i] = signal[i] > threshold;
            
            if (debugMode && i < 10) {
                System.out.printf("  Signal[%d] = %.4f > %.4f = %s\n", 
                                i, signal[i], threshold, bits[i]);
            }
        }
        
        return bits;
    }
    
    /**
     * Décodage spécifique pour AMI
     */
    private boolean[] decodeAMI(double[] signal) {
        System.out.println("\nDécodage AMI spécialisé");
        boolean[] bits = new boolean[signal.length];
        
        // Pour AMI, on utilise la valeur absolue avec un seuil adaptatif
        double[] absSignal = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            absSignal[i] = Math.abs(signal[i]);
        }
        
        // Trouver le seuil pour distinguer 0 des ±1
        double threshold = calculateOptimalThreshold(absSignal);
        
        // Décodage : bit 1 si |signal| > threshold
        for (int i = 0; i < signal.length; i++) {
            bits[i] = absSignal[i] > threshold;
        }
        
        return bits;
    }
    
    /**
     * Décodage des symboles Manchester en bits
     */
    private boolean[] decodeManchesterSymbols(boolean[] symbols) {
        if (symbols.length % 2 != 0) {
            // Ajout d'un zéro pour corriger l'impair
            boolean[] padded = Arrays.copyOf(symbols, symbols.length + 1);
            symbols = padded;
        }

        int numBits = symbols.length / 2;
        boolean[] bits = new boolean[numBits];
        double threshold = 0.2;  // Seuil de tolérance

        for (int i = 0; i < numBits; i++) {
            int idx = i * 2;
            boolean s1 = symbols[idx];
            boolean s2 = symbols[idx + 1];
            
            // Décision robuste avec seuil
            if (s1 && !s2) bits[i] = true;    // 10 -> 1
            else if (!s1 && s2) bits[i] = false; // 01 -> 0
            else {
                // Cas ambigu: utilisation du niveau moyen
                bits[i] = (i > 0) ? bits[i-1] : false;  // Héritage du bit précédent
            }
        }
        return bits;
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
    
    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }
    
    /**
     * Classe interne pour stocker le résultat K-means
     */
    private static class KMeansResult {
        final double[] centers;
        
        KMeansResult(double[] centers) {
            this.centers = centers;
        }
    }
}