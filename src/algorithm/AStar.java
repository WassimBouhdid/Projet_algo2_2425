package algorithm;

import algorithm.graph.Edge;
import algorithm.graph.Graph;
import data.Stop;

import java.time.LocalTime;
import java.util.*;

/**
 * A* : distTo[] = g-score, fScore = g-score + heuristic
 */
public class AStar {
    private record State(
            Stop stop,
            int timeSec,       // instant actuel en secondes depuis minuit
            int gCost,         // coût accumulé
            int fCost,         // gCost + heuristique
            State parent,      // état précédent
            Edge via           // arc emprunté pour arriver ici
    ) implements Comparable<State> {
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
    private final CostFunction costFunction;

    /**
     * Constructeur pour initialiser A*
     */
    public AStar(Graph graph, Stop source, Stop target, LocalTime departure, CostFunction costFunction) {
        this.graph = graph;
        this.source = source;
        this.target = target;
        this.departureSec = departure.toSecondOfDay();
        this.adj = graph.getAdjacencyMap();
        this.costFunction = costFunction;
    }

    /**
     * @return liste des arcs empruntés pour arriver à destination.
     */
    public List<Edge> pathTo() {
        PriorityQueue<State> open = new PriorityQueue<>();
        Map<Stop, Integer> bestTime = new HashMap<>();

        State start = new State(source, departureSec, 0, heuristicSec(source), null, null);
        open.add(start);
        bestTime.put(source, departureSec);

        State endState = null;
        while (!open.isEmpty()) {
            State cur = open.poll();
            if (cur.stop.equals(target)) {
                endState = cur;
                break;
            }

            if (cur.timeSec > bestTime.getOrDefault(cur.stop, Integer.MAX_VALUE))
                continue;

            for (Edge e : adj.getOrDefault(cur.stop, Collections.emptyList())) {
                int depart = cur.timeSec;
                if (e.getTripId() != null) {
                    int sched = e.getDepartureTimeSec();
                    if (sched < depart) sched += 24 * 3600;
                    depart = sched;
                }

                int c = costFunction.cost(e, cur.via);
                int arrive = depart + c;

                Stop next = e.getTo();
                if (arrive < bestTime.getOrDefault(next, Integer.MAX_VALUE)) {
                    bestTime.put(next, arrive);
                    int g = cur.gCost + c;
                    int f = g + heuristicSec(next);
                    open.add(new State(next, arrive, g, f, cur, e));
                }
            }
        }

        if (endState == null) return null;
        LinkedList<Edge> path = new LinkedList<>();
        for (State s = endState; s.via != null; s = s.parent)
            path.addFirst(s.via);
        return path;
    }

    /**
     * Heuristique: distance à vol d'oiseau / vitesse max (en secondes)
     */
    private int heuristicSec(Stop s) {
        double dist = graph.haversine(s, target);
        double maxSpeed = 30.0; // m/s
        return (int) (dist / maxSpeed);
    }
}
