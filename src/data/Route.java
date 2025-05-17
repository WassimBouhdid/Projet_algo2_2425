package data;

/**
 * Représente une ligne de transport.
 */
public class Route {

    private final String routeId;
    private final String shortName;
    private final String type;

    /**
     * @param routeId   Identifiant de la ligne
     * @param shortName Nom court de la ligne
     * @param longName  Nom long de la ligne
     * @param type      Type de transport (ex. "BUS", "TRAM")
     */
    public Route(String routeId, String shortName, String longName, String type) {
        this.routeId = routeId;
        this.shortName = shortName;
        this.type = type;
    }

    /** @return Identifiant de la ligne */
    public String getRouteId() {
        return routeId;
    }

    /** @return Nom court de la ligne */
    public String getShortName() {
        return shortName;
    }

    /** @return Type de transport */
    public String getType() {
        return type;
    }
}
