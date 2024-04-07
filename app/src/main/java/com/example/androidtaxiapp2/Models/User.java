package com.example.androidtaxiapp2.Models;

public class User {

    private String _uid;
    private String _name;
    private String _lastname;
    private String _email;
    private String _password;
    private String _urlImage;
    private String _phone;
    private String _roleId;
    private String _registrationDate;

    public User(String _uid, String _name, String _lastname, String _email, String _password, String _urlImage, String _phone, String _roleId, String _registrationDate) {
        this._uid = _uid;
        this._name = _name;
        this._lastname = _lastname;
        this._email = _email;
        this._password = _password;
        this._urlImage = _urlImage;
        this._phone = _phone;
        this._roleId = _roleId;
        this._registrationDate = _registrationDate;
    }

    public User(){
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public void set_lastname(String _lastname) {
        this._lastname = _lastname;
    }

    public void set_email(String _email) {
        this._email = _email;
    }

    public void set_password(String _password) {
        this._password = _password;
    }

    public void set_phone(String _phone) {
        this._phone = _phone;
    }
    public void set_urlImage(String _imgUri){this._urlImage = _imgUri;}

    public void set_roleId(String _roleId) {
        this._roleId = _roleId;
    }

    public String get_uid() {
        return _uid;
    }

    public String get_name() {
        return _name;
    }

    public String get_lastname() {
        return _lastname;
    }

    public String get_email() {
        return _email;
    }

    public String get_password() {
        return _password;
    }

    public String get_urlImage() {
        return _urlImage;
    }

    public String get_phone() {
        return _phone;
    }

    public String get_roleId() {
        return _roleId;
    }

    public String get_registrationDate() {
        return _registrationDate;
    }

    @Override
    public String toString() {
        return "User{" +
                "_uid='" + _uid + '\'' +
                ", _name='" + _name + '\'' +
                ", _lastname='" + _lastname + '\'' +
                ", _email='" + _email + '\'' +
                ", _password='" + _password + '\'' +
                ", _urlImage='" + _urlImage + '\'' +
                ", _phone='" + _phone + '\'' +
                ", _roleId='" + _roleId + '\'' +
                ", _registrationDate='" + _registrationDate + '\'' +
                '}';
    }
}
