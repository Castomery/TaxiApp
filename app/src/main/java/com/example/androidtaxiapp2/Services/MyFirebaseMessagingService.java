package com.example.androidtaxiapp2.Services;

import androidx.annotation.NonNull;

import com.example.androidtaxiapp2.EventBus.AcceptOrder;
import com.example.androidtaxiapp2.EventBus.DeclineOrder;
import com.example.androidtaxiapp2.EventBus.FinishOrder;
import com.example.androidtaxiapp2.Utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.androidtaxiapp2.Models.Common;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            UserUtils.updateToken(this, token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Map<String,String> dataRecv = message.getData();

        if (dataRecv != null){

            if (dataRecv.get(Common.NOTI_TITLE).equals(Common.CANCEL_ORDER_TITLE)){
                EventBus.getDefault().postSticky(new DeclineOrder());
                Common.showNotification(this, new Random().nextInt(),
                        dataRecv.get(Common.NOTI_TITLE),
                        dataRecv.get(Common.NOTI_CONTENT),
                        null);
            }
            else if(dataRecv.get(Common.NOTI_TITLE).equals(Common.ACCEPT_ORDER_TITLE)){
                EventBus.getDefault().postSticky(new AcceptOrder(dataRecv.get(Common.DRIVER_NAME),dataRecv.get(Common.CAR_INFO)));
            }
            else if(dataRecv.get(Common.NOTI_TITLE).equals(Common.FINISH_ORDER_TITLE)){
                EventBus.getDefault().postSticky(new FinishOrder());
                Common.showNotification(this, new Random().nextInt(),
                        dataRecv.get(Common.NOTI_TITLE),
                        dataRecv.get(Common.NOTI_CONTENT),
                        null);
            }
            else{
                Common.showNotification(this, new Random().nextInt(),
                        dataRecv.get(Common.NOTI_TITLE),
                        dataRecv.get(Common.NOTI_CONTENT),
                        null);
            }
        }
    }
}
