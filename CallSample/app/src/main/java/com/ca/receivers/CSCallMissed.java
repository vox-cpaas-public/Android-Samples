package com.ca.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.text.format.DateUtils;

import com.ca.callsample.MainActivity;
import com.ca.utils.utils;
import com.ca.wrapper.CSClient;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This broadCast receiver will receive all missed calls
 */
public class CSCallMissed extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    MainActivity.context = context.getApplicationContext();
                    long time = intent.getLongExtra("time", new Date().getTime());
                    String calltime = "";
                    if (DateUtils.isToday(time)) {
                        calltime = "Today " + new SimpleDateFormat("hh:mm a").format(time);
                    } else if (utils.isYesterday(time)) {
                        calltime = "Yesterday " + new SimpleDateFormat("hh:mm a").format(time);
                    } else {
                        calltime = new SimpleDateFormat("dd-MM-yyyy hh:mm a").format(time);
                    }
                    String calldirection = intent.getStringExtra("direction");
                    // once receive missed call event from server it will show in notification as missed call
                    utils.notifyCallMissed(context, intent.getStringExtra("number"), intent.getStringExtra("name"), intent.getStringExtra("callid"), calldirection, calltime, 0);
                } catch (Exception ex) {
                    utils.logStacktrace(ex);
                }
            }
        }).start();
    }
}