package data;

public class Stop {
    private String stopId;
    private String name;
    private double lat;
    private double lon;

    public Stop(String stopId, String name, double lat, double lon) {
        this.stopId = stopId;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
