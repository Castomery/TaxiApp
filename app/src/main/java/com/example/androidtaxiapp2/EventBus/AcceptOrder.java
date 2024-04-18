package com.example.androidtaxiapp2.EventBus;

public class AcceptOrder {
    private String driverName;
    private String carInfo;

    public AcceptOrder(String driverName, String carInfo) {
        this.driverName = driverName;
        this.carInfo = carInfo;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getCarInfo() {
        return carInfo;
    }

    public void setCarInfo(String carInfo) {
        this.carInfo = carInfo;
    }
}
