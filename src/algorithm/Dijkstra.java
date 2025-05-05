package algorithm;

import algorithm.graph.Edge;
import algorithm.graph.Graph;
import data.Stop;

import java.util.*;

public class Dijkstra {
    private final Map<Stop, Integer> distTo = new HashMap<>();
    private final Map<Stop, Edge> edgeTo = new HashMap<>();
    private PriorityQueue<Double> pq;

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
            Stop v = n.getStop();
            // on ignore les entrées obsolètes
            if (n.getDist() > distTo.get(v)) continue;
            // relaxer toutes les arêtes sortantes
            for (Edge e : G.getOutgoing(v)) {
                relax(e, pq);
            }
        }
    }

    // relax edge and update pq if changed
    private void relax(Edge edge, PriorityQueue<Node> pq) {
        Stop v = edge.getFrom();
        Stop w = edge.getTo();
        int weight = edge.getTravelTimeSeconds();
        int dv = distTo.get(v);
        int dw = distTo.get(w);
        if (dv + weight < dw) {
            distTo.put(w, dv + weight);
            edgeTo.put(w, edge);
            pq.add(new Node(w, dv + weight));
        }
    }

    public int distTo(Stop stop) {
        return distTo.getOrDefault(stop, Integer.MAX_VALUE);
    }

    public boolean hasPathTo(Stop source) {
        return distTo(source) < Integer.MAX_VALUE;
    }

    public List<Edge> pathTo(Stop v) {
        if (!hasPathTo(v)) return null;
        LinkedList<Edge> path = new LinkedList<>();
        for (Edge e = edgeTo.get(v); e != null; e = edgeTo.get(e.getFrom())) {
            path.addFirst(e);
        }
        return path;
    }
}
