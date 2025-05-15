# transmission-numerique
Ce projet est une simulation complète d'une chaîne de transmission numérique, implémentée en Java. Il permet de visualiser et d'interagir avec les différentes étapes du processus de transmission, depuis la génération d'une séquence binaire jusqu'à sa réception, en passant par le codage, la modulation et un canal bruité.
📋 Table des matières

Aperçu
Fonctionnalités
Installation
Utilisation
Structure du projet
Description technique
Captures d'écran
Dépendances
Contribuer
Licence
Contact

📝 Aperçu
Cette application permet de simuler le fonctionnement d'une chaîne de transmission numérique complète, en visualisant l'évolution du signal à chaque étape du processus. Elle a été développée dans le cadre d'un mini-projet académique pour mettre en pratique les concepts théoriques des communications numériques.
La simulation comprend les blocs suivants:

Émetteur (codage en ligne, filtrage, modulation)
Canal de propagation (avec bruit)
Récepteur (démodulation, filtrage, récupération d'horloge, décision)

✨ Fonctionnalités

Génération de séquences binaires aléatoires de longueur configurable
Codage en ligne avec plusieurs options:

NRZ (Non-Return to Zero)
Manchester
AMI (Alternate Mark Inversion)
HDB3 (High Density Bipolar 3)


Filtrage avec différents types:

Rectangulaire
Cosinus surélevé
Racine de cosinus surélevé


Modulation avec plusieurs schémas:

ASK (Amplitude Shift Keying)
FSK (Frequency Shift Keying)
PSK (Phase Shift Keying)
QPSK (Quadrature Phase Shift Keying)
QAM (Quadrature Amplitude Modulation)


Canal de propagation avec:

Bruit blanc gaussien additif (AWGN)
Évanouissement de Rayleigh
Évanouissement de Rician


Démodulation et récupération du signal d'origine
Calcul et affichage du taux d'erreur binaire (BER)
Visualisation graphique des signaux à chaque étape

🚀 Installation
Prérequis

Java JDK 11 ou supérieur
Maven 3.6 ou supérieur

Étapes d'installation

Cloner le dépôt:
bashgit clone https://github.com/votre-utilisateur/transmission-numerique.git
cd transmission-numerique

Compiler le projet avec Maven:
bashmvn clean package

Exécuter l'application:
bashjava -jar target/transmission-numerique-1.0-SNAPSHOT.jar


🎮 Utilisation

Lancez l'application
Configurez les paramètres de simulation:

Longueur de la séquence binaire
Type de codage en ligne
Type de filtre
Type de modulation
Rapport signal/bruit (SNR)


Cliquez sur "Générer une séquence" pour créer une séquence binaire aléatoire
Cliquez sur "Démarrer la simulation" pour lancer la simulation
Observez les résultats dans les différents onglets et le panneau des résultats

📁 Structure du projet
src/main/java/com/transmissionnumerique/
├── Main.java                      # Point d'entrée de l'application
├── model/                         # Couche modèle (logique métier)
│   ├── BinarySequence.java        # Représentation des séquences binaires
│   ├── Channel.java               # Modélisation du canal de propagation
│   ├── ClockRecovery.java         # Récupération d'horloge
│   ├── Decoder.java               # Décision et décodage
│   ├── Demodulator.java           # Démodulation du signal
│   ├── LineEncoder.java           # Codage en ligne
│   ├── Modulator.java             # Modulation du signal
│   ├── ReceptionFilter.java       # Filtrage en réception
│   ├── TransmissionChain.java     # Chaîne de transmission complète
│   └── TransmissionFilter.java    # Filtrage en émission
├── controller/                    # Couche contrôleur
│   └── TransmissionController.java # Coordination entre modèle et vue
└── view/                          # Couche vue (interface utilisateur)
    └── SwingTransmissionApp.java  # Interface graphique
🔧 Description technique
Architecture
Le projet suit le modèle d'architecture MVC (Modèle-Vue-Contrôleur):

Modèle: Classes représentant les différents composants de la chaîne de transmission
Vue: Interface graphique Swing avec JFreeChart pour les graphiques
Contrôleur: Coordination entre le modèle et la vue, gestion des actions utilisateur

Implémentation des blocs fonctionnels
Émetteur

Séquence binaire:

Génération aléatoire ou saisie manuelle
Stockage sous forme de tableau de booléens


Codage en ligne:

Conversion des bits en signaux électriques
Plusieurs formats disponibles (NRZ, Manchester, AMI, HDB3)


Filtrage d'émission:

Mise en forme spectrale du signal
Différents types de filtres disponibles


Modulation:

Conversion du signal en bande de base vers une fréquence porteuse
Plusieurs schémas de modulation disponibles



Canal de propagation

Modélisation du canal avec ajout de bruit
Rapport signal/bruit (SNR) configurable
Plusieurs modèles de perturbation

Récepteur

Démodulation:

Extraction du signal en bande de base
Adaptation au type de modulation utilisé


Filtrage de réception:

Élimination du bruit et des interférences
Filtre adapté au filtre d'émission


Récupération d'horloge:

Synchronisation avec l'émetteur
Échantillonnage aux instants optimaux


Décision:

Conversion du signal analogique en séquence binaire
Seuil de décision adaptatif



Évaluation des performances

Calcul du taux d'erreur binaire (BER)
Comparaison des séquences d'entrée et de sortie
Visualisation des effets des différents paramètres sur la performance

📸 Captures d'écran
Afficher l'image
Interface principale de l'application
Afficher l'image
Visualisation du signal modulé
Afficher l'image
Comparaison des séquences d'entrée et de sortie
📦 Dépendances

JFreeChart (1.5.3): Bibliothèque pour la création de graphiques
JUnit (5.8.2): Framework de tests unitaires
Maven: Outil de gestion de projet et de dépendances

🤝 Contribuer
Les contributions sont les bienvenues! Voici comment vous pouvez contribuer:

Fork le projet
Créez votre branche de fonctionnalité (git checkout -b feature/amazing-feature)
Committez vos changements (git commit -m 'Add some amazing feature')
Push vers la branche (git push origin feature/amazing-feature)
Ouvrez une Pull Request

📄 Licence
Ce projet est sous licence MIT - voir le fichier LICENSE pour plus de détails.
📞 Contact
[SAGHRO] - [ayoubsaghro27@gmail.com]
Lien du projet: https://github.com/saghro/transmission-numerique
