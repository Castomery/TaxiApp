package com.example.androidtaxiapp2.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.androidtaxiapp2.Models.Common;
import com.example.androidtaxiapp2.Models.TokenModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserUtils {

    private static final String FCM_ENDPOINT = "https://fcm.googleapis.com/v1/projects/androidtaxiapp-3a893/messages:send";

    public static void updateToken(Context context, String token){
        TokenModel tokenModel = new TokenModel(token);

        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(Common.currentUser.get_uid())
                .setValue(tokenModel)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });


    }

    private static void sendNotification(String notificationBody){
        OkHttpClient client = new OkHttpClient();
        FirebaseAccessToken firebaseAccessToken = new FirebaseAccessToken();
        String token = firebaseAccessToken.getAccessToken();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, notificationBody);

        Request request = new Request.Builder()
                .url(FCM_ENDPOINT)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                // Handle the response from FCM
                Log.d("NOTIFICATION RESPONS",responseData);
            }
        });
    }

    public static void sendOrderTakenNotification(String toUserToken, String carModel, String carPlate) throws JSONException {

        JSONObject payload = new JSONObject();

        JSONObject message = new JSONObject();
        message.put("token", toUserToken);

        JSONObject notification = new JSONObject();
        notification.put("body", "Your order has been accepted");
        notification.put("driver", Common.currentUser.get_name() + " " + Common.currentUser.get_lastname());
        notification.put("car", carModel + " " + carPlate);
        notification.put("title", Common.ACCEPT_ORDER_TITLE);

        message.put("data", notification);
        payload.put("message", message);

        sendNotification(payload.toString());
    }

    public static void sendOrderCanceledNotification(String driverToken) throws JSONException {
//        OkHttpClient client = new OkHttpClient();
//        FirebaseAccessToken firebaseAccessToken = new FirebaseAccessToken();
//        String  token = firebaseAccessToken.getAccessToken();


        //MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject payload = new JSONObject();

        JSONObject message = new JSONObject();
        message.put("token", driverToken);

        JSONObject notification = new JSONObject();
        notification.put("body", "Order has been canceled");
        notification.put("title", Common.CANCEL_ORDER_TITLE);

        message.put("data", notification);
        payload.put("message", message);

        sendNotification(payload.toString());

//        RequestBody body = RequestBody.create(JSON, payload.toString());
//        Request request = new Request.Builder()
//                .url(FCM_ENDPOINT)
//                .addHeader("Authorization", "Bearer " + token)
//                .addHeader("Content-Type", "application/json")
//                .post(body)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String responseData = response.body().string();
//                // Handle the response from FCM
//                Log.d("NOTIFICATION RESPONS",responseData);
//            }
//        });

    }

    public static void sendOrderFinishedNotification(String token) throws JSONException {
        JSONObject payload = new JSONObject();

        JSONObject message = new JSONObject();
        message.put("token", token);

        JSONObject notification = new JSONObject();
        notification.put("body", "Order has been finished");
        notification.put("title", Common.FINISH_ORDER_TITLE);

        message.put("data", notification);
        payload.put("message", message);

        sendNotification(payload.toString());
    }
}
