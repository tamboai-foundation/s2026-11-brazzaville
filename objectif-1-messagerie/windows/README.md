# NGoma — Windows (Compose Desktop + TModeler)

Projet de départ Foundation. Package : `ai.tambo.foundation.ngoma`. Charte rouge (`#E0262D`).

## Lancer
```
gradlew.bat run
```
JDK 21 requis.

## Écrans
Splash de chargement, connexion / inscription, liste des conversations (recherche, filtres, épingler, sourdine, archiver), conversation (envoi, réponse, modification, suppression, réactions).

## Mocks (niveau ui-bridge-TModeler)
`src/main/kotlin/ai/tambo/foundation/ngoma/bridge/Bridges.kt` : interfaces `AccountBridge` et `MessagingBridge`, avec `AccountBridgeMock` et `MessagingBridgeMock` (tout identifiant est accepté).
Le point de bascule est l'objet `Bridges` dans `Main.kt` : remplacer les mocks par des implémentations TModeler.

## TModeler
- `co.tamboai:TModeler:1.0.1` (Maven Central).
- Init : `NGomaTm.kt` (`DB_VERSION` : l'incrémenter repart d'une base vide).
- Exemple de modèle : `models/Note.kt`.

- Test Git : Papa Ibrahima SAMB