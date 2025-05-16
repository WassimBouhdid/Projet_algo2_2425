package data;

public class Route {

    private final String routeId;
    private final String shortName;
    private final String type;

    public Route(String routeId, String shortName, String longName, String type) {
        this.routeId = routeId;
        this.shortName = shortName;
        this.type = type;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getShortName() {
        return shortName;
    }

    public String getType() {
        return type;
    }

}
