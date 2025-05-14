package algorithm.graph;

import data.Stop;

public class Edge {
    private final Stop from;
    private final Stop to;
    private final int travelTimeSeconds;  // en secondes
    private final String tripId;          // identifiant du trip, null si marche

    /**
     * Constructeur pour les arêtes timetabled (bus, tram, train, etc.).
     * @param from                arrêt de départ
     * @param to                  arrêt d'arrivée
     * @param travelTimeSeconds   durée en secondes
     * @param tripId              identifiant du trip (ex. "STIB-1234"), null si marche
     */
    public Edge(Stop from, Stop to, int travelTimeSeconds, String tripId) {
        this.from = from;
        this.to = to;
        this.travelTimeSeconds = travelTimeSeconds;
        this.tripId = tripId;
    }

    /**
     * Constructeur « simplifié » pour les liaisons piétonnes (tripId = null).
     */
    public Edge(Stop from, Stop to, int travelTimeSeconds) {
        this(from, to, travelTimeSeconds, null);
    }

    public Stop getFrom() {
        return from;
    }

    public Stop getTo() {
        return to;
    }

    public int getTravelTimeSeconds() {
        return travelTimeSeconds;
    }

    /**
     * @return le tripId si c'est un segment timetabled, ou null pour un déplacement à pied
     */
    public String getTripId() {
        return tripId;
    }
}
