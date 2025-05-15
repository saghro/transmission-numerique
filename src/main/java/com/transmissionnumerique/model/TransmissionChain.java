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
    // ...

    public void process() {
        // Étape 1: Encodage en ligne
        double[] encodedSignal = lineEncoder.encode(inputSequence.getBits());

        // Étape 2: Filtrage d'émission
        double[] filteredSignal = txFilter.filter(encodedSignal);

        // Étape 3: Modulation
        double[] modulatedSignal = modulator.modulate(filteredSignal);

        // Étape 4: Transmission dans le canal
        double[] noisySignal = channel.transmit(modulatedSignal);

        // Étape 5: Démodulation
        double[] demodulatedSignal = demodulator.demodulate(noisySignal);

        // Étape 6: Filtrage de réception
        double[] rxFilteredSignal = rxFilter.filter(demodulatedSignal);

        // Étape 7: Récupération d'horloge
        double[] recoveredSignal = clockRecovery.recover(rxFilteredSignal);

        // Étape 8: Décision/Décodage
        boolean[] decodedBits = decoder.decode(recoveredSignal);

        // Étape 9: Mise à jour de la séquence de sortie
        outputSequence.setBits(decodedBits);
    }
}