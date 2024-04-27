package com.example.androidtaxiapp2.Activities.Driver;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.androidtaxiapp2.Activities.OrderHistoryActivity;
import com.example.androidtaxiapp2.Activities.User.UserProfileActivity;
import com.example.androidtaxiapp2.Enums.OrderStatus;
import com.example.androidtaxiapp2.EventBus.DeclineOrder;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Order;
import com.example.androidtaxiapp2.Models.TokenModel;
import com.example.androidtaxiapp2.R;
import com.example.androidtaxiapp2.Utils.UserUtils;
import com.example.androidtaxiapp2.databinding.ActivityDriverHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.optimization.v1.MapboxOptimization;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DriverHomeActivity extends AppCompatActivity implements OnMapReadyCallback, OnLocationClickListener, PermissionsListener, OnCameraTrackingChangedListener {

    protected DrawerLayout drawer;
    private ImageView menu;
    private LinearLayout home, account, orderHistory;
    protected ActivityDriverHomeBinding binding;
    private Button _becomeDriverbtn;
    private Order currOrder;
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
    private Point origin;
    private FloatingActionButton myLocationButton;
    private List<Point> destinations = new ArrayList<>();
    private boolean isInTrackingMode;
    private Point userCurrLocation;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private OkHttpClient okHttpClient;
    private Button startTripButton;
    private Button finishTripButton;
    private Button takeOrderButton;

    @Override
    protected void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this,getString(R.string.mapbox_access_token));
        binding = ActivityDriverHomeBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        drawer = binding.driverDrawerLayout;
        menu = findViewById(R.id.menu);
        home = findViewById(R.id.home);
        account = findViewById(R.id.account);
        orderHistory = findViewById(R.id.orderHistory);
        _becomeDriverbtn = findViewById(R.id.become_driver_btn);
        startTripButton = binding.driverStartTrip;
        finishTripButton = binding.finishTrip;
        myLocationButton = binding.driverFocusLocation;
        takeOrderButton = binding.takeAvailableOrderBtn;

        _becomeDriverbtn.setVisibility(View.INVISIBLE);
        startTripButton.setVisibility(View.GONE);
        finishTripButton.setVisibility(View.GONE);

        menu.setOnClickListener(v -> openDrawer(drawer));

        home.setOnClickListener(v -> recreate());
        mapView = binding.driverMapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        takeOrderButton.setOnClickListener(v -> {
            redirectActivity(DriverHomeActivity.this, DriverTakeOrderActivity.class);
        });

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.OPTIMIZED_ROUTES_REFERENCE);

        account.setOnClickListener(v -> redirectActivity(DriverHomeActivity.this, UserProfileActivity.class));
        orderHistory.setOnClickListener(v -> redirectActivity(DriverHomeActivity.this, OrderHistoryActivity.class));

        initializeDriver();
    }

    private void checkIfHasActiveOrders(){
        reference = database.getReference(Common.ORDERS_REFERENCE);
        reference.orderByChild("_driverid").equalTo(Common.currentUser.get_uid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot childSnapshot : snapshot.getChildren()){
                        Order order = childSnapshot.getValue(Order.class);
                        if (order!=null && order.get_orderStatus().equals(OrderStatus.InProgress.toString())){
                            getPoints(order.get_route());
                            startTrip(order);
                            Toast.makeText(DriverHomeActivity.this,"There is active Order", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    Log.d("TAG", snapshot.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startTrip(Order order){
        takeOrderButton.setVisibility(View.GONE);
        startTripButton.setVisibility(View.VISIBLE);
        finishTripButton.setVisibility(View.GONE);
        myLocationButton.setVisibility(View.GONE);
        displayRouteToUser();

        startTripButton.setOnClickListener(v -> {
            startTripButton.setVisibility(View.GONE);
            finishTripButton.setVisibility(View.VISIBLE);
            displayRoute();
        });

        finishTripButton.setOnClickListener(v -> {
            setfinishOrderStatus(order);
            reloadMap();
            finishTripButton.setVisibility(View.GONE);
            takeOrderButton.setVisibility(View.VISIBLE);
            myLocationButton.setVisibility(View.VISIBLE);
        });

    }

    private void reloadMap(){
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
        });
    }

    private void openDrawer(DrawerLayout drawer){
        drawer.openDrawer(GravityCompat.START);
    }

    private void closeDrawer(DrawerLayout drawer){
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    private void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity,secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private void initializeDriver() {

        TextView txt_name = findViewById(R.id.txt_name);
        TextView txt_phone = findViewById(R.id.txt_phone);
        ImageView img  = findViewById(R.id.imageView);

        txt_name.setText(Common.buildWelcomeMessage());
        txt_phone.setText(Common.currentUser != null ? Common.currentUser.get_phone():"");
        if (Common.currentUser != null && !TextUtils.isEmpty(Common.currentUser.get_urlImage())){
            Glide.with(this)
                    .load(Common.currentUser.get_urlImage())
                    .into(img);
        }
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        binding = null;
//
//        if (optimizedClient != null) {
//            optimizedClient.cancelCall();
//        }
//        if(mapboxMap != null){
//            mapboxMap.removeOnMapClickListener(this);
//        }
//        mapView.onDestroy();
//    }


    @Override
    protected void onStop() {
        super.onStop();
        if(EventBus.getDefault().hasSubscriberForEvent(DeclineOrder.class)){
            EventBus.getDefault().removeStickyEvent(DeclineOrder.class);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        closeDrawer(drawer);
    }

    @Override
    public void onExplanationNeeded(List<String> list) {

    }

    @Override
    public void onPermissionResult(boolean b) {
        if (b) {
            reloadMap();
            mapboxMap.getStyle(style -> enableLocationComponent(style));
        } else {
            Toast.makeText(DriverHomeActivity.this, "Permission not granted", Toast.LENGTH_LONG).show();
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

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        okHttpClient = new OkHttpClient();
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            enableLocationComponent(style);
            checkIfHasActiveOrders();
        });

        Intent intent = getIntent();

        if (intent!= null && intent.hasExtra("order")){
            currOrder = (Order) intent.getSerializableExtra("order");
            getPoints(currOrder.get_route());
            startTrip(currOrder);
        }
    }

    private void displayRoute() {
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            new Thread(() -> getRouteFromServer(style, origin,destinations)).start();
        });
    }

    private void getPoints(String route){
        String[] routeStr = route.split(";");
        for (int i = 0; i < routeStr.length; i++){
            String[] cordsStr = routeStr[i].split(",");
            double lon = Double.parseDouble(cordsStr[0]);
            double lat = Double.parseDouble(cordsStr[1]);
            if (i== 0){
                origin = Point.fromLngLat(lon,lat);
            }
            else{
                destinations.add(Point.fromLngLat(lon,lat));
            }
        }
    }

    private void displayRouteToUser() {
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            enableLocationComponent(style);
            initMarkerIconSymbolLayer(style, userCurrLocation);
            initOptimizedRouteLineLayer(style);
            List<Point> stops = new ArrayList<>();
            stops.add(userCurrLocation);
            stops.add(origin);
            addDestinationMarkers(style,stops);
            getOptimizedRoute(style,stops);;
        });
    }

    private void getRouteFromServer(@NonNull Style loadedMapStyle, Point origin, List<Point> destinations){

        String body = getDestinationsAsString(destinations);

        String org = origin.longitude()+","+origin.latitude();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);

        Request request = new Request.Builder().url("http://192.168.0.211:5249/api/Distribution/GetRoute?origin="+org)
                .post(requestBody)
                .addHeader("accept", "text/plain")
                .addHeader("Content-Type", "application/json")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(DriverHomeActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("TAG", e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {


                    Gson gson = new Gson();
                    List<String> points = null;

                    try {
                        String json = response.body().string();
                        points = gson.fromJson(json, List.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    List<Point> routeCoord = parseToPoints(points);

                runOnUiThread(() -> {
                    initMarkerIconSymbolLayer(loadedMapStyle, origin);
                    initOptimizedRouteLineLayer(loadedMapStyle);
                    addDestinationMarkers(loadedMapStyle,routeCoord);
                    Log.d("TAG", routeCoord.toString());
                    getOptimizedRoute(loadedMapStyle,routeCoord);
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

    private String getDestinationsAsString(List<Point> destinations){
        String[] coordinates = new String[destinations.size()];
        for (int i = 0; i < destinations.size(); i++){
            coordinates[i] = destinations.get(i).longitude() + "," + destinations.get(i).latitude();
        }
        JsonArray jsonArray = new JsonArray();
        for (String coordinate : coordinates){
            jsonArray.add(coordinate);
        }
        return  jsonArray.toString();
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            if (mapboxMap != null){
                LocationComponent locationComponent = mapboxMap.getLocationComponent();

                if (locationComponent!=null && loadedMapStyle != null){
                    locationComponent.activateLocationComponent(
                            LocationComponentActivationOptions.builder(this, loadedMapStyle).build());
                    }

                    // Enable to make component visible
                    locationComponent.setLocationComponentEnabled(true);

                    // Set the component's camera mode
                    locationComponent.setCameraMode(CameraMode.TRACKING);

                    // Set the component's render mode
                    locationComponent.setRenderMode(RenderMode.COMPASS);

                    myLocationButton.setOnClickListener(view -> {
                        if (!isInTrackingMode) {
                            isInTrackingMode = true;
                            locationComponent.setCameraMode(CameraMode.TRACKING);
                            locationComponent.zoomWhileTracking(16f);
                        } else {

                        }
                    });

                Location lastKnownLocation = locationComponent.getLastKnownLocation();
                if (lastKnownLocation != null){
                    userCurrLocation = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),locationComponent.getLastKnownLocation().getLatitude());
                }else{

                }
            }


            // Activate with options


            //locationComponent.addOnLocationClickListener(this);

            // Add the camera tracking listener. Fires if the map camera is manually moved.
            //locationComponent.addOnCameraTrackingChangedListener(this);

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void initMarkerIconSymbolLayer(@NonNull Style loadedMapStyle, Point origin) {
        // Add the marker image to map
        Bitmap icon = BitmapFactory.decodeResource(this.getResources(),R.drawable.red_marker);
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

    private void addDestinationMarkers(@NonNull Style style, List<Point> coords) {
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
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onDeclineOrder(DeclineOrder event){
        reloadMap();
        takeOrderButton.setVisibility(View.VISIBLE);
        myLocationButton.setVisibility(View.VISIBLE);
        startTripButton.setVisibility(View.GONE);
        finishTripButton.setVisibility(View.GONE);
        //recreate();
    }

    private void setfinishOrderStatus(Order order) {
        order.set_orderStatus(OrderStatus.Done.toString());
        reference = database.getReference(Common.ORDERS_REFERENCE);

        reference.child(order.get_uid()).setValue(order).addOnCompleteListener(task -> notifyUserAboutOrderFinish(order.get_userid()));
    }

    private void notifyUserAboutOrderFinish(String userId)
    {
        reference = database.getReference(Common.TOKEN_REFERENCE);
        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    TokenModel token = snapshot.getValue(TokenModel.class);
                    if (token != null){
                        new Thread(() -> {
                            try {
                                UserUtils.sendOrderFinishedNotification(token.getToken());
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}