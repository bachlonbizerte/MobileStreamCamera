# Mobile Stream Camera V1 — projet Android

Projet source initial d'une application Android de caméra de streaming Wevent.

## État exact de cette V1

Cette archive est une **base de projet Android**, pas un APK prêt à installer.
Elle contient :
- structure Android Kotlin ;
- écran de configuration initial (résolution, FPS, bitrate, RTMP/SRT, option double destination) ;
- modèle de configuration et validation ;
- permissions caméra, microphone et réseau ;
- dépendance RootEncoder 2.8.1, qui prend en charge RTMP, SRT, MediaCodec matériel et l'enregistrement MP4.

**Important :** l'écran est un prototype de configuration. La prévisualisation Camera2, l'encodage réel, l'envoi RTMP/SRT, le fan-out simultané vers deux destinations et l'enregistrement ne sont pas encore câblés. Le bouton ne lance donc pas un direct. Ces modules doivent être implémentés et testés sur le téléphone avant toute utilisation en production.

## Compilation

Ouvrir le dossier dans Android Studio ou l'envoyer dans un dépôt GitHub puis utiliser un workflow Android Gradle.
JDK 17 recommandé avec Android Gradle Plugin 8.7.3.

## Dépendance de streaming

RootEncoder (Apache-2.0) est utilisé comme moteur envisagé. Il prend en charge RTMP et SRT, l'encodage matériel MediaCodec et l'enregistrement MP4. Documentation :
https://github.com/pedroSG94/RootEncoder

## Prochaine étape de développement

1. Ajouter Camera2 / preview et détection des tailles/FPS réellement supportés.
2. Connecter RootEncoder pour un flux RTMP ou SRT simple.
3. Ajouter gestion audio AAC et enregistrement MP4.
4. Implémenter le double streaming avec un encodeur unique et une sortie multi-destination, ou mesurer les limites d'une double session.
5. Tester sur Infinix X1101B Android 14, puis optimiser chauffe, batterie et stabilité.

## Générer un APK avec GitHub Actions

1. Envoyer tous les fichiers du projet à la racine du dépôt GitHub `MobileStreamCamera`.
2. Ouvrir l'onglet **Actions**.
3. Choisir **Build Mobile Stream Camera V1**.
4. Cliquer sur **Run workflow**.
5. Une fois le job terminé avec succès, télécharger l'Artifact `MobileStreamCamera-V1-debug`.

Cette compilation génère un APK de prototype. Elle ne transforme pas encore l'écran de configuration en caméra de streaming fonctionnelle.
