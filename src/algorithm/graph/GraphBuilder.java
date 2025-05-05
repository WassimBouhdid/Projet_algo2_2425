package algorithm.graph;

import data.Stop;
import data.StopTime;

import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GraphBuilder {

    /**
     * Construit un graphe statique
     *
     * @param stops     liste de tous les arrêts
     * @param stopTimes liste de tous les StopTime (non filtrés, mais idéalement triés par trip_id puis stop_sequence)
     */
    public static Graph buildStaticGraph(List<Stop> stops, List<StopTime> stopTimes) {
        Graph g = new Graph();

        for (Stop s : stops) {
            g.addStop(s);
        }

        Map<String, Stop> stopById = stops.stream().collect(Collectors.toMap(Stop::getStopId, s -> s));

        Map<String, List<StopTime>> byTrip = stopTimes.stream()
                .sorted(Comparator.comparing(StopTime::getTripId).thenComparingInt(StopTime::getStopSequence))
                .collect(Collectors.groupingBy(StopTime::getTripId, LinkedHashMap::new, Collectors.toList()));

        // 4) Pour chaque trajet, créer les arêtes entre stops successifs
        for (List<StopTime> seq : byTrip.values()) {
            for (int i = 0; i < seq.size() - 1; i++) {
                StopTime cur = seq.get(i);
                StopTime next = seq.get(i + 1);

                long delta = Duration.between(cur.getDepartureTime(), next.getDepartureTime()).getSeconds();
                if (delta < 0) delta += 24 * 3600;

                Stop from = stopById.get(cur.getStopId());
                Stop to = stopById.get(next.getStopId());
                if (from != null && to != null) {
                    g.addEdge(new Edge(from, to, (int) delta));
                }
            }
        }

        return g;
    }
}

