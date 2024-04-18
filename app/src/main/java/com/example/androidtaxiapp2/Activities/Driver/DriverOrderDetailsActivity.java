package com.example.androidtaxiapp2.Activities.Driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.androidtaxiapp2.Enums.OrderStatus;
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

    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityDriverOrderDetailsBinding.inflate(getLayoutInflater());

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.ORDERS_REFERENCE);

        Intent intent = getIntent();

        Order order = (Order) intent.getSerializableExtra("order");

        String addressesStr = createStringFromOrder(order.get_addresses());
        String priceStr = "Price: " + order.get_price();
        String durationStr = "Duration: " + order.get_duration();

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
            checkIfOrderStillAvailable(order.get_uid());
            setDriverToOrder(order);
        });

        setContentView(binding.getRoot());
    }

    private void checkIfOrderStillAvailable(String orderId) {
        reference.child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Order order = snapshot.getValue(Order.class);
                    if (order.get_driverid().isEmpty()){
                        setDriverToOrder(order);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setDriverToOrder(Order order) {
        order.set_driverid(Common.currentUser.get_uid());
        order.set_orderStatus(OrderStatus.InProgress.toString());
        reference.child(order.get_uid()).setValue(order).addOnCompleteListener(task -> {
            notifyUserAboutTakenOrder(order.get_userid());
            Intent intent1 = new Intent(DriverOrderDetailsActivity.this, DriverHomeActivity.class);
            intent1.putExtra("order", order);
            startActivity(intent1);
            finish();
        });
    }

    private void notifyUserAboutTakenOrder(String userId) {
        reference = database.getReference(Common.TOKEN_REFERENCE);
        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            TokenModel token = snapshot.getValue(TokenModel.class);
                            if (token != null){
                                new Thread(() -> {
                                    try {
                                        UserUtils.sendOrderTakenNotification(token.getToken());
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