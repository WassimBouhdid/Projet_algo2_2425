package data;

/**
 * Représente un arrêt avec ses coordonnées gps.
 */
public class Stop {
    private final String stopId;
    private final String name;
    private final double lat;
    private final double lon;

    /**
     * @param stopId identifiant unique de l'arrêt
     * @param name   nom de l'arrêt
     * @param lat    latitude en degrés décimaux
     * @param lon    longitude en degrés décimaux
     */
    public Stop(String stopId, String name, double lat, double lon) {
        this.stopId = stopId;
        this.name   = name;
        this.lat    = lat;
        this.lon    = lon;
    }

    /**
     * @return identifiant
     */
    public String getStopId() {
        return stopId;
    }

    /**
     * @return nom de l'arrêt
     */
    public String getStopName() {
        return name;
    }

    /**
     * @return latitude
     */
    public double getLat() {
        return lat;
    }

    /**
     * @return longitude
     */
    public double getLon() {
        return lon;
    }
}
