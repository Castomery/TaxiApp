package com.example.androidtaxiapp2.Models;

public class BlockedUserModel {

    private String _uid;
    private String _userId;
    private String _date;

    public BlockedUserModel(){}

    public BlockedUserModel(String _uid, String _userId, String _date) {
        this._uid = _uid;
        this._userId = _userId;
        this._date = _date;
    }

    public String get_uid() {
        return _uid;
    }

    public void set_uid(String _uid) {
        this._uid = _uid;
    }

    public String get_userId() {
        return _userId;
    }

    public void set_userId(String _userId) {
        this._userId = _userId;
    }

    public String get_date() {
        return _date;
    }

    public void set_date(String _date) {
        this._date = _date;
    }
}
