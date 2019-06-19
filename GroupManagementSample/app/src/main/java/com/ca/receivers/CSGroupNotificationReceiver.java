package com.ca.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Looper;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.groupmanagementsample.MainActivity;
import com.ca.utils.utils;
import com.ca.wrapper.CSDataProvider;

import java.util.ArrayList;

/**
 * This broadCast receiver will receive the notifications for group updates
 */
public class CSGroupNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    MainActivity.context = context.getApplicationContext();
                    int notificationtype = intent.getIntExtra("reqcode", 0);
                    String grpid = intent.getStringExtra("grpid");
                    String displaystring = intent.getStringExtra("displaystring");

                    if (notificationtype == CSConstants.E_DELETED_FROM_GROUP_RQT || notificationtype == CSConstants.E_GROUP_DELETED_TO_GROUP_RQT) {
                        String deletedgrpname = intent.getStringExtra("grpname");
                        utils.notifyCallinProgress(context, displaystring, deletedgrpname, grpid, "", 0);
                    } else {

                        String grpname = "";
                        Cursor cur = CSDataProvider.getGroupsCursorByFilter(CSDbFields.KEY_GROUP_ID, grpid);
                        if (cur.getCount() > 0) {
                            cur.moveToNext();
                            grpname = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_NAME));
                        }
                        cur.close();
                        // once fetching of all details it will show notification to user
                        ArrayList numberslist = intent.getStringArrayListExtra("numberslist");
                        if (numberslist == null) {
                            utils.notifyGroupNotification(context, displaystring, grpname, grpid);
                        } else {
                            for (int i = 0; i < numberslist.size(); i++) {
                                utils.notifyGroupNotification(context, displaystring, grpname, grpid);
                            }
                        }
                    }
                } catch (Exception ex) {
                    utils.logStacktrace(ex);
                }
            }
        }).start();
    }

}