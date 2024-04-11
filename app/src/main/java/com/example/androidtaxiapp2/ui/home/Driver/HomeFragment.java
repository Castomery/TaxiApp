package com.example.androidtaxiapp2.ui.home.Driver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.mapbox.mapboxsdk.Mapbox;

import com.example.androidtaxiapp2.Activities.UserHomeActivity;
import com.example.androidtaxiapp2.R;
import com.example.androidtaxiapp2.databinding.FragmentHomeBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;

public class HomeFragment extends Fragment {

    //private MapView mapView;
    private FloatingActionButton myLocationButton;

    private  final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if(o){
                Toast.makeText(getActivity(),"Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getActivity(),"Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    });

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

        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));

        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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
    }
}