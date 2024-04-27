package com.example.androidtaxiapp2.Activities.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidtaxiapp2.Activities.Admin.AdminHomeActivity;
import com.example.androidtaxiapp2.Activities.Driver.DriverHomeActivity;
import com.example.androidtaxiapp2.Activities.SplashScreenActivity;
import com.example.androidtaxiapp2.Activities.Client.UserHomeActivity;
import com.example.androidtaxiapp2.Enums.Roles;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Role;
import com.example.androidtaxiapp2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

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
        _changePassButton = findViewById(R.id.change_password_btn);
        _deleteButton = findViewById(R.id.delete_account_btn);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Common.USERS_REFERENCE).child(Common.currentUser.get_uid());

        _updatedImg.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,PICK_IMAGE_REQUEST);
        });

        displayValues();

        _backButton.setOnClickListener(v -> {
            goToHomeActivity();
        });

        _changePassButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        _changeDataButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, ChangeUserInfoActivity.class);
            startActivity(intent);
        });

        _deleteButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this, R.style.AlertDialogDark);
            builder.setTitle("Are you sure?")
                    .setMessage("Deleting this account will result in completely removing your account from the system and you won`t be able to access the app.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.delete().addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                reference.removeValue();
                                Common.currentUser = null;
                                Intent intent = new Intent(UserProfileActivity.this, SplashScreenActivity.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(UserProfileActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setCancelable(false);
            AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(dialog -> {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getResources().getColor(R.color.colorAccent));
            });

            alertDialog.show();
        });

        _signOutButton.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this, R.style.AlertDialogDark);
            builder.setTitle("Sign out")
                    .setMessage("Do you really want to sign out?")
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Sign out", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Common.currentUser = null;
                        Intent intent = new Intent(UserProfileActivity.this, SplashScreenActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
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
        });

    }

    private void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity,secondActivity);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    private void goToHomeActivity(){
        database.getReference(Common.ROLES_REFERENCE).child(Common.currentUser.get_roleId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Role role = snapshot.getValue(Role.class);
                            if (role.get_name().equals(Roles.Client.toString())){
                                redirectActivity(UserProfileActivity.this, UserHomeActivity.class);
                            }
                            else if(role.get_name().equals(Roles.Driver.toString())){
                                redirectActivity(UserProfileActivity.this, DriverHomeActivity.class);
                            }
                            else if (role.get_name().equals(Roles.Admin.toString())){
                                redirectActivity(UserProfileActivity.this, AdminHomeActivity.class);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
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