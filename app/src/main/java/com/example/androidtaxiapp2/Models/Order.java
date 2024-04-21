package com.example.androidtaxiapp2.Models;

import java.io.Serializable;
import java.util.Arrays;

public class Order implements Serializable {
    private String _uid;
    private String _userid;
    private String _driverid;
    private String _orderStatus;
    private String _orderDate;
    private String _carTypeId;
    private String _route;
    private String _price;
    private String _duration;
    private String _destributionPrice;
    private String _addresses;

    public Order( String uuid,String _userid, String _driverid, String _orderStatus, String _orderDate,String _carType, ShortestRoute _route) {
        this._uid = uuid;
        this._userid = _userid;
        this._driverid = _driverid;
        this._orderStatus = _orderStatus;
        this._orderDate = _orderDate;
        this._carTypeId = _carType;
        this._route = String.join(";",_route.route);
        this._price = String.valueOf(_route.totalPrice);
        this._duration = String.valueOf(_route.duration);
        String temp = Arrays.toString(_route.priceDistribution);
        temp = temp.substring(1,temp.length()-1);
        this._destributionPrice = temp;
        this._addresses = String.join(";",_route.pointsNames);
    }

    public Order(){

    }

    public String get_carTypeId() {
        return _carTypeId;
    }

    public void set_carTypeId(String _carTypeId) {
        this._carTypeId = _carTypeId;
    }

    public String get_uid() {
        return _uid;
    }

    public void set_uid(String _uid) {
        this._uid = _uid;
    }

    public String get_userid() {
        return _userid;
    }

    public void set_userid(String _userid) {
        this._userid = _userid;
    }

    public String get_driverid() {
        return _driverid;
    }

    public void set_driverid(String _driverid) {
        this._driverid = _driverid;
    }

    public String get_orderStatus() {
        return _orderStatus;
    }

    public void set_orderStatus(String _orderStatus) {
        this._orderStatus = _orderStatus;
    }

    public String get_orderDate() {
        return _orderDate;
    }

    public void set_orderDate(String _orderDate) {
        this._orderDate = _orderDate;
    }

    public String get_route() {
        return _route;
    }

    public void set_route(String _route) {
        this._route = _route;
    }

    public String get_price() {
        return _price;
    }

    public void set_price(String _price) {
        this._price = _price;
    }

    public String get_duration() {
        return _duration;
    }

    public void set_duration(String _duration) {
        this._duration = _duration;
    }

    public String get_destributionPrice() {
        return _destributionPrice;
    }

    public void set_destributionPrice(String _destributionPrice) {
        this._destributionPrice = _destributionPrice;
    }

    public String get_addresses() {
        return _addresses;
    }

    public void set_addresses(String _addresses) {
        this._addresses = _addresses;
    }

}
