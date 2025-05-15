package com.transmissionnumerique.controller;

import com.transmissionnumerique.model.*;

/**
 * Contrôleur principal de l'application.
 * Coordonne les interactions entre la vue et le modèle.
 */
public class TransmissionController {

    private TransmissionChain transmissionChain;
    private BinarySequence inputSequence;
    private BinarySequence outputSequence;

    // Variables pour stocker les signaux intermédiaires
    private double[] encodedSignal;
    private double[] filteredSignal;
    private double[] modulatedSignal;
    private double[] noisySignal;
    private double[] demodulatedSignal;
    private double[] rxFilteredSignal;
    private double[] recoveredSignal;

    // Ajouter cette déclaration
    private Decoder decoder;

    // Paramètres constants
    private static final int SAMPLES_PER_SYMBOL = 8;
    private static final double CARRIER_FREQUENCY = 10000; // Hz
    private static final double SAMPLE_RATE = 80000; // Hz
    private static final double ROLL_OFF = 0.35;
    private static final double THRESHOLD = 0.0;

    public TransmissionController() {
        transmissionChain = new TransmissionChain();
        // Ajoutez cette ligne pour initialiser le décodeur
        decoder = new Decoder(THRESHOLD);
    }

    /**
     * Génère une séquence binaire aléatoire.
     * @param length Longueur de la séquence
     */
    public void generateRandomSequence(int length) {
        inputSequence = new BinarySequence(length);
        inputSequence.generateRandom();
    }

    /**
     * Exécute la simulation complète de la chaîne de transmission.
     */
    public void runSimulation(int sequenceLength, LineEncoder.EncodingType encodingType,
                              TransmissionFilter.FilterType filterType,
                              Modulator.ModulationType modulationType,
                              double snr) {

        // Génération de la séquence d'entrée si nécessaire
        if (inputSequence == null || inputSequence.getLength() != sequenceLength) {
            generateRandomSequence(sequenceLength);
        }

        // Initialisation de la séquence de sortie avec la MÊME longueur
        outputSequence = new BinarySequence(inputSequence.getLength());

        // Encodage en ligne
        LineEncoder lineEncoder = new LineEncoder(encodingType);
        encodedSignal = lineEncoder.encode(inputSequence.getBits());

        // Filtrage d'émission
        TransmissionFilter txFilter = new TransmissionFilter(filterType, SAMPLES_PER_SYMBOL, ROLL_OFF);
        filteredSignal = txFilter.filter(encodedSignal);

        // Modulation
        Modulator modulator = new Modulator(modulationType, CARRIER_FREQUENCY, SAMPLE_RATE);
        modulatedSignal = modulator.modulate(filteredSignal);

        // Canal de propagation
        Channel channel = new Channel(Channel.NoiseType.AWGN, snr);
        noisySignal = channel.transmit(modulatedSignal);

        // Démodulation
        Demodulator demodulator = new Demodulator(modulationType, CARRIER_FREQUENCY, SAMPLE_RATE);
        demodulatedSignal = demodulator.demodulate(noisySignal);

        // Filtrage de réception
        ReceptionFilter rxFilter = new ReceptionFilter(filterType, SAMPLES_PER_SYMBOL, ROLL_OFF);
        rxFilteredSignal = rxFilter.filter(demodulatedSignal);

        // Récupération d'horloge
        ClockRecovery clockRecovery = new ClockRecovery(SAMPLES_PER_SYMBOL);
        recoveredSignal = clockRecovery.recover(rxFilteredSignal);

        // Assurez-vous que les bits décodés ont la même longueur que la séquence d'entrée
        boolean[] decodedBits = decoder.decode(recoveredSignal);

        // Si les longueurs sont différentes, ajustez la taille
        if (decodedBits.length != inputSequence.getLength()) {
            boolean[] resizedBits = new boolean[inputSequence.getLength()];
            // Copier les bits en préservant la longueur minimale
            int minLength = Math.min(decodedBits.length, inputSequence.getLength());
            System.arraycopy(decodedBits, 0, resizedBits, 0, minLength);
            decodedBits = resizedBits;
        }

        // Mise à jour de la séquence de sortie
        outputSequence.setBits(decodedBits);
    }

    /**
     * Calcule le taux d'erreur binaire entre les séquences d'entrée et de sortie.
     * @return Taux d'erreur binaire
     */
    public double calculateBER() {
        if (inputSequence == null || outputSequence == null) {
            System.out.println("Une des séquences est null");
            return 0.0;
        }

        System.out.println("Longueur séquence d'entrée: " + inputSequence.getLength());
        System.out.println("Longueur séquence de sortie: " + outputSequence.getLength());

        try {
            return inputSequence.calculateBER(outputSequence);
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur de calcul BER: " + e.getMessage());
            return 0.0; // Valeur par défaut en cas d'erreur
        }
    }

    // Getters pour les différents signaux

    public boolean[] getInputSequence() {
        return inputSequence.getBits();
    }

    public boolean[] getOutputSequence() {
        return outputSequence.getBits();
    }

    public double[] getEncodedSignal() {
        return encodedSignal;
    }

    public double[] getModulatedSignal() {
        return modulatedSignal;
    }

    public double[] getNoisySignal() {
        return noisySignal;
    }

    public double[] getDemodulatedSignal() {
        return demodulatedSignal;
    }
}