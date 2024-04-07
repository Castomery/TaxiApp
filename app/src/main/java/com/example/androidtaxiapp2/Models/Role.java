package com.example.androidtaxiapp2.Models;

public class Role {
    private String _uid;
    private String _name;

    public Role(String _uid, String _name) {
        this._uid = _uid;
        this._name = _name;
    }

    public Role() {
    }

    public String get_uid() {
        return _uid;
    }

    public String get_name() {
        return _name;
    }

    public void set_uid(String _uid) {
        this._uid = _uid;
    }

    public void set_name(String _name) {
        this._name = _name;
    }
}
