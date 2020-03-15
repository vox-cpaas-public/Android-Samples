package com.ca.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ca.callsample.MainActivity;
import com.ca.utils.Constants;
import com.ca.utils.utils;
import com.ca.wrapper.CSCall;
import com.ca.wrapper.CSClient;

/**
 * This BroadCast Receiver will receive the incoming calls
 */
public class CSCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        MainActivity.context = context;
        // if call count is 1 it will send third parameter as true. true indicates alreday one call running
        // if call running this will handle in startCall method in utils


        if (Constants.incallcount > 0) {
            utils.startCall(context, intent, true);
        } else {
            utils.startCall(context, intent, false);
        }
    }
}