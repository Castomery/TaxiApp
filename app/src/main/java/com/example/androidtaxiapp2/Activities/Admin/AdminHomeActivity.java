package com.example.androidtaxiapp2.Activities.Admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.androidtaxiapp2.Activities.User.UserProfileActivity;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.R;
import com.example.androidtaxiapp2.databinding.ActivityAdminHomeBinding;

public class AdminHomeActivity extends AppCompatActivity {

    private LinearLayout adminHeader;
    private CardView userList;
    private CardView addCar;
    private CardView statementsList;
    private CardView blockedUsersList;
    private CardView categoryPrices;
    private CardView assignDriverList;
    private ActivityAdminHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminHomeBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        adminHeader = binding.adminHeader;
        userList = binding.adminUserList;
        addCar = binding.adminAddCar;
        statementsList = binding.adminStatements;
        blockedUsersList = binding.adminBlockedUsers;
        categoryPrices = binding.adminPrices;
        assignDriverList = binding.adminAssignCar;
        initializeAdmin();

        adminHeader.setOnClickListener(v -> redirectActivity(AdminHomeActivity.this, UserProfileActivity.class));
        userList.setOnClickListener(v -> redirectActivity(AdminHomeActivity.this, UsersListActivity.class));
        addCar.setOnClickListener(v -> redirectActivity(AdminHomeActivity.this, AddCarActivity.class));
        statementsList.setOnClickListener(v -> redirectActivity(AdminHomeActivity.this, StatementsListActivity.class));
        blockedUsersList.setOnClickListener(v -> redirectActivity(AdminHomeActivity.this, BlockedUsersListActivity.class));
        categoryPrices.setOnClickListener(v -> redirectActivity(AdminHomeActivity.this, CategoryPricesActivity.class));
        assignDriverList.setOnClickListener(v -> redirectActivity(AdminHomeActivity.this, CarListActivity.class));
    }

    private void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity,secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private void initializeAdmin() {
        TextView txt_name = findViewById(R.id.admin_txt_name);
        TextView txt_phone = findViewById(R.id.admin_txt_phone);
        ImageView img  = findViewById(R.id.admin_imageView);

        txt_name.setText(Common.buildWelcomeMessage());
        txt_phone.setText(Common.currentUser != null ? Common.currentUser.get_phone():"");
        if (Common.currentUser != null && !TextUtils.isEmpty(Common.currentUser.get_urlImage())){
            Glide.with(this)
                    .load(Common.currentUser.get_urlImage())
                    .into(img);
        }
    }
}