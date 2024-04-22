package com.example.androidtaxiapp2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidtaxiapp2.Interfaces.RecyclerViewInterface;
import com.example.androidtaxiapp2.Models.Statement;
import com.example.androidtaxiapp2.R;

import java.util.List;

public class Statement_RecyclerViewAdapter extends RecyclerView.Adapter<Statement_RecyclerViewAdapter.StatementViewHolder>{

    private Context context;
    private List<Statement> statementList;

    private RecyclerViewInterface recyclerViewInterface;

    public Statement_RecyclerViewAdapter(Context context, List<Statement> statementList, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.statementList = statementList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public Statement_RecyclerViewAdapter.StatementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.statements_recyclerview_row,parent,false);

        return new Statement_RecyclerViewAdapter.StatementViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull Statement_RecyclerViewAdapter.StatementViewHolder holder, int position) {
        holder.userIdTextView.setText(statementList.get(position).get_userid());
        holder.statementDateTextView.setText(statementList.get(position).get_statementDate());
    }

    @Override
    public int getItemCount() {
        return statementList.size();
    }

    public static class StatementViewHolder extends RecyclerView.ViewHolder{

        private TextView userIdTextView;
        private TextView statementDateTextView;
        public StatementViewHolder(@NonNull View itemView,RecyclerViewInterface recyclerViewInterface) {

            super(itemView);

            userIdTextView = itemView.findViewById(R.id.userId_txt);
            statementDateTextView = itemView.findViewById(R.id.statement_date);

            itemView.setOnClickListener(v -> {
                if(recyclerViewInterface != null){
                    int pos = getAdapterPosition();

                    if (pos != RecyclerView.NO_POSITION){
                        recyclerViewInterface.onItemClick(pos);
                    }
                }
            });
        }
    }
}
