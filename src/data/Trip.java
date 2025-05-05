package data;

public class Trip {
    private String tripId;
    private String routeId;

    public Trip(String tripId, String routeId) {
        this.tripId = tripId;
        this.routeId = routeId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
}
