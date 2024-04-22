package com.example.androidtaxiapp2.Activities.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.androidtaxiapp2.Models.Car;
import com.example.androidtaxiapp2.Models.CarTypes;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddCarActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private EditText carModelText;
    private EditText carPlateText;
    private Button addCarBtn;
    private Button backBtn;
    private Spinner spinnerTextView;
    private ArrayAdapter<String> adapter;
    private List<CarTypes> carTypes = new ArrayList<>();
    private int typeIndex;
    private ArrayList<String> typesNames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        carModelText = findViewById(R.id.carModel_txt);
        carPlateText = findViewById(R.id.carPlate_txt);
        spinnerTextView = findViewById(R.id.spinner);
        addCarBtn = findViewById(R.id.add_car_btn);
        spinnerTextView.setOnItemSelectedListener(this);
        backBtn = findViewById(R.id.add_car_btn_back);

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(AddCarActivity.this, AdminHomeActivity.class);
            startActivity(intent);
            finish();
        });

        getCarTypes();
    }

    private void getCarTypes() {
        FirebaseDatabase.getInstance().getReference(Common.CAR_TYPES_REFERENCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for(DataSnapshot childSnapshot : snapshot.getChildren()){
                                if (childSnapshot.exists()){
                                    CarTypes carType = childSnapshot.getValue(CarTypes.class);
                                    carTypes.add(carType);
                                    typesNames.add(carType.get_name());
                                }
                            }

                            adapter = new ArrayAdapter<>(getBaseContext(), R.layout.cartype_list_item,typesNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerTextView.setAdapter(adapter);

                            addCarBtn.setOnClickListener(v -> checkIfCarAlreadyExist(carTypes.get(typeIndex)));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIfCarAlreadyExist(CarTypes carType){
        FirebaseDatabase.getInstance().getReference(Common.CARS_REFERENCE)
                .orderByChild("_carPlate")
                .equalTo(carPlateText.getText().toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Toast.makeText(getBaseContext(),"Car with this plate number already exist", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            addCarToDB(carType);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void addCarToDB(CarTypes carTypes) {
        String uid = UUID.randomUUID().toString();
        String model = carModelText.getText().toString();
        String plate = carPlateText.getText().toString();
        if (model.isEmpty() || plate.isEmpty()){
            Toast.makeText(this,"Fill all fields",Toast.LENGTH_SHORT).show();
        }
        else{
            Car car = new Car(uid,"",model,carTypes.get_uid(),plate);
            FirebaseDatabase.getInstance().getReference(Common.CARS_REFERENCE).child(uid).setValue(car).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(getBaseContext(),"Car successfully added", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spinner){
            typeIndex = position;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}