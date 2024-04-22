package com.example.androidtaxiapp2.Activities.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.androidtaxiapp2.Activities.Client.ClientOrderDetailsActivity;
import com.example.androidtaxiapp2.Activities.OrderHistoryActivity;
import com.example.androidtaxiapp2.Adapters.Statement_RecyclerViewAdapter;
import com.example.androidtaxiapp2.Interfaces.RecyclerViewInterface;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Statement;
import com.example.androidtaxiapp2.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StatementsListActivity extends AppCompatActivity implements RecyclerViewInterface {

    private List<Statement> statementList = new ArrayList<>();
    private Button backBtn;
    private RecyclerView statementRecyclerview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statements_list);
        backBtn = findViewById(R.id.statements_list_btn_back);

        statementRecyclerview = findViewById(R.id.statements_recyclerView);

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(StatementsListActivity.this, AdminHomeActivity.class);
            startActivity(intent);
            finish();
        });

        getStatements(this);
    }

    private void getStatements(RecyclerViewInterface recyclerViewInterface){
        FirebaseDatabase.getInstance().getReference(Common.STATEMENTS_REFERENCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for(DataSnapshot childSnap : snapshot.getChildren()){
                                Statement statement = childSnap.getValue(Statement.class);
                                statementList.add(statement);
                            }

                            Statement_RecyclerViewAdapter adapter = new Statement_RecyclerViewAdapter(getBaseContext(), statementList, recyclerViewInterface);
                            statementRecyclerview.setAdapter(adapter);
                            statementRecyclerview.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(StatementsListActivity.this, StatementDetailsActivity.class);
        intent.putExtra("statement", statementList.get(position));
        startActivity(intent);
        finish();
    }
}