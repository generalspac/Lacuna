#!/bin/bash

DEFAULT_FUZZ="fuzzer.fuzzmodel.FuzzJeu"

DICT_FILE=""
ACTION=""
TEST_NAME=""
FUZZ_NAME=""

run_game() {

    echo "[INFO] Compilation sans Maven"

    rm -rf target/classes
    mkdir -p target/classes

    javac -cp "lib/sqlite-jdbc.jar" -d target/classes \
        src/main/java/global/*.java \
        src/main/java/model/*.java \
        src/main/java/model/IA/*.java \
        src/main/java/network/*.java \
        src/main/java/view/*.java \
        src/main/java/controller/*.java \
        src/main/java/view/animations/*.java \
        src/main/java/view/render/*.java \
        src/main/java/view/manager/*.java \
        src/main/java/view/component/*.java \
        src/main/java/main/*.java

    cp -r src/main/resources/* target/classes/

    echo "[INFO] Lancement de Lacuna"

    java -cp "target/classes:lib/sqlite-jdbc.jar" main.Lacuna
}

run_game_maven() {

    echo "[INFO] Compilation avec Maven"

    mvn clean compile

    echo "[INFO] Lancement de Lacuna"

    java -cp target/classes main.Lacuna
}

run_all_tests() {

    echo "[INFO] Lancement de tous les tests"

    mvn test
}

run_single_test() {

    local TEST_NAME=$1

    echo "[INFO] Lancement du test : $TEST_NAME"

    mvn test -Dtest=$TEST_NAME
}

prepare_dictionary() {

    local INPUT_DICT=$1
    local OUTPUT_DICT="/tmp/jazzer_dict_$$.dict"

    grep '^"' "$INPUT_DICT" | \
    perl -pe 's/\\([0-7]{1,3})/sprintf("\\x%02X",oct($1))/ge' \
    > "$OUTPUT_DICT"

    echo "$OUTPUT_DICT"
}

run_fuzzer() {

    local FUZZ_TARGET=$1

    echo "[INFO] Compilation du projet"

    mvn clean test-compile

    echo "[INFO] Lancement du fuzzer : $FUZZ_TARGET"

    CMD="java -cp target/test-classes:target/classes:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q) \
    com.code_intelligence.jazzer.Jazzer \
    --target_class=$FUZZ_TARGET \
    -max_total_time=15 \
    -rss_limit_mb=4096 \
    -artifact_prefix=crashes/"

    if [ -n "$DICT_FILE" ]; then

        echo "[INFO] Utilisation du dictionnaire : $DICT_FILE"

        TMP_DICT=$(prepare_dictionary "$DICT_FILE")

        CMD="$CMD -dict=$TMP_DICT"
    fi

    eval $CMD
}

if [ $# -eq 0 ]; then
    run_game
    exit 0
fi

for arg in "$@"
do

    case $arg in

        mvn)
            ACTION="mvn"
            ;;

        test)
            ACTION="test_all"
            ;;

        test=*)
            ACTION="test_single"
            TEST_NAME="${arg#*=}"
            ;;

        fuzz)
            ACTION="fuzz"
            FUZZ_NAME="$DEFAULT_FUZZ"
            ;;

        fuzz=*)
            ACTION="fuzz"

            FUZZ_NAME="${arg#*=}"

            if [[ "$FUZZ_NAME" != *"."* ]]; then
                FUZZ_NAME="fuzzer.fuzzmodel.$FUZZ_NAME"
            fi
            ;;

        dict=*)
            DICT_FILE="dicos/${arg#*=}"

            if [ ! -f "$DICT_FILE" ]; then
                echo "[ERREUR] Dictionnaire introuvable : $DICT_FILE"
                exit 1
            fi
            ;;

        *)
            echo "[ERREUR] Argument inconnu : $arg"
            exit 1
            ;;

    esac

done

case $ACTION in

    mvn)
        run_game_maven
        ;;

    test_all)
        run_all_tests
        ;;

    test_single)
        run_single_test "$TEST_NAME"
        ;;

    fuzz)
        run_fuzzer "$FUZZ_NAME"
        ;;

    *)
        echo "[ERREUR] Aucune action valide"
        exit 1
        ;;

esac