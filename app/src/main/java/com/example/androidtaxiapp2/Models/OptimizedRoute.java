package com.example.androidtaxiapp2.Models;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;

import java.util.List;

public class OptimizedRoute {
    private String route;
    private double distance;

    public OptimizedRoute( String route, double distance) {
        this.route = route;
        this.distance = distance;
    }
    public OptimizedRoute() {
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
