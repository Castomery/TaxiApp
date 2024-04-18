package com.example.androidtaxiapp2.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextThemeWrapper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidtaxiapp2.Activities.Client.UserHomeActivity;
import com.example.androidtaxiapp2.Activities.Driver.DriverHomeActivity;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Role;
import com.example.androidtaxiapp2.Models.User;
import com.example.androidtaxiapp2.R;
import com.example.androidtaxiapp2.Utils.UserUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_NOTIFICATION = 2;
    private EditText _loginEmail;
    private EditText _loginPassword;
    private TextView _signUpReditectText;
    private Button _loginButton;

    private static final String TAG = "LoginActivity";

    private FirebaseAuth firebaseAuth;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.log_in_layout);

    firebaseAuth = FirebaseAuth.getInstance();
    database = FirebaseDatabase.getInstance();
    reference = database.getReference(Common.USERS_REFERENCE);

    _loginEmail = findViewById(R.id.edt_login_email_text);
    _loginPassword = findViewById(R.id.edt_login_password_text);
    _signUpReditectText = findViewById(R.id.register_redirect_text);
    _loginButton = findViewById(R.id.btn_login);

    _loginButton.setOnClickListener(v -> {
        String email = _loginEmail.getText().toString();
        String password = _loginPassword.getText().toString();
        if (validateUserEmail(email) && validatePassword(password)){
            loginUser(email,password);
        }
    });

    _signUpReditectText.setOnClickListener(v -> {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    });

    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},RC_NOTIFICATION);
    }
   }

    @Override
   protected void onStart(){
        super.onStart();
        FirebaseUser user= firebaseAuth.getCurrentUser();
        if(user !=null){
            if(user.isEmailVerified()){
                Toast.makeText(LoginActivity.this,"Already Logged in", Toast.LENGTH_SHORT).show();
                setCurrentUser();
            }
        }
   }

    private void setCurrentUser() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        reference.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Common.currentUser = snapshot.getValue(User.class);
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnFailureListener(e -> Toast.makeText(LoginActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show())
                                    .addOnSuccessListener(s -> {
                                        Log.d("TOKEN", s);
                                        UserUtils.updateToken(LoginActivity.this,s);
                                    });
                            goToHomeActivity();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){


                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if(user.isEmailVerified()){
                        Toast.makeText(LoginActivity.this,"Logged in", Toast.LENGTH_SHORT).show();
                        setCurrentUser();
                    }
                    else{
                        user.sendEmailVerification();
                        firebaseAuth.signOut();
                        showAlertDialog();
                    }
                }
                else {
                    try{
                        throw task.getException();
                    }
                    catch (FirebaseAuthInvalidUserException e){
                        _loginEmail.setError("User doesn`t exist");
                        _loginEmail.requestFocus();
                    }
                    catch (FirebaseAuthInvalidCredentialsException e){
                        _loginEmail.setError("Invalid credentials");
                        _loginEmail.requestFocus();
                    }
                    catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    private void goToHomeActivity() {
        database.getReference(Common.ROLES_REFERENCE).child(Common.currentUser.get_roleId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Role role = snapshot.getValue(Role.class);
                            if (role.get_name().equals("Client")){
                                Log.d("TAG","Role:" + role);
                                startActivity(new Intent(LoginActivity.this, UserHomeActivity.class));
                                finish();
                            }
                            else if(role.get_name().equals("Driver")){
                                Log.d("TAG","Role:" + role.get_name());
                                startActivity(new Intent(LoginActivity.this, DriverHomeActivity.class));
                                finish();
                            }
                            else{
                                Log.d("TAG"," no if Role:" + role);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this,R.style.AlertDialogDark));
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email");

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               Intent intent = new Intent(Intent.ACTION_MAIN);
               intent.addCategory(Intent.CATEGORY_APP_EMAIL);
               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               startActivity(intent);
            }
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    public Boolean validateUserEmail(String email){

        if (email.isEmpty()){
            _loginEmail.setError("Field can`t be empty");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            _loginEmail.setError("Invalid email");
            _loginEmail.requestFocus();
            return false;
        }
        else{
            _loginEmail.setError(null);
            return true;
        }
   }

   public Boolean validatePassword(String password){
       if (password.isEmpty()){
           _loginPassword.setError("Password can`t be empty");
           return false;
       }
       else{
           _loginPassword.setError(null);
           return true;
       }
   }
}
