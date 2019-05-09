/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttp.ttp_webservice;

import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.ttp.ttp_commons.Location;
import com.ttp.ttp_commons.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Graham
 */
public class TtpCalculation {
    
    public static List<Path> runSolutions(LatLng[] locationList, String apiKey, String mode){
        GeoApiContext mGeoApicontext = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
        List<Location> locations = getLocations(locationList);
        List<Path> paths = getPaths(locations, mGeoApicontext, mode);
        List<Path> route = createRoute(paths, locations);
        return route;
    }

    private static List<Path> getPaths(List<Location> locations, GeoApiContext mGeoApicontext, String mode) {
        List<Path> paths = new ArrayList<>();
        for(int i = 0; i < locations.size(); i++ ){
            for(int j = 0; j < locations.size(); j++){
                if(!(i == j)){
                    Path path = new Path(locations.get(i), locations.get(j));
                    path.calculateDirections(mGeoApicontext, mode);
                    paths.add(path);
                }
            }
        }
        return paths;
    }

    private static List<Location> getLocations(LatLng[] locationList) {
        List<Location> locations = new ArrayList<>();
        for(int i = 0; i < locationList.length; i++){
            locations.add(new Location(locationList[i], (i == 0)));
        }
        return locations;
    }
    
     private static List<Path> createRoute(List<Path> paths, List<Location> locations){
        List<Path> route = new ArrayList<>();
        route.addAll(calculate2Opt(paths, locations));
        return route;
    }   
     
    private static List<Path> calculateNNRoute(List<Path> paths, List<Location> locs) {
        Location start = locs.stream().filter(x -> x.isStartingLoc() == true).collect(Collectors.toList()).get(0);
        Location current = start;
 
        List<Path> route = new ArrayList<>();
        for(int i = 0; i < locs.size() - 1; i++){
            current.setInRoute();
            Path shortest = getShortestPath(paths, current, locs);
            route.add(shortest);
            current = shortest.getTo();
        }
        route.add(getLastPath(paths, current, start));
        
        return route;
    }

    private static Path getShortestPath(List<Path> paths, Location current, List<Location> locations) {
        List<Path> currentPaths = getCurrentPaths(paths, current, locations);
        currentPaths.sort((Path p1, Path p2) -> Long.compare(p1.getDistance(), p2.getDistance()));
        return currentPaths.get(0);
    }
    
    private static Path getLastPath(List<Path> paths, Location current, Location start) {
        return paths.stream()
                .filter(x -> x.getFrom() == current && x.getTo() == start)
                .collect(Collectors.toList())
                .get(0);
    }
    
    
    private static List<Path> getCurrentPaths(List<Path> paths, Location current, List<Location> locations) {
        return paths.stream()
                .filter(x -> x.getFrom() == current && !locations.get(locations.indexOf(x.getTo())).getInRoute())
                .collect(Collectors.toList());
    }
    
     private static List<Path> calculate2Opt(List<Path> paths, List<Location> locs) {
        List<Path> route = calculateNNRoute(paths, locs);
        Path tempStart = route.get(0);
        Path tempEnd = route.get(route.size() - 1);
        route.remove(0);
        if (!route.isEmpty()) {
            route.remove(route.size() - 1);
        }
        double bestDis = CalculateTotalDistance(route);
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 0; i < route.size() - 1; i++) {
                for (int j = i + 1; j < route.size(); j++) {
                    List<Path> newRoute = TwoOptSwap(route, i, j);
                    double newDistance = CalculateTotalDistance(newRoute);
                    if (newDistance < bestDis) {
                        route = newRoute;
                        bestDis = newDistance;
                        improved = true;
                    }
                }
            }
        }
        route.add(0, tempStart);
        route.add(tempEnd);

        return route;
    }

    private static double CalculateTotalDistance(List<Path> route) {
        double total = 0;
        for(Path path : route){
            total += path.getDistance().doubleValue();
        }
        return total;
    }

    private static List<Path> TwoOptSwap(List<Path> route, int i, int j) {
        List<Path> newRoute = new ArrayList<>();
        for(int c = 0; c < i; c++){
            newRoute.add(route.get(c));
        }
        int dec = 0;
        for(int c = i; c < j; c++){
            newRoute.add(route.get(j - dec));
        }
        for(int c = j; c < route.size(); c++){
            newRoute.add(route.get(c));
        }
        return newRoute;
    }

    
}
