package com.transmissionnumerique.model;

public class TransmissionChain {
    private BinarySequence inputSequence;
    private LineEncoder lineEncoder;
    private TransmissionFilter txFilter;
    private Modulator modulator;
    private Channel channel;
    private Demodulator demodulator;
    private ReceptionFilter rxFilter;
    private ClockRecovery clockRecovery;
    private Decoder decoder;
    private BinarySequence outputSequence;

    // Getters and setters
    public void setInputSequence(BinarySequence inputSequence) {
        this.inputSequence = inputSequence;
    }

    public void setLineEncoder(LineEncoder lineEncoder) {
        this.lineEncoder = lineEncoder;
    }

    public void setTxFilter(TransmissionFilter txFilter) {
        this.txFilter = txFilter;
    }

    public void setModulator(Modulator modulator) {
        this.modulator = modulator;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setDemodulator(Demodulator demodulator) {
        this.demodulator = demodulator;
    }

    public void setRxFilter(ReceptionFilter rxFilter) {
        this.rxFilter = rxFilter;
    }

    public void setClockRecovery(ClockRecovery clockRecovery) {
        this.clockRecovery = clockRecovery;
    }

    public void setDecoder(Decoder decoder) {
        this.decoder = decoder;
    }

    public void setOutputSequence(BinarySequence outputSequence) {
        this.outputSequence = outputSequence;
    }

    public BinarySequence getOutputSequence() {
        return outputSequence;
    }

    public void process() {
    	
        // IMPORTANT : S'assurer que le décodeur connaît le type de modulation
        if (decoder != null && modulator != null) {
            decoder.setModulationType(modulator.getModulationType());
        }
        
        // Étape 1: Encodage en ligne
        double[] encodedSignal = lineEncoder.encode(inputSequence.getBits());
        System.out.println("Longueur après encodage: " + encodedSignal.length);

        // Étape 2: Filtrage d'émission
        double[] filteredSignal = txFilter.filter(encodedSignal);
        System.out.println("Longueur après filtrage TX: " + filteredSignal.length);

        // Étape 3: Modulation
        double[] modulatedSignal = modulator.modulate(filteredSignal);
        System.out.println("Longueur après modulation: " + modulatedSignal.length);

        // Étape 4: Transmission dans le canal
        double[] noisySignal = channel.transmit(modulatedSignal);
        System.out.println("Longueur après canal: " + noisySignal.length);
        
        // ANALYSE DU SIGNAL - SNR effectif
        double effectiveSNR = SignalAnalyzer.calculateEffectiveSNR(modulatedSignal, noisySignal);
        System.out.println("\n>>> SNR effectif mesuré: " + effectiveSNR + " dB");

        // Étape 5: Démodulation
        double[] demodulatedSignal = demodulator.demodulate(noisySignal);
        System.out.println("Longueur après démodulation: " + demodulatedSignal.length);

        // Étape 6: Filtrage de réception
        double[] rxFilteredSignal = rxFilter.filter(demodulatedSignal);
        System.out.println("Longueur après filtrage RX: " + rxFilteredSignal.length);

        // Étape 7: Récupération d'horloge
        double[] recoveredSignal = clockRecovery.recover(rxFilteredSignal);
        System.out.println("Longueur après récupération: " + recoveredSignal.length);
        
        // ANALYSE DU SIGNAL - Avant décodage
        SignalAnalyzer.analyzeSignalDistribution(recoveredSignal, "Signal avant décodage");
        SignalAnalyzer.plotHistogram(recoveredSignal, 10);
        
        // Calcul de la marge de bruit (pour ASK avec seuil à 0)
        double threshold = 0.0;
        double noiseMargin = SignalAnalyzer.calculateNoiseMargin(recoveredSignal, threshold);
        
        // Analyse du diagramme de l'œil
        SignalAnalyzer.analyzeEyePattern(rxFilteredSignal, 4); // 4 échantillons par symbole

        // Étape 8: Décision/Décodage
        boolean[] decodedBits = decoder.decode(recoveredSignal);
        System.out.println("Longueur finale: " + decodedBits.length);

        // Étape 9: Mise à jour de la séquence de sortie
        outputSequence.setBits(decodedBits);
        
        // DIAGNOSTIC : Afficher les premières valeurs
        System.out.println("Premiers bits d'entrée: " + java.util.Arrays.toString(java.util.Arrays.copyOf(inputSequence.getBits(), Math.min(8, inputSequence.getLength()))));
        System.out.println("Premiers bits de sortie: " + java.util.Arrays.toString(java.util.Arrays.copyOf(decodedBits, Math.min(8, decodedBits.length))));
    }
}