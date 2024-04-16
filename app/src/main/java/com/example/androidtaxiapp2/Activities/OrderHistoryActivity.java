package com.example.androidtaxiapp2.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;

import com.example.androidtaxiapp2.Adapters.Order_RecyclerViewAdapter;
import com.example.androidtaxiapp2.Interfaces.RecyclerViewInterface;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Order;
import com.example.androidtaxiapp2.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity implements RecyclerViewInterface {

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private RecyclerView recyclerView;

    private Button orderHistoryBackBtn;

    private List<Order> userOrders = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.ORDERS_REFERENCE);
        orderHistoryBackBtn = findViewById(R.id.order_history_btn_back);
        recyclerView = findViewById(R.id.mrecyclerview);
        getUserOrders(this);

        orderHistoryBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderHistoryActivity.this, UserHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void getUserOrders(RecyclerViewInterface recyclerViewInterface) {
        reference.orderByChild("_userid").equalTo(Common.currentUser.get_uid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for(DataSnapshot childSnapshot : snapshot.getChildren()){
                        Order order = childSnapshot.getValue(Order.class);
                        userOrders.add(order);
                    }
                    Order_RecyclerViewAdapter adapter = new Order_RecyclerViewAdapter(getBaseContext(),userOrders, recyclerViewInterface);
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
        Intent intent = new Intent(OrderHistoryActivity.this,OrderDetailsActivity.class);
        intent.putExtra("order", userOrders.get(position));
        startActivity(intent);
        finish();
    }
}