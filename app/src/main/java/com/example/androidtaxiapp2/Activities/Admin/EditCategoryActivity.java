package com.example.androidtaxiapp2.Activities.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.androidtaxiapp2.Models.CarTypes;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

public class EditCategoryActivity extends AppCompatActivity {

    private Button backBtn;
    private Button saveChanges;
    private TextView typeName;
    private TextView priceForCar;
    private TextView pricePerKm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_category);

        Intent intent = getIntent();

        CarTypes carType = (CarTypes) intent.getSerializableExtra("carType");

        backBtn = findViewById(R.id.edit_category_btn_back);
        saveChanges = findViewById(R.id.edit_category_save_btn);
        typeName = findViewById(R.id.type_name_edit);
        priceForCar = findViewById(R.id.price_for_car_edit);
        pricePerKm = findViewById(R.id.price_per_km_edit);

        typeName.setText(carType.get_name());
        priceForCar.setText(String.valueOf(carType.get_priceForCar()));
        pricePerKm.setText(String.valueOf(carType.get_pricePerKm()));

        saveChanges.setOnClickListener(v -> updateData(carType,carType.get_uid()));
        backBtn.setOnClickListener(v -> {
            Intent intent1 = new Intent(EditCategoryActivity.this, CategoryPricesActivity.class);
            startActivity(intent1);
            finish();
        });
    }

    private void updateData(CarTypes carType, String uid) {
        carType.set_name(typeName.getText().toString());
        carType.set_priceForCar(Double.valueOf(priceForCar.getText().toString()));
        carType.set_pricePerKm(Double.valueOf(pricePerKm.getText().toString()));

        FirebaseDatabase.getInstance().getReference(Common.CAR_TYPES_REFERENCE)
                .child(uid).setValue(carType).addOnCompleteListener(task -> {
                    Intent intent = new Intent(EditCategoryActivity.this, CategoryPricesActivity.class);
                    startActivity(intent);
                    finish();
                });
    }
}