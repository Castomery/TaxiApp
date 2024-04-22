package com.example.androidtaxiapp2.Activities.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.androidtaxiapp2.Adapters.BlockedUsers_RecyclerViewAdapter;
import com.example.androidtaxiapp2.Models.BlockedUserModel;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BlockedUsersListActivity extends AppCompatActivity {

    private RecyclerView blockedUsersRecyclerView;
    private Button backBtn;

    private List<BlockedUserModel> blockedUserModelList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users_list);

        blockedUsersRecyclerView = findViewById(R.id.blockeUsers_recyclerView);
        backBtn = findViewById(R.id.blocked_users_list_btn_back);

        getBlockedUsers();

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(BlockedUsersListActivity.this, AdminHomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void getBlockedUsers() {
        FirebaseDatabase.getInstance().getReference(Common.BLOCKED_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot child : snapshot.getChildren()){
                                BlockedUserModel blockedUserModel = child.getValue(BlockedUserModel.class);
                                blockedUserModelList.add(blockedUserModel);
                            }
                            BlockedUsers_RecyclerViewAdapter blockedUsersRecyclerViewAdapter = new BlockedUsers_RecyclerViewAdapter(getBaseContext(),blockedUserModelList);
                            blockedUsersRecyclerView.setAdapter(blockedUsersRecyclerViewAdapter);
                            blockedUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}