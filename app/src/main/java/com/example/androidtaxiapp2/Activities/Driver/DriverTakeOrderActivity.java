package com.example.androidtaxiapp2.Activities.Driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.androidtaxiapp2.Activities.Client.ClientOrderDetailsActivity;
import com.example.androidtaxiapp2.Adapters.Order_RecyclerViewAdapter;
import com.example.androidtaxiapp2.Enums.OrderStatus;
import com.example.androidtaxiapp2.Interfaces.RecyclerViewInterface;
import com.example.androidtaxiapp2.Models.Car;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Order;
import com.example.androidtaxiapp2.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DriverTakeOrderActivity extends AppCompatActivity implements RecyclerViewInterface {

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private RecyclerView recyclerView;

    private Button takeOrderBackBtn;

    private List<Order> availableOrders = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_take_order);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.ORDERS_REFERENCE);
        takeOrderBackBtn = findViewById(R.id.take_order_btn_back);
        recyclerView = findViewById(R.id.driver_mrecyclerview);
        getUserOrders(this);

        takeOrderBackBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DriverTakeOrderActivity.this, DriverHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void getUserOrders(RecyclerViewInterface recyclerViewInterface) {

        FirebaseDatabase.getInstance().getReference(Common.CARS_REFERENCE)
                .orderByChild("_driverId")
                .equalTo(Common.currentUser.get_uid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    for (DataSnapshot child : snapshot.getChildren()){
                                        Car car = child.getValue(Car.class);
                                        getOrders(recyclerViewInterface,car.get_carTypeId());
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
    }

    private void getOrders( RecyclerViewInterface recyclerViewInterface ,String carTypeId) {
        FirebaseDatabase.getInstance().getReference(Common.ORDERS_REFERENCE)
                        .orderByChild("_carTypeId")
                        .equalTo(carTypeId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    for (DataSnapshot child : snapshot.getChildren()){
                                        Order order = child.getValue(Order.class);
                                        if (order.get_orderStatus().equals(OrderStatus.LookingForDriver.toString())){
                                            availableOrders.add(order);
                                        }
                                    }
                                    Order_RecyclerViewAdapter adapter = new Order_RecyclerViewAdapter(getBaseContext(),availableOrders, recyclerViewInterface);
                                    recyclerView.setAdapter(adapter);
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
        Intent intent = new Intent(DriverTakeOrderActivity.this, DriverOrderDetailsActivity.class);
        intent.putExtra("order", availableOrders.get(position));
        startActivity(intent);
        finish();
    }
}