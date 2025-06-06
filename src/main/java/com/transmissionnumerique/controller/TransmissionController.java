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
    
    // Variables pour stocker les métriques d'analyse
    private double effectiveSNR;
    private double noiseMargin;
    private double eyeOpening;

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
        
        int effectiveSamplesPerSymbol = SAMPLES_PER_SYMBOL;
        if(encodingType == LineEncoder.EncodingType.MANCHESTER) {
            effectiveSamplesPerSymbol = SAMPLES_PER_SYMBOL * 2;
        }

        // Génération de la séquence d'entrée si nécessaire
        if (inputSequence == null || inputSequence.getLength() != sequenceLength) {
            generateRandomSequence(sequenceLength);
        }

        // Initialisation de la séquence de sortie avec la MÊME longueur
        outputSequence = new BinarySequence(inputSequence.getLength());

        System.out.println("\n========== DÉBUT DE LA SIMULATION ==========");
        
        // Encodage en ligne
        LineEncoder lineEncoder = new LineEncoder(encodingType);
        encodedSignal = lineEncoder.encode(inputSequence.getBits());
        System.out.println("Étape 1: Encodage terminé - " + encodedSignal.length + " échantillons");

        // Filtrage d'émission
        TransmissionFilter txFilter = new TransmissionFilter(filterType, SAMPLES_PER_SYMBOL, ROLL_OFF);
        filteredSignal = txFilter.filter(encodedSignal);
        System.out.println("Étape 2: Filtrage TX terminé - " + filteredSignal.length + " échantillons");

        // Modulation
        Modulator modulator = new Modulator(modulationType, CARRIER_FREQUENCY, SAMPLE_RATE);
        modulatedSignal = modulator.modulate(filteredSignal);
        System.out.println("Étape 3: Modulation terminée - " + modulatedSignal.length + " échantillons");
        
        // ANALYSE PRÉ-CANAL
        System.out.println("\n--- Analyse du signal modulé ---");
        SignalAnalyzer.analyzeSignalDistribution(modulatedSignal, "Signal modulé (avant canal)");

        // Canal de propagation
        Channel channel = new Channel(Channel.NoiseType.AWGN, snr);
        noisySignal = channel.transmit(modulatedSignal);
        System.out.println("\nÉtape 4: Transmission dans le canal terminée");
        
        // ANALYSE POST-CANAL - SNR effectif
        effectiveSNR = SignalAnalyzer.calculateEffectiveSNR(modulatedSignal, noisySignal);
        System.out.println("\n>>> SNR théorique: " + snr + " dB");
        System.out.println(">>> SNR effectif mesuré: " + String.format("%.2f", effectiveSNR) + " dB");

        // Démodulation
        Demodulator demodulator = new Demodulator(modulationType, CARRIER_FREQUENCY, SAMPLE_RATE);
        demodulatedSignal = demodulator.demodulate(noisySignal);
        System.out.println("\nÉtape 5: Démodulation terminée");

        // Filtrage de réception
        
        ReceptionFilter rxFilter = new ReceptionFilter(filterType, SAMPLES_PER_SYMBOL, ROLL_OFF);
        rxFilter.setEncodingType(encodingType);  // AJOUTER CETTE LIGNE !
        rxFilteredSignal = rxFilter.filter(demodulatedSignal);
        System.out.println("Étape 6: Filtrage RX terminé");
        
        // ANALYSE DU DIAGRAMME DE L'ŒIL
        System.out.println("\n--- Analyse du diagramme de l'œil ---");
        SignalAnalyzer.analyzeEyePattern(rxFilteredSignal, SAMPLES_PER_SYMBOL);

        // Récupération d'horloge
        ClockRecovery clockRecovery = new ClockRecovery(SAMPLES_PER_SYMBOL);
        recoveredSignal = clockRecovery.recover(rxFilteredSignal);
        System.out.println("\nÉtape 7: Récupération d'horloge terminée - " + recoveredSignal.length + " symboles");
        
        // ANALYSE PRÉ-DÉCODAGE
        System.out.println("\n--- Analyse du signal avant décodage ---");
        SignalAnalyzer.analyzeSignalDistribution(recoveredSignal, "Signal récupéré");
        SignalAnalyzer.plotHistogram(recoveredSignal, 10);
        
        // Calcul de la marge de bruit selon le type de modulation
        double threshold = 0.0;
        if (modulationType == Modulator.ModulationType.ASK) {
            // Pour ASK, le seuil est entre les deux niveaux
            threshold = 0.0; // Avec modulation bipolaire -0.8/+0.8
        }
        noiseMargin = SignalAnalyzer.calculateNoiseMargin(recoveredSignal, threshold);

        // Configuration du décodeur avec le bon type de modulation
        
        decoder.setModulationType(modulationType);
        decoder.setEncodingType(encodingType);
        
        // Décodage
        boolean[] decodedBits = decoder.decode(recoveredSignal);
        System.out.println("\nÉtape 8: Décodage terminé");

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
        
        System.out.println("\n========== FIN DE LA SIMULATION ==========\n");
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
    
    /**
     * Lance une série de tests de performance pour différents SNR
     * @param modulationType Type de modulation à tester
     * @param sequenceLength Longueur de la séquence de test
     * @return Tableau de résultats [SNR, BER]
     */
    public double[][] runPerformanceTest(Modulator.ModulationType modulationType, int sequenceLength) {
        System.out.println("\n=== TEST DE PERFORMANCE - " + modulationType + " ===");
        
        double[] snrValues = {-3, 0, 3, 5, 10, 15, 20, 30};
        double[][] results = new double[snrValues.length][2];
        
        LineEncoder.EncodingType encoding = LineEncoder.EncodingType.NRZ;
        TransmissionFilter.FilterType filter = TransmissionFilter.FilterType.RECTANGULAR;
        
        for (int i = 0; i < snrValues.length; i++) {
            double snr = snrValues[i];
            double totalBER = 0;
            int numTrials = 5; // Nombre de simulations pour moyenner
            
            for (int trial = 0; trial < numTrials; trial++) {
                // Générer une nouvelle séquence aléatoire pour chaque essai
                generateRandomSequence(sequenceLength);
                
                // Exécuter la simulation
                runSimulation(sequenceLength, encoding, filter, modulationType, snr);
                
                // Calculer le BER
                double ber = calculateBER();
                totalBER += ber;
            }
            
            double avgBER = totalBER / numTrials;
            results[i][0] = snr;
            results[i][1] = avgBER;
            
            System.out.printf("SNR = %3.0f dB : BER moyen = %.6f\n", snr, avgBER);
        }
        
        return results;
    }
    
    /**
     * Affiche un résumé des métriques d'analyse
     * @return Chaîne contenant le résumé des métriques
     */
    public String getAnalysisMetricsSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("\n=== MÉTRIQUES D'ANALYSE ===\n");
        summary.append(String.format("SNR effectif: %.2f dB\n", effectiveSNR));
        summary.append(String.format("Marge de bruit: %.3f\n", noiseMargin));
        summary.append(String.format("Ouverture de l'œil: %.3f\n", eyeOpening));
        return summary.toString();
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
}