package com.example.androidtaxiapp2.Activities.User;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeUserInfoActivity extends AppCompatActivity {

    private TextView editTextName;
    private TextView editTextSurname;
    private TextView editTextPhone;
    private Button updateButton;
    private Button backButton;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_info);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.USERS_REFERENCE).child(Common.currentUser.get_uid());

        editTextName = findViewById(R.id.change_data_textView_show_firstname);
        editTextSurname = findViewById(R.id.change_data_textView_show_surname);
        editTextPhone = findViewById(R.id.change_data_textView_show_phone);
        updateButton = findViewById(R.id.save_new_user_info_btn);
        backButton = findViewById(R.id.change_data_btn_back);

        String mobileRegex = "\\+?3?8?0\\d{9}";


        displayeUserInfo();

        backButton.setOnClickListener(v -> {
            goBackToUserProfile();
        });

        updateButton.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String surname = editTextSurname.getText().toString();
            String phone = editTextPhone.getText().toString();

            Matcher mobileMatcher;
            Pattern mobilePattern = Pattern.compile(mobileRegex);
            mobileMatcher = mobilePattern.matcher(phone);

            if(!mobileMatcher.find() || phone.length() > 13){
                editTextPhone.setError("Invalid phone");
                editTextPhone.requestFocus();
            }
            else {
                reference.child("_name").setValue(name);
                reference.child("_lastname").setValue(surname);
                reference.child("_phone").setValue(phone);
                goBackToUserProfile();
            }
        });
    }

    private void displayeUserInfo(){
        editTextName.setText(Common.currentUser.get_name());
        editTextSurname.setText(Common.currentUser.get_lastname());
        editTextPhone.setText(Common.currentUser.get_phone());
    }

    private void goBackToUserProfile(){
        Intent intent = new Intent(ChangeUserInfoActivity.this,UserProfileActivity.class);
        startActivity(intent);
        finish();
    }
}