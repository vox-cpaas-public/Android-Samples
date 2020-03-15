package com.ca.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.ContactsContract;

import androidx.core.app.NotificationCompat;

import android.util.Log;
import android.widget.Toast;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.app.App;
import com.ca.callsample.CallScreenActivity;
import com.ca.callsample.EmptyActivity;
import com.ca.callsample.MainActivity;
import com.ca.callsample.PlayNewAudioCallActivity;
import com.ca.callsample.PlayNewVideoCallActivity;
import com.ca.callsample.R;
import com.ca.callsample.ShowUserLogActivity;
import com.ca.receivers.InComingCallHandlingReceiver;
import com.ca.receivers.InComingCallHandlingReceiverA10;
import com.ca.receivers.MissedCallNotificationHandler;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Random;

public class utils {


    private static String TAG="Utils";

    public static int notifyIncomigCall(Context context, String conatctName, String callType, boolean secondcall) {
        int notificationId = 5;
        Intent intent = new Intent(context, EmptyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Long.toString(System.currentTimeMillis()));


        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentAction = new Intent(context, InComingCallHandlingReceiver.class);
        intentAction.setAction("EndCall");
        PendingIntent endCallIntent = PendingIntent.getBroadcast(context, 1, intentAction, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent intentAction1 = new Intent(context, InComingCallHandlingReceiver.class);
        intentAction1.setAction("AnswerCall");
        PendingIntent answerCallIntent = PendingIntent.getBroadcast(context, 1, intentAction1, PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder noti = new NotificationCompat.Builder(context, "Default")
                .setContentTitle(conatctName)
                .setContentText(callType)
                .addAction(0, "Decline", endCallIntent)
                .addAction(0, "Answer", answerCallIntent)
                .setOngoing(true)
                .setContentIntent(pIntent)
                .setAutoCancel(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            noti.setSmallIcon(R.drawable.app_icon);
        } else {
            noti.setSmallIcon(R.drawable.app_icon);
        }
        if (secondcall) {
            NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle();
            inboxStyle.addLine("On Clicking Answer any existing ");
            inboxStyle.addLine("call/s will be disconnected.");
            noti.setStyle(inboxStyle);
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, noti.build());
        return notificationId;
    }

    /**
     * This method will show the missed call notifications
     *
     * @param context
     * @param number
     * @param name
     * @param callid
     * @param calldirection
     * @param calltime
     * @param reqcode
     * @return
     */
    public static int notifyCallMissed(Context context, String number, String name, String callid, String calldirection, String calltime, int reqcode) {
        Random rand = new Random();
        PreferenceProvider preferenceProvider = new PreferenceProvider(context);

        int notificationid = 0;
        try {
            {
                notificationid = preferenceProvider.getPrefInt(number + "Missed");
                if (notificationid == 0) {
                    notificationid = rand.nextInt(1000001);
                    preferenceProvider.setPrefint(number + "Missed", notificationid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //  Log.i(TAG, "notifyCallMissed: " + isNotificationVisible(context, notificationid));
        try {
            createNotificationChannel(context,"Default");
            Log.i(TAG,"I am in notifyCallMissed" + name + " direction " + calldirection);

            Intent intent = new Intent(context, MissedCallNotificationHandler.class);
            intent.putExtra("number", number);
            intent.putExtra("name", name);
            intent.putExtra("id", callid);
            intent.putExtra("direction", calldirection);
            Intent intent1 = new Intent(context, ShowUserLogActivity.class);
            intent1.putExtra("number", number);
            intent1.putExtra("name", name);
            intent1.putExtra("id", callid);
            intent1.putExtra("direction", calldirection);
            Log.i(TAG,"I am in userdisplaystring name" + name);
            String userdisplaystring = "";
            if (name.equals("")) {
                Log.i(TAG,"yes name is null" + number);
                userdisplaystring = number;
            } else {
                userdisplaystring = name;
            }
            Log.i(TAG,"I am in userdisplaystring" + userdisplaystring);

            String mycalldirection = "";
            if (calldirection.equals(CSConstants.MISSED_VIDEO_CALL)) {
                mycalldirection = "missed video call";
            } else {
                mycalldirection = "missed audio call";
            }
            String description = mycalldirection;

            String notificationMessage = "";
            {
                notificationMessage = preferenceProvider.getPrefString(number + "MissedData");
                if (notificationMessage.equals("")) {
                    notificationMessage = mycalldirection;
                } else {
                    notificationMessage = notificationMessage + "|" + mycalldirection;
                }
                preferenceProvider.setPrefString(number + "MissedData", notificationMessage);
            }
            String[] notificationMessages = notificationMessage.split("\\|");


            PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pIntent1 = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //Notification noti = new Notification.Builder(context)
            NotificationCompat.Builder noti = new NotificationCompat.Builder(context, "Default")

                    .setContentTitle(userdisplaystring)
                    .setContentText(description)
                    .setSound(uri)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentIntent(pIntent1)
                    .setAutoCancel(true)
                    .setDeleteIntent(pIntent);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // hide the notification after its selected
            //noti.flags |= Notification.FLAG_AUTO_CANCEL;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                noti.setSmallIcon(R.drawable.app_icon);
                noti.setColor(context.getResources().getColor(R.color.colorPrimary));
            } else {
                noti.setSmallIcon(R.drawable.app_icon);
            }

            NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle();
            for (int i = notificationMessages.length - 1; i >= 0; i--) {
                inboxStyle.addLine(notificationMessages[i]);
            }
            noti.setStyle(inboxStyle);
            noti.setNumber(notificationMessages.length);

            noti.setStyle(inboxStyle);

            notificationManager.notify(notificationid, noti.build());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return notificationid;
    }


    /**
     * This method will show the call in progress notification
     *
     * @param context
     * @param calle
     * @param description
     * @param callee
     * @param calltype
     * @param reqcode
     * @return
     */
    public static int notifycallinprogress(Context context, String calle, String description, String callee, String calltype, int reqcode) {
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
     * This method will show popup for settings
     * if user click settings button it will navigate to device settings screen
     *
     * @param result
     * @param callingactivity
     * @return
     */
    public static boolean showSettingsAlert(String result, Activity callingactivity) {
        try {
            android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(callingactivity);
            successfullyLogin.setMessage(result);
            successfullyLogin.setPositiveButton("Settings",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
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


    private static void createNotificationChannel(Context context,String channelname) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CallSample";
            String description = "Default Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelname, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        }
    }

    /**
     * This method will initiate new video call
     *
     * @param numbertodial
     * @param callingactivity
     */
    public static void doNewVideoCall(String numbertodial, Activity callingactivity) {
        try {
            CSClient CSClientObj = new CSClient();
            // if device is login state then only it will initiate VideoCall otherwise show popup message with error message
            if (CSClientObj.getLoginstatus()) {
                if (!numbertodial.equals("") && !numbertodial.equals(com.ca.utils.Constants.phoneNumber)) {
                    Intent intent = new Intent(callingactivity, PlayNewVideoCallActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("dstnumber", numbertodial);
                    intent.putExtra("isinitiatior", true);
                    callingactivity.startActivityForResult(intent, 954);
                } else {
                    Toast.makeText(callingactivity, "No valid Number", Toast.LENGTH_SHORT).show();
                }
            } else {
                showSettingsAlert("Couldn't place call. Please check internet connection and try again", callingactivity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method will initiate new Audio Call
     *
     * @param numbertodial
     * @param callingactivity
     */
    public static void doNewVoiceCall(String numbertodial, Activity callingactivity) {
        try {
            CSClient CSClientObj = new CSClient();
            // if device is login state then only it will initiate AudioCall otherwise show popup message with error message
            if (CSClientObj.getLoginstatus()) {
                if (!numbertodial.equals("") && !numbertodial.equals(com.ca.utils.Constants.phoneNumber)) {
                    Intent intent = new Intent(callingactivity, PlayNewAudioCallActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("dstnumber", numbertodial);
                    intent.putExtra("isinitiatior", true);
                    callingactivity.startActivityForResult(intent, 954);
                } else {
                    Toast.makeText(callingactivity, "No valid Number", Toast.LENGTH_SHORT).show();
                }
            } else {
                utils.showSettingsAlert("Couldn't place call. Please check internet connection and try again", callingactivity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method will start Audio/Video Call received from CSCallReceiver
     *
     * @param context
     * @param intent
     * @param secondcall
     */
    public static void startCall(Context context, Intent intent, boolean secondcall) {
        try {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    try {
                        MainActivity.context = context.getApplicationContext();
                        //if(!CSDataProvider.getUINotificationsMuteStatus()) {
                        MainActivity.context = context.getApplicationContext();
                        String sDstMobNu = intent.getStringExtra("sDstMobNu");
                        String callid = intent.getStringExtra("callid");
                        int callactive = intent.getIntExtra("callactive", 0);
                        String callType = intent.getStringExtra("callType");
                        String srcnumber = intent.getStringExtra("srcnumber");
                        CSClient CSClientObj = new CSClient();
                        long callstarttime = intent.getLongExtra("callstarttime", CSClientObj.getTime());
                        String calldirection = "";
                        Cursor cur = CSDataProvider.getCallLogCursorByFilter(CSDbFields.KEY_CALLLOG_CALLID, callid);
                        if (cur.getCount() > 0) {
                            cur.moveToNext();
                            calldirection = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DIR));
                        }
                        cur.close();
                        long presenttime = CSClientObj.getTime();
                        long timedifference = (presenttime - callstarttime) / 1000;
                        // if time difference between two incoming calls is less than 50sec then only it will process the incoming call
                        if (timedifference < 50) {

                         if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && new App().getActivityStackCount()<=0) {
                                showCallSensitiveNotofication(secondcall,false,sDstMobNu,callactive,callType,srcnumber,callid,callstarttime);
                            } else {
                                Intent intent1;
                                intent1 = new Intent(context.getApplicationContext(), CallScreenActivity.class);
                                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent1.putExtra("secondcall", secondcall);
                                intent1.putExtra("isinitiatior", false);
                                intent1.putExtra("sDstMobNu", sDstMobNu);
                                intent1.putExtra("callactive", callactive);
                                intent1.putExtra("callType", callType);
                                intent1.putExtra("srcnumber", srcnumber);
                                intent1.putExtra("callid", callid);
                                intent1.putExtra("callstarttime", callstarttime);
                                context.startActivity(intent1);
                            }


                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    public static void showCallSensitiveNotofication(boolean secondcall,boolean isinitiatior,String sDstMobNu,int callactive,String callType,String srcnumber,String callid,long callstarttime) {

            createNotificationChannel(MainActivity.context,"Android 10 Calls");

        String title = "Incoming "+callType+" call";


String name = srcnumber;
        Cursor ccfr = CSDataProvider.getContactCursorByNumber(srcnumber);
        if (ccfr.getCount() > 0) {
            ccfr.moveToNext();
            name = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
        }
        ccfr.close();

        Intent intent1 = new Intent(MainActivity.context, CallScreenActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.putExtra("secondcall", secondcall);
        intent1.putExtra("isinitiatior", false);
        intent1.putExtra("sDstMobNu", sDstMobNu);
        intent1.putExtra("callactive", callactive);
        intent1.putExtra("callType", callType);
        intent1.putExtra("srcnumber", srcnumber);
        intent1.putExtra("callid", callid);
        intent1.putExtra("callstarttime", callstarttime);
        intent1.setAction(Long.toString(System.currentTimeMillis()));

        PendingIntent pIntent = PendingIntent.getActivity(MainActivity.context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);



        //PendingIntent pIntent = PendingIntent.getActivity(MainActivity.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
/*
        Intent intentAction = new Intent(MainActivity.context, InComingCallHandlingReceiverA10.class);
        intentAction.setAction("EndCall");
        PendingIntent endCallIntent = PendingIntent.getBroadcast(MainActivity.context, 1, intentAction, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent intentAction1 = new Intent(MainActivity.context, InComingCallHandlingReceiverA10.class);
        intentAction1.setAction("AnswerCall");
        PendingIntent answerCallIntent = PendingIntent.getBroadcast(MainActivity.context, 1, intentAction1, PendingIntent.FLAG_CANCEL_CURRENT);
*/


        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(MainActivity.context)
                        .setChannelId("Android 10 Calls")
                        .setSound(uri)
                        .setSmallIcon(R.drawable.app_icon)
                        .setContentTitle(title)
                        .setContentText(name)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(0)
                        .setAutoCancel(false)
                        //.addAction(R.drawable.uiendcall1, "Decline", endCallIntent)
                        //.addAction(R.drawable.ui_answer_call, "Answer", answerCallIntent)//getPendingIntent(Constants.ACCEPT_CALL,callId,callType,number)) //getPendingIntent(Constants.ACCEPT_CALL,callId,callType))
                        //.setDeleteIntent(pIntent) //onDismissPendingIntent)
                        .setContentIntent(pIntent)
                        .setFullScreenIntent(pIntent, true);
        Notification incomingCallNotification = notificationBuilder.build();
        NotificationManager notificationManager = (NotificationManager) MainActivity.context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(new Random().nextInt(1000001), incomingCallNotification);
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
     * @param e
     */
    public static void logStacktrace(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
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

            return activeNetworkInfo != null
                    && activeNetworkInfo.isConnectedOrConnecting();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Bitmap loadContactPhoto(long id) {
        try {
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
            InputStream stream = ContactsContract.Contacts.openContactPhotoInputStream(
                    MainActivity.context.getContentResolver(), uri, true);
            return BitmapFactory.decodeStream(stream);
        } catch (Exception ex) {
            return null;
        }
    }

}
