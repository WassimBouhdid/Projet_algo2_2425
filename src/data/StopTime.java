package data;

import java.time.LocalTime;

/**
 * Représente un horaire d'arrêt d'un trajet.
 */
public class StopTime {
    private final String tripId;
    private final LocalTime departure;
    private final String stopId;
    private final int sequence;

    /**
     * @param tripId    identifiant du trajet
     * @param departure heure de départ à cet arrêt
     * @param stopId    identifiant de l'arrêt
     * @param sequence  position de l'arrêt dans l'ordre du trajet
     */
    public StopTime(String tripId, LocalTime departure, String stopId, int sequence) {
        this.tripId = tripId;
        this.departure = departure;
        this.stopId = stopId;
        this.sequence = sequence;
    }

    /**
     * @return l'identifiant du trajet
     */
    public String getTripId() {
        return tripId;
    }

    /**
     * @return l'heure de départ à cet arrêt
     */
    public LocalTime getDepartureTime() {
        return departure;
    }

    /**
     * @return l'identifiant de l'arrêt
     */
    public String getStopId() {
        return stopId;
    }

    /**
     * @return la position de cet arrêt dans le trajet
     */
    public int getStopSequence() {
        return sequence;
    }
}
