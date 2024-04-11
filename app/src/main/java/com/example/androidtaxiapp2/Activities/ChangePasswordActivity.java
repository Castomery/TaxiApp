package com.example.androidtaxiapp2.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText currPass;
    private EditText newPass;
    private EditText confNewPass;
    private Button changePassBtn;
    private Button backBtn;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        currPass = findViewById(R.id.curr_password_text);
        newPass = findViewById(R.id.new_pass_text);
        confNewPass = findViewById(R.id.confirm_new_pass_text);
        changePassBtn = findViewById(R.id.save_changed_pass_btn);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.USERS_REFERENCE).child(Common.currentUser.get_uid());

        backBtn = findViewById(R.id.change_pass_btn_back);

        backBtn.setOnClickListener(v -> goToUserProfile());

        changePassBtn.setOnClickListener(v -> {
            String pass = currPass.getText().toString();
            String npass = newPass.getText().toString();
            String confpass= confNewPass.getText().toString();

            if(!npass.equals(confpass)){
                confNewPass.setError("Pass doesn`t match");
                confNewPass.requestFocus();
            }
            else{
                updatePassword(pass, npass);
                goToUserProfile();
            }
        });
    }

    private void goToUserProfile() {
        Intent intent = new Intent(ChangePasswordActivity.this,UserProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void updatePassword(String pass, String newPassword){


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(),pass);

        user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        reference.child("_password").setValue(newPassword);
                        Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChangePasswordActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}