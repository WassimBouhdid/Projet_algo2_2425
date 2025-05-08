import algorithm.AStar;
import algorithm.Dijkstra;
import algorithm.graph.Edge;
import algorithm.graph.Graph;
import algorithm.graph.GraphBuilder;
import data.Company;
import data.Stop;
import utils.CSVReader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        /*if (args.length != 3){
            System.out.println("mauvais nombre d'argument.");
            System.out.println("Le bon format est le suivant : ");
            System.out.println("java [arrêt de départ] [arrêt de destination] [heure de départ]");
        }*/

        Path directory = Paths.get("src", "resources", "STIB");
        System.out.println("Dossier de données : " + directory.toAbsolutePath());


        Company stib = CSVReader.loadCompany(directory, "STIB");
        System.out.println("✓ Routes chargées      : " + stib.getRoutes().size());
        System.out.println("✓ Trajets chargés      : " + stib.getTrips().size());
        System.out.println("✓ Arrêts chargés       : " + stib.getStops().size());
        System.out.println("✓ StopTimes chargés    : " + stib.getStopTimes().size());

        Graph graph = GraphBuilder.buildStaticGraph(stib.getStops(), stib.getStopTimes());
        System.out.println("✓ Graphe : " + graph.getStops().size() + " nœuds, "
                + stib.getStopTimes().size() + " arêtes créées");

        Map<String, Stop> stopMap = stib.getStops().stream()
                .collect(Collectors.toMap(Stop::getStopId, s -> s));

        String sourceId = "STIB-3515";
        String targetId = "STIB-3520";

        Stop source = stopMap.get(sourceId);
        Stop target = stopMap.get(targetId);
        if (source == null || target == null) {
            System.err.println("Arrêt introuvable !");
            if (source == null) System.err.println("   • Source introuvable : " + sourceId);
            if (target == null) System.err.println("   • Cible introuvable : "   + targetId);
            System.err.println("→ Voici les Stop IDs disponibles :");
            stopMap.keySet().stream()
                    .sorted()
                    .forEach(id -> System.err.println("   - " + id));
            System.exit(2);
        }

        System.out.println("→ Lancement de Dijkstra de " + source.getStopId() +
                " vers " + target.getStopId() + "...");
        System.out.println("→ Dijkstra de " + sourceId + " → " + targetId + "...");
        Dijkstra sp = new Dijkstra(graph, source);

        if (!sp.hasPathTo(target)) {
            System.out.println("Aucun chemin trouvé entre " + sourceId + " et " + targetId + ".");
        } else {
            System.out.println("Distance totale (s) : " + sp.distTo(target));
            System.out.println("Chemin :");
            for (Edge e : sp.pathTo(target)) {
                System.out.printf("  %s → %s (%d s)%n",
                        e.getFrom().getStopId(),
                        e.getTo().getStopId(),
                        e.getTravelTimeSeconds());
            }
        }

        System.out.println("→ Lancement de A* de " + source.getStopId() + " vers " + target.getStopId() + "…");
        AStar astar = new AStar(graph, source, target);

        if (!astar.hasPathTo(target)) {
            System.out.println("Aucun chemin trouvé.");
        } else {
            System.out.println("Temps estimé (s) : " + astar.distTo(target));
            System.out.println("Chemin :");
            for (Edge e : astar.pathTo(target)) {
                System.out.printf("  %s → %s (%d s)%n",
                        e.getFrom().getStopId(),
                        e.getTo().getStopId(),
                        e.getTravelTimeSeconds());
            }
        }
    }
}