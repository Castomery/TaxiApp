package com.example.androidtaxiapp2.Activities.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.androidtaxiapp2.Adapters.Cars_RecyclerViewAdapter;
import com.example.androidtaxiapp2.Interfaces.RecyclerViewInterface;
import com.example.androidtaxiapp2.Models.Car;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CarListActivity extends AppCompatActivity implements RecyclerViewInterface {

    private Button backBtn;
    private RecyclerView recyclerView;
    private List<Car> carList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);

        backBtn = findViewById(R.id.car_list_btn_back);
        recyclerView = findViewById(R.id.cars_recyclerview);

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(CarListActivity.this, AdminHomeActivity.class);
            startActivity(intent);
            finish();
        });

        getAvailableCars(this);
    }

    private void getAvailableCars(RecyclerViewInterface recyclerViewInterface) {
        FirebaseDatabase.getInstance().getReference(Common.CARS_REFERENCE)
                .orderByChild("_driverId").equalTo("")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot child : snapshot.getChildren()){
                                Car car = child.getValue(Car.class);
                                carList.add(car);
                            }
                            Cars_RecyclerViewAdapter carsRecyclerViewAdapter = new Cars_RecyclerViewAdapter(getBaseContext(),carList,recyclerViewInterface);
                            recyclerView.setAdapter(carsRecyclerViewAdapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(CarListActivity.this, AssignDriverActivity.class);
        intent.putExtra("car", carList.get(position));
        startActivity(intent);
        finish();
    }
}