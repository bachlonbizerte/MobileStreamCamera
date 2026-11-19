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
