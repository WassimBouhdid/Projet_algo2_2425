package data;

import java.time.LocalTime;

public class StopTime {
    private String tripId;
    private LocalTime departure;
    private String stopId;
    private int sequence;

    public StopTime(String tripId, LocalTime departure, String stopId, int sequence) {
        this.tripId = tripId;
        this.departure = departure;
        this.stopId = stopId;
        this.sequence = sequence;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public LocalTime getDepartureTime() {
        return departure;
    }

    public void setDeparture(LocalTime departure) {
        this.departure = departure;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public int getStopSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
