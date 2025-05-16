package algorithm.graph;

import data.Stop;
import data.StopTime;

import java.util.*;
import java.util.concurrent.*;

/**
 * Optimized static graph builder for transit network.
 * Minimizes allocations and uses multithreading to build the graph in under 20s.
 */
public class GraphBuilder {
    private static final double DEFAULT_WALKING_THRESHOLD_METERS = 500.0;
    private static final double DEFAULT_WALKING_SPEED_MPS = 1.4;
    private static final int THREADS = Runtime.getRuntime().availableProcessors();

    public static Graph buildStaticGraph(List<Stop> stops, List<StopTime> stopTimes) {
        return buildStaticGraph(stops, stopTimes, DEFAULT_WALKING_THRESHOLD_METERS, DEFAULT_WALKING_SPEED_MPS);
    }

    public static Graph buildStaticGraph(
            List<Stop> stops,
            List<StopTime> stopTimes,
            double walkingThresholdMeters,
            double walkingSpeedMps
    ) {
        // 0) Unique stops by ID
        Map<String, Stop> stopById = new LinkedHashMap<>(stops.size());
        for (Stop s : stops) stopById.putIfAbsent(s.getStopId(), s);
        Stop[] uniqueStops = stopById.values().toArray(new Stop[0]);

        Graph g = new Graph(uniqueStops.length);
        for (Stop s : uniqueStops) g.addStop(s);

        // 1) Timetabled edges
        Map<String, List<StopTime>> byTrip = new HashMap<>();
        for (StopTime st : stopTimes) {
            byTrip.computeIfAbsent(st.getTripId(), k -> new ArrayList<>()).add(st);
        }

        ForkJoinPool fj = new ForkJoinPool(THREADS);
        try {
            fj.submit(() -> byTrip.values().parallelStream().forEach(seq -> {
                seq.sort(Comparator.comparingInt(StopTime::getStopSequence));
                String prevId = null;
                int prevSec = 0;
                for (StopTime st : seq) {
                    String curId = st.getStopId();
                    int curSec = st.getDepartureTime().toSecondOfDay();
                    if (prevId != null) {
                        int delta = curSec - prevSec;
                        if (delta < 0) delta += 24 * 3600;
                        g.addEdge(new Edge(stopById.get(prevId), stopById.get(curId), delta, st.getTripId(), prevSec));
                    }
                    prevId = curId;
                    prevSec = curSec;
                }
            })).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        }

        // 2) Spatial bucketing
        final double deltaLat = walkingThresholdMeters / 111000.0;
        double avgLat = 0;
        for (Stop s : uniqueStops) avgLat += s.getLat();
        avgLat = (avgLat / uniqueStops.length) * Math.PI / 180.0;
        final double deltaLon = walkingThresholdMeters / (111000.0 * Math.cos(avgLat));

        int gx = (int) (360.0 / deltaLon) + 1;
        int gy = (int) (180.0 / deltaLat) + 1;
        List<Stop>[][] grid = new List[gx][gy];
        for (Stop s : uniqueStops) {
            int x = (int) ((s.getLon()+180.0) / deltaLon);
            int y = (int) ((s.getLat()+90.0) / deltaLat);
            var bucket = grid[x][y];
            if (bucket == null) grid[x][y] = bucket = new ArrayList<>();
            bucket.add(s);
        }

        // 3) Walking edges
        fj.submit(() -> Arrays.stream(uniqueStops).parallel().forEach(s -> {
            int bx = (int) ((s.getLon()+180.0) / deltaLon);
            int by = (int) ((s.getLat()+90.0) / deltaLat);
            for (int dx = -1; dx <= 1; dx++) {
                int nx = bx + dx;
                if (nx < 0 || nx >= gx) continue;
                for (int dy = -1; dy <= 1; dy++) {
                    int ny = by + dy;
                    if (ny < 0 || ny >= gy) continue;
                    var bucket = grid[nx][ny];
                    if (bucket == null) continue;
                    for (Stop t : bucket) {
                        if (s == t) continue;
                        double d = haversine(s, t);
                        if (d <= walkingThresholdMeters) {
                            int wt = (int) Math.ceil(d / walkingSpeedMps);
                            g.addEdge(new Edge(s, t, wt));
                        }
                    }
                }
            }
        })).join();
        fj.shutdown();

        return g;
    }

    private static double haversine(Stop a, Stop b) {
        final double R = 6371000;
        double dLat = Math.toRadians(b.getLat() - a.getLat());
        double dLon = Math.toRadians(b.getLon() - a.getLon());
        double sinLat = Math.sin(dLat * 0.5);
        double sinLon = Math.sin(dLon * 0.5);
        double x = sinLat * sinLat + Math.cos(Math.toRadians(a.getLat())) * Math.cos(Math.toRadians(b.getLat())) * sinLon * sinLon;
        return 2 * R * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x));
    }
}