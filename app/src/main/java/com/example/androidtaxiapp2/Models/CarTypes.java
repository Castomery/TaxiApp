package com.example.androidtaxiapp2.Models;

import java.io.Serializable;

public class CarTypes implements Serializable {
    private String _uid;
    private String _name;
    private double _priceForCar;
    private double _pricePerKm;

    public CarTypes(String _uid, String _name, double _priceForCar, double _pricePerKm) {
        this._uid = _uid;
        this._name = _name;
        this._priceForCar = _priceForCar;
        this._pricePerKm = _pricePerKm;
    }

    public CarTypes(){}

    public String get_uid() {
        return _uid;
    }

    public void set_uid(String _uid) {
        this._uid = _uid;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public double get_priceForCar() {
        return _priceForCar;
    }

    public void set_priceForCar(double _priceForCar) {
        this._priceForCar = _priceForCar;
    }

    public double get_pricePerKm() {
        return _pricePerKm;
    }

    public void set_pricePerKm(double _pricePerKm) {
        this._pricePerKm = _pricePerKm;
    }
}
