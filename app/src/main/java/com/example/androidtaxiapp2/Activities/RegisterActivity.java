package com.example.androidtaxiapp2.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidtaxiapp2.Models.Common;

import com.example.androidtaxiapp2.Models.User;
import com.example.androidtaxiapp2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText _signUpName;
    private EditText _signUpLastname;
    private EditText _signUpPhone;
    private EditText _signUpEmail;
    private EditText _signUpPassword;
    private EditText _signUpConfirmPassword;
    private TextView _loginRedirectText;
    private Button _signUpButton;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        _signUpName = findViewById(R.id.edt_first_name);
        _signUpLastname = findViewById(R.id.edt_last_name);
        _signUpPhone = findViewById(R.id.edt_phone_number);
        _signUpEmail = findViewById(R.id.edt_email_text);
        _signUpPassword = findViewById(R.id.edt_password_text);
        _signUpConfirmPassword = findViewById(R.id.edt_confirm_password_text);
        _loginRedirectText = findViewById(R.id.login_redirect_text);
        _signUpButton = findViewById(R.id.btn_register);



        _signUpButton.setOnClickListener(v -> {
            database = FirebaseDatabase.getInstance();
            reference = database.getReference("users");

            String name = _signUpName.getText().toString();
            String lastname = _signUpLastname.getText().toString();
            String phone = _signUpPhone.getText().toString();
            String email = _signUpEmail.getText().toString();
            String password = _signUpPassword.getText().toString();
            String confirmPass = _signUpConfirmPassword.getText().toString();

            String mobileRegex = "\\+?3?8?0\\d{9}";
            Matcher mobileMatcher;
            Pattern mobilePattern = Pattern.compile(mobileRegex);
            mobileMatcher = mobilePattern.matcher(phone);

            if (name.isEmpty() || lastname.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()){
                Toast.makeText(RegisterActivity.this, "Fill all fields", Toast.LENGTH_SHORT).show();
            }
            else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                _signUpEmail.setError("Invalid email");
                _signUpEmail.requestFocus();
            }
            else if(!mobileMatcher.find()){
                _signUpPhone.setError("Invalid phone");
                _signUpPhone.requestFocus();
            }
            else if(!password.equals(confirmPass)){
                _signUpConfirmPassword.setError("Pass doesn`t match");
                _signUpConfirmPassword.requestFocus();
            }
            else {
                database.getReference().child("Roles").child("Client_role_id").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String roleId = snapshot.getKey();

                            registerUserFireBaseAuth(name, lastname, email, password, "", phone, roleId, Calendar.getInstance().getTime().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

        _loginRedirectText.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
            startActivity(intent);
        });
    }
    private void registerUserFireBaseAuth(String name, String lastname, String email, String password,String imgUrl, String phone, String roleId, String date) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                    User user = new User(firebaseUser.getUid(), name, lastname, email, password, "", phone, roleId, Calendar.getInstance().getTime().toString());
                    reference.child(firebaseUser.getUid()).setValue(user).addOnCompleteListener(task1 -> {

                        if (task1.isSuccessful()){
                            firebaseUser.sendEmailVerification();

                            Toast.makeText(RegisterActivity.this,"User succesfuly registered", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, SplashScreenActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
//                                Common.currentUser = user;
//                                Intent intent = new Intent(RegisterActivity.this, UserHomeActivity.class);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(intent);
//                                finish();
                        }else{
                            Toast.makeText(RegisterActivity.this,"User registration failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        _signUpPassword.setError("Your password is to weak");
                        _signUpPassword.requestFocus();
                    }
                    catch (FirebaseAuthInvalidCredentialsException e){
                        _signUpEmail.setError("Your email is invalid or already in use");
                        _signUpEmail.requestFocus();
                    }
                    catch (FirebaseAuthUserCollisionException e){
                        _signUpEmail.setError("User is already registered with this email");
                        _signUpEmail.requestFocus();
                    }
                    catch (Exception e){

                    }
                }
            }
        });
    }
}
