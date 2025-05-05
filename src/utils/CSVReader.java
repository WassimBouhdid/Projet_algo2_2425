package utils;

import data.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {

    public static Company loadCompany(Path directory, String companyName) {
        CSVReader csvReader = new CSVReader();
        Company company = new Company(companyName);

        company.setRoutes(csvReader.loadRoutes(directory + "/routes.csv"));
        company.setStops(csvReader.loadStops(directory + "/stops.csv"));
        company.setTrips(csvReader.loadTrips(directory + "/trips.csv"));
        company.setStopTimes(csvReader.loadStopTimes(directory + "/stop_times.csv"));

        return company;
    }

    private List<Route> loadRoutes(String filename) {
        List<Route> routes = new ArrayList<>();


        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] value = line.split(",");
                Route newRoute = new Route(value[0], value[1], value[2], value[3]);
                routes.add(newRoute);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Unknown File : " + filename);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occured while reading the file : " + filename);
            e.printStackTrace();
        }

        return routes;
    }

    private List<Trip> loadTrips(String filename) {
        List<Trip> trips = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] value = line.split(",");
                Trip newTrips = new Trip(value[0], value[1]);
                trips.add(newTrips);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Unknown File : " + filename);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occured while reading the file : " + filename);
            e.printStackTrace();
        }
        return trips;
    }

    private List<Stop> loadStops(String filename) {
        List<Stop> stops = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] value = line.split(",");

                Stop newStops = new Stop(value[0], value[1], Double.parseDouble(value[2]), Double.parseDouble(value[3]));
                stops.add(newStops);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Unknown File : " + filename);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occured while reading the file : " + filename);
            e.printStackTrace();
        }
        return stops;
    }

    private List<StopTime> loadStopTimes(String filename) {
        List<StopTime> stopTimes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] value = line.split(",");
                LocalTime departure;
                String[] parts = value[1].split(":");
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                int s = Integer.parseInt(parts[2]);
                departure = LocalTime.of(h % 24, m, s);

                stopTimes.add(new StopTime(value[0], departure, value[2], Integer.parseInt(value[3])));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unknown File : " + filename);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occured while reading the file : " + filename);
            e.printStackTrace();
        }
        return stopTimes;
    }
}
