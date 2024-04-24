package com.example.androidtaxiapp2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidtaxiapp2.Interfaces.RecyclerViewInterface;
import com.example.androidtaxiapp2.Models.CarTypes;
import com.example.androidtaxiapp2.R;

import java.util.List;

public class CarTypes_RecyclerViewAdapter extends RecyclerView.Adapter<CarTypes_RecyclerViewAdapter.CarTypesViewHolder>{

    private Context context;
    private List<CarTypes> carTypesList;
    private RecyclerViewInterface recyclerViewInterface;

    public CarTypes_RecyclerViewAdapter(Context context, List<CarTypes> carTypesList, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.carTypesList = carTypesList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public CarTypesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.prices_recyclerview_row,parent,false);
        return new CarTypes_RecyclerViewAdapter.CarTypesViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull CarTypesViewHolder holder, int position) {
        holder.typeName.setText(carTypesList.get(position).get_name());
        holder.priceForCar.setText(String.valueOf(carTypesList.get(position).get_priceForCar()));
        holder.pricePerKm.setText(String.valueOf(carTypesList.get(position).get_pricePerKm()));
    }

    @Override
    public int getItemCount() {
        return carTypesList.size();
    }

    public static class CarTypesViewHolder extends RecyclerView.ViewHolder {

        private TextView typeName;
        private TextView priceForCar;
        private TextView pricePerKm;
        public CarTypesViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            typeName = itemView.findViewById(R.id.type_name_txt);
            priceForCar = itemView.findViewById(R.id.price_for_car_txt);
            pricePerKm = itemView.findViewById(R.id.price_per_km_txt);

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
