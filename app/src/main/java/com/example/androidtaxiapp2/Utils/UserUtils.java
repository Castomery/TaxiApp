package com.example.androidtaxiapp2.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.androidtaxiapp2.Activities.Driver.DriverOrderDetailsActivity;
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
    private static final String YOUR_JWT_TOKEN = "ff09032a01d693547d532439d67427ad1bc54256";

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

    public static void sendOrderTakenNotification() throws JSONException {
        OkHttpClient client = new OkHttpClient();
        FirebaseAccessToken firebaseAccessToken = new FirebaseAccessToken();
        String token = firebaseAccessToken.getAccessToken();


        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String deviceToken = "cdNAFV1TSxWaLN2RhvHz3_:APA91bE5Ok7r6n6rV8sgYPR0Rx3PJ4Ml9pk9DsaRBd8Po8n-nxE9ObyetrenyrRp5j33ZiirN7Pj6p1BI2HgveWPc18VRXfbZIgiRkLDqW3_NFaBreJ-PSRK_cn98Grw6bOo4yr2VJKW";
        JSONObject payload = new JSONObject();

        JSONObject message = new JSONObject();
        message.put("token", deviceToken);

        JSONObject notification = new JSONObject();
        notification.put("body", "This is an FCM notification message!");
        notification.put("title", "FCM Message");

        message.put("data", notification);
        payload.put("message", message);

        RequestBody body = RequestBody.create(JSON, payload.toString());
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
}
