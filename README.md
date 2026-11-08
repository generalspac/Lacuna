# Lacuna — Jeu de stratégie et intelligence artificielle

Adaptation en **Java 17 / Swing** du jeu de société Lacuna, réalisée en équipe dans le cadre d'un projet universitaire à l'Université Grenoble Alpes. Cette publication reprend la version finale du dépôt universitaire du **27 mai 2026** (`1ac68b5`), avec une préparation pour GitHub : lancement corrigé, JAR autonome et corrections de données de tests.

**[Télécharger le jeu](https://github.com/generalspac/Lacuna/releases/latest)** · **[Portfolio](https://ouryportfolio.vercel.app)**

## Intelligence artificielle

- Recherche **Minimax avec élagage alpha-bêta** et tri des coups pour limiter l'exploration.
- Trois difficultés : profondeurs 1, 3 et 5 ; heuristiques avancées pour les niveaux moyen et difficile.
- Évaluation des majorités de couleurs, des menaces adverses, de la mobilité et de l'attribution des fleurs restantes en phase 2.
- Simulation sur une copie de l'état du jeu ; calcul en dehors du thread graphique Swing.
- Modes joueur contre IA et IA contre IA pour observer et comparer les stratégies.

L'IA repose sur des algorithmes de recherche et des heuristiques, sans entraînement d'un modèle de machine learning. La profondeur et le nombre de coups explorés sont limités pour conserver un temps de réponse adapté au jeu.

Le code se trouve dans [`lacuna/src/main/java/model/IA`](lacuna/src/main/java/model/IA). Les scénarios de comparaison sont dans [`TestsIAVsIA.java`](lacuna/src/test/java/testsmodel/TestsIAVsIA.java).

## Jouer

Java **17 ou plus récent** est nécessaire. Télécharger `lacuna-1.0-standalone.jar` depuis les [releases](https://github.com/generalspac/Lacuna/releases/latest), puis lancer :

```bash
java -jar lacuna-1.0-standalone.jar
```

Le jeu propose aussi un mode humain contre humain, un mode réseau, des sauvegardes SQLite, un tutoriel et des animations.

## Compiler et tester

Depuis la racine du dépôt, avec Java 17+ et Maven :

```bash
cd lacuna
mvn verify
java -jar target/lacuna-1.0-standalone.jar
```

Sans écran graphique :

```bash
mvn verify -Djava.awt.headless=true
```

Le test de contrôleur créant une fenêtre Swing est alors ignoré ; les tests de modèle, de géométrie, d'IA et de rendu sans fenêtre restent exécutés. Deux anciennes données de tests ont été corrigées : les points du cercle et l'utilisation d'un pion appartenant réellement au plateau pour vérifier les collisions.

Sur macOS/Linux, `./run.sh` permet aussi de compiler et lancer avec `javac` et le pilote SQLite fourni. La documentation Doxygen est disponible dans [`lacuna/docs/html`](lacuna/docs/html), et le [README universitaire original](lacuna/docs/README-original.md) décrit les outils de fuzzing.

## Équipe et provenance

Projet collectif du semestre S6 :

- ADAM Fayçal
- AUAUVRE Niels
- BAH Thierno Souleymane
- BARRY Mamadou Oury
- CHALLAK Layan
- JOUINI Iyed

Le README de travail local attribue les travaux et tests d'IA à Niels et Oury. Ce dépôt présente le projet de l'équipe ; il conserve les crédits des auteurs. Les ressources graphiques et les règles sont celles fournies avec le projet universitaire. Aucune licence supplémentaire n'est ajoutée à ces éléments.
