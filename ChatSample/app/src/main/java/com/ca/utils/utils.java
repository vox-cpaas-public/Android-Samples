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
import android.graphics.Canvas;
import android.graphics.RectF;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ca.chatsample.ChatAdvancedActivity;
import com.ca.chatsample.EmptyActivity;
import com.ca.chatsample.MainActivity;
import com.ca.chatsample.R;
import com.ca.wrapper.CSDataProvider;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class utils {


    public static int notifyUserChat(Context context, String destinationid, String[] notificationMessages, int isgroupmessage,  String title,String lastfinalmessage) {
        Log.i("util", "notifyUserChat: " + destinationid + " " + isgroupmessage + " " );

        String entityId = destinationid;
        //int notificationid = new Random().nextInt(1000001);
        int notificationid = 1;



        try {



            createNotificationChannel(context);
            //LOG.info("I am in NotifyChat" + destinationid);
            Intent intent = new Intent();

            if (isgroupmessage == 0) {
                intent = new Intent(context, ChatAdvancedActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("Sender", destinationid);
                intent.putExtra("IS_GROUP", false);
                intent.setAction(Long.toString(System.currentTimeMillis()));
            } else {
                /*intent = new Intent(context, ChatActivityGroup.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("Sender", destinationid);
                intent.putExtra("grpname", title);
                intent.putExtra("IS_GROUP", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setAction(Long.toString(System.currentTimeMillis()));*/
            }


            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder noti = new NotificationCompat.Builder(context, "Default")
                    //Notification noti = new Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText(lastfinalmessage)
                    .setSound(uri)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                noti.setSmallIcon(R.drawable.ic_notification_transaperent);
                noti.setColor(context.getResources().getColor(R.color.theme_color));
            } else {
                noti.setSmallIcon(R.drawable.app_icon);
            }
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            for (int i = notificationMessages.length - 1; i >= 0; i--) {
                inboxStyle.addLine(notificationMessages[i]);
            }
            noti.setStyle(inboxStyle);
            noti.setNumber(notificationMessages.length);

            notificationManager.notify(entityId,notificationid, noti.build());

        } catch (Exception ex) {
            utils.logStacktrace(ex);
        }
        return notificationid;
    }

    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ChatSample";
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



    /**
     * This method will show the notification when call in progress
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
     * This method will give the path for sent image folder in device storage
     *
     * @return
     */
    public static String getSentImagesDirectory() {

        try {


            String imagedirectorysent = "ChatSample" + "/Images/Sent";
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + imagedirectorysent;

        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * This method will give the received image folder in Device storage
     *
     * @return
     */
    public static String getReceivedImagesDirectory() {

        try {
            String imagedirectoryreceived = "ChatSample" + "/Images/Received";
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + imagedirectoryreceived;

        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * This method will check whether device is connected to network or not
     *
     * @param context
     * @return
     */
    public static boolean isinternetavailable(Context context) {
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


    public static Bitmap scaleCenterCropChatImage(Bitmap source, int newHeight, int newWidth) {

        Bitmap dest = null;

        try {
            int sourceWidth = source.getWidth();
            int sourceHeight = source.getHeight();

            // Compute the scaling factors to fit the new height and width, respectively.
            // To cover the final image, the final scaling will be the bigger
            // of these two.
            float xScale = (float) newWidth / sourceWidth;
            float yScale = (float) newHeight / sourceHeight;
            float scale = Math.max(xScale, yScale);

            // Now get the size of the source bitmap when scaled
            float scaledWidth = scale * sourceWidth;
            float scaledHeight = scale * sourceHeight;

            // Let's find out the upper left coordinates if the scaled bitmap
            // should be centered in the new size give by the parameters
            float left = (newWidth - scaledWidth) / 2;
            float top = (newHeight - scaledHeight) / 5;

            // The target rectangle for the new, scaled version of the source bitmap will now
            // be
            RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

            // Finally, we create a new bitmap of the specified size and draw our new,
            // scaled bitmap onto it.
            dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
            Canvas canvas = new Canvas(dest);
            canvas.drawBitmap(source, null, targetRect, null);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dest;
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
    public static Bitmap loadContactPhoto(long id, Context context) {
        try {
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
            InputStream stream = ContactsContract.Contacts.openContactPhotoInputStream(
                    context.getContentResolver(), uri, true);
            return BitmapFactory.decodeStream(stream);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * This method will return the date from the given time stamp
     * @param dateStr
     * @return
     */
    public static String getTiemStamp(long dateStr) {
        try {
            return new SimpleDateFormat("hh:mm:ss a").format(dateStr);
        } catch (Exception e) {
            utils.logStacktrace(e);
        }
        return "";
    }


    public static boolean setFileTrasferPathsHelper() {
        try {



            Constants.chatappname = MainActivity.context.getApplicationInfo().loadLabel(MainActivity.context.getPackageManager()).toString();


            Constants.imagedirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Images";
            Constants.imagedirectorysent = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Images/Sent";//used for location and doc
            Constants.imagedirectoryreceived = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Images/Received"; //used for location and thumbainal internally


            //video
            Constants.videodirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Videos";
            Constants.videodirectorysent = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Videos/Sent";//used for location and doc
            Constants.videodirectoryreceived = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Videos/Received"; //used for location and thumbainal internally

            //audio
            Constants.audiodirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Audios";
            Constants.audiodirectorysent = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Audios/Sent";//used for location and doc
            Constants.audiodirectoryreceived = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Audios/Received"; //used for location and thumbainal internally

            //Documents
            Constants.docsdirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Documents";
            Constants.docsdirectorysent = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Documents/Sent";//used for location and doc
            Constants.docsdirectoryreceived = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Documents/Received"; //used for location and thumbainal internally

            //profiles
            Constants.profilesdirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Profile Photos";
            //Constants.profilesdirectorysent = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname+"/Profile Photos/Sent";//used for location and doc
            //Constants.profilesdirectoryreceived = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname+"/Profile Photos/Received"; //used for location and thumbainal internally

            Constants.thumbnailsdirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Thumbnails";

            Constants.recordingsdirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ Constants.chatappname + "/Recordings";



        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;

    }
    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result = "";
        try {

            Cursor cursor = null;

            if (contentURI != null) {
                cursor = context.getContentResolver().query(contentURI, null, null, null, null);
            }


            if (cursor == null) { // Source is Dropbox or other similar local file path
                result = contentURI.getPath();
            } else {

                //LOG.info("Test count:" + cursor.getCount());


                cursor.moveToFirst();

                //LOG.info("Test col1 name:"+cursor.getColumnName(0));
                //LOG.info("Test col2 name:"+cursor.getColumnName(1));

                //LOG.info("Test col1 data:"+cursor.getColumnIndex(cursor.getColumnName(0)));
                //LOG.info("Test col2 data:"+cursor.getColumnIndex(cursor.getColumnName(1)));

                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
                cursor.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            //result = getRealDocPathFromURI(context, contentURI);
        }
        return result;
    }

    /**
     * Converting timestamp to date, comparing for Today and Yesterday
     *
     * @param timeStamp
     * @return
     */
    public static String getDateForChat(long timeStamp, Context context) {

        try {
            DateFormat sdf = new SimpleDateFormat(Constants.ONLY_DATE_FORMAT);
            boolean is24fromat = android.text.format.DateFormat.is24HourFormat(context);
            DateFormat sdfForTime = null;
            if (is24fromat) {
                sdfForTime = new SimpleDateFormat(Constants.TIME_FORMAT_24HR);
            } else {
                sdfForTime = new SimpleDateFormat(Constants.TIME_FORMAT);
            }

            Date netDate = (new Date(timeStamp));

            Calendar smsTime = Calendar.getInstance();
            smsTime.setTimeInMillis(timeStamp);

            Calendar now = Calendar.getInstance();


            if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
                return sdfForTime.format(netDate);
            } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
                return "Yesterday";
            }

            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

}
