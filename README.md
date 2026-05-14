# Lacuna

Jeu Lacuna avec implémentation IA

## Compilation

placer vous dans le repertoire "main_files"

Donnez le droit d'execution au fichier compile.sh

Lancer le fichier compile.sh (il fait la compilation )

pour lancer il suffit de taper la commande :

```
java -cp bin main.Lacuna
```

ou lancer le fichier run.sh en lui donnant les droit d'execution


# Faycal

-IHM avec layan (ne pas oublier le compte rendu pour l'audit et le projet final)
-Ajout de retour au menu lorsqu'on abandonne la partie 

# Tout le monde

-Jouer au jeu et vérifier les bugs 
- Pour les bugs que vous pouvez resoudre, si vous les resolvez, 
    supprimez les dans la partie bugs(en bas) et faites le savoir dans le groupe

# Niels & oury

- Tester les IA et faire le rapport 


# Layan

- IHM

# Souleymane
-Tests 
-REfactorisation

# Iyed
 --Gestion de load (historique)


# BUGS
 --en appuyant sur une fleur pour placer un pion, il se place exactement à la place de la fleur après l'avoir 
 --- collectée. Or que le pion doit être entre les 2 fleurs collectés

# oury
    -refaire une factorisation des variables globales utilisées par le modèle et generateurCoup ia
        -peut être créer une autre classe (evite la duplication)
    

    tests IA : 
    
        sudo apt install sqlite3

        sqlite3 lacuna.db "DELETE FROM resultats_ia;"
        
        ./run.sh

        javac -cp "lib/sqlite-jdbc.jar:target/classes" -d target/test-classes src/test/java/testsmodel/TestsIAVsIA.java && java -cp "target/test-classes:target/classes:lib/sqlite-jdbc.jar" testsmodel.TestsIAVsIA

        sqlite3 lacuna.db "SELECT * FROM resultats_ia;"
    


