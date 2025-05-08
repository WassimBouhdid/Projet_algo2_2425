package algorithm.graph;

import data.Stop;

import java.util.*;

public class Graph {
    private final Map<Stop, List<Edge>> adj;

    public Graph(int expectedStops) {
        this.adj = new HashMap<>(expectedStops);
    }

    public void addStop(Stop s) {
        adj.putIfAbsent(s, new ArrayList<>());
    }

    public void addEdge(Edge e) {
        addStop(e.getFrom());
        addStop(e.getTo());
        adj.get(e.getFrom()).add(e);
    }

    public List<Edge> getOutgoing(Stop s) {
        return adj.getOrDefault(s, Collections.emptyList());
    }

    public Set<Stop> getStops() {
        return adj.keySet();
    }


}
