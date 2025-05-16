package data;

public class Trip {
    private String idRoute;
    private String idTrip;

    public Trip(String tripId, String routeId) {
        idRoute = routeId;
        idTrip = tripId;
    }

    public String getIdRoute() { return idRoute; }
    public String getIdTrip() { return idTrip; }
}
