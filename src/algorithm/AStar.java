package algorithm;

import algorithm.graph.Edge;
import algorithm.graph.Graph;
import data.Route;
import data.Stop;
import data.Trip;

import java.time.LocalTime;
import java.util.*;

/**
 * A* Shortest Paths: distTo[] = g-score, fScore = g-score + heuristic
 */
public class AStar {
    private record State(Stop stop, int timeSec, int gCost, int fCost, State parent, Edge via) implements Comparable<State> {
        @Override
            public int compareTo(State o) {
                return Integer.compare(this.fCost, o.fCost);
            }
        }

    private final Graph graph;
    private final Stop source;
    private final Stop target;
    private final int departureSec;
    private final Map<Stop, List<Edge>> adj;
    private final Map<String, Trip> allTrips;
    private final Map<String, Route> allRoutes;
    private final List<String> trasportMod;

    public AStar(Graph graph, Stop source, Map<String, Trip> allTrips, Map<String, Route> allRoutes, Stop target, LocalTime departure,List<String> trasportMod) {
        this.graph = graph;
        this.source = source;
        this.target = target;
        this.departureSec = departure.toSecondOfDay();
        this.adj = graph.getAdjacencyMap(); // suppose expose Map<Stop,List<Edge>>

        this.allTrips = allTrips;
        this.allRoutes = allRoutes;
        this.trasportMod = trasportMod;
    }

    public List<Edge> pathTo() {
        PriorityQueue<State> open = new PriorityQueue<>();
        Map<Stop, Integer> bestTime = new HashMap<>();

        State start = new State(source, departureSec,0,
                heuristicSec(source), null, null);
        open.add(start);
        bestTime.put(source, departureSec);

        State endState = null;

        while (!open.isEmpty()) {
            State cur = open.poll();
            if (cur.stop.equals(target)) {
                endState = cur;
                break;
            }
            // Ignore stale states
            if (cur.timeSec > bestTime.getOrDefault(cur.stop, Integer.MAX_VALUE))
                continue;

            for (Edge e : adj.getOrDefault(cur.stop, Collections.emptyList())) {
                int depart = cur.timeSec;
                String transportType = "";
                if (e.getTripId() != null) {
                    String tripId = e.getTripId();
                    String routeId = allTrips.get(tripId).getIdRoute();
                    transportType = allRoutes.get(routeId).getType();
                    // horaire : on doit attendre le prochain départ
                    int sched = e.getTotalPoints();
                    if (sched < depart) sched += 24*3600;
                    depart = sched;
                }

                int arrive;
                if(trasportMod.contains(transportType)){
                    arrive = depart + e.getTotalPoints();
                }else{
                    arrive = depart + e.getTotalPoints() + 100000;
                }



                Stop next = e.getTo();
                if (arrive < bestTime.getOrDefault(next, Integer.MAX_VALUE)) {
                    bestTime.put(next, arrive);
                    int g = arrive - departureSec;
                    int f = g + heuristicSec(next);
                    open.add(new State(next, arrive, g, f, cur, e));
                }
            }
        }

        if (endState == null) return null;
        // Reconstituer le chemin
        LinkedList<Edge> path = new LinkedList<>();
        for (State s = endState; s.via != null; s = s.parent) {
            path.addFirst(s.via);
        }
        return path;
    }

    /** Heuristique: distance à vol d'oiseau / vitesse max (en secondes) */
    private int heuristicSec(Stop s) {
        double dist = graph.haversine(s, target);
        double maxSpeed = 30.0; // m/s (~108 km/h)
        return (int)(dist / maxSpeed);
    }

}
