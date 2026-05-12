#!/bin/bash

# Utilisation :
#
# ./run.sh
#     -> lance le jeu
#     @param mvn pour compiler et lancer avec Maven (nécessaire pour les tests et fuzzers)
#
# ./run.sh test
#     -> lance tous les tests
#         doivent être compilés avec Maven au préalable
#
# ./run.sh test=TestsController
#     -> lance une classe de test précise
#
# ./run.sh fuzz
#     -> lance le fuzzer par défaut :
#        fuzzer.fuzzmodel.FuzzJeu
#
# ./run.sh fuzz=FuzzCercle
#     -> lance un fuzzer précis
# Par défaut, le fuzzer lancé est FuzzJeu

DEFAULT_FUZZ="fuzzer.fuzzmodel.FuzzJeu"

run_game() {
    echo "[INFO] Compilation sans Maven"

    rm -rf target/classes
    mkdir -p target/classes

    javac -cp "lib/sqlite-jdbc.jar" -d target/classes \
    src/main/java/global/*.java \
    src/main/java/model/*.java \
    src/main/java/model/IA/*.java \
    src/main/java/view/*.java \
    src/main/java/controller/*.java \
    src/main/java/main/*.java

    cp -r src/main/resources/* target/classes/

    echo "[INFO] Lancement de Lacuna"


    java -cp "target/classes:lib/sqlite-jdbc.jar" main.Lacuna
}

run_game_maven() {
    echo "[INFO] Compilation avec Maven"

    mvn clean compile

    echo "[INFO] Lancement de Lacuna avec Java"

    java -cp target/classes main.Lacuna
}

run_all_tests() {
    echo "[INFO] Lancement de tous les tests"

    mvn test
}

run_single_test() {
    TEST_NAME=$1

    echo "[INFO] Lancement du test : $TEST_NAME"

    mvn test -Dtest=$TEST_NAME
}

run_fuzzer() {
    FUZZ_TARGET=$1

    echo "[INFO] Compilation du projet"

    mvn clean test-compile

    echo "[INFO] Lancement du fuzzer : $FUZZ_TARGET"

    java -cp target/test-classes:target/classes:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q) \
    com.code_intelligence.jazzer.Jazzer \
    --target_class=$FUZZ_TARGET \
    -max_total_time=30
}

if [ $# -eq 0 ]; then
    run_game
    exit 0
fi

for arg in "$@"
do

    case $arg in

        mvn)
            run_game_maven
            ;;

        test)
            run_all_tests
            ;;

        test=*)
            TEST_NAME="${arg#*=}"
            run_single_test "$TEST_NAME"
            ;;

        fuzz)
            run_fuzzer "$DEFAULT_FUZZ"
            ;;

        fuzz=*)
            FUZZ_NAME="${arg#*=}"

            if [[ "$FUZZ_NAME" != *"."* ]]; then
                FUZZ_NAME="fuzzer.fuzzmodel.$FUZZ_NAME"
            fi

            run_fuzzer "$FUZZ_NAME"
            ;;

        *)
            echo "[ERREUR] Argument inconnu : $arg"
            ;;

    esac
done