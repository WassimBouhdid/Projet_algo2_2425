import algorithm.AStar;
import algorithm.Dijkstra;
import algorithm.graph.Edge;
import algorithm.graph.Graph;
import algorithm.graph.GraphBuilder;
import data.Company;
import data.Route;
import data.Stop;
import data.StopTime;
import data.Trip;
import utils.CSVReader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final String[] AGENCIES = { "STIB", "TEC", "DELIJN", "SNCB" };
    private static final Path BASE_DIR      = Paths.get("src", "resources");

    public static void main(String[] args) {
        String sourceId = args[0];
        String targetId = args[1];
        LocalTime departure = parseTimeOrExit(args[2]);

        System.out.println("→ Itinéraire de " + sourceId +
                " vers " + targetId +
                " à partir de " + departure);

        // Démarrage du benchmark global
        long globalStart = System.nanoTime();

        // 2) Chargement des compagnies
        long t0 = System.nanoTime();
        List<Company> companies = loadAllCompanies();
        printCounts(companies);
        long t1 = System.nanoTime();
        System.out.printf("⏱ Chargement : %.2f ms%n", (t1 - t0)/1e6);

        // 3) Fusion des listes stops et stopTimes
        long t2 = System.nanoTime();
        List<Stop>     allStops     = new ArrayList<>();
        List<StopTime> allStopTimes = new ArrayList<>();
        for (Company c : companies) {
            allStops   .addAll(c.getStops());
            allStopTimes.addAll(c.getStopTimes());
        }
        long t3 = System.nanoTime();
        System.out.printf("⏱ Fusion data : %.2f ms%n", (t3 - t2)/1e6);

        // 4) Construction du graphe (timetabled + marche)
        long t4 = System.nanoTime();
        Graph graph = GraphBuilder.buildStaticGraph(allStops, allStopTimes);
        long t5 = System.nanoTime();
        System.out.printf("⏱ Graphe      : %.2f ms • %d nœuds, %d arêtes%n",
                (t5 - t4)/1e6, graph.getStops().size(), allStopTimes.size());

        // 5) Préparation des maps Trip et Route
        Map<String, Trip>  tripById  = new HashMap<>();
        Map<String, Route> routeById = new HashMap<>();
        for (Company c : companies) {
            c.getTrips().forEach(t -> tripById.put(t.getTripId(), t));
            c.getRoutes().forEach(r -> routeById.put(r.getRouteId(), r));
        }

        // 6) Lookup source / target
        Map<String, Stop> stopById = allStops.stream()
                .collect(Collectors.toMap(Stop::getStopId, s -> s));
        Stop source = stopById.get(sourceId);
        Stop target = stopById.get(targetId);
        if (source == null || target == null) {
            exitWithMissingStop(sourceId, targetId, stopById);
        }

        // 7) Exécution Dijkstra (pour comparaison)
        long t6 = System.nanoTime();
        new Dijkstra(graph, source);  // résultat non affiché
        long t7 = System.nanoTime();
        System.out.printf("⏱ Dijkstra    : %.2f ms%n", (t7 - t6)/1e6);

        // 8) Exécution A* (itinéraire)
        long t8 = System.nanoTime();
        AStar astar = new AStar(graph, source, target);
        long t9 = System.nanoTime();
        System.out.printf("⏱ A*          : %.2f ms%n", (t9 - t8)/1e6);

        // 9) Affichage de l’itinéraire
        List<Edge> path = astar.pathTo(target);
        if (path == null) {
            System.out.println("✗ Aucun chemin trouvé.");
        } else {
            printItinerary(path, departure, tripById, routeById);
        }

        // Fin du benchmark global
        long globalEnd = System.nanoTime();
        System.out.printf("⏱ Total       : %.2f s%n", (globalEnd - globalStart)/1e9);
    }

    private static LocalTime parseTimeOrExit(String s) {
        try {
            return LocalTime.parse(s);
        } catch (DateTimeParseException ex) {
            System.err.println("✗ Heure invalide «" + s + "». Format HH:mm[:ss]");
            System.exit(1);
            return null;
        }
    }

    private static void exitWithMissingStop(
            String src, String tgt, Map<String, Stop> stopById
    ) {
        System.err.println("❌ Arrêt introuvable :");
        if (!stopById.containsKey(src)) System.err.println("   • Source : " + src);
        if (!stopById.containsKey(tgt)) System.err.println("   • Cible  : " + tgt);
        System.err.println("→ Arrêts disponibles :");
        stopById.keySet().stream().sorted()
                .forEach(id -> System.err.println("   - " + id));
        System.exit(2);
    }

    private static List<Company> loadAllCompanies() {
        List<Company> list = new ArrayList<>();
        for (String ag : AGENCIES) {
            System.out.println("  • Chargement de " + ag + "...");
            list.add(CSVReader.loadCompany(BASE_DIR.resolve(ag), ag));
        }
        return list;
    }

    private static void printCounts(List<Company> companies) {
        for (Company c : companies) {
            System.out.printf(
                    "✓ [%s] R:%3d T:%3d S:%4d ST:%6d%n",
                    c.getName(),
                    c.getRoutes().size(),
                    c.getTrips().size(),
                    c.getStops().size(),
                    c.getStopTimes().size()
            );
        }
    }

    private static void printItinerary(
            List<Edge> path,
            LocalTime departure,
            Map<String, Trip>  trips,
            Map<String, Route> routes
    ) {
        LocalTime curr = departure;
        for (Edge e : path) {
            LocalTime next = curr.plusSeconds(e.getTravelTimeSeconds());
            String from = e.getFrom().getStopName();
            String to   = e.getTo().getStopName();

            if (e.getTripId() == null) {
                System.out.printf(
                        "Walk from %s (%s) to %s (%s)%n",
                        from, curr, to, next
                );
            } else {
                Trip  t = trips.get(e.getTripId());
                Route r = routes.get(t.getRouteId());
                String comp = e.getTripId().split("-")[0];  // STIB, TEC, etc.
                System.out.printf(
                        "Take %s %s %s from %s (%s) to %s (%s)%n",
                        comp, r.getType(), r.getShortName(),
                        from, curr, to, next
                );
            }
            curr = next;
        }
    }
}