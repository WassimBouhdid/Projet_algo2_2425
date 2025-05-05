package algorithm.graph;

import data.Stop;

public class Edge {
    private final Stop from;
    private final Stop to;
    private final int travelTimeSeconds;  // en secondes

    public Edge(Stop from, Stop to, int travelTimeSeconds) {
        this.from = from;
        this.to = to;
        this.travelTimeSeconds = travelTimeSeconds;
    }

    public Stop getFrom() { return from; }
    public Stop getTo()   { return to; }
    public int getTravelTimeSeconds() { return travelTimeSeconds; }
}