package com.example.androidtaxiapp2.Activities.Client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidtaxiapp2.R;
import com.example.androidtaxiapp2.databinding.ActivityCreateGoupRideBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateGoupRideActivity extends AppCompatActivity {

    private int REQEST_CODE_AUTOCOMPLETE = 1;
    private ActivityCreateGoupRideBinding binding;
    private Boolean onSetOriginClicked = false;
    private Boolean onSetDestinationClicked = false;
    private Point origin;
    private List<Point> destinations = new ArrayList<>();
    private HashMap<String,String> addresses = new HashMap<>();
    private LinearLayout layout;
    private int countOfDestinations = 0;

    private Button backButton;
    private Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCreateGoupRideBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        layout = binding.destinationContainer;

        backButton = binding.setRouteBtnBack;

        doneButton = binding.btnDone;

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(CreateGoupRideActivity.this, UserHomeActivity.class);
            startActivity(intent);
            finish();
        });

        doneButton.setOnClickListener(v -> {
            Intent intent = new Intent(CreateGoupRideActivity.this, UserHomeActivity.class);
            intent.putExtra("origin", origin);
            intent.putExtra("destinations", (Serializable) destinations);
            intent.putExtra("addressesName", addresses);
            startActivity(intent);
            finish();
        });

        binding.setStartPoint.setOnClickListener(v -> {
            onSetOriginClicked = true;
            setLocation();
        });

        binding.addDestinationPoint.setOnClickListener(v -> {
            if (countOfDestinations < 6){
                onSetDestinationClicked = true;
                setLocation();
            }
            else{
                Toast.makeText(CreateGoupRideActivity.this,"Destinations Limit", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLocation(){
        Intent intent = new PlaceAutocomplete.IntentBuilder()
                .accessToken(getString(R.string.mapbox_access_token))
                .placeOptions(PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#ffffff"))
                        .hint("Enter Address")
                        .build())
                .build(CreateGoupRideActivity.this);

        startActivityForResult(intent,REQEST_CODE_AUTOCOMPLETE);
    }

    private String parsePointToString(Point point){
        String value = String.valueOf(point.longitude())+","+String.valueOf(point.latitude());

        return value;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == CreateGoupRideActivity.RESULT_OK && requestCode == REQEST_CODE_AUTOCOMPLETE) {

            CarmenFeature feature = PlaceAutocomplete.getPlace(data);

            if(onSetOriginClicked){
                binding.textViewOriginPoint.setText(feature.placeName());
                origin = (Point) feature.geometry();
                addresses.put(feature.placeName(),parsePointToString(origin));
                onSetOriginClicked = false;
            }
            else if (onSetDestinationClicked){
                if (!destinations.contains((Point) feature.geometry())){
                    LinearLayout destinationLayout = new LinearLayout(this);
                    destinationLayout.setOrientation(LinearLayout.HORIZONTAL);
                    destinationLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                    if (!addresses.containsKey(feature.placeName())){
                        addresses.put(feature.placeName(),parsePointToString((Point) feature.geometry()));
                    }

                    // Create a TextView to display the location name
                    TextView value = new TextView(this);
                    value.setText(feature.placeName());
                    value.setTextSize(20);
                    value.setTextColor(Color.parseColor("#000000"));
                    value.setLayoutParams(new LinearLayout.LayoutParams(0, 50, 1.0f));
                    destinationLayout.addView(value);

                    // Create a delete button
                    Button deleteButton = new Button(this);
                    deleteButton.setText("Delete");
                    deleteButton.setOnClickListener(v -> {
                        layout.removeView(destinationLayout);
                        destinations.remove(feature.geometry());
                    });
                    destinationLayout.addView(deleteButton);
                    layout.addView(destinationLayout);
                    destinations.add((Point) feature.geometry());
                    countOfDestinations++;
                    onSetDestinationClicked = false;
                }
            }
        }
    }
}