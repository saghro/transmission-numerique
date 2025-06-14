package com.transmissionnumerique.controller;

import com.transmissionnumerique.model.*;
import com.transmissionnumerique.model.SignalAnalyzer.EyePatternMetrics;

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
    
    // Variables pour stocker les métriques d'analyse
    private double effectiveSNR;
    private double noiseMargin;
    private double eyeOpening;
    private EyePatternMetrics eyeMetrics;

    // Paramètres constants
    private static final int SAMPLES_PER_SYMBOL = 8;
    private static final double CARRIER_FREQUENCY = 10000; // Hz
    private static final double SAMPLE_RATE = 80000; // Hz
    private static final double ROLL_OFF = 0.35;
    private static final double THRESHOLD = 0.0;

    public TransmissionController() {
        transmissionChain = new TransmissionChain();
        decoder = new Decoder(THRESHOLD);
        decoder.setDebugMode(false); // Désactiver le mode debug
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
        
        int effectiveSamplesPerSymbol = SAMPLES_PER_SYMBOL;
        if(encodingType == LineEncoder.EncodingType.MANCHESTER) {
            effectiveSamplesPerSymbol = SAMPLES_PER_SYMBOL * 2;
        }

        // Génération de la séquence d'entrée si nécessaire
        if (inputSequence == null || inputSequence.getLength() != sequenceLength) {
            generateRandomSequence(sequenceLength);
        }

        // Initialisation de la séquence de sortie
        outputSequence = new BinarySequence(inputSequence.getLength());
        
        // Encodage en ligne
        LineEncoder lineEncoder = new LineEncoder(encodingType);
        encodedSignal = lineEncoder.encode(inputSequence.getBits());

        // Filtrage d'émission
        if (filterType == TransmissionFilter.FilterType.RECTANGULAR) {
            TransmissionFilter txFilter = new TransmissionFilter(filterType, SAMPLES_PER_SYMBOL, ROLL_OFF);
            filteredSignal = txFilter.filter(encodedSignal);
        } else {
            // Utiliser le filtre simplifié pour RC et RRC
            SimplifiedRaisedCosineFilter txFilter = new SimplifiedRaisedCosineFilter(SAMPLES_PER_SYMBOL, ROLL_OFF);
            filteredSignal = txFilter.filterWithEnergyPreservation(encodedSignal);
        }

        // Modulation
        Modulator modulator = new Modulator(modulationType, CARRIER_FREQUENCY, SAMPLE_RATE);
        modulatedSignal = modulator.modulate(filteredSignal);

        // Canal de propagation
        Channel channel = new Channel(Channel.NoiseType.AWGN, snr);
        noisySignal = channel.transmit(modulatedSignal);
        
        // SNR effectif
        effectiveSNR = SignalAnalyzer.calculateEffectiveSNR(modulatedSignal, noisySignal);

        // Démodulation
        Demodulator demodulator = new Demodulator(modulationType, CARRIER_FREQUENCY, SAMPLE_RATE);
        demodulatedSignal = demodulator.demodulate(noisySignal);

        // Filtrage de réception
        ReceptionFilter rxFilter = new ReceptionFilter(filterType, SAMPLES_PER_SYMBOL, ROLL_OFF);
        rxFilter.setEncodingType(encodingType);
        rxFilteredSignal = rxFilter.filter(demodulatedSignal);
        
        // Analyse du diagramme de l'œil
        eyeMetrics = SignalAnalyzer.analyzeEyePattern(rxFilteredSignal, SAMPLES_PER_SYMBOL);
        eyeOpening = eyeMetrics.maxOpening;

        // Récupération d'horloge
        ClockRecovery clockRecovery = new ClockRecovery(SAMPLES_PER_SYMBOL);
        clockRecovery.setDebugMode(false); // Désactiver le mode debug
        recoveredSignal = clockRecovery.recover(rxFilteredSignal);
        
        // Calcul de la marge de bruit
        noiseMargin = SignalAnalyzer.calculateNoiseMargin(recoveredSignal, THRESHOLD);

        // Configuration du décodeur
        decoder.setModulationType(modulationType);
        decoder.setEncodingType(encodingType);
        
        // Décodage
        boolean[] decodedBits = decoder.decode(recoveredSignal);

        // Ajustement de la taille si nécessaire
        if (decodedBits.length != inputSequence.getLength()) {
            boolean[] resizedBits = new boolean[inputSequence.getLength()];
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
            return 0.0;
        }

        return inputSequence.calculateBER(outputSequence);
    }
    
    /**
     * Lance une série de tests de performance pour différents SNR
     * @param modulationType Type de modulation à tester
     * @param sequenceLength Longueur de la séquence de test
     * @return Tableau de résultats [SNR, BER]
     */
    public double[][] runPerformanceTest(Modulator.ModulationType modulationType, int sequenceLength) {
        double[] snrValues = {-3, 0, 3, 5, 10, 15, 20, 30};
        double[][] results = new double[snrValues.length][2];
        
        LineEncoder.EncodingType encoding = LineEncoder.EncodingType.NRZ;
        TransmissionFilter.FilterType filter = TransmissionFilter.FilterType.RECTANGULAR;
        
        for (int i = 0; i < snrValues.length; i++) {
            double snr = snrValues[i];
            double totalBER = 0;
            int numTrials = 5;
            
            for (int trial = 0; trial < numTrials; trial++) {
                generateRandomSequence(sequenceLength);
                runSimulation(sequenceLength, encoding, filter, modulationType, snr);
                double ber = calculateBER();
                totalBER += ber;
            }
            
            double avgBER = totalBER / numTrials;
            results[i][0] = snr;
            results[i][1] = avgBER;
        }
        
        return results;
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
    
    public double[] getFilteredSignal() {
        return filteredSignal;
    }
    
    public double[] getRxFilteredSignal() {
        return rxFilteredSignal;
    }
    
    // Getters pour les métriques d'analyse
    public double getEffectiveSNR() {
        return effectiveSNR;
    }
    
    public double getNoiseMargin() {
        return noiseMargin;
    }
    
    public double getEyeOpening() {
        return eyeOpening;
    }
    
    public EyePatternMetrics getEyeMetrics() {
        return eyeMetrics;
    }
    
    public int getSamplesPerSymbol() {
        return SAMPLES_PER_SYMBOL;
    }
}