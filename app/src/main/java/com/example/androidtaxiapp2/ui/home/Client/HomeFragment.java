package com.example.androidtaxiapp2.ui.home.Client;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.androidtaxiapp2.Activities.CreateGoupRideActivity;
import com.example.androidtaxiapp2.Models.Common;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsAdapterFactory;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.optimization.v1.MapboxOptimization;

import com.example.androidtaxiapp2.R;
import com.example.androidtaxiapp2.databinding.FragmentHomeBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.api.optimization.v1.models.OptimizationResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.OnLocationClickListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment implements OnMapReadyCallback, OnLocationClickListener, PermissionsListener, OnCameraTrackingChangedListener {

    private static final String ICON_GEOJSON_SOURCE_ID = "icon-source-id";
    private static final String FIRST = "first";
    private static final String LAST = "last";
    private static final String TEAL_COLOR = "#23D2BE";
    private static final float POLYLINE_WIDTH = 5;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private DirectionsRoute optimizedRoute;
    private MapboxOptimization optimizedClient;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;

    private List<Point> stops = new ArrayList<>();
    private Point origin;
    private FloatingActionButton myLocationButton;
    private List<Point> destinations = new ArrayList<>();
    private boolean isInTrackingMode;
    OkHttpClient client;

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private UUID currOrderUid;
    private int currPermutationId;

    private Button showRoute;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Mapbox.getInstance(getContext(),getString(R.string.mapbox_access_token));

        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        client = new OkHttpClient();

        mapView = binding.mapView;
        showRoute = binding.showRoute;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        showRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateGoupRideActivity.class);
                startActivity(intent);
            }
        });

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.OPTIMIZED_ROUTES_REFERENCE);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        if (optimizedClient != null) {
            optimizedClient.cancelCall();
        }
        mapView.onDestroy();
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {

            // Add origin and destination to the mapboxMap
            enableLocationComponent(style);
//            showRoute.setOnClickListener(v -> {
//
//                initMarkerIconSymbolLayer(style);
//                initOptimizedRouteLineLayer(style);
//                addDestinationMarker(style,stops);
//                getOptimizedRoute(style,stops);
//            });
        });
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getActivity())) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(getActivity(), loadedMapStyle).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            origin = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),locationComponent.getLastKnownLocation().getLatitude());

            locationComponent.addOnLocationClickListener(this);

                    // Add the camera tracking listener. Fires if the map camera is manually moved.
            locationComponent.addOnCameraTrackingChangedListener(this);

            binding.focusLocation.setOnClickListener(view -> {
                if (!isInTrackingMode) {
                    isInTrackingMode = true;
                    locationComponent.setCameraMode(CameraMode.TRACKING);
                    locationComponent.zoomWhileTracking(16f);
                } else {

                }
            });



        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    private void post(@NonNull Style loadedMapStyle){

        String body = "[\"32.043707542675094,49.45315589003039\",\"32.07101602980575,49.4464253366084\"]";

        String org = origin.longitude()+","+origin.latitude();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);

        Request request = new Request.Builder().url("http://10.0.2.2:5249/api/Distribution?origin="+org)
                .post(requestBody)
                .addHeader("accept", "text/plain")
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(),"Failed", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        List<String> coordinates = null;
                        try {
                            coordinates = gson.fromJson(response.body().string(), List.class);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        Log.d("TAG", coordinates.toString());
                        List<Point> routeCoord = parseToPoints(coordinates);
                        addDestinationMarker(loadedMapStyle,routeCoord);
                        Log.d("TAG", routeCoord.toString());
                        getOptimizedRoute(loadedMapStyle,routeCoord);
                    }
                });
            }
        });
    }

    private List<Point> parseToPoints(List<String> coordinates) {
        List<Point> result = new ArrayList<>();

        for (String coord : coordinates){
            String[] parts = coord.split(",");
            double lon = Double.parseDouble(parts[0]);
            double lat = Double.parseDouble(parts[1]);
            result.add(Point.fromLngLat(lon,lat));
        }
        return result;
    }

    private String ListOfPointsToString(List<Point> coordinates){
        String route = "";
        for (int i = 0; i < coordinates.size(); i++){
            List<Double> cord = coordinates.get(i).coordinates();
            route += cord.get(0).toString()+","+cord.get(1).toString();
            if (i < coordinates.size()-1){
                route+=";";
            }
        }
        return route;
    }

    private void initMarkerIconSymbolLayer(@NonNull Style loadedMapStyle) {
        // Add the marker image to map
        Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.red_marker);
        loadedMapStyle.addImage("icon-image", icon);

        // Add the source to the map

        loadedMapStyle.addSource(new GeoJsonSource(ICON_GEOJSON_SOURCE_ID,
                Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude()))));

        loadedMapStyle.addLayer(new SymbolLayer("icon-layer-id", ICON_GEOJSON_SOURCE_ID).withProperties(
                iconImage("icon-image"),
                iconSize(1f),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                iconOffset(new Float[] {0f, -7f})
        ));
    }

    private void addDestinationMarker(@NonNull Style style, List<Point> coords) {
        List<Feature> destinationMarkerList = new ArrayList<>();
        for (Point singlePoint : coords) {
            destinationMarkerList.add(Feature.fromGeometry(
                    Point.fromLngLat(singlePoint.longitude(), singlePoint.latitude())));
        }
        GeoJsonSource iconSource = style.getSourceAs(ICON_GEOJSON_SOURCE_ID);
        if (iconSource != null) {
            iconSource.setGeoJson(FeatureCollection.fromFeatures(destinationMarkerList));
        }
    }

    private void initOptimizedRouteLineLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource("optimized-route-source-id"));
        loadedMapStyle.addLayerBelow(new LineLayer("optimized-route-layer-id", "optimized-route-source-id")
                .withProperties(
                        lineColor(Color.parseColor(TEAL_COLOR)),
                        lineWidth(POLYLINE_WIDTH)
                ), "icon-layer-id");
    }

    private void drawOptimizedRoute(@NonNull Style style, DirectionsRoute route) {
        GeoJsonSource optimizedLineSource = style.getSourceAs("optimized-route-source-id");
        if (optimizedLineSource != null) {
            optimizedLineSource.setGeoJson(FeatureCollection.fromFeature(Feature.fromGeometry(
                    LineString.fromPolyline(route.geometry(), PRECISION_6))));
        }
    }

    private void getOptimizedRoute(@NonNull final Style style, List<Point> coordinates) {
        optimizedClient = MapboxOptimization.builder()
                .source(FIRST)
                .destination(LAST)
                .coordinates(coordinates)
                .roundTrip(false)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.mapbox_access_token))
                .build();

        optimizedClient.enqueueCall(new retrofit2.Callback<OptimizationResponse>() {


            @Override
            public void onResponse(retrofit2.Call<OptimizationResponse> call, retrofit2.Response<OptimizationResponse> response) {
                if (!response.isSuccessful()) {
                } else {
                    if (response.body() != null) {
                        List<DirectionsRoute> routes = response.body().trips();
                        if (routes != null) {
                            if (routes.isEmpty()) {
                            } else {
                                // Get most optimized route from API response
                                optimizedRoute = routes.get(0);
                                Log.d("TAG", routes.get(0).toString());
                                drawOptimizedRoute(style, optimizedRoute);
                            }
                        } else {
                        }
                    } else {
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<OptimizationResponse> call, Throwable t) {
            }
        });
    }
    @Override
    public void onExplanationNeeded(List<String> list) {
    }

    @Override
    public void onPermissionResult(boolean b) {
        if (b) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(getActivity(), "Permission not franted", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCameraTrackingDismissed() {
        isInTrackingMode = false;
    }

    @Override
    public void onCameraTrackingChanged(int currentMode) {

    }

    @Override
    public void onLocationComponentClick() {
        if (locationComponent.getLastKnownLocation() != null) {
            LatLng newLocation = new LatLng(locationComponent.getLastKnownLocation().getLatitude(),
                    locationComponent.getLastKnownLocation().getLongitude());
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(newLocation));
        }
    }
}