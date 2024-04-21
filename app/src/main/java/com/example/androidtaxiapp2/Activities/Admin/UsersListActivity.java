package com.example.androidtaxiapp2.Activities.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;

import com.example.androidtaxiapp2.Adapters.Users_RecyclerViewAdapter;
import com.example.androidtaxiapp2.Enums.Roles;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Order;
import com.example.androidtaxiapp2.Models.Role;
import com.example.androidtaxiapp2.Models.User;
import com.example.androidtaxiapp2.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class UsersListActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private RecyclerView recyclerView;
    private Button usersListBackBtn;
    private List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.USERS_REFERENCE);
        recyclerView = findViewById(R.id.usersRecyclerView);
        usersListBackBtn = findViewById(R.id.users_list_btn_back);
        getClientsAndDrivers();

        usersListBackBtn.setOnClickListener(v -> {
            Intent intent = new Intent(UsersListActivity.this, AdminHomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void getClientsAndDrivers() {
        reference = database.getReference(Common.ROLES_REFERENCE);
        reference.orderByChild("_name").equalTo(Roles.Admin.toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for(DataSnapshot snapshot1 : snapshot.getChildren()){
                        Role role = snapshot1.getValue(Role.class);
                        addUsersToList(role);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addUsersToList(Role role) {
        FirebaseDatabase.getInstance().getReference(Common.USERS_REFERENCE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot childSnapshot : snapshot.getChildren()){
                        if (childSnapshot.exists()){
                            User user = childSnapshot.getValue(User.class);
                            if (!user.get_roleId().equals(role.get_uid())){
                                userList.add(user);
                            }
                        }
                    }

                    Users_RecyclerViewAdapter adapter = new Users_RecyclerViewAdapter(getBaseContext(), userList);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUsers(){
//        reference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot childSnapshot : snapshot.getChildren()){
//                    if (childSnapshot != null){
//                        User user = childSnapshot.getValue(User.class);
//                        if (user != null){
//                            String roleId = user.get_roleId();
//
//                            DatabaseReference roleReference = database.getReference(Common.ROLES_REFERENCE).child(roleId);
//                            roleReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot roleSnapshot) {
//                                    Role role = roleSnapshot.getValue(Role.class);
//                                    if (role != null && !role.get_name().equals(Roles.Admin.toString())) {
//                                        userList.add(user);
//                                    }

//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//                                    // Handle error
//                                }
//                            });
//                        }
//                    }
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }
}