package com.ca.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import com.ca.callsample.MainActivity;
import com.ca.utils.utils;
import com.ca.wrapper.CSClient;

/**
 * This receiver will receiver if any new user registered with server
 */
public class CSUserJoined extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    MainActivity.context = context.getApplicationContext();
                    String name = intent.getStringExtra("name");
                    String number = intent.getStringExtra("number");
                    if (name.equals("")) {
                        name = number;
                    }
                    // if new user registered with server it will show notification as "New User Joined"
                    utils.notifycallinprogress(context, "New User Joined", name, "", "", 0);
                } catch (Exception ex) {
                    utils.logStacktrace(ex);
                }
            }
        }).start();
    }
}