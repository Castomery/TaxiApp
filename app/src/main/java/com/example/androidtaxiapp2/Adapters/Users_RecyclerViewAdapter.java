package com.example.androidtaxiapp2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidtaxiapp2.Models.BlockedUserModel;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Role;
import com.example.androidtaxiapp2.Models.User;
import com.example.androidtaxiapp2.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class Users_RecyclerViewAdapter extends RecyclerView.Adapter<Users_RecyclerViewAdapter.UsersViewHolder>{

    private Context context;
    private List<User> users;

    public Users_RecyclerViewAdapter(Context context, List<User> users){
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public Users_RecyclerViewAdapter.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.users_recycler_view_row,parent,false);
        return new Users_RecyclerViewAdapter.UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Users_RecyclerViewAdapter.UsersViewHolder holder, int position) {
        holder.userFullname.setText(users.get(position).get_name()+" "+users.get(position).get_lastname());
        holder.userRegisterDate.setText(users.get(position).get_registrationDate());
        FirebaseDatabase.getInstance().getReference(Common.ROLES_REFERENCE).child(users.get(position).get_roleId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Role role = snapshot.getValue(Role.class);

                    holder.userRoleName.setText(role.get_name());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.blockBtn.setOnClickListener(v -> checkIfUserAlreadyBlocked(users.get(position).get_uid()));
    }

    private void checkIfUserAlreadyBlocked(String userId) {

        FirebaseDatabase.getInstance().getReference(Common.BLOCKED_USERS)
                .orderByChild("_userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Toast.makeText(context.getApplicationContext(), "User Already Blocked", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            blockUser(userId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser(String userid) {
        String uid = UUID.randomUUID().toString();
        BlockedUserModel blockedUser = new BlockedUserModel(uid,userid, Calendar.getInstance().getTime().toString());

        FirebaseDatabase.getInstance().getReference(Common.BLOCKED_USERS)
                .child(uid).setValue(blockedUser);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        private TextView userFullname;
        private TextView userRoleName;
        private TextView userRegisterDate;
        private Button blockBtn;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            userFullname = itemView.findViewById(R.id.userList_fullname_textView);
            userRoleName =  itemView.findViewById(R.id.userList_role);
            userRegisterDate = itemView.findViewById(R.id.userList_registartionDate);
            blockBtn = itemView.findViewById(R.id.userList_blockBtn);
        }
    }
}
