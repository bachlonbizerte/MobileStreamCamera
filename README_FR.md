# Mobile Stream Camera V1.1

## Nouveautés V1.1
- Aperçu caméra réel avec CameraX.
- Basculement entre caméra arrière et caméra avant.
- Détection de disponibilité des deux caméras.
- Interface paysage adaptée à un usage caméra.

## Compilation APK
GitHub Actions > Build Mobile Stream Camera V1.1 > Run workflow.
Télécharger l'Artifact `MobileStreamCamera-V1.1-debug`.

## État fonctionnel
Cette version implémente l'aperçu et le changement de caméra. Elle ne transmet pas encore de flux RTMP/SRT et n'enregistre pas encore de MP4. Le streaming et le basculement sans interruption pendant un direct sont les étapes suivantes. Le basculement de caméra en preview est testé à l'exécution sur l'appareil.

## Versions
AGP 8.7.3, Kotlin 2.3.21, Gradle 8.9, JDK 17, CameraX 1.4.2.
