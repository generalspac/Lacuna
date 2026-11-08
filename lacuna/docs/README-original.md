# Lacuna

Lacuna est une adaptation du jeu de société **Lacuna** réalisée en Java avec une interface graphique Swing.

Le projet propose plusieurs modes de jeu :

- Humain vs Humain
- Joueur vs IA
- IA vs IA
- Mode Réseau

Le jeu inclut également :

- un système de sauvegarde SQLite,
- des animations graphiques,
- des tests unitaires Maven,
- du fuzzing avec Jazzer,
- une documentation automatique avec Doxygen.

---

# Technologies utilisées

- Java
- Swing
- Maven
- SQLite
- Doxygen
- Graphviz
- Jazzer

---

# Structure du projet

```text
src/
 ├── main/java/
 │    ├── controller/
 │    ├── global/
 │    ├── model/
 │    ├── network/
 │    ├── view/
 │    └── main/
 │
 └── main/resources/
```

---

# Compilation et exécution

## Avec NetBeans

Le projet peut être ouvert directement avec NetBeans.

Il suffit ensuite de :

1. Ouvrir le projet
2. Compiler
3. Exécuter `main.Lacuna`

---

## Sans NetBeans

Si vous utilisez uniquement le terminal, vous devez installer :

- Java
- Maven

### Installation Maven (Linux)

```bash
sudo apt install maven
```

### Vérification

```bash
mvn -version
```

---

# Script principal

Le projet utilise le script :

```bash
./run.sh
```

Pensez à rendre le script exécutable :

```bash
chmod +x run.sh
```

---

# Lancer le jeu

## Compilation classique

```bash
./run.sh
```

---

## Compilation avec Maven

```bash
./run.sh mvn
```

---

# Tests

## Tous les tests

```bash
./run.sh test
```

## Un test spécifique

```bash
./run.sh test=NomDuTest
```

Exemple :

```bash
./run.sh test=JeuTest
```

---

# Fuzzing

## Lancer le fuzzer par défaut

```bash
./run.sh fuzz
```

## Lancer un fuzzer spécifique

```bash
./run.sh fuzz=NomFuzzer
```

Exemple :

```bash
./run.sh fuzz=FuzzJeu
```

## Utiliser un dictionnaire

```bash
./run.sh fuzz dict=jeu.dict
```

---

# Génération de la documentation

La documentation est générée avec Doxygen.

## Installation de Doxygen

```bash
sudo apt install doxygen
```

## Installation de Graphviz (optionnel)

Graphviz permet de générer les graphes UML et les dépendances entre classes.

```bash
sudo apt install graphviz
```

---

## Générer la documentation

```bash
./run.sh doc
```

La documentation sera générée dans :

```text
docs/html/index.html
```

Ouvrir ensuite :

```bash
xdg-open docs/html/index.html
```

---

# Auteurs

Projet universitaire réalisé dans le cadre du semestre S6 par

ADAM Fayçal

AUAUVRE Niels

BAH Thierno Souleymane

BARRY Mamadou Oury

CHALLAK Layan

JOUINI Iyed.