package com.example.androidtaxiapp2.Activities.Client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Statement;
import com.example.androidtaxiapp2.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class StatementActivity extends AppCompatActivity {

    private EditText statementText;
    private Button applyBtn;
    private FirebaseDatabase database;
    private Button buttonBack;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statement);

        statementText = findViewById(R.id.statement_text);
        applyBtn = findViewById(R.id.create_statement_btn);
        buttonBack = findViewById(R.id.statement_btn_back);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.STATEMENTS_REFERENCE);

        buttonBack.setOnClickListener(v -> goToHome());

        applyBtn.setOnClickListener(v -> {
            generateStatement();
        });
    }

    private void generateStatement() {
        reference.orderByChild("_userid").equalTo(Common.currentUser.get_uid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    String statementStr = statementText.getText().toString();
                    Statement statement = new Statement(Common.currentUser.get_uid(), statementStr, Calendar.getInstance().getTime().toString());
                    reference.push().setValue(statement).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(StatementActivity.this,"Statement created", Toast.LENGTH_SHORT).show();
                        }
                    });
                    goToHome();
                }
                else{
                    Toast.makeText(StatementActivity.this,"Statement already exits", Toast.LENGTH_SHORT).show();
                    goToHome();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void goToHome(){
        Intent intent = new Intent(StatementActivity.this, UserHomeActivity.class);
        startActivity(intent);
        finish();
    }
}