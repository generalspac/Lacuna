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

    find src/main/java -name '*.java' > target/sources.list
    javac --release 17 -cp "lib/sqlite-jdbc.jar" -d target/classes @target/sources.list || exit 1

    cp -r src/main/resources/* target/classes/

    echo "[INFO] Lancement de Lacuna"

    java -cp "target/classes:lib/sqlite-jdbc.jar" main.Lacuna
}

run_game_maven() {

    echo "[INFO] Compilation avec Maven"

    mvn clean compile

    echo "[INFO] Lancement de Lacuna"

    mvn exec:java -Dexec.mainClass=main.Lacuna
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

generate_docs() {

    echo "[INFO] Génération de la documentation Doxygen"

    if ! command -v doxygen &> /dev/null; then

        echo "[ERREUR] Doxygen n'est pas installé"
        exit 1
    fi

    doxygen Doxyfile

    echo "[INFO] Documentation générée dans docs/html/index.html"
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

        doc)
            ACTION="doc"
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

    doc)
        generate_docs
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