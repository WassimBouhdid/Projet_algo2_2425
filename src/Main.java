import algorithm.AStar;
import algorithm.Dijkstra;
import algorithm.graph.Edge;
import algorithm.graph.Graph;
import algorithm.graph.GraphBuilder;
import data.Company;
import data.Stop;
import data.StopTime;
import utils.CSVReader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final String[] AGENCIES = { "STIB", "TEC", "DELIJN", "SNCB" };
    private static final Path BASE_DIR = Paths.get("src", "resources");

    public static void main(String[] args) {
        if (args.length != 2) {
            printUsageAndExit();
        }
        String sourceId = args[0];
        String targetId = args[1];

        // **Début du timer global**
        long totalStart = System.nanoTime();
        System.out.println("→ Benchmark démarré");

        // 1) Chargement des compagnies
        long t0 = System.nanoTime();
        List<Company> companies = loadAllCompanies();
        long t1 = System.nanoTime();
        printCounts(companies);
        System.out.printf("⏱ Chargement compagnies : %.2f ms%n", (t1 - t0) / 1_000_000.0);

        // 2) Fusion des stops et stopTimes
        long t2 = System.nanoTime();
        List<Stop> allStops = companies.stream()
                .flatMap(c -> c.getStops().stream())
                .collect(Collectors.toList());
        List<StopTime> allStopTimes = companies.stream()
                .flatMap(c -> c.getStopTimes().stream())
                .collect(Collectors.toList());
        long t3 = System.nanoTime();
        System.out.printf("⏱ Fusion données       : %.2f ms%n", (t3 - t2) / 1_000_000.0);

        // 3) Construction du graphe
        long t4 = System.nanoTime();
        Graph graph = GraphBuilder.buildStaticGraph(allStops, allStopTimes);
        long t5 = System.nanoTime();
        System.out.printf("⏱ Construction graphe  : %.2f ms%n", (t5 - t4) / 1_000_000.0);
        System.out.printf("    → Nœuds : %d, Arêtes : %d%n",
                graph.getStops().size(), allStopTimes.size());

        // 4) Préparation des arrêts source/target
        Map<String, Stop> stopMap = allStops.stream()
                .collect(Collectors.toMap(Stop::getStopId, s -> s));
        Stop source = stopMap.get(sourceId);
        Stop target = stopMap.get(targetId);
        if (source == null || target == null) {
            System.err.println("❌ Stop introuvable !");
            if (source == null) System.err.println("   • Source : " + sourceId);
            if (target == null) System.err.println("   • Cible  : " + targetId);
            System.err.println("→ Liste Stop IDs dispo :");
            stopMap.keySet().stream().sorted().forEach(id -> System.err.println("   - " + id));
            System.exit(2);
        }

        // 5) Exécution Dijkstra
        long t6 = System.nanoTime();
        Dijkstra dijkstra = new Dijkstra(graph, source);
        long t7 = System.nanoTime();
        System.out.printf("⏱ Dijkstra exécution   : %.2f ms%n", (t7 - t6) / 1_000_000.0);
        printPath("Dijkstra", dijkstra.hasPathTo(target), dijkstra.distTo(target), dijkstra.pathTo(target));

        // 6) Exécution A*
        long t8 = System.nanoTime();
        AStar astar = new AStar(graph, source, target);
        long t9 = System.nanoTime();
        System.out.printf("⏱ A* exécution         : %.2f ms%n", (t9 - t8) / 1_000_000.0);
        printPath("A*", astar.hasPathTo(target), astar.distTo(target), astar.pathTo(target));

        // **Fin du timer global**
        long totalEnd = System.nanoTime();
        System.out.printf("⏱ Temps total           : %.2f s%n", (totalEnd - totalStart) / 1_000_000_000.0);

        System.out.println("→ Benchmark terminé");
    }

    private static List<Company> loadAllCompanies() {
        List<Company> list = new ArrayList<>();
        for (String ag : AGENCIES) {
            Path dir = BASE_DIR.resolve(ag);
            System.out.println("  • Chargement de " + ag + "...");
            Company c = CSVReader.loadCompany(dir, ag);
            list.add(c);
        }
        return list;
    }

    private static void printCounts(List<Company> companies) {
        for (Company c : companies) {
            System.out.printf("✓ [%s] Routes: %3d | Trips: %3d | Stops: %4d | StopTimes: %6d%n",
                    c.getName(),
                    c.getRoutes().size(),
                    c.getTrips().size(),
                    c.getStops().size(),
                    c.getStopTimes().size()
            );
        }
    }

    private static void printPath(String label, boolean hasPath, int distance, List<Edge> path) {
        System.out.printf("→ %s résultat :%n", label);
        if (!hasPath) {
            System.out.println("   Aucun chemin trouvé.");
        } else {
            System.out.println("   Distance (s) : " + distance);
            System.out.println("   Chemin :");
            for (Edge e : path) {
                System.out.printf("    %s → %s (%d s)%n",
                        e.getFrom().getStopId(),
                        e.getTo().getStopId(),
                        e.getTravelTimeSeconds()
                );
            }
        }
    }

    private static void printUsageAndExit() {
        System.err.println("Usage: java -cp <classpath> app.Main <source_stop_id> <target_stop_id>");
        System.err.println("Exemple: java app.Main STIB-3515 STIB-3520");
        System.exit(1);
    }
}
