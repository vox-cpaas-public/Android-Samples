package com.ca.receivers;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import com.ca.groupmanagementsample.MainActivity;
import com.ca.utils.utils;
import com.ca.wrapper.CSClient;

/**
 * This BroadCast Receiver will get if any new user  joined un group and pass to notification for user update
 */
public class CSUserJoined extends BroadcastReceiver
{
	CSClient CSClientObj = new CSClient();
    @Override
    public void onReceive(final Context context, final Intent intent)
    {
		new Thread(new Runnable() {
			public void run() {
				Looper.prepare();
    	try {
			MainActivity.context = context.getApplicationContext();
            String name = intent.getStringExtra("name");
            String number = intent.getStringExtra("number");
            if(name.equals("")) {
                name = number;
            }
	utils.notifyCallinProgress(context, "New User Joined",name, "", "",  0);
		} catch(Exception ex){
			utils.logStacktrace(ex);
		}
			}
		}).start();
    }
}