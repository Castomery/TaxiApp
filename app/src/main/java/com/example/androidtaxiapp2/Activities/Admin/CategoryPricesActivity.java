package com.example.androidtaxiapp2.Activities.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.androidtaxiapp2.Activities.Client.ClientOrderDetailsActivity;
import com.example.androidtaxiapp2.Activities.OrderHistoryActivity;
import com.example.androidtaxiapp2.Adapters.CarTypes_RecyclerViewAdapter;
import com.example.androidtaxiapp2.Interfaces.RecyclerViewInterface;
import com.example.androidtaxiapp2.Models.CarTypes;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryPricesActivity extends AppCompatActivity implements RecyclerViewInterface {

    private RecyclerView recyclerView;
    private Button backBtn;
    private List<CarTypes> carTypesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_prices);

        recyclerView = findViewById(R.id.prices_recyclerview);
        backBtn = findViewById(R.id.category_prices_btn_back);

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryPricesActivity.this, AdminHomeActivity.class);
            startActivity(intent);
            finish();
        });

        getCarTypes(this);
    }

    private void getCarTypes(RecyclerViewInterface recyclerViewInterface) {
        FirebaseDatabase.getInstance().getReference(Common.CAR_TYPES_REFERENCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot child : snapshot.getChildren()){
                                CarTypes carType = child.getValue(CarTypes.class);
                                carTypesList.add(carType);
                            }

                            CarTypes_RecyclerViewAdapter carTypesRecyclerViewAdapter = new CarTypes_RecyclerViewAdapter(getBaseContext(),carTypesList,recyclerViewInterface);
                            recyclerView.setAdapter(carTypesRecyclerViewAdapter);
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
        Intent intent = new Intent(CategoryPricesActivity.this, EditCategoryActivity.class);
        intent.putExtra("carType", carTypesList.get(position));
        startActivity(intent);
        finish();

    }
}