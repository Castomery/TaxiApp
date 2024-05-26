package com.example.androidtaxiapp2.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidtaxiapp2.Models.Common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.androidtaxiapp2.Models.User;
import com.example.androidtaxiapp2.R;
import com.example.androidtaxiapp2.Utils.UserUtils;
import com.example.androidtaxiapp2.databinding.ActivitySplashScreenBinding;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.List;

public class SplashScreenActivity extends AppCompatActivity {

    private final static int LOGIN_REQUEST_CODE = 7171;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    //Continue
    private FirebaseDatabase database;
    private DatabaseReference reference;

    private ActivitySplashScreenBinding binding;

    @Override
    protected void onStart(){
        super.onStart();
        delaySplashScreen();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

    }

    private void showLoginLayout() {
        Intent intent = new Intent(SplashScreenActivity.this,LoginActivity.class);
        startActivity(intent);
    }

    private void delaySplashScreen() {

         binding.progressBar.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
                    Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
        }
                , 3000);
    }
}