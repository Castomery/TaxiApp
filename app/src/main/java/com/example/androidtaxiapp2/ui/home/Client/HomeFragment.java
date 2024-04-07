package com.example.androidtaxiapp2.ui.home.Client;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.OptimizedRoute;
import com.example.androidtaxiapp2.Models.RouteCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.optimization.v1.MapboxOptimization;

import com.example.androidtaxiapp2.Activities.UserHomeActivity;
import com.example.androidtaxiapp2.R;
import com.example.androidtaxiapp2.databinding.FragmentHomeBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.optimization.v1.models.OptimizationResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private static final String ICON_GEOJSON_SOURCE_ID = "icon-source-id";
    private static final String FIRST = "first";
    private static final String LAST = "last";
    private static final String TEAL_COLOR = "#23D2BE";
    private static final float POLYLINE_WIDTH = 5;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private DirectionsRoute optimizedRoute;
    private MapboxOptimization optimizedClient;
    private List<Point> stops = new ArrayList<>();
    private Point origin;
    private FloatingActionButton myLocationButton;
    private List<OptimizedRoute> optimizedRoutes;
    private List<Point> destinations = new ArrayList<>();

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private UUID currOrderUid;
    private int currPermutationId;

    private Button showRoute;

//    private  final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
//        @Override
//        public void onActivityResult(Boolean o) {
//            if(o){
//                Toast.makeText(getActivity(),"Permission Granted", Toast.LENGTH_SHORT).show();
//            }
//            else{
//                Toast.makeText(getActivity(),"Permission Denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    });

//    private final OnIndicatorBearingChangedListener onIndicatorBearingChangedListener = new OnIndicatorBearingChangedListener() {
//        @Override
//        public void onIndicatorBearingChanged(double v) {
//            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().bearing(v).build());
//        }
//    };
//
//    private final OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = new OnIndicatorPositionChangedListener() {
//        @Override
//        public void onIndicatorPositionChanged(@NonNull Point point) {
//            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(15.0).build());
//            getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(point));
//        }
//    };
//
//    private final OnMoveListener onMoveListener = new OnMoveListener() {
//        @Override
//        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
//            getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
//            getLocationComponent(mapView).removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
//            getGestures(mapView).removeOnMoveListener(onMoveListener);
//            myLocationButton.show();
//        }
//
//        @Override
//        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
//            return false;
//        }
//
//        @Override
//        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {
//
//        }
//    };


    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Mapbox.getInstance(getContext(),getString(R.string.mapbox_access_token));

        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mapView = binding.mapView;
        showRoute = binding.showRoute;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.OPTIMIZED_ROUTES_REFERENCE);

        origin = Point.fromLngLat(32.10641545195378,49.422827298067375);
        stops.add(origin);
        Point destination1 = Point.fromLngLat(32.07101602980575,49.4464253366084);
        Point destination2 = Point.fromLngLat(32.043707542675094,49.45315589003039);
//        stops.add(destination1);
//        stops.add(destination2);
        destinations.add(destination1);
        destinations.add(destination2);

//        mapView = binding.mapView;
//        myLocationButton = binding.focusLocation;
//
//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
//        }
//
//        mapView.getMapboxMap().loadStyle(Style.STANDARD, new Style.OnStyleLoaded() {
//            @Override
//            public void onStyleLoaded(@NonNull Style style) {
//                mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(15.0).build());
//                LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
//                locationComponentPlugin.setEnabled(true);
//                LocationPuck2D locationPuck2D = new LocationPuck2D();
//                locationPuck2D.setBearingImage(ImageHolder.from(R.drawable.alternate_map_marker));
//                locationComponentPlugin.setLocationPuck(locationPuck2D);
//                locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
//                locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
//                getGestures(mapView).addOnMoveListener(onMoveListener);
//
//                myLocationButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
//                        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
//                        getGestures(mapView).addOnMoveListener(onMoveListener);
//                    }
//                });
//            }
//        });

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
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                // Add origin and destination to the mapboxMap
                initMarkerIconSymbolLayer(style);
                initOptimizedRouteLineLayer(style);
                showRoute.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currOrderUid = UUID.randomUUID();
                        checkAllPosibleRoutes(style);
                    }
                });
                //Toast.makeText(OptimizationActivity.this, R.string.click_instructions, Toast.LENGTH_SHORT).show();
//                mapboxMap.addOnMapClickListener(OptimizationActivity.this);
//                mapboxMap.addOnMapLongClickListener(OptimizationActivity.this);
            }
        });

    }

    private void checkAllPosibleRoutes(@NonNull final Style style){
        currPermutationId = 0;
        List<List<Point>> permutations = new ArrayList<>();
        for (int i = 0; i < destinations.size(); i++) {
            List<Point> points = new ArrayList<>();
            points.add(origin); // Origin
            for (int j = 0; j < destinations.size(); j++) {
                int index = (i + j) % destinations.size();
                points.add(destinations.get(index));
            }
            permutations.add(points);
        }

        for (List<Point> permutation : permutations){
            getOptimizedRoute(style,permutation);
            currPermutationId++;
        }
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

    private void addDestinationMarker(@NonNull Style style) {
        List<Feature> destinationMarkerList = new ArrayList<>();
        for (Point singlePoint : stops) {
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

    private void getOptimizedRoute(@NonNull final Style style, List<Point> coordinates) {
        optimizedClient = MapboxOptimization.builder()
                .source(FIRST)
                .destination(LAST)
                .coordinates(coordinates)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.mapbox_access_token))
                .build();

//        Callback<OptimizationResponse> responseCallback = new RouteCallback();
//        optimizedClient.enqueueCall(responseCallback);
//
//        Log.d("TAG", "route:" + ((RouteCallback) responseCallback).getDistance());

        optimizedClient.enqueueCall(new Callback<OptimizationResponse>() {
            @Override
            public void onResponse(Call<OptimizationResponse> call, Response<OptimizationResponse> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getActivity(), "No success", Toast.LENGTH_SHORT).show();
                } else {
                    if (response.body() != null) {
                        List<DirectionsRoute> routes = response.body().trips();
                        if (routes != null) {
                            if (routes.isEmpty()) {

                                Toast.makeText(getActivity(), "No routes",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Get most optimized route from API response
                                optimizedRoute = routes.get(0);
                                OptimizedRoute optimizedRoute1 = new OptimizedRoute(ListOfPointsToString(coordinates), routes.get(0).distance());
                                reference.child(String.valueOf(currOrderUid)).push().setValue(optimizedRoute1);
                                //drawOptimizedRoute(style, optimizedRoute);
                            }
                        } else {
                            Toast.makeText(getActivity(), "Null response", Toast.LENGTH_SHORT).show();
                        }
                    } else {

                        Toast.makeText(getActivity(), "Null body", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<OptimizationResponse> call, Throwable throwable) {
            }
        });
    }

    private void drawOptimizedRoute(@NonNull Style style, DirectionsRoute route) {
        GeoJsonSource optimizedLineSource = style.getSourceAs("optimized-route-source-id");
        if (optimizedLineSource != null) {
            optimizedLineSource.setGeoJson(FeatureCollection.fromFeature(Feature.fromGeometry(
                    LineString.fromPolyline(route.geometry(), PRECISION_6))));
        }
    }



}