package com.example.androidtaxiapp2.Activities.Driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.androidtaxiapp2.Enums.OrderStatus;
import com.example.androidtaxiapp2.Models.Car;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Order;
import com.example.androidtaxiapp2.Models.TokenModel;
import com.example.androidtaxiapp2.Utils.UserUtils;
import com.example.androidtaxiapp2.databinding.ActivityDriverOrderDetailsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

public class DriverOrderDetailsActivity extends AppCompatActivity {

    private TextView addressesTextView;
    private TextView priceTextView;
    private TextView durationTextView;
    private Button backButton;
    private Button takeOrderBtn;
    private ActivityDriverOrderDetailsBinding binding;
    private Order currOrder;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityDriverOrderDetailsBinding.inflate(getLayoutInflater());

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.ORDERS_REFERENCE);

        Intent intent = getIntent();

       currOrder = (Order) intent.getSerializableExtra("order");

        String addressesStr = createStringFromOrder(currOrder.get_addresses());
        String priceStr = "Price: " + currOrder.get_price();
        String durationStr = "Duration: " + currOrder.get_duration();

        addressesTextView = binding.driverOrderDetailsAddressesTextView;
        priceTextView = binding.driverOrderDetailsPriceTextView;
        durationTextView = binding.driverOrderDetailsDurationTextView;
        backButton = binding.driverOrderDetailsBtnBack;
        takeOrderBtn = binding.driverTakeOrder;

        addressesTextView.setText(addressesStr);
        priceTextView.setText(priceStr);
        durationTextView.setText(durationStr);

        backButton.setOnClickListener(v -> {
            Intent intent1 = new Intent(DriverOrderDetailsActivity.this, DriverTakeOrderActivity.class);
            startActivity(intent1);
            finish();
        });

        takeOrderBtn.setOnClickListener(v -> {
            checkIfOrderStillAvailable();
            setDriverToOrder();
        });

        setContentView(binding.getRoot());
    }

    private void checkIfOrderStillAvailable() {
        reference.child(currOrder.get_uid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Order order = snapshot.getValue(Order.class);
                    if (order.get_driverid().isEmpty()){
                        setDriverToOrder();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setDriverToOrder() {
        currOrder.set_driverid(Common.currentUser.get_uid());
        currOrder.set_orderStatus(OrderStatus.WaitingForDriver.toString());
        reference.child(currOrder.get_uid()).setValue(currOrder).addOnCompleteListener(task -> {
            getDriverCar();
        });
    }

    private void getDriverCar() {

        FirebaseDatabase.getInstance().getReference(Common.CARS_REFERENCE)
                .orderByChild("_driverId")
                .equalTo(Common.currentUser.get_uid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for(DataSnapshot child : snapshot.getChildren()){
                                Car car = child.getValue(Car.class);
                                notifyUserAboutTakenOrder(car.get_carModel(),car.get_carPlate(),currOrder.get_userid());
                                Intent intent1 = new Intent(DriverOrderDetailsActivity.this, DriverHomeActivity.class);
                                intent1.putExtra("order", currOrder);
                                startActivity(intent1);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void notifyUserAboutTakenOrder(String carModel, String carPlate, String userId) {
        reference = database.getReference(Common.TOKEN_REFERENCE);
        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            TokenModel token = snapshot.getValue(TokenModel.class);
                            if (token != null){
                                new Thread(() -> {
                                    try {
                                        UserUtils.sendOrderTakenNotification(token.getToken(), carModel, carPlate);
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

    private String createStringFromOrder(String addresses) {
        String[] adr = addresses.split(";");

        return String.join("\n", adr);
    }
}