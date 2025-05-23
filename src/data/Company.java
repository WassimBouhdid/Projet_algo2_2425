package data;

import java.util.List;

/**
 * Représente une compagnie de transport.
 */
public class Company {
    private final String name;
    private List<Route> routes;
    private List<Stop> stops;
    private List<Trip> trips;
    private List<StopTime> stopTimes;

    /**
     * @param name Nom de la compagnie (STIB, SNCB, DELIJN, TEC)
     */
    public Company(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }

    public List<StopTime> getStopTimes() {
        return stopTimes;
    }

    public void setStopTimes(List<StopTime> stopTimes) {
        this.stopTimes = stopTimes;
    }
}
