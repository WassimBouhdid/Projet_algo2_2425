package algorithm.graph;

import data.Stop;

/**
 * Représente une arête du graphe de transport.
 * Peut être un trajet programmé (tripId != null) ou une marche (tripId == null).
 */
public class Edge {
    private final Stop from;
    private final Stop to;
    private final int travelTimeSec;
    private final String tripId;
    private final int departureTimeSec; // -1 si marche

    public Edge(Stop from, Stop to, int travelTimeSec, String tripId, int departureTimeSec) {
        this.from = from;
        this.to = to;
        this.travelTimeSec = travelTimeSec;
        this.tripId = tripId;
        this.departureTimeSec = departureTimeSec;
    }

    public Edge(Stop from, Stop to, int travelTimeSec) {
        this(from, to, travelTimeSec, null, -1);
    }

    public Stop getFrom() {
        return from;
    }

    public Stop getTo() {
        return to;
    }

    public int getTravelTimeSec() {
        return travelTimeSec;
    }

    public String getTripId() {
        return tripId;
    }

    public int getDepartureTimeSec() {
        return departureTimeSec;
    }
}