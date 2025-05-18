# GTFS : Itinéraire le plus court avec A*

Ce projet implémente un calculateur d'itinéraire le plus court basé sur des données **GTFS**, en utilisant l'algorithme **A Star**.  
La fonction de coût est **configurable** : temps de parcours, nombre de changements, pénalisation de la marche, évitement de certains modes de transport, etc.

---

## 📦 Prérequis

- **Java Development Kit (JDK) 21**
---

## ⚙️ Compilation

1. Placez-vous à la racine du projet (là où se trouve le dossier `src`).

2. Créez un dossier pour les fichiers `.class` :
   ```bash
   mkdir -p bin

3. Compilez tout le code source 
   - sur windows :
   ```bash
   javac -d bin (Get-ChildItem -Recurse -Filter *.java -Path src).FullName
   ```
   - sur linux
   ```bash
   javac -d bin $(find src -name "*.java")

4.Executez le fichier 

   ```bash
   java -cp bin Main
  ```

