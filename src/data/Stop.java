package data;

public class Stop {
    private final String stopId;
    private final String name;
    private final double lat;
    private final double lon;

    public Stop(String stopId, String name, double lat, double lon) {
        this.stopId = stopId;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public String getStopId() {
        return stopId;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public String getStopName() {
        return name;
    }
}
