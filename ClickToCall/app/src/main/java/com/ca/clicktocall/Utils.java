package com.ca.clicktocall;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import java.net.InetAddress;
import java.util.Random;

public class Utils {


    /**
     * This method will show simplealert
     * if user click settings button it will navigate to device settings screen
     * @param result
     * @param mActivity
     * @return
     */
    public static boolean showSimpleAlert(Activity mActivity,String result) {
        try {
            android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(mActivity);
            successfullyLogin.setMessage(result);
            successfullyLogin.setPositiveButton("ok",
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
     * This method will show popup for settings
     * if user click settings button it will navigate to device settings screen
     * @param result
     * @param mActivity
     * @return
     */
    public static boolean showSettingsAlert(Activity mActivity, String result) {
        try {

            System.out.println("connectsdk showSettingsAlert");
            android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(mActivity);
            successfullyLogin.setMessage(result);
            successfullyLogin.setPositiveButton("Settings",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mActivity.startActivity(intent);
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
     * This method checks network available or not.
     *
     * @param context application context.
     * @return boolean variable
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager
                    .getActiveNetworkInfo();

            boolean isconnected = activeNetworkInfo != null
                    && activeNetworkInfo.isConnectedOrConnecting();
            if (isconnected){

                    int exitValue = -1;
                    try {
                        Runtime runtime = Runtime.getRuntime();
                        Process ipProcess = runtime.exec("/system/bin/ping -c 1 -W 2 8.8.8.8");
                        exitValue = ipProcess.waitFor();
                    } catch (Exception ex) {ex.printStackTrace();}

                    if (exitValue == 0) {
                        return true;
                    } else {
                        InetAddress ipAddr = InetAddress.getByName("google.com");
                        if(ipAddr.equals("")) {
                            return false;
                        } else {
                            return true;
                        }
                    }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    public static int notifycallinprogress(Context context, String calle, String description) {

        Random rand = new Random();
        int notificationid = rand.nextInt(1000001);
        try {
            createNotificationChannel(context);

            Intent intent = new Intent(context, EmptyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Long.toString(System.currentTimeMillis()));


/*
            Intent intent = new Intent(context, ManageGroupActivity.class);
            intent.putExtra("grpid", callee);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Long.toString(System.currentTimeMillis()));
*/

            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            //  Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //Notification noti = new Notification.Builder(context)
            NotificationCompat.Builder noti = new NotificationCompat.Builder(context, "Default")
                    .setContentTitle(calle)
                    .setContentText(description)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                noti.setSmallIcon(R.drawable.ic_notification_transaperent);
                noti.setColor(context.getResources().getColor(R.color.theme_color));
            } else {
                noti.setSmallIcon(R.drawable.app_icon);
            }
            // hide the notification after its selected
            Notification notification = noti.build();
            notification.flags |= Notification.FLAG_NO_CLEAR;

            notificationManager.notify(notificationid, notification);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return notificationid;
    }

    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ClickToCall";
            String description = "Default Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("Default", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
