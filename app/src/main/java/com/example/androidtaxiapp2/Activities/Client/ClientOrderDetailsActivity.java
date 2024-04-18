package com.example.androidtaxiapp2.Activities.Client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.androidtaxiapp2.Activities.Driver.DriverHomeActivity;
import com.example.androidtaxiapp2.Activities.Driver.DriverOrderDetailsActivity;
import com.example.androidtaxiapp2.Activities.OrderHistoryActivity;
import com.example.androidtaxiapp2.Enums.OrderStatus;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Order;
import com.example.androidtaxiapp2.Models.TokenModel;
import com.example.androidtaxiapp2.R;
import com.example.androidtaxiapp2.Utils.UserUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

public class ClientOrderDetailsActivity extends AppCompatActivity {

    private TextView addresses;
    private TextView status;
    private TextView price;
    private TextView duration;
    private TextView date;
    private Button backButton;
    private Button cancelOrderBtn;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        Intent intent = getIntent();

        Order order = (Order) intent.getSerializableExtra("order");

        String addressesStr = createStringFromOrder(order.get_addresses(),order.get_destributionPrice());
        String statusStr = "Status: " + order.get_orderStatus();
        String priceStr = "Price: " + order.get_price();
        String durationStr = "Duration: " + order.get_duration();
        String dateStr = "Date: " + order.get_orderDate();

        addresses = findViewById(R.id.order_details_addressesTextView);
        status = findViewById(R.id.order_details_orderStatusTextView);
        price = findViewById(R.id.order_details_priceTextView);
        duration = findViewById(R.id.order_details_durationTextView);
        date = findViewById(R.id.order_details_dateTextView);
        backButton = findViewById(R.id.order_details_btn_back);
        cancelOrderBtn = findViewById(R.id.cancel_order_btn);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.ORDERS_REFERENCE);

        addresses.setText(addressesStr);
        status.setText(statusStr);
        price.setText(priceStr);
        duration.setText(durationStr);
        date.setText(dateStr);

        backButton.setOnClickListener(v -> {
            redirectActivity(this,OrderHistoryActivity.class);
        });

        cancelOrderBtn.setOnClickListener(v -> {
            checkIfDriverIsSet(order.get_uid());
            redirectActivity(this, UserHomeActivity.class);
        });
    }

    private void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity,secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private void checkIfDriverIsSet(String orderId) {

        FirebaseDatabase.getInstance().getReference(Common.ORDERS_REFERENCE)
                .child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Order order = snapshot.getValue(Order.class);
                            if (order.get_driverid().isEmpty()){
                                cancelOrder(order);
                            }
                            else{
                                cancelOrder(order);
                                notifyDriverAboutCancelation(order.get_driverid());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void cancelOrder(Order order) {
        order.set_orderStatus(OrderStatus.Canceled.toString());
        reference.child(order.get_uid()).setValue(order);
    }

    private String createStringFromOrder(String addresses, String destributionPrice) {
        String[] adr = addresses.split(";");
        String[] dstr = destributionPrice.split(",");
        String spliter = "----------------------------";

        String result = "";
        for (int i = 0; i < adr.length; i++){
            if(i == 0){
                result += adr[i] +"\n";
                result += spliter;
            }
            else{
                if (dstr != null && dstr[i-1] != null){
                    result += adr[i]+"\n";
                    result += "Price to pay: " + dstr[i-1] +"\n";
                    result += spliter;
                }
                else {
                    result += adr[i] +"\n";
                    result += spliter;
                }

            }
            if (i != adr.length -1){
                result += "\n";
            }
        }
        return result;
    }

    private void notifyDriverAboutCancelation(String driverId) {
        FirebaseDatabase.getInstance().getReference(Common.TOKEN_REFERENCE)
                .child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            TokenModel token = snapshot.getValue(TokenModel.class);
                            if (token != null){
                                new Thread(() -> {
                                    try {
                                        UserUtils.sendOrderCanceledNotification(token.getToken());
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