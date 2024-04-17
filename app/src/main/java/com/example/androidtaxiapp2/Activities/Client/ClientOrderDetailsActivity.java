package com.example.androidtaxiapp2.Activities.Client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.androidtaxiapp2.Activities.OrderHistoryActivity;
import com.example.androidtaxiapp2.Models.Order;
import com.example.androidtaxiapp2.R;

public class ClientOrderDetailsActivity extends AppCompatActivity {

    private TextView addresses;
    private TextView status;
    private TextView price;
    private TextView duration;
    private TextView date;

    private Button backButton;

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

        addresses.setText(addressesStr);
        status.setText(statusStr);
        price.setText(priceStr);
        duration.setText(durationStr);
        date.setText(dateStr);

        backButton.setOnClickListener(v -> {
            Intent intent1 = new Intent(ClientOrderDetailsActivity.this, OrderHistoryActivity.class);
            startActivity(intent1);
            finish();
        });
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
}