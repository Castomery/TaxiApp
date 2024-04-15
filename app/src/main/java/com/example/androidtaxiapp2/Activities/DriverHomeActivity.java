package com.example.androidtaxiapp2.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidtaxiapp2.databinding.ActivityDriverHomeBinding;
import com.google.firebase.auth.FirebaseAuth;

public class DriverHomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityDriverHomeBinding binding;
    protected NavigationView navigationView;
    protected NavController navController;
    protected DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarDriverHome.toolbar);
        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_user_profile)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_driver_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        initializeDriver();
    }

    private void initializeDriver() {

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_user_profile) {
                Intent intent = new Intent(DriverHomeActivity.this, UserProfileActivity.class);
                startActivity(intent);
                finish();
            }
            return true;
        });

//        View headerView = navigationView.getHeaderView(0);
//        TextView txt_name = headerView.findViewById(R.id.driver_txt_name);
//        TextView txt_phone = headerView.findViewById(R.id.driver_txt_phone);
//        ImageView img  = headerView.findViewById(R.id.driver_imageView);
//
//        txt_name.setText(Common.buildWelcomeMessage());
//        txt_phone.setText(Common.currentUser != null ? Common.currentUser.get_phone():"");
//        if (Common.currentUser != null && !TextUtils.isEmpty(Common.currentUser.get_urlImage())){
//            Glide.with(this)
//                    .load(Common.currentUser.get_urlImage())
//                    .into(img);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_driver_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}