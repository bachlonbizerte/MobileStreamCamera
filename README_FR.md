# Mobile Stream Camera V1.2 — RTMP/SRT prototype

Cette version remplace le pipeline CameraX par les sources Camera2 intégrées à RootEncoder, afin que la même caméra/preview serve à l'encodage et au streaming.

Fonctions incluses :
- aperçu caméra via OpenGlView ;
- sélection RTMP ou SRT ;
- URL éditable ;
- résolution, FPS et bitrate ;
- audio AAC depuis le microphone ;
- basculement caméra via `switchCamera()` pendant le preview ou le stream ;
- retours d'état de connexion RootEncoder.

Limites :
- un seul protocole et une seule destination par session dans cette V1.2 ;
- le double streaming simultané vers V5 + externe n'est pas encore implémenté ;
- pas encore d'enregistrement local ni service de streaming arrière-plan ;
- le changement de caméra en direct est pris en charge par RootEncoder mais doit être testé sur le modèle réel et la résolution choisie ;
- RTMP/SRT doivent être testés avec les URLs et paramètres correspondant à ton serveur. Pour SRT, entrer l'URL complète et le streamid si ton serveur l'exige.

Dépendance : RootEncoder 2.8.1. Sa documentation décrit les classes RtmpCamera2/SrtCamera2, `prepareVideo`, `prepareAudio`, `startStream` et `switchCamera`.
Build : GitHub Actions > Build Mobile Stream Camera V1.2 > Run workflow.
\n\n## Interface V1.2.1\n- Interface entièrement défilable verticalement (ScrollView).\n- Textes des champs, listes et boutons en blanc pour contraste sur fond bleu nuit.\n- Boutons agrandis, pleine largeur et plus lisibles.\n- Aperçu caméra conservé dans une zone de hauteur fixe pour ne pas bloquer le défilement.\n

## Écran pendant le direct
Pendant un stream actif, l'application active `FLAG_KEEP_SCREEN_ON` pour empêcher Android d'éteindre l'écran. Le verrou est retiré à l'arrêt, à la déconnexion, en cas d'échec de connexion et à la fermeture de l'activité. Cela garde l'écran allumé tant que l'écran de l'application reste affiché ; ce n'est pas un service de streaming en arrière-plan.
