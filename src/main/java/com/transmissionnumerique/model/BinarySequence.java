package com.transmissionnumerique.model;

import java.util.Random;

public class BinarySequence {
    private boolean[] bits;

    public BinarySequence(int length) {
        bits = new boolean[length];
    }

    public void generateRandom() {
        Random random = new Random();
        for (int i = 0; i < bits.length; i++) {
            bits[i] = random.nextBoolean();
        }
    }

    public void setBits(boolean[] bits) {
        this.bits = bits;
    }

    public boolean[] getBits() {
        return bits;
    }

    public int getLength() {
        return bits.length;
    }

    // Calcul du taux d'erreur binaire avec diagnostic détaillé
    public double calculateBER(BinarySequence other) {
        // Si les séquences sont de longueurs différentes, utiliser la plus courte des deux
        int minLength = Math.min(this.getLength(), other.getLength());

        System.out.println("=== CALCUL DU BER ===");
        System.out.println("Longueur séquence d'entrée: " + this.getLength());
        System.out.println("Longueur séquence de sortie: " + other.getLength());
        System.out.println("Longueur utilisée pour comparaison: " + minLength);

        if (minLength == 0) {
            return 1.0; // Si l'une des séquences est vide, considérer une erreur maximale
        }

        int errors = 0;
        System.out.println("Comparaison bit par bit (premiers 10 bits):");
        for (int i = 0; i < minLength; i++) {
            boolean inputBit = this.bits[i];
            boolean outputBit = other.getBits()[i];
            boolean isError = inputBit != outputBit;
            
            if (isError) errors++;
            
            // Afficher les premiers bits pour diagnostic
            if (i < 10) {
                System.out.println("Bit " + i + ": " + inputBit + " -> " + outputBit + 
                                 (isError ? " ERREUR" : " OK"));
            }
        }
        
        double ber = (double) errors / minLength;
        System.out.println("Nombre total d'erreurs: " + errors + "/" + minLength);
        System.out.println("BER calculé: " + ber);
        System.out.println("=====================");

        return ber;
    }
}