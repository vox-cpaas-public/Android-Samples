package com.ca.receivers;


import android.util.Log;

import com.ca.wrapper.CSClient;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FireBaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static int count = 0;
    CSClient csClient = new CSClient();

    @Override
    public void onCreate() {
        super.onCreate();
        // register and un register receiver
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        try {
            Log.e(TAG, "Message Type: " + remoteMessage);
            //Log.e(TAG, "Notification Object:" + remoteMessage.getNotification());


            csClient.processPushMessage(getApplicationContext(), remoteMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onNewToken(String mToken) {
        super.onNewToken(mToken);
        Log.i("FireBaseInstance", "Refreshed Token: " + mToken);
        csClient.processInstanceID(getApplicationContext(),mToken);
    }
}


