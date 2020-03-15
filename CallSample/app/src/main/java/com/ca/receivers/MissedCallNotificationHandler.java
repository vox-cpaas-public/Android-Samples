package com.ca.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ca.utils.PreferenceProvider;


public class MissedCallNotificationHandler extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PreferenceProvider preferenceProvider = new PreferenceProvider(context);
        Log.i("Misses", "onReceive: missedCallReceievr called");
        String managecontactnumber = intent.getStringExtra("number");
        String managedirection = intent.getStringExtra("direction");
        String name = intent.getStringExtra("name");
        String id = intent.getStringExtra("id");
        preferenceProvider.setPrefString(managecontactnumber + "MissedData", "");
        Log.i("Misses", "onReceive: " + managecontactnumber + managedirection + name + id);
        Log.i("Misses", "onReceive: "+intent.getAction());

    }
}
