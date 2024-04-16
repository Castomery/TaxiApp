package com.example.androidtaxiapp2.Models;

public class Statement {
    private String _userid;
    private String _statementText;
    private String _statementDate;

    public Statement( String userId, String statementText, String date){
        _userid = userId;
        _statementText = statementText;
        _statementDate = date;
    }

    public Statement(){}

    public String get_userid() {
        return _userid;
    }

    public void set_userid(String _userid) {
        this._userid = _userid;
    }

    public String get_statementText() {
        return _statementText;
    }

    public void set_statementText(String _statementText) {
        this._statementText = _statementText;
    }

    public String get_statementDate() {
        return _statementDate;
    }

    public void set_statementDate(String _statementDate) {
        this._statementDate = _statementDate;
    }
}
