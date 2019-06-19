package com.ca.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;

import com.ca.groupmanagementsample.EmptyActivity;
import com.ca.groupmanagementsample.MainActivity;
import com.ca.chatsample.R;
import com.ca.groupmanagementsample.ManageGroupActivity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Random;

public class utils {
    /**
     * This method will show notification for group updates
     *
     * @param context
     * @param displaystring
     * @param grpname
     * @param grpid
     * @return
     */
    public static int notifyGroupNotification(Context context, String displaystring, String grpname, String grpid) {

        Random rand = new Random();
        int notificationid = rand.nextInt(1000001);
        try {


            Intent intent = new Intent(context, ManageGroupActivity.class);
            intent.putExtra("grpid", grpid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Long.toString(System.currentTimeMillis()));


            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Notification noti = new Notification.Builder(context)
                    .setContentTitle(displaystring)
                    .setContentText(grpname)
                    .setSound(uri)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentIntent(pIntent).build();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // hide the notification after its selected
            noti.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(notificationid, noti);

        } catch (Exception ex) {
            utils.logStacktrace(ex);
        }
        return notificationid;
    }

    /**
     * This method will show the notification call inProgress
     *
     * @param context
     * @param calle
     * @param description
     * @param callee
     * @param calltype
     * @param reqcode
     * @return
     */
    public static int notifyCallinProgress(Context context, String calle, String description, String callee, String calltype, int reqcode) {
        Random rand = new Random();
        int notificationid = rand.nextInt(1000001);
        try {
            Intent intent = new Intent(context, EmptyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Long.toString(System.currentTimeMillis()));
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Notification noti = new Notification.Builder(context)
                    .setContentTitle(calle)
                    .setContentText(description)
                    .setSound(uri)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentIntent(pIntent).build();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            noti.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(notificationid, noti);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return notificationid;
    }

    /**
     * This method will show the popUp with setting options
     * if user select setting it will navigate to settings page of device
     *
     * @param result
     * @param callingactivity
     * @param intent
     * @return
     */
    public static boolean showIntentAlert(String result, Activity callingactivity, final Intent intent) {
        try {
            android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(callingactivity);
            successfullyLogin.setMessage(result);
            successfullyLogin.setPositiveButton("Settings",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            MainActivity.context.startActivity(intent);
                        }
                    });
            successfullyLogin.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                        }
                    });
            successfullyLogin.show();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * This method will check given date is yesterday or not
     * if date is yesterday date it will return true
     * if date is not yesterdays date it will return false
     *
     * @param date
     * @return
     */
    public static boolean isYesterday(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);
        now.add(Calendar.DATE, -1);
        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }

    /**
     * This method will print all exceptions messages
     *
     * @param e
     */
    public static void logStacktrace(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
        } catch (Exception ex) {
        }
    }

    /**
     * This method will get the real path of file from URI
     *
     * @param context
     * @param contentURI
     * @return
     */
    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result = "";
        try {

            Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
            if (cursor == null) { // Source is Dropbox or other similar local file path
                result = contentURI.getPath();
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
                cursor.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }


}
