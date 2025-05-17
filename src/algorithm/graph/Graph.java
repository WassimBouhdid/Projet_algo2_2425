package algorithm.graph;

import data.Stop;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Représente un graphe dirigé statique d'arrêts
 */
public class Graph {
    private final Map<Stop, List<Edge>> adj;

    public Graph(int initialCapacity) {
        this.adj = new LinkedHashMap<>(initialCapacity);
    }

    public void addStop(Stop stop) {
        adj.putIfAbsent(stop, new ArrayList<>());
    }

    public void addEdge(Edge edge) {
        List<Edge> list = adj.get(edge.getFrom());
        if (list == null) throw new IllegalArgumentException("Stop non reconnu: " + edge.getFrom());
        list.add(edge);
    }

    public Map<Stop, List<Edge>> getAdjacencyMap() {
        return Collections.unmodifiableMap(adj);
    }

    public double haversine(Stop a, Stop b) {
        final double R = 6_371_000;
        double phi1 = Math.toRadians(a.getLat());
        double lambda1 = Math.toRadians(a.getLon());
        double phi2 = Math.toRadians(b.getLat());
        double lambda2 = Math.toRadians(b.getLon());
        double dphi = phi2 - phi1;
        double dlambda = lambda2 - lambda1;
        double sinDphi = Math.sin(dphi/2);
        double sinDlambda = Math.sin(dlambda/2);
        double h = sinDphi*sinDphi + Math.cos(phi1)*Math.cos(phi2)*sinDlambda*sinDlambda;
        return 2*R*Math.atan2(Math.sqrt(h), Math.sqrt(1-h));
    }
}
