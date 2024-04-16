package com.example.androidtaxiapp2.Utils;

import java.util.List;

public class ShortestRoute {
    public List<String> route;
    public List<String> pointsNames;
    public double distance;
    public double duration;
    public double totalPrice;
    public double[] priceDistribution;
    public List<String> getRoute() {
        return route;
    }

    public List<String> getPointsNames() {
        return pointsNames;
    }

    public void setPointsNames(List<String> pointsNames) {
        this.pointsNames = pointsNames;
    }

    public void setRoute(List<String> route) {
        this.route = route;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double[] getPriceDistribution() {
        return priceDistribution;
    }

    public void setPriceDistribution(double[] priceDistribution) {
        this.priceDistribution = priceDistribution;
    }
}
