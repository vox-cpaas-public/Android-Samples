package com.ca.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class InComingCallHandlingReceiverA10 extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i("IncomingNotification", "getting action" + action);
        if (action.equals("EndCall")) {
            Intent endIntent = new Intent("NotificationHandleReceiver");
            endIntent.putExtra("operationToPerform",action);
            context.sendBroadcast(endIntent);
        } else if (action.equals("AnswerCall")) {
            Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(closeIntent);
            Intent endIntent = new Intent("NotificationHandleReceiver");
            endIntent.putExtra("operationToPerform",action);
            context.sendBroadcast(endIntent);
        }

    }


}
