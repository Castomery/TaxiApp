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

import com.example.androidtaxiapp2.Enums.OrderStatus;
import com.example.androidtaxiapp2.Enums.Roles;
import com.example.androidtaxiapp2.Models.BlockedUserModel;
import com.example.androidtaxiapp2.Models.Car;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.Order;
import com.example.androidtaxiapp2.Models.Role;
import com.example.androidtaxiapp2.Models.User;
import com.example.androidtaxiapp2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        holder.blockBtn.setOnClickListener(v -> checkIfUserAlreadyBlocked(users.get(position),position));
    }

    private void checkIfUserAlreadyBlocked(User user, int position) {

        FirebaseDatabase.getInstance().getReference(Common.BLOCKED_USERS)
                .orderByChild("_userId").equalTo(user.get_uid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Toast.makeText(context.getApplicationContext(), "User Already Blocked", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            checkUserRole(user.get_roleId(),user.get_uid(), position);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIfClientHasActiveOrder(String userId, int position) {
        FirebaseDatabase.getInstance().getReference(Common.ORDERS_REFERENCE).orderByChild("_userid").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasActiveOrder = false;
                if(snapshot.exists()){
                    for(DataSnapshot child : snapshot.getChildren()){
                        Order order = child.getValue(Order.class);
                        if (order.get_orderStatus().equals(OrderStatus.LookingForDriver.toString()) || order.get_orderStatus().equals(OrderStatus.WaitingForDriver.toString()) || order.get_orderStatus().equals(OrderStatus.InProgress.toString())){
                            Toast.makeText(context.getApplicationContext(), "User has active order", Toast.LENGTH_SHORT).show();
                            hasActiveOrder = true;
                            return;
                        }
                    }
                    if (!hasActiveOrder){
                        blockUser(userId);
                        users.remove(position);
                        notifyItemRemoved(position);
                    }
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
                .child(uid).setValue(blockedUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    private void checkUserRole(String userRole, String userId, int position) {
        FirebaseDatabase.getInstance().getReference(Common.ROLES_REFERENCE).child(userRole).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Role role = snapshot.getValue(Role.class);
                    if (role.get_name().equals(Roles.Driver.toString())){
                        checkIfDriverHasActiveOrder(userId, position);
                    }
                    else if (role.get_name().equals(Roles.Client.toString())){
                        checkIfClientHasActiveOrder(userId,position);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkIfDriverHasActiveOrder(String userId, int position) {
    FirebaseDatabase.getInstance().getReference(Common.ORDERS_REFERENCE).orderByChild("_driverid").addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            boolean hasActiveOrder = false;
            if(snapshot.exists()){
                for(DataSnapshot child : snapshot.getChildren()){
                    Order order = child.getValue(Order.class);
                    if (order.get_orderStatus().equals(OrderStatus.WaitingForDriver.toString()) || order.get_orderStatus().equals(OrderStatus.InProgress.toString())){
                        Toast.makeText(context.getApplicationContext(), "User has active order", Toast.LENGTH_SHORT).show();
                        hasActiveOrder = true;
                        return;
                    }
                }
                if (!hasActiveOrder){
                    blockUser(userId);
                    removeDriverFromCar(userId);
                    users.remove(position);
                    notifyItemRemoved(position);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });

    }

    private void removeDriverFromCar(String userId) {
        FirebaseDatabase.getInstance().getReference(Common.CARS_REFERENCE)
                .orderByChild("_driverId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot child : snapshot.getChildren()){
                                Car car = child.getValue(Car.class);
                                car.set_driverId("");
                                FirebaseDatabase.getInstance().getReference(Common.CARS_REFERENCE).child(car.get_uid()).setValue(car);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
