# GTFS : Itinéraire le plus court avec A*

Ce projet implémente un calculateur d'itinéraire le plus court basé sur des données **GTFS**, en utilisant l'algorithme **A Star**.  
La fonction de coût est **configurable** : temps de parcours, nombre de changements, pénalisation de la marche, évitement de certains modes de transport, etc.

---

## 📦 Prérequis

- **Java Development Kit (JDK) 21**
- Maven
---

## ⚙️ Compilation

1. Placez-vous à la racine du projet (là où se trouve le dossier `src`).

2. Compilation
   ```bash
   maven clean install
   ```
3. éxécution du programme
   ```bash
   java -Xms2048m -Xmx8192m -jar target/GTFS-1.0-SNAPSHOT.jar
   ```
