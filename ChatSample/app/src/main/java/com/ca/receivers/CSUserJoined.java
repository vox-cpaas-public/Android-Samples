package com.ca.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import com.ca.chatsample.MainActivity;
import com.ca.utils.utils;
import com.ca.wrapper.CSClient;

/**
 * This BroadCast receiver will fire when new user registered in Serevr
 */
public class CSUserJoined extends BroadcastReceiver {
    CSClient CSClientObj = new CSClient();

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
                    // once received broadcast this class will show notification to user
                    utils.notifycallinprogress(context, "New User Joined", name, "", "", 0);
                } catch (Exception ex) {
                    utils.logStacktrace(ex);
                }
            }
        }).start();
    }
}