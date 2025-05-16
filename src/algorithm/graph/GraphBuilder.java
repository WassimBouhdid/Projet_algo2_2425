package algorithm.graph;

import data.Stop;
import data.StopTime;

import java.util.*;
import java.util.stream.*;

/**
 * Construction d'un graphe statique pour un réseau de transports en commun,
 * en tenant compte des trajets (stopTimes) et des liaisons piétonnes.
 * Intègre les horaires pour les arêtes timetabled (avec departureTimeSec).
 */
public class GraphBuilder {
    private static final double DEFAULT_WALKING_THRESHOLD_METERS = 500.0;
    private static final double DEFAULT_WALKING_SPEED_MPS        = 1.4;

    public static Graph buildStaticGraph(List<Stop> stops, List<StopTime> stopTimes) {
        return buildStaticGraph(
                stops,
                stopTimes,
                DEFAULT_WALKING_THRESHOLD_METERS,
                DEFAULT_WALKING_SPEED_MPS
        );
    }

    public static Graph buildStaticGraph(
            List<Stop> stops,
            List<StopTime> stopTimes,
            double walkingThresholdMeters,
            double walkingSpeedMps
    ) {
        // 0) Déduplication des arrêts par stopId
        Map<String, Stop> uniqueById = new LinkedHashMap<>();
        for (Stop s : stops) {
            uniqueById.putIfAbsent(s.getStopId(), s);
        }
        List<Stop> uniqueStops = new ArrayList<>(uniqueById.values());

        // 1) Initialisation du graphe et mapping stopId -> Stop
        Graph g = new Graph(uniqueStops.size());
        Map<String, Stop> stopById = new HashMap<>(uniqueStops.size());
        for (Stop s : uniqueStops) {
            g.addStop(s);
            stopById.put(s.getStopId(), s);
        }

        // 2) Génération parallèle des arêtes timetabled (avec horaires)
        Map<String, List<StopTime>> byTrip = stopTimes.stream()
                .collect(Collectors.groupingBy(StopTime::getTripId));

        List<Edge> timedEdges = byTrip.values()
                .parallelStream()
                .flatMap(seq -> {
                    seq.sort(Comparator.comparingInt(StopTime::getStopSequence));
                    String prevId = null;
                    int prevSec = 0;
                    Stream.Builder<Edge> builder = Stream.builder();
                    for (StopTime st : seq) {
                        String curId = st.getStopId();
                        int curSec = st.getDepartureTime().toSecondOfDay();
                        if (prevId != null) {
                            int delta = curSec - prevSec;
                            if (delta < 0) delta += 24 * 3600;
                            Stop from = stopById.get(prevId);
                            Stop to   = stopById.get(curId);
                            builder.add(new Edge(from, to, delta, st.getTripId(), prevSec));
                        }
                        prevId  = curId;
                        prevSec = curSec;
                    }
                    return builder.build();
                })
                .collect(Collectors.toList());
        timedEdges.forEach(g::addEdge);

        // 3) Bucketing spatial pour marche
        double deltaLat  = walkingThresholdMeters / 111_000.0;
        double avgLatRad = uniqueStops.stream()
                .mapToDouble(Stop::getLat).average().orElse(0.0) * Math.PI / 180.0;
        double deltaLon  = walkingThresholdMeters / (111_000.0 * Math.cos(avgLatRad));

        Map<Cell, List<Stop>> grid = new HashMap<>();
        for (Stop s : uniqueStops) {
            int x = (int) (s.getLon() / deltaLon);
            int y = (int) (s.getLat() / deltaLat);
            Cell c = new Cell(x, y);
            grid.computeIfAbsent(c, k -> new ArrayList<>()).add(s);
        }

        // 4) Génération parallèle des arêtes de marche
        List<Edge> walkEdges = uniqueStops.parallelStream()
                .flatMap(s -> {
                    int bx = (int) (s.getLon() / deltaLon);
                    int by = (int) (s.getLat() / deltaLat);
                    Stream.Builder<Edge> builder = Stream.builder();
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            List<Stop> bucket = grid.get(new Cell(bx + dx, by + dy));
                            if (bucket == null) continue;
                            for (Stop t : bucket) {
                                if (s == t) continue;
                                double dist = haversine(s, t);
                                if (dist <= walkingThresholdMeters) {
                                    int walkTime = (int) Math.ceil(dist / walkingSpeedMps);
                                    builder.add(new Edge(s, t, walkTime));
                                }
                            }
                        }
                    }
                    return builder.build();
                })
                .collect(Collectors.toList());
        walkEdges.forEach(g::addEdge);

        return g;
    }

    /**
     * Calcul de la distance à vol d'oiseau (haversine) entre deux arrêts.
     */
    private static double haversine(Stop a, Stop b) {
        final double R = 6_371_000;
        double phi1    = Math.toRadians(a.getLat());
        double lambda1 = Math.toRadians(a.getLon());
        double phi2    = Math.toRadians(b.getLat());
        double lambda2 = Math.toRadians(b.getLon());
        double dphi    = phi2 - phi1;
        double dlambda = lambda2 - lambda1;
        double sinDphi    = Math.sin(dphi / 2);
        double sinDlambda = Math.sin(dlambda / 2);
        double h = sinDphi * sinDphi
                + Math.cos(phi1) * Math.cos(phi2)
                * sinDlambda * sinDlambda;
        return 2 * R * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }

    private record Cell(int x, int y) {}
}
