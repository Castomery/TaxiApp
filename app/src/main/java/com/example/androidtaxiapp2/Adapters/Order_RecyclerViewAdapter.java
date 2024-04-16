package com.example.androidtaxiapp2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidtaxiapp2.Interfaces.RecyclerViewInterface;
import com.example.androidtaxiapp2.Models.Order;
import com.example.androidtaxiapp2.R;

import java.util.List;

public class Order_RecyclerViewAdapter extends RecyclerView.Adapter<Order_RecyclerViewAdapter.MyViewHolder>{

    private final RecyclerViewInterface recyclerViewInterface;
    private Context context;
    private List<Order> orders;

    public Order_RecyclerViewAdapter(Context context, List<Order> ordersList, RecyclerViewInterface recyclerViewInterface){
        this.context = context;
        orders = ordersList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public Order_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row,parent,false);
        return new Order_RecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull Order_RecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.addressesTextView.setText(getStringAddresses(orders.get(position).get_addresses()));
        holder.priceTextView.setText("Price: " + orders.get(position).get_price());
        holder.durationTextView.setText("Duration: " + orders.get(position).get_duration());
        holder.orderStatusTextView.setText("Status: " + orders.get(position).get_orderStatus());
    }

    private String getStringAddresses(String addresses) {
        String[] adr = addresses.split(";");
        return String.join("\n",adr);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView addressesTextView;
        private TextView priceTextView;
        private TextView durationTextView;
        private TextView orderStatusTextView;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            addressesTextView= itemView.findViewById(R.id.addressTextView);
            priceTextView = itemView.findViewById(R.id.orderPriceTextView);
            durationTextView = itemView.findViewById(R.id.orderDurationTextView);
            orderStatusTextView = itemView.findViewById(R.id.orderStatusTextView);

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
