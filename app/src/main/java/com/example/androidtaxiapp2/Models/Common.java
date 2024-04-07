package com.example.androidtaxiapp2.Models;

public class Common {
    public static final String USERS_REFERENCE = "users";
    public static final String ROLES_REFERENCE = "Roles";
    public static final String OPTIMIZED_ROUTES_REFERENCE = "Optimized Routes";

    public static User currentUser;

    public static String buildWelcomeMessage() {
        if (Common.currentUser != null){
            return new StringBuilder("Welcome ")
                    .append(Common.currentUser.get_name())
                    .append(" ")
                    .append(Common.currentUser.get_lastname()).toString();
        }
        else{
            return "";
        }
    }
}
