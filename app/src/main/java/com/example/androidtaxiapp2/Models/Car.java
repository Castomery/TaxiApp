package com.example.androidtaxiapp2.Models;

public class Car {

    private String _uid;
    private String _driverId;
    private String _carModel;
    private String _carTypeId;
    private String _carPlate;

    public Car(){}

    public Car(String _uid, String _driverId, String _carModel, String _carTypeId, String _carPlate) {
        this._uid = _uid;
        this._driverId = _driverId;
        this._carModel = _carModel;
        this._carTypeId = _carTypeId;
        this._carPlate = _carPlate;
    }

    public String get_uid() {
        return _uid;
    }

    public void set_uid(String _uid) {
        this._uid = _uid;
    }

    public String get_driverId() {
        return _driverId;
    }

    public void set_driverId(String _driverId) {
        this._driverId = _driverId;
    }

    public String get_carModel() {
        return _carModel;
    }

    public void set_carModel(String _carModel) {
        this._carModel = _carModel;
    }

    public String get_carTypeId() {
        return _carTypeId;
    }

    public void set_carTypeId(String _carTypeId) {
        this._carTypeId = _carTypeId;
    }

    public String get_carPlate() {
        return _carPlate;
    }

    public void set_carPlate(String _carPlate) {
        this._carPlate = _carPlate;
    }
}
