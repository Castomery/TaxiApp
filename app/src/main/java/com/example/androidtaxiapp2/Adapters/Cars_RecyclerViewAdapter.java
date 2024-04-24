package com.example.androidtaxiapp2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidtaxiapp2.Interfaces.RecyclerViewInterface;
import com.example.androidtaxiapp2.Models.Car;
import com.example.androidtaxiapp2.Models.CarTypes;
import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class Cars_RecyclerViewAdapter extends RecyclerView.Adapter<Cars_RecyclerViewAdapter.CarsViewHolder> {

    private Context context;
    private List<Car> carList;
    private RecyclerViewInterface recyclerViewInterface;

    public Cars_RecyclerViewAdapter(Context context, List<Car> carList, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.carList = carList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public Cars_RecyclerViewAdapter.CarsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.cars_recyclerview_row,parent,false);
        return new Cars_RecyclerViewAdapter.CarsViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull CarsViewHolder holder, int position) {

        holder.carModel.setText(carList.get(position).get_carModel());
        holder.carPlate.setText(carList.get(position).get_carPlate());

        FirebaseDatabase.getInstance().getReference(Common.CAR_TYPES_REFERENCE).child(carList.get(position).get_carTypeId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    CarTypes carType = snapshot.getValue(CarTypes.class);
                    holder.carType.setText(carType.get_name());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public static class CarsViewHolder extends RecyclerView.ViewHolder {

        private TextView carModel;
        private TextView carType;
        private TextView carPlate;
        public CarsViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            carModel = itemView.findViewById(R.id.carList_carModel_txt);
            carType = itemView.findViewById(R.id.carList_type);
            carPlate = itemView.findViewById(R.id.carList_plate_txt);

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
