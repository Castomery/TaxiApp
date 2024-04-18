package com.example.androidtaxiapp2.Activities.Client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidtaxiapp2.Enums.OrderStatus;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Order;
import com.example.androidtaxiapp2.Models.ShortestRoute;
import com.example.androidtaxiapp2.databinding.ActivityCreateRequestBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Point;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateRequestActivity extends AppCompatActivity {

    private Point origin;
    private List<Point> destinations;
    private HashMap<String,String> addressesName;
    private Button economBtn;
    private Button comfortBtn;
    private Button busBtn;
    private Button backBtn;

    private Button makeRequestOneCarBtn;
    private Button makeRequestRecommendationsBtn;

    private ShortestRoute routeOneCar;
    private List<ShortestRoute> recommendations;
    private LinearLayout pointsNamesContainer;
    private TextView textViewPrice;
    private TextView textViewDuration;
    private OkHttpClient client;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private LinearLayout recomendationContainer;
    private ActivityCreateRequestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateRequestBinding.inflate(getLayoutInflater());
        economBtn = binding.economBtn;
        comfortBtn = binding.comfortBtn;
        busBtn = binding.busBtn;
        pointsNamesContainer = binding.pointsNameContainer;
        textViewPrice = binding.textViewPrice;
        textViewDuration = binding.textViewDuration;
        client = new OkHttpClient();
        recomendationContainer = binding.recommendationContainer;
        makeRequestOneCarBtn = binding.makeOrderBtn;
        makeRequestRecommendationsBtn = binding.makeOrderFromRecommendationBtn;
        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.ORDERS_REFERENCE);
        backBtn = binding.createRequestBtnBack;

        setContentView(binding.getRoot());

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra("origin") && intent.hasExtra("destinations")){
            origin = (Point) intent.getSerializableExtra("origin");
            destinations = (List<Point>) intent.getSerializableExtra("destinations");
            addressesName = (HashMap<String, String>) intent.getSerializableExtra("addressesName");
        }

        economBtn.setOnClickListener(v -> {
            getTripDetails(50,10);
            getRecommendation(50,10);
        });
        comfortBtn.setOnClickListener(v -> getTripDetails(55,11));
        busBtn.setOnClickListener(v -> getTripDetails(60,12));
        backBtn.setOnClickListener(v -> {
            Intent intent1 = new Intent(CreateRequestActivity.this,UserHomeActivity.class);
            startActivity(intent1);
            finish();
        });
    }

    private void getTripDetails(double priceForCar, double pricePerKm){
        pointsNamesContainer.removeAllViews();
        String body = getDestinationsAsString(destinations);

        String org = origin.longitude()+","+origin.latitude();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);

        Request request = new Request.Builder().url("http://10.0.2.2:5249/api/Distribution/GetOptimalRoute?origin="+org+"&priceForCar="+priceForCar+"&pricePerKm="+pricePerKm)
                .post(requestBody)
                .addHeader("accept", "text/plain")
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateRequestActivity.this,"Failed", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                runOnUiThread(() -> {
                    Gson gson = new Gson();
                    try {
                        String json = response.body().string();
                        routeOneCar = gson.fromJson(json, ShortestRoute.class);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    for(int i = 0; i < routeOneCar.route.size(); i++) {
                        for (Map.Entry<String, String> address : addressesName.entrySet()) {
                            if (address.getValue().equals(routeOneCar.route.get(i))){
                                routeOneCar.pointsNames.set(i,address.getKey());
                                break;
                            }
                        }
                    }

                    for (int j = 0; j < routeOneCar.route.size();j++){
                        TextView textView = new TextView(getBaseContext());
                        textView.setText(routeOneCar.pointsNames.get(j));
                        textView.setTextSize(15);
                        textView.setTextColor(Color.parseColor("#000000"));
                        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 50));
                        pointsNamesContainer.addView(textView);

                        if (j!=0 && routeOneCar.priceDistribution != null){
                            TextView textViewdistributedPrice = new TextView(getBaseContext());
                            textViewdistributedPrice.setText("Price to pay:" + String.valueOf(routeOneCar.priceDistribution[j-1]));
                            textViewdistributedPrice.setTextSize(15);
                            textViewdistributedPrice.setTextColor(Color.parseColor("#000000"));
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 50);
                            layoutParams.setMargins(0,10,0,20);
                            textViewdistributedPrice.setLayoutParams(layoutParams);
                            pointsNamesContainer.addView(textViewdistributedPrice);
                        }
                    }


                    textViewPrice.setText("Price: " + String.valueOf(routeOneCar.totalPrice));
                    textViewDuration.setText("Duration: " + String.valueOf(routeOneCar.duration));

                });
                makeRequestOneCarBtn.setOnClickListener(v -> {
                    String uuid = UUID.randomUUID().toString();
                    Order order= new Order(uuid,Common.currentUser.get_uid(),"", OrderStatus.LookingForDriver.toString(), Calendar.getInstance().getTime().toString(),routeOneCar);
                    reference.child(uuid).setValue(order).addOnCompleteListener(task -> {
                        if (task.isSuccessful()){

                            Toast.makeText(CreateRequestActivity.this,"Order created", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CreateRequestActivity.this, UserHomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(CreateRequestActivity.this,"Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });

                });
            }
        });
    }

    private void getRecommendation(double priceForCar, double pricePerKm){
        pointsNamesContainer.removeAllViews();
        String body = getDestinationsAsString(destinations);

        String org = origin.longitude()+","+origin.latitude();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);

        Request request = new Request.Builder().url("http://10.0.2.2:5249/api/Distribution/GetDistribution?origin="+org+"&priceForCar="+priceForCar+"&pricePerKm="+pricePerKm)
                .post(requestBody)
                .addHeader("accept", "text/plain")
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateRequestActivity.this,"Failed", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                runOnUiThread(() -> {
                    Gson gson = new Gson();
                    try {
                        String json = response.body().string();
                        Type recomendationList = new TypeToken<List<ShortestRoute>>(){}.getType();
                        recommendations = gson.fromJson(json, recomendationList);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    for(int i = 0; i < recommendations.size(); i++) {

                        ShortestRoute currRoute = (ShortestRoute) recommendations.get(i);

                        for (int j = 0; j < currRoute.route.size(); j++) {
                            for (Map.Entry<String, String> address : addressesName.entrySet()) {
                                if (address.getValue().equals(currRoute.route.get(j))){
                                    currRoute.pointsNames.set(j,address.getKey());
                                    break;
                                }
                            }
                        }
                        LinearLayout layout = new LinearLayout(getBaseContext());
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));

                        for (int j = 0; j < currRoute.route.size();j++){
                            TextView textView = new TextView(getBaseContext());
                            textView.setText(currRoute.pointsNames.get(j));
                            textView.setTextSize(15);
                            textView.setTextColor(Color.parseColor("#000000"));
                            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 50));
                            layout.addView(textView);

                            if (j!=0 && currRoute.priceDistribution != null){
                                TextView textViewdistributedPrice = new TextView(getBaseContext());
                                textViewdistributedPrice.setText("Price to pay:" + String.valueOf(currRoute.priceDistribution[j-1]));
                                textViewdistributedPrice.setTextSize(15);
                                textViewdistributedPrice.setTextColor(Color.parseColor("#000000"));
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 50);
                                layoutParams.setMargins(0,10,0,20);
                                textViewdistributedPrice.setLayoutParams(layoutParams);
                                layout.addView(textViewdistributedPrice);
                            }
                        }

                        TextView textviewPrice = new TextView(getBaseContext());
                        textviewPrice.setText(String.valueOf(currRoute.totalPrice));
                        textviewPrice.setTextSize(20);
                        textviewPrice.setTextColor(Color.parseColor("#000000"));
                        textviewPrice.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                        TextView textviewDuration = new TextView(getBaseContext());
                        textviewDuration.setText(String.valueOf(currRoute.duration));
                        textviewDuration.setTextSize(20);
                        textviewDuration.setTextColor(Color.parseColor("#000000"));
                        textviewDuration.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));

                        layout.addView(textviewPrice);
                        layout.addView(textviewDuration);
                        recomendationContainer.addView(layout);
                    }

                    makeRequestRecommendationsBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (ShortestRoute route : recommendations){
                                String uuid = UUID.randomUUID().toString();
                                Order order= new Order(uuid,Common.currentUser.get_uid(),"", OrderStatus.InProgress.toString(), Calendar.getInstance().getTime().toString(),route);
                                reference.child(uuid).setValue(order).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()){

                                        Toast.makeText(CreateRequestActivity.this,"Order created", Toast.LENGTH_SHORT).show();
//                                Common.currentUser = user;
//                                Intent intent = new Intent(RegisterActivity.this, UserHomeActivity.class);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(intent);
//                                finish();
                                    }else{
                                        Toast.makeText(CreateRequestActivity.this,"Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            Intent intent = new Intent(CreateRequestActivity.this, UserHomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
                });
            }
        });
    }

//    private Point (String coordinates) {
//
//        Point result;
//
//        String[] parts = coordinates.split(",");
//        double lon = Double.parseDouble(parts[0]);
//        double lat = Double.parseDouble(parts[1]);
//        result = Point.fromLngLat(lon,lat);
//
//        return result;
//    }

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
}