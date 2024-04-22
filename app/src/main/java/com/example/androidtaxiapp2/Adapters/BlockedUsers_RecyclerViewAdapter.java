package com.example.androidtaxiapp2.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidtaxiapp2.Activities.Admin.BlockedUsersListActivity;
import com.example.androidtaxiapp2.Models.BlockedUserModel;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BlockedUsers_RecyclerViewAdapter extends RecyclerView.Adapter<BlockedUsers_RecyclerViewAdapter.BlockedUsersViewHolder>{

    private Context context;
    private List<BlockedUserModel> blockedUserModelList;

    public BlockedUsers_RecyclerViewAdapter(Context context, List<BlockedUserModel> blockedUserModelList) {
        this.context = context;
        this.blockedUserModelList = blockedUserModelList;
    }

    @NonNull
    @Override
    public BlockedUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.blockeduser_recyclerview_row,parent,false);
        return new BlockedUsers_RecyclerViewAdapter.BlockedUsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockedUsersViewHolder holder, int position) {

        holder.userId.setText(blockedUserModelList.get(position).get_userId());
        holder.blockDate.setText(blockedUserModelList.get(position).get_date());
        holder.unBlockBtn.setOnClickListener(v -> unBlockUser(blockedUserModelList.get(position).get_uid(), position));
    }

    private void unBlockUser(String blockedId, int position) {
        FirebaseDatabase.getInstance().getReference(Common.BLOCKED_USERS)
                .child(blockedId).removeValue();
        blockedUserModelList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return blockedUserModelList.size();
    }

    public static class BlockedUsersViewHolder extends RecyclerView.ViewHolder{

        private TextView userId;
        private TextView blockDate;
        private Button unBlockBtn;
        public BlockedUsersViewHolder(@NonNull View itemView) {
            super(itemView);

            userId = itemView.findViewById(R.id.blocked_userId_txt);
            blockDate = itemView.findViewById(R.id.blocked_date_txt);
            unBlockBtn = itemView.findViewById(R.id.unblock_user_btn);
        }
    }
}
