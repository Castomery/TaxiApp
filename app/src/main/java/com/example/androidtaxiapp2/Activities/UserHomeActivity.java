package com.example.androidtaxiapp2.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.R;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.NavigationUI;

import com.example.androidtaxiapp2.databinding.ActivityUserHomeBinding;
import com.google.firebase.auth.FirebaseAuth;

public class UserHomeActivity extends AppCompatActivity {

    protected AppBarConfiguration mAppBarConfiguration;
    protected DrawerLayout drawer;
    protected NavigationView navigationView;
    protected NavController navController;
    protected ActivityUserHomeBinding binding;
    private Button _becomeDriverbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUserHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarUserHome.toolbar);
        drawer = binding.drawerLayout;
        navigationView = binding.navView;

        // Set up navigation
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_user_profile)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_user_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Custom initialization for user-specific data
        initializeUser();

        _becomeDriverbtn = findViewById(R.id.become_driver_btn);
        _becomeDriverbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void initializeUser() {

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_user_profile) {
                Intent intent = new Intent(UserHomeActivity.this, UserProfileActivity.class);
                startActivity(intent);
                finish();
            }
            return true;
        });

        View headerView = navigationView.getHeaderView(0);
        TextView txt_name = headerView.findViewById(R.id.txt_name);
        TextView txt_phone = headerView.findViewById(R.id.txt_phone);
        ImageView img  = headerView.findViewById(R.id.imageView);

        txt_name.setText(Common.buildWelcomeMessage());
        txt_phone.setText(Common.currentUser != null ? Common.currentUser.get_phone():"");
        if (Common.currentUser != null && !TextUtils.isEmpty(Common.currentUser.get_urlImage())){
            Glide.with(this)
                    .load(Common.currentUser.get_urlImage())
                    .into(img);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_user_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}