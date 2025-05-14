package algorithm.graph;

import data.Stop;
import data.StopTime;

import java.util.*;

public class GraphBuilder {
    // valeurs par défaut pour la marche
    private static final double DEFAULT_WALKING_THRESHOLD_METERS = 500.0;
    private static final double DEFAULT_WALKING_SPEED_MPS        = 1.4;

    /**
     * Surcharge « simplifiée » : pas besoin de préciser seuil et vitesse, on prend les valeurs par défaut.
     */
    public static Graph buildStaticGraph(List<Stop> stops, List<StopTime> stopTimes) {
        return buildStaticGraph(
                stops,
                stopTimes,
                DEFAULT_WALKING_THRESHOLD_METERS,
                DEFAULT_WALKING_SPEED_MPS
        );
    }

    /**
     * Version complète, avec seuil de marche et vitesse.
     */
    public static Graph buildStaticGraph(
            List<Stop> stops,
            List<StopTime> stopTimes,
            double walkingThresholdMeters,
            double walkingSpeedMps
    ) {
        // 1) Connexions timetabled
        Graph g = buildStaticGraphNoWalk(stops, stopTimes);

        // 2) Ajout des liaisons piétonnes (bucketing)
        addWalkingEdges(g, stops, walkingThresholdMeters, walkingSpeedMps);

        return g;
    }

    private static Graph buildStaticGraphNoWalk(List<Stop> stops, List<StopTime> stopTimes) {
        Graph g = new Graph(stops.size());
        Map<String, Stop> stopById = new HashMap<>(stops.size());
        for (Stop s : stops) {
            g.addStop(s);
            stopById.put(s.getStopId(), s);
        }
        Map<String, List<StopTime>> byTrip = new HashMap<>(stopTimes.size() / 4);
        for (StopTime st : stopTimes) {
            byTrip.computeIfAbsent(st.getTripId(), k -> new ArrayList<>()).add(st);
        }
        for (List<StopTime> seq : byTrip.values()) {
            seq.sort(Comparator.comparingInt(StopTime::getStopSequence));
            String prevId = null;
            int prevSec = 0;
            for (StopTime st : seq) {
                String curId = st.getStopId();
                int curSec = st.getDepartureTime().toSecondOfDay();
                if (prevId != null) {
                    int delta = curSec - prevSec;
                    if (delta < 0) delta += 24 * 3600;
                    Stop from = stopById.get(prevId);
                    Stop to   = stopById.get(curId);
                    if (from != null && to != null) {
                        g.addEdge(new Edge(from, to, delta, st.getTripId()));
                    }
                }
                prevId  = curId;
                prevSec = curSec;
            }
        }
        return g;
    }

    private static void addWalkingEdges(
            Graph g,
            List<Stop> stops,
            double thresholdMeters,
            double walkingSpeedMps
    ) {
        double deltaLat = thresholdMeters / 111_000.0;
        double avgLatRad = stops.stream()
                .mapToDouble(Stop::getLat)
                .average().orElse(0.0) * Math.PI / 180.0;
        double deltaLon = thresholdMeters / (111_000.0 * Math.cos(avgLatRad));

        Map<Cell, List<Stop>> grid = new HashMap<>();
        for (Stop s : stops) {
            Cell c = new Cell(
                    (int)(s.getLon() / deltaLon),
                    (int)(s.getLat() / deltaLat)
            );
            grid.computeIfAbsent(c, k -> new ArrayList<>()).add(s);
        }

        for (Stop s : stops) {
            Cell base = new Cell(
                    (int)(s.getLon() / deltaLon),
                    (int)(s.getLat() / deltaLat)
            );
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    Cell neighbour = new Cell(base.x + dx, base.y + dy);
                    List<Stop> bucket = grid.get(neighbour);
                    if (bucket == null) continue;
                    for (Stop t : bucket) {
                        if (s == t) continue;
                        double dist = haversine(s, t);
                        if (dist <= thresholdMeters) {
                            int walkTime = (int)Math.ceil(dist / walkingSpeedMps);
                            g.addEdge(new Edge(s, t, walkTime));
                        }
                    }
                }
            }
        }
    }

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
                + Math.cos(phi1) * Math.cos(phi2) * sinDlambda * sinDlambda;
        return 2 * R * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }

    private static class Cell {
        final int x, y;
        Cell(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Cell)) return false;
            Cell c = (Cell) o;
            return c.x == x && c.y == y;
        }
        @Override public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}