package com.example.androidtaxiapp2.Activities.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidtaxiapp2.Enums.OrderStatus;
import com.example.androidtaxiapp2.Enums.Roles;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Order;
import com.example.androidtaxiapp2.Models.Role;
import com.example.androidtaxiapp2.Models.Statement;
import com.example.androidtaxiapp2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StatementDetailsActivity extends AppCompatActivity {

    private TextView userIdTextView;
    private TextView statementDateTextView;
    private TextView statementTextTextView;

    private Button acceptBtn;
    private Button declineBtn;
    private Button backBtn;

    private Statement statement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statement_details);

        userIdTextView = findViewById(R.id.statement_details_userId_txt);
        statementDateTextView = findViewById(R.id.statement_details_date);
        statementTextTextView = findViewById(R.id.statement_details_text);
        backBtn = findViewById(R.id.statement_details_btn_back);
        acceptBtn = findViewById(R.id.accept_statement_btn);
        declineBtn = findViewById(R.id.decline_statement_btn);

        Intent intent = getIntent();
        statement = (Statement) intent.getSerializableExtra("statement");

        userIdTextView.setText(statement.get_userid());
        statementDateTextView.setText(statement.get_statementDate());
        statementTextTextView.setText(statement.get_statementText());

        backBtn.setOnClickListener(v -> redirectActivity(StatementDetailsActivity.this, StatementsListActivity.class));

        acceptBtn.setOnClickListener(v -> checkIfHasActiveOrders(statement.get_userid()));

        declineBtn.setOnClickListener(v -> {
            declineStatement(statement.get_uid());
        });
    }

    private void checkIfHasActiveOrders(String userId) {
        FirebaseDatabase.getInstance().getReference(Common.ORDERS_REFERENCE).orderByChild("_userid").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean hasActiveOrder = false;
                        if(snapshot.exists()){
                            for(DataSnapshot child : snapshot.getChildren()){
                                Order order = child.getValue(Order.class);
                                if (order.get_orderStatus().equals(OrderStatus.LookingForDriver.toString()) || order.get_orderStatus().equals(OrderStatus.WaitingForDriver.toString()) || order.get_orderStatus().equals(OrderStatus.InProgress.toString())){
                                    Toast.makeText(StatementDetailsActivity.this, "User has active order", Toast.LENGTH_SHORT).show();
                                    hasActiveOrder = true;
                                    return;
                                }
                            }
                            if (!hasActiveOrder){
                                acceptStatement(statement.get_uid(), statement.get_userid());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void declineStatement(String statementId) {
        FirebaseDatabase.getInstance().getReference(Common.STATEMENTS_REFERENCE)
                .child(statementId).removeValue().addOnCompleteListener(task -> redirectActivity(StatementDetailsActivity.this,StatementsListActivity.class));
    }

    private void acceptStatement(String statementId, String userId) {
        FirebaseDatabase.getInstance().getReference(Common.ROLES_REFERENCE)
                .orderByChild("_name")
                .equalTo(Roles.Driver.toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for(DataSnapshot childSnapshot : snapshot.getChildren()){
                        Role role = childSnapshot.getValue(Role.class);
                        changeUserRole(userId, role.get_uid());
                        FirebaseDatabase.getInstance().getReference(Common.STATEMENTS_REFERENCE).child(statementId).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void changeUserRole(String userId, String roleId) {

        FirebaseDatabase.getInstance().getReference(Common.USERS_REFERENCE)
                .child(userId)
                .child("_roleId")
                .setValue(roleId).addOnCompleteListener(task -> {
                   redirectActivity(StatementDetailsActivity.this,StatementsListActivity.class);
                });
    }

    private void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity,secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}