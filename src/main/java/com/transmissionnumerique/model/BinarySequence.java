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

    public double calculateBER(BinarySequence other) {
        int minLength = Math.min(this.getLength(), other.getLength());

        if (minLength == 0) {
            return 1.0;
        }

        int errors = 0;
        for (int i = 0; i < minLength; i++) {
            if (this.bits[i] != other.getBits()[i]) {
                errors++;
            }
        }
        
        return (double) errors / minLength;
    }
}