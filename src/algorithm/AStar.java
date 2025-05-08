package algorithm;

import algorithm.graph.Edge;
import algorithm.graph.Graph;
import data.Stop;

import java.util.*;

/**
 * A* Shortest Paths: distTo[] = g-score, fScore = g-score + heuristic
 */
public class AStar {
    private final Map<Stop, Integer> distTo = new HashMap<>();      // g(u)
    private final Map<Stop, Edge> edgeTo = new HashMap<>();
    private final Stop goal;

    public AStar(Graph G, Stop source, Stop goal) {
        this.goal = goal;
        // 1) initialisation
        for (Stop v : G.getStops()) {
            distTo.put(v, Integer.MAX_VALUE);
        }
        distTo.put(source, 0);

        // 2) min-heap sur f(u) = g(u) + h(u)
        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.add(new Node(source, 0, heuristic(source)));

        // 3) boucle principale
        while (!pq.isEmpty()) {
            Node n = pq.poll();
            Stop u = n.stop;
            // on ignore les entrées obsolètes
            if (n.fScore > distTo.get(u) + heuristic(u)) continue;
            // si on arrive à la cible, on peut arrêter
            if (u.equals(goal)) break;

            for (Edge e : G.getOutgoing(u)) {
                relax(e, pq);
            }
        }
    }

    private void relax(Edge e, PriorityQueue<Node> pq) {
        Stop u = e.getFrom();
        Stop v = e.getTo();
        int weight = e.getTravelTimeSeconds();
        int gU = distTo.get(u);
        int gV = distTo.getOrDefault(v, Integer.MAX_VALUE);

        if (gU + weight < gV) {
            distTo.put(v, gU + weight);
            edgeTo.put(v, e);
            double fV = gU + weight + heuristic(v);
            pq.add(new Node(v, gU + weight, fV));
        }
    }

    /** Heuristique admissible : distance à vol d’oiseau / vitesse piétonne (~1.4 m/s) */
    private double heuristic(Stop u) {
        double R = 6_371_000; // rayon terre en m
        double lat1 = Math.toRadians(u.getLat());
        double lon1 = Math.toRadians(u.getLon());
        double lat2 = Math.toRadians(goal.getLat());
        double lon2 = Math.toRadians(goal.getLon());
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(lat1)*Math.cos(lat2)*Math.sin(dLon/2)*Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = R * c;             // en mètres
        return dist / 1.4;               // en secondes (v ~1.4 m/s)
    }

    /** g-score vers v, ou ∞ si non atteint */
    public int distTo(Stop v) {
        return distTo.getOrDefault(v, Integer.MAX_VALUE);
    }

    /** Y a-t-il un chemin vers v ? */
    public boolean hasPathTo(Stop v) {
        return distTo(v) < Integer.MAX_VALUE;
    }

    /** Reconstruction du chemin source→v */
    public List<Edge> pathTo(Stop v) {
        if (!hasPathTo(v)) return null;
        LinkedList<Edge> path = new LinkedList<>();
        for (Edge e = edgeTo.get(v); e != null; e = edgeTo.get(e.getFrom())) {
            path.addFirst(e);
        }
        return path;
    }

    private static class Node implements Comparable<Node> {
        final Stop stop;
        final int gScore;
        final double fScore;

        Node(Stop stop, int gScore, double fScore) {
            this.stop   = stop;
            this.gScore = gScore;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(this.fScore, o.fScore);
        }
    }
}
