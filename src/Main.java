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

        Stop source = stopMap.get(10);
        Stop target = stopMap.get(12);
        if (source == null || target == null) {
            System.err.println("Source ou target introuvable !");
            System.exit(2);
        }

        System.out.println("→ Lancement de Dijkstra de " + source.getStopId() +
                " vers " + target.getStopId() + "...");
        Dijkstra sp = new Dijkstra(graph, source);

        if (sp.hasPathTo(target)) {
            System.out.println("Distance (s) : " + sp.distTo(target));
            System.out.println("Chemin :");
            for (Edge e : sp.pathTo(target)) {
                System.out.printf("  %s → %s (%d s)%n",
                        e.getFrom().getStopId(),
                        e.getTo().getStopId(),
                        e.getTravelTimeSeconds());
            }
        } else {
            System.out.println("Aucun chemin trouvé.");
        }
    }
}