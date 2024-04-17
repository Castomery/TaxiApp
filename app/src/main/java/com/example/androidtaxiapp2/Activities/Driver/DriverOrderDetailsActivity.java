package com.example.androidtaxiapp2.Activities.Driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.androidtaxiapp2.Activities.Client.ClientOrderDetailsActivity;
import com.example.androidtaxiapp2.Activities.OrderHistoryActivity;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Order;
import com.example.androidtaxiapp2.R;
import com.example.androidtaxiapp2.Utils.UserUtils;
import com.example.androidtaxiapp2.databinding.ActivityDriverOrderDetailsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;

import java.io.Serializable;

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
            setDriverToOrder(order);
        });

        setContentView(binding.getRoot());
    }

    private void setDriverToOrder(Order order) {
        order.set_driverid(Common.currentUser.get_uid());
        reference.child(order.get_uid()).setValue(order).addOnCompleteListener(task -> {
            new Thread(() -> {
                try {
                    UserUtils.sendOrderTakenNotification();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            Intent intent1 = new Intent(DriverOrderDetailsActivity.this, DriverHomeActivity.class);
            intent1.putExtra("route", order.get_route());
            startActivity(intent1);
            finish();
        });
    }

    private String createStringFromOrder(String addresses) {
        String[] adr = addresses.split(";");

        return String.join("\n", adr);
    }
}