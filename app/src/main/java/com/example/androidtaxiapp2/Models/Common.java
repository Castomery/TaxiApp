package com.example.androidtaxiapp2.Models;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.androidtaxiapp2.R;
import com.example.androidtaxiapp2.Services.MyFirebaseMessagingService;

public class Common {
    public static final String USERS_REFERENCE = "users";
    public static final String ROLES_REFERENCE = "Roles";
    public static final String ORDERS_REFERENCE = "Orders";
    public static final String OPTIMIZED_ROUTES_REFERENCE = "Optimized Routes";
    public static final String STATEMENTS_REFERENCE = "Statements";
    public static final String TOKEN_REFERENCE = "Tokens";
    public static final String NOTI_TITLE = "title";
    public static final String NOTI_CONTENT = "body";
    public static final String CANCEL_ORDER_TITLE = "Order canceled";
    public static final String ACCEPT_ORDER_TITLE = "Order accept";
    public static final String FINISH_ORDER_TITLE = "Order finished";
    public static final String DRIVER_NAME = "driver";
    public static final String CAR_INFO = "car";

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

    public static void showNotification(Context context, int id, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        if(intent != null) {
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
            String NOTIFICATION_CHANNEL_ID = "android_taxi_app";
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        "AndroidTaxiApp", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("AndroidTaxiApp");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.WHITE);
                notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
                notificationChannel.enableVibration(true);

                notificationManager.createNotificationChannel(notificationChannel);
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
            builder.setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(R.drawable.frontal_taxi_cab_svgrepo_com)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.frontal_taxi_cab_svgrepo_com));
            if(pendingIntent != null){
                builder.setContentIntent(pendingIntent);
            }

            Notification notification = builder.build();
            notificationManager.notify(id,notification);
    }
}
