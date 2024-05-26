package com.example.androidtaxiapp2.Activities.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.androidtaxiapp2.Enums.Roles;
import com.example.androidtaxiapp2.Models.Car;
import com.example.androidtaxiapp2.Models.CarTypes;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Role;
import com.example.androidtaxiapp2.Models.User;
import com.example.androidtaxiapp2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AssignDriverActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Button backBtn;
    private Button assignBtn;
    private TextView carModelTxt;
    private TextView carPlateTxt;
    private TextView carTypeTxt;
    private Spinner availableDriversSpinner;
    private List<User> allDrivers = new ArrayList<>();
    private List<User> driversWithoutCar = new ArrayList<>();
    private List<String> driversFullName = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private int driverIndex;
    private Car currCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_driver);

        Intent intent = getIntent();

        currCar = (Car) intent.getSerializableExtra("car");

        backBtn = findViewById(R.id.assign_driver_btn_back);
        assignBtn = findViewById(R.id.assign_driver_save_btn);
        carModelTxt = findViewById(R.id.assign_car_model_txt);
        carPlateTxt = findViewById(R.id.assign_car_plate_txt);
        carTypeTxt = findViewById(R.id.assign_car_type_txt);
        availableDriversSpinner = findViewById(R.id.select_driver_spinner);
        availableDriversSpinner.setOnItemSelectedListener(this);

        backBtn.setOnClickListener(v -> {
            redirectActivity(AssignDriverActivity.this,CarListActivity.class);
        });

        displayValues();

        getAvailableDrivers();
    }

    private void displayValues() {
        carModelTxt.setText(currCar.get_carModel());
        carPlateTxt.setText(currCar.get_carPlate());
        FirebaseDatabase.getInstance().getReference(Common.CAR_TYPES_REFERENCE).child(currCar.get_carTypeId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    CarTypes carType = snapshot.getValue(CarTypes.class);
                    carTypeTxt.setText(carType.get_name());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAvailableDrivers() {
        FirebaseDatabase.getInstance().getReference(Common.ROLES_REFERENCE)
                .orderByChild("_name").equalTo(Roles.Driver.toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot child : snapshot.getChildren()){
                                Role role = child.getValue(Role.class);
                                getDrivers(role.get_uid());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getDrivers(String role_id) {
        FirebaseDatabase.getInstance().getReference(Common.USERS_REFERENCE)
                .orderByChild("_roleId").equalTo(role_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot child : snapshot.getChildren()){
                                User user = child.getValue(User.class);
                                checkIfBlocked(user);
                            }
                            findDriversWithoutCar();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void checkIfBlocked(User user) {
        FirebaseDatabase.getInstance().getReference(Common.BLOCKED_USERS).orderByChild("_userId").equalTo(user.get_uid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    allDrivers.add(user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void findDriversWithoutCar() {
        FirebaseDatabase.getInstance().getReference(Common.CARS_REFERENCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (User user : allDrivers){
                                boolean noCar = true;
                                for (DataSnapshot child : snapshot.getChildren()){
                                    Car car = child.getValue(Car.class);
                                    if (car.get_driverId().equals(user.get_uid())){
                                        noCar = false;
                                        break;
                                    }
                                }
                                if (noCar){
                                    driversWithoutCar.add(user);
                                    driversFullName.add(user.get_name()+" " + user.get_lastname());
                                }
                            }

                            adapter = new ArrayAdapter<>(getBaseContext(), R.layout.driver_name_item,driversFullName);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            availableDriversSpinner.setAdapter(adapter);

                            assignBtn.setOnClickListener(v -> setDriverToCar(driversWithoutCar.get(driverIndex).get_uid()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setDriverToCar(String driverId) {
        currCar.set_driverId(driverId);

        FirebaseDatabase.getInstance().getReference(Common.CARS_REFERENCE)
                .child(currCar.get_uid())
                .setValue(currCar)
                .addOnCompleteListener(task -> {
                    redirectActivity(AssignDriverActivity.this,CarListActivity.class);
                });
    }

    private void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity,secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spinner){
            driverIndex = position;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}