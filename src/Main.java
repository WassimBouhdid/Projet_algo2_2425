import algorithm.AStar;
import algorithm.graph.Edge;
import algorithm.graph.Graph;
import algorithm.graph.GraphBuilder;
import data.*;
import utils.CSVReader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class Main {
    private static final String[] AGENCIES = {"STIB", "TEC", "DELIJN", "SNCB"};
    private static final Path BASE_DIR = Paths.get("src", "resources");

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            // Chargement parallèle des compagnies
            long t0 = System.nanoTime();
            List<Company> companies = loadAllCompaniesParallel();
            printCounts(companies);
            long t1 = System.nanoTime();
            System.out.printf("Chargement des compagnies en : %.2f ms%n", (t1 - t0) / 1e6);

            // Fusion et déduplication des arrêts
            long fusion_t0 = System.nanoTime();
            Map<String, Stop> uniqueStopsById = new HashMap<>();
            List<StopTime> allStopTimes = new ArrayList<>();
            for (Company c : companies) {
                c.getStops().forEach(s -> uniqueStopsById.putIfAbsent(s.getStopId(), s));
                allStopTimes.addAll(c.getStopTimes());
            }
            List<Stop> allStops = new ArrayList<>(uniqueStopsById.values());
            long fusion_t1 = System.nanoTime();
            System.out.printf("Fusion : %.2f ms%n", (fusion_t1 - fusion_t0) / 1e6);


            // Construction du graphe
            long graph_t0 = System.nanoTime();
            Graph graph = GraphBuilder.buildStaticGraph(allStops, allStopTimes);
            long graph_t1 = System.nanoTime();
            System.out.printf("Création graphe : %.2f ms%n", (graph_t1 - graph_t0) / 1e6);

            // Cartes Trip et Route
            Map<String, Trip> tripById = new HashMap<>();
            Map<String, Route> routeById = new HashMap<>();
            companies.forEach(c -> c.getTrips().forEach(t -> tripById.put(t.getTripId(), t)));
            companies.forEach(c -> c.getRoutes().forEach(r -> routeById.put(r.getRouteId(), r)));

            // Index par nom (minuscule)
            Map<String, List<Stop>> stopsByName = allStops.stream().collect(Collectors.groupingBy(s -> s.getStopName().toLowerCase()));

            // Lecture interactive de la source et cible
            Stop source = readStop(sc, stopsByName, "station de départ");
            Stop target = readStop(sc, stopsByName, "station d'arrivée");

            // Lecture de l'heure de départ
            LocalTime departure = readDepartureTime(sc);

            System.out.printf("\n→ Itinéraire de %s vers %s à partir de %s%n%n", source.getStopName(), target.getStopName(), departure);

            // Exécution A* temps-dépendant
            long tA = System.nanoTime();
            AStar astar = new AStar(graph, source, target, departure);
            System.out.printf("A*       : %.2f ms%n", (System.nanoTime() - tA) / 1e6);

            // Récupération et affichage du chemin
            List<Edge> path = astar.pathTo();
            if (path == null) {
                System.out.println("✗ Aucun chemin trouvé.");
            } else {
                printItinerary(path, departure, tripById, routeById);
            }

        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Erreur durant l'exécution : " + e.getMessage());
        }
    }

    private static Stop readStop(Scanner sc, Map<String, List<Stop>> stopsByName, String prompt) {
        Stop chosen;
        while (true) {
            System.out.printf("Entrez le nom de la %s : ", prompt);
            String name = sc.nextLine().trim().toLowerCase();
            List<Stop> list = stopsByName.get(name);
            if (list == null) {
                System.err.println("Station introuvable. Réessayez.");
                continue;
            } else if (list.size() > 1) {
                System.out.println("Plusieurs correspondances :");
                list.forEach(s -> System.out.println(" - " + s.getStopId() + " : " + s.getStopName()));
                System.out.println("Précisez en saisissant l'ID :");
                String id = sc.nextLine().trim();
                chosen = list.stream().filter(s -> s.getStopId().equals(id)).findFirst().orElse(null);
                if (chosen == null) {
                    System.err.println("ID invalide.");
                    continue;
                }
            } else {
                chosen = list.get(0);
            }
            break;
        }
        return chosen;
    }

    private static LocalTime readDepartureTime(Scanner sc) {
        LocalTime time;
        while (true) {
            System.out.print("Entrez l'heure de départ (HH:mm[:ss]) : ");
            String str = sc.nextLine().trim();
            try {
                time = LocalTime.parse(str);
                break;
            } catch (DateTimeParseException e) {
                System.err.println("Format invalide. Utilisez HH:mm ou HH:mm:ss.");
            }
        }
        return time;
    }

    private static List<Company> loadAllCompaniesParallel() throws InterruptedException, ExecutionException {
        int threads = Math.min(AGENCIES.length, Runtime.getRuntime().availableProcessors());
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        List<Future<Company>> futures = new ArrayList<>();
        for (String ag : AGENCIES) {
            futures.add(exec.submit(() -> CSVReader.loadCompany(BASE_DIR.resolve(ag), ag)));
        }
        exec.shutdown();
        List<Company> list = new ArrayList<>();
        for (Future<Company> f : futures) list.add(f.get());
        return list;
    }

    private static void printCounts(List<Company> companies) {
        companies.forEach(c -> System.out.printf(
                "[%s] R:%3d T:%3d S:%4d ST:%6d%n",
                c.getName(), c.getRoutes().size(), c.getTrips().size(),
                c.getStops().size(), c.getStopTimes().size())
        );
    }

    private static void printItinerary(
            List<Edge> path,
            LocalTime departure,
            Map<String, Trip> trips,
            Map<String, Route> routes
    ) {
        if (path.isEmpty()) return;
        int elapsed = 0;
        for (int i = 0; i < path.size(); ) {
            Edge e0 = path.get(i);
            String curTrip = e0.getTripId();
            String curRoute = curTrip != null ? trips.get(curTrip).getRouteId() : null;
            int segment = 0, j = i;
            while (j < path.size()) {
                Edge ej = path.get(j);
                String tid = ej.getTripId();
                if (curRoute == null ? tid != null : tid == null
                        || !trips.get(tid).getRouteId().equals(curRoute)) break;
                segment += ej.getTravelTimeSec();
                j++;
            }
            LocalTime t0 = departure.plusSeconds(elapsed);
            elapsed += segment;
            LocalTime t1 = departure.plusSeconds(elapsed);
            String from = e0.getFrom().getStopName();
            String to = path.get(j - 1).getTo().getStopName();
            if (curRoute == null) {
                System.out.printf("Walk from %s (%s) to %s (%s)%n", from, t0, to, t1);
            } else {
                Trip t = trips.get(curTrip);
                Route r = routes.get(t.getRouteId());
                String agency = curTrip.split("-")[0];
                System.out.printf("Take %s %s %s from %s (%s) to %s (%s)%n",
                        agency, r.getType().toUpperCase(), r.getShortName(),
                        from, t0, to, t1);
            }
            i = j;
        }
    }
}