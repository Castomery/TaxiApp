package com.example.androidtaxiapp2.Activities.Client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidtaxiapp2.Enums.OrderStatus;
import com.example.androidtaxiapp2.Models.CarTypes;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Order;
import com.example.androidtaxiapp2.Models.ShortestRoute;
import com.example.androidtaxiapp2.databinding.ActivityCreateRequestBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Point;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Dictionary;
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


    private final String econcomType = "econom";
    private final String comfortType = "comfort";
    private final String miniBusType = "minibus";
    private final int MAX_PASSENGERS_FOR_CAR = 4;
    private final int MAX_PASSENGERS_FOR_BUS = 6;
    private Point origin;
    private List<Point> destinations;
    private HashMap<String,String> addressesName;
    private Button economBtn;
    private Button comfortBtn;
    private Button busBtn;
    private Button backBtn;
    private Button makeRequestOneCarBtn;
    private Button makeRequestRecommendationsBtn;
    private HashMap<String, List<ShortestRoute>> routes;
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
            getPricesForTrip(econcomType, MAX_PASSENGERS_FOR_CAR);
        });
        comfortBtn.setOnClickListener(v -> getPricesForTrip(comfortType,MAX_PASSENGERS_FOR_CAR));
        busBtn.setOnClickListener(v -> getPricesForTrip(miniBusType, MAX_PASSENGERS_FOR_BUS));
        backBtn.setOnClickListener(v -> {
            Intent intent1 = new Intent(CreateRequestActivity.this,UserHomeActivity.class);
            startActivity(intent1);
            finish();
        });
    }

    private void getPricesForTrip(String typeOfCar, int max_passengers){
        pointsNamesContainer.removeAllViews();
        recomendationContainer.removeAllViews();
        textViewPrice.setText("");
        textViewDuration.setText("");
        FirebaseDatabase.getInstance().getReference(Common.CAR_TYPES_REFERENCE)
                        .orderByChild("_name").equalTo(typeOfCar).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for(DataSnapshot childSnapshot : snapshot.getChildren()){
                                CarTypes carType = childSnapshot.getValue(CarTypes.class);
                                if (carType != null){
                                    getTripDetails(carType.get_uid(),carType.get_priceForCar(), carType.get_pricePerKm(), max_passengers);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getTripDetails(String carTypeId,double priceForCar, double pricePerKm, int max_passengers){
        reference = database.getReference(Common.ORDERS_REFERENCE);
        String body = getDestinationsAsString(destinations);

        String org = origin.longitude()+","+origin.latitude();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url(Common.IP_ADDRESS + "/api/Distribution/GetRouteMatrix?origin="+org+"&priceForCar="+priceForCar+"&pricePerKm="+pricePerKm+"&max_passengers="+max_passengers)
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

                        Gson gson = new Gson();
                        try {
                            String json = response.body().string();
                            Type recomendationList = new TypeToken<HashMap<String,List<ShortestRoute>>>(){}.getType();
                            routes = gson.fromJson(json, recomendationList);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }


                        runOnUiThread(() -> {
                            if(routes.containsKey("oneCar")){
                                getOneCarTrip(carTypeId,routes.get("oneCar").get(0));
                            }
                            if (routes.get("recommendations").size() != 1){
                                getRecommendation(carTypeId,routes.get("recommendations"));
                            }
                        });
                    }
                });
            }
        }).start();

    }

    private void getOneCarTrip(String carTypeId, ShortestRoute trip){

        for(int i = 0; i < trip.route.size(); i++) {
            for (Map.Entry<String, String> address : addressesName.entrySet()) {
                if (address.getValue().equals(trip.route.get(i))){
                    trip.pointsNames.add(address.getKey());
                    break;
                }
            }
        }

        for (int j = 0; j < trip.route.size();j++){
            TextView textView = new TextView(getBaseContext());
            textView.setText(trip.pointsNames.get(j));
            textView.setTextSize(15);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 50));
            pointsNamesContainer.addView(textView);

            if (j!=0 && trip.priceDistribution != null){
                TextView textViewdistributedPrice = new TextView(getBaseContext());
                textViewdistributedPrice.setText("Price to pay:" + String.valueOf(trip.priceDistribution[j-1]));
                textViewdistributedPrice.setTextSize(15);
                textViewdistributedPrice.setTextColor(Color.parseColor("#000000"));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 50);
                layoutParams.setMargins(0,10,0,20);
                textViewdistributedPrice.setLayoutParams(layoutParams);
                pointsNamesContainer.addView(textViewdistributedPrice);
            }
        }

        textViewPrice.setText("Price: " + String.valueOf(trip.totalPrice));
        textViewDuration.setText("Duration: " + String.valueOf(trip.duration));

        makeRequestOneCarBtn.setOnClickListener(v -> {
            createRequest(trip, carTypeId);
            Intent intent = new Intent(CreateRequestActivity.this, UserHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

    }

    private void getRecommendation(String carTypeId,List<ShortestRoute> recommendations){

        for(int i = 0; i < recommendations.size(); i++) {
            ShortestRoute currRoute = (ShortestRoute) recommendations.get(i);
            for (int j = 0; j < currRoute.route.size(); j++) {
                for (Map.Entry<String, String> address : addressesName.entrySet()) {
                    if (address.getValue().equals(currRoute.route.get(j))){
                        currRoute.pointsNames.add(address.getKey());
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
            textviewPrice.setText("Price: " + String.valueOf(currRoute.totalPrice));
            textviewPrice.setTextSize(20);
            textviewPrice.setTextColor(Color.parseColor("#000000"));
            textviewPrice.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView textviewDuration = new TextView(getBaseContext());
            textviewDuration.setText( "Duration: "+String.valueOf(currRoute.duration));
            textviewDuration.setTextSize(20);
            textviewDuration.setTextColor(Color.parseColor("#000000"));
            textviewDuration.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));

            layout.addView(textviewPrice);
            layout.addView(textviewDuration);
            recomendationContainer.addView(layout);
        }
        makeRequestRecommendationsBtn.setOnClickListener(v -> {
            for (ShortestRoute route : recommendations){
                createRequest(route, carTypeId);
            }
            Intent intent = new Intent(CreateRequestActivity.this, UserHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void createRequest(ShortestRoute route, String carTypeId){
        String uuid = UUID.randomUUID().toString();
        Order order= new Order(uuid,Common.currentUser.get_uid(),"", OrderStatus.LookingForDriver.toString(), Calendar.getInstance().getTime().toString(),carTypeId,route);
        reference.child(uuid).setValue(order).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(CreateRequestActivity.this,"Order created", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(CreateRequestActivity.this,"Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
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
}