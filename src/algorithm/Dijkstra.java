package algorithm;

import algorithm.graph.Edge;
import algorithm.graph.Graph;
import data.Stop;

import java.util.*;

/**
 * Calcule les plus courts chemins (en temps) depuis une source dans un graphe statique.
 */
public class Dijkstra {
    private final Map<Stop, Integer> distTo = new HashMap<>();
    private final Map<Stop, Edge> edgeTo = new HashMap<>();

    public Dijkstra(Graph G, Stop source) {
        // 1) Initialisation
        for (Stop v : G.getStops()) {
            distTo.put(v, Integer.MAX_VALUE);
        }
        distTo.put(source, 0);

        // 2) File de priorité (min-heap sur dist)
        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.add(new Node(source, 0));

        // 3) Boucle principale
        while (!pq.isEmpty()) {
            Node n = pq.poll();
            Stop v = n.stop;
            // on ignore les entrées obsolètes
            if (n.dist > distTo.get(v)) continue;
            // relaxer toutes les arêtes sortantes
            for (Edge e : G.getOutgoing(v)) {
                relax(e, pq);
            }
        }
    }

    private void relax(Edge e, PriorityQueue<Node> pq) {
        Stop v = e.getFrom();
        Stop w = e.getTo();
        int weight = e.getTravelTimeSeconds();
        int dv = distTo.get(v);
        int dw = distTo.get(w);
        if (dv + weight < dw) {
            distTo.put(w, dv + weight);
            edgeTo.put(w, e);
            pq.add(new Node(w, dv + weight));
        }
    }

    /**
     * Distance (temps en secondes) jusqu’à v, ou Integer.MAX_VALUE si non joignable
     */
    public int distTo(Stop v) {
        return distTo.getOrDefault(v, Integer.MAX_VALUE);
    }

    /**
     * Existe-t-il un chemin de la source à v ?
     */
    public boolean hasPathTo(Stop v) {
        return distTo(v) < Integer.MAX_VALUE;
    }

    /**
     * Renvoie la liste d’arêtes du plus court chemin vers v (ordre source→v), ou null
     */
    public List<Edge> pathTo(Stop v) {
        if (!hasPathTo(v)) return null;
        LinkedList<Edge> path = new LinkedList<>();
        for (Edge e = edgeTo.get(v); e != null; e = edgeTo.get(e.getFrom())) {
            path.addFirst(e);
        }
        return path;
    }

    // wrapper pour le priority queue
    private static class Node implements Comparable<Node> {
        private final Stop stop;
        private final int dist;

        public Node(Stop stop, int dist) {
            this.stop = stop;
            this.dist = dist;
        }

        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.dist, o.dist);
        }
    }
}
