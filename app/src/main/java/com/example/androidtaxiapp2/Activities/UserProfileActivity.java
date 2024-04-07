package com.example.androidtaxiapp2.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Role;
import com.example.androidtaxiapp2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 7172;
    private TextView _fullnameField;
    private TextView _phoneField;
    private Button _changeDataButton;
    private Button _changePassButton;
    private Button _deleteButton;
    private Button _signOutButton;
    private Button _backButton;

    private ImageView _updatedImg;
    private AlertDialog waitingDialog;
    private StorageReference storageReference;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);


        _fullnameField = findViewById(R.id.textView_show_fullname);
        _phoneField = findViewById(R.id.textView_show_phone);
        _updatedImg = findViewById(R.id.upd_imageView);
        _changeDataButton = findViewById(R.id.change_data_btn);
        _signOutButton = findViewById(R.id.sign_out_btn);
        _backButton = findViewById(R.id.btn_back);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.USERS_REFERENCE).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        _updatedImg.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,PICK_IMAGE_REQUEST);
        });

        displayValues();

        _backButton.setOnClickListener(v -> {
            database.getReference(Common.ROLES_REFERENCE).child(Common.currentUser.get_roleId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                Role role = snapshot.getValue(Role.class);
                                if (role.get_name().equals("Client")){
                                    startActivity(new Intent(UserProfileActivity.this, UserHomeActivity.class));
                                    finish();
                                }
                                else if(role.get_name().equals("Driver")){
                                    startActivity(new Intent(UserProfileActivity.this, DriverHomeActivity.class));
                                    finish();
                                }
                                else{
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        });

        _changeDataButton.setOnClickListener(v -> {
            
//            String name = _updatedName.getText().toString();
//            String lastname = _updatedLastname.getText().toString();
//            String phone = _updatedPhone.getText().toString();
//
//            String mobileRegex = "\\+?3?8?0\\d{9}";
//            Matcher mobileMatcher;
//            Pattern mobilePattern = Pattern.compile(mobileRegex);
//            mobileMatcher = mobilePattern.matcher(phone);
//
//            if (name.isEmpty() || lastname.isEmpty() || phone.isEmpty()){
//                Toast.makeText(UserProfileActivity.this, "Fill all fields", Toast.LENGTH_SHORT).show();
//            }
//            else if(!mobileMatcher.find()){
//                _updatedPhone.setError("Invalid phone");
//                _updatedPhone.requestFocus();
//            }
//            else {
//                setValueAndUpdateDB(name,lastname,phone);
//            }
        });

        _signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                builder.setTitle("Sign out")
                        .setMessage("Do you really want to sign out?")
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Sign out", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(UserProfileActivity.this, SplashScreenActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setCancelable(false);
                AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(dialog -> {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(getResources().getColor(R.color.colorAccent));
                });

                alertDialog.show();
            }
        });

    }

    private void setValueAndUpdateDB(String name, String lastname, String phone) {
        reference.child("_name").setValue(name);
        reference.child("_lastname").setValue(lastname);
        reference.child("_phone").setValue(phone);

        Common.currentUser.set_name(name);
        Common.currentUser.set_lastname(lastname);
        Common.currentUser.set_phone(phone);
    }

    private void displayValues(){
        _fullnameField.setText(Common.currentUser.get_name() + " " + Common.currentUser.get_lastname());
        _phoneField.setText(Common.currentUser.get_phone());
        if (!TextUtils.isEmpty(Common.currentUser.get_urlImage())){
            _updatedImg.setImageURI(Uri.parse(Common.currentUser.get_urlImage()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            if(data != null && data.getData() != null){
                imageUri = data.getData();
                _updatedImg.setImageURI(imageUri);
                showDialogUpload();
            }
        }
    }

    private void showDialogUpload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Change avatar")
                .setMessage("Do you really want to change avatar?")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Upload", (dialog, which) -> {
                    if (imageUri != null){
                        waitingDialog.setMessage("Uploading...");
                        waitingDialog.show();

                        String uid = Common.currentUser.get_uid();
                        StorageReference avatarFolder = storageReference.child("avatars/"+uid);

                        avatarFolder.putFile(imageUri).addOnFailureListener(e -> waitingDialog.dismiss()).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                avatarFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                    reference.child("_urlImage").setValue(uri.toString());
                                    Common.currentUser.set_urlImage(uri.toString());
                                });
                            }
                        }).addOnProgressListener(snapshot -> {
                            double progress = (100.0 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                            waitingDialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                        });
                    }
                })
                .setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialog -> {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(getResources().getColor(R.color.colorAccent));
        });

        alertDialog.show();
    }
}