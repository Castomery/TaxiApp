package com.example.androidtaxiapp2.Models;

import android.widget.Toast;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.optimization.v1.models.OptimizationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouteCallback implements Callback<OptimizationResponse> {

    private DirectionsRoute optimizedRoute;
    private double distance;

    @Override
    public void onResponse(Call<OptimizationResponse> call, Response<OptimizationResponse> response) {
        if (!response.isSuccessful()) {
        } else {
            if (response.body() != null) {
                List<DirectionsRoute> routes = response.body().trips();
                if (routes != null) {
                    if (routes.isEmpty()) {
                    } else {
                        // Get most optimized route from API response
                        optimizedRoute = routes.get(0);
                        distance = routes.get(0).distance();
                    }
                } else {
                }
            } else {
            }
        }
    }

    public DirectionsRoute getOptimizedRoute() {
        return optimizedRoute;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public void onFailure(Call<OptimizationResponse> call, Throwable throwable) {
    }
}

