package utils;

import data.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitaire pour charger efficacement des fichiers CSV sans dépendances externes.
 *
 * <p>Cette classe optimise la lecture en :</p>
 * <ul>
 *   <li>Détermination rapide du nombre de lignes via un scan byte pour pré-allocation.</li>
 *   <li>Lecture NIO avec buffer de 16 Ko pour un I/O maximal.</li>
 *   <li>Parser CSV natif maison gérant les guillemets.</li>
 *   <li>Parsing d'heure flexibles, supportant les heures >=24h pour GTFS.</li>
 * </ul>
 */
public class CSVReader {

    /**
     * Interface fonctionnelle pour mapper un tableau de champs CSV vers un objet métier.
     *
     * @param <T> type de l'objet résultant
     */
    @FunctionalInterface
    private interface RowMapper<T> {
        /**
         * Transforme un tableau de colonnes CSV en instance de T.
         *
         * @param columns valeurs extraites d'une ligne CSV
         * @return l'objet mappé
         */
        T map(String[] columns);
    }

    /**
     * Charge les données d'une compagnie depuis un répertoire de CSV.
     *
     * @param directory   chemin du dossier contenant les CSV
     * @param companyName nom de la compagnie
     * @return Company peuplée
     * @throws IOException si un fichier est introuvable ou mal formé
     */
    public static Company loadCompany(Path directory, String companyName) throws IOException {
        CSVReader reader = new CSVReader();
        Company company = new Company(companyName);

        company.setRoutes(reader.readCsv(
                directory.resolve("routes.csv"),
                cols -> new Route(cols[0], cols[1], cols[2], cols[3])
        ));
        company.setStops(reader.readCsv(
                directory.resolve("stops.csv"),
                cols -> new Stop(
                        cols[0].trim(),
                        cols[1].trim(),
                        Double.parseDouble(cols[2].trim()),
                        Double.parseDouble(cols[3].trim())
                )
        ));
        company.setTrips(reader.readCsv(
                directory.resolve("trips.csv"),
                cols -> new Trip(cols[0], cols[1])
        ));
        company.setStopTimes(reader.readCsv(
                directory.resolve("stop_times.csv"),
                cols -> new StopTime(
                        cols[0],
                        parseTime(cols[1]),
                        cols[2],
                        Integer.parseInt(cols[3])
                )
        ));

        return company;
    }

    /**
     * Lit un CSV et mappe chaque ligne en T, avec pré-allocation basée sur le comptage rapide des lignes.
     *
     * @param file   fichier CSV à lire
     * @param mapper fonction de mappage
     * @param <T>    type des objets retournés
     * @return liste des objets mappés
     * @throws IOException en cas d'erreur d'I/O
     */
    private <T> List<T> readCsv(Path file, RowMapper<T> mapper) throws IOException {
        int estimated = Math.max(0, countLines(file) - 1);
        List<T> list = new ArrayList<>(estimated);

        try (BufferedReader br = new BufferedReader(
                Files.newBufferedReader(file, StandardCharsets.UTF_8),
                16 * 1024
        )) {
            // sauter l'en-tête
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = parseCsvLine(line);
                list.add(mapper.map(tokens));
            }
        }
        return list;
    }

    /**
     * Compte rapidement le nombre de lignes d'un fichier en scannant les bytes.
     *
     * @param file CSV à analyser
     * @return nombre total de lignes (incluant l'en-tête)
     * @throws IOException si le fichier ne peut être lu
     */
    private int countLines(Path file) throws IOException {
        int lines = 0;
        try (InputStream is = Files.newInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(is, 16 * 1024)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = bis.read(buffer)) != -1) {
                for (int i = 0; i < read; i++) {
                    if (buffer[i] == '\n') lines++;
                }
            }
        }
        return lines;
    }

    /**
     * Analyse une ligne CSV en respectant les guillemets.
     *
     * @param line ligne CSV brute
     * @return tableau des champs sans guillemets externes
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0, len = line.length(); i < len; i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * Parse l'heure au format HH:mm[:ss]
     *
     * @param timeStr chaîne de caractères représentant l'heure
     * @return LocalTime
     */
    private static LocalTime parseTime(String timeStr) {
        String[] parts = timeStr.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        int s = (parts.length > 2) ? Integer.parseInt(parts[2]) : 0;
        // dans GTFS, 24:00:00 ou plus valide pour service après minuit
        return LocalTime.of(h % 24, m, s);
    }
}