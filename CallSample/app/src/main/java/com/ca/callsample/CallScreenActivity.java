package com.ca.callsample;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.utils.Constants;
import com.ca.Utils.CSConstants;
import com.ca.utils.utils;
import com.ca.wrapper.CSCall;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.lang.ref.WeakReference;

/**
 * This class will handle all the incoming call request
 */
public class CallScreenActivity extends AppCompatActivity {
    private boolean callAnsweredFlag = false;
    private int notificationId = 0;
    private String mContactName = "";
    private final Handler mPlayerHandler = new Handler();
    private Runnable mPlayerRunnable;
    private int mPlayerDelay = 1000;
    private long remainingTimeOffset = 35;
    private String mDestinationNumber = "";
    private String mCallId = "";
    private int callactive = 0;
    private String callType = "audio";
    private String srcnumber = "";
    private CSClient CSClientObj = new CSClient();
    private long callstarttime = 0;
    private Button AnswerCall;
    private Button EndCall;
    private TextView CallType;
    private TextView multiplecallalerttext;
    private CircularImageView calleravathar;
    private CSCall CSCallObj = new CSCall();
    private boolean callanswerstarted = false;
    private boolean secondcall = false;
    private Ringtone ringtone;
    private String TAG = "CallScreenActivity";
    /**
     * ImageLoader variables
     */
    DisplayImageOptions mOptions;
    ImageLoader mImageLoader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callscreen);
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            Constants.incallcount = Constants.incallcount + 1;
            callstarttime = CSClientObj.getTime();
            AnswerCall = findViewById(R.id.answer_call_button);
            EndCall = findViewById(R.id.end_call_button);
            CallType = findViewById(R.id.contact_number_tv);
            calleravathar = (CircularImageView) findViewById(R.id.contact_avatar);
            multiplecallalerttext = (TextView) findViewById(R.id.call_screen_help_text_tv);
            mDestinationNumber = getIntent().getStringExtra("sDstMobNu");
            mCallId = getIntent().getStringExtra("callid");
            callactive = getIntent().getIntExtra("callactive", 0);
            callType = getIntent().getStringExtra("callType");
            srcnumber = getIntent().getStringExtra("srcnumber");
            callstarttime = getIntent().getLongExtra("callstarttime", CSClientObj.getTime());
            secondcall = getIntent().getBooleanExtra("secondcall", false);
            mImageLoader = ImageLoader.getInstance();
            if (!mImageLoader.isInited()) {
                mImageLoader.init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
            }

            mOptions = new DisplayImageOptions.Builder()
                    /*.showImageOnLoading(R.drawable.img_profile_default)
                    .showImageForEmptyUri(R.drawable.img_profile_default)
                    .showImageOnFail(R.drawable.img_profile_default)*/
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            // if no call running in background when incoming call came case it will hide the help text to user
            if (!secondcall) {
                multiplecallalerttext.setVisibility(View.INVISIBLE);
            }

            getProfilePicture();
            long presenttime = CSClientObj.getTime();
            callAnsweredFlag = false;
            remainingTimeOffset = 50 - ((presenttime - callstarttime) / 1000);
            if (remainingTimeOffset < 0) {
                remainingTimeOffset = 0;
            }
            String nativecontactid = "";
            mContactName = srcnumber;
            String contactName = "UnKnown";
            // below logic will get the contact name of incoming call number
            Cursor ccfr = CSDataProvider.getContactCursorByNumber(srcnumber);
            if (ccfr.getCount() > 0) {
                ccfr.moveToNext();
                contactName = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
                nativecontactid = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
            }
            ccfr.close();
            String notificationName = contactName;
            if (contactName.equals("UnKnown")) {
                notificationName = srcnumber;
            }
            contactName = contactName + "\n" + srcnumber;
            // if incoming call is video it will update UI for incoming video call and send CallRinging acknowledgement to caller
            // if incoming call is audio it will update UI for incoming audio call and send CallRinging acknowledgement to caller
            if (callType.equals("video")) {
                notificationId = utils.notifyIncomigCall(getApplicationContext(), notificationName, "Incoming video call", secondcall);
                CallType.setText(contactName + "\nVideo Call");
                CSCallObj.sendVideoCallRinging(srcnumber, mCallId, false);
            } else {
                notificationId = utils.notifyIncomigCall(getApplicationContext(), notificationName, "Incoming audio call", secondcall);
                CallType.setText(contactName + "\nAudio Call");
                CSCallObj.sendVoiceCallRinging(srcnumber, mCallId, false);
            }

            // bellow handler will close the calling screen if call not answered with specified time
            mPlayerRunnable = new Runnable() {
                int i = 0;

                public void run() {
                    mPlayerHandler.postDelayed(this, mPlayerDelay);
                    CallScreenActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            i++;
                            if (i >= remainingTimeOffset || i >= 55) {
                                endCall(srcnumber, mCallId);
                            }
                        }
                    });
                }
            };
            mPlayerHandler.postDelayed(mPlayerRunnable, mPlayerDelay);
            registerReceiver(NotificationReceiver, new IntentFilter("NotificationHandleReceiver"));
            // Answer call button click listener
            AnswerCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (ringtone != null) {
                            ringtone.stop();
                        }
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(notificationId);
                        mPlayerHandler.removeCallbacks(mPlayerRunnable);
                        // if already call running below logic will send terminate call broadcast to PlayNewAudioCallActivity screen
                        if (Constants.answeredcallcount > 0) {
                            AnswerCall.setEnabled(false);
                            AnswerCall.setAlpha(0.5f);
                            Intent intentt = new Intent("TerminateForSecondCall");
                            intentt.putExtra("callid", Constants.lastcallid);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentt);
                        } else {
                            // if no call running below method will answer the call
                            answerCall();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            // endCall button click listener
            EndCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // this method will end the incoming call
                        endCall(srcnumber, mCallId);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            // below logic will get the contact image id to show in calling page
            String picid = "";
            Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, srcnumber);
            if (cur.getCount() > 0) {
                cur.moveToNext();
                picid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
            }
            cur.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }

    /**
     * This method will end the incoming call
     *
     * @param srcnumber
     * @param callid
     */
    public void endCall(String srcnumber, String callid) {
        try {
            // this logic will release the incoming ringtone
            if (ringtone != null) {
                ringtone.stop();
            }
            // this will stop the handler
            mPlayerHandler.removeCallbacks(mPlayerRunnable);

            // if incoming call callType equal to video it will call endVideoCall API
            // if incoming call callType equal to audio it will call endVoiceCall API
            if (callType.equals("video")) {
                CSCallObj.endVideoCall(srcnumber, callid, CSConstants.E_UserBusy, CSConstants.UserBusy);
            } else {
                CSCallObj.endVoiceCall(srcnumber, callid, CSConstants.E_UserBusy, CSConstants.UserBusy);
            }
            Constants.incallcount = Constants.incallcount - 1;
            if (Constants.incallcount < 0) {
                Constants.incallcount = 0;
            }
            // this line of code will unregister the registered receivers
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
            callAnsweredFlag = true;
            finish();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method will update the UI with SDK events
     *
     * @param str
     */
    public void updateUI(String str) {
        try {
            if (str.equals("NetworkError")) {
                // this block will handle network related events
                if (ringtone != null) {
                    ringtone.stop();
                }
                // this will stop the incoming call handler
                mPlayerHandler.removeCallbacks(mPlayerRunnable);
                // this will unregister the registered receivers
                LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);

                // this will reset the call count to default
                Constants.incallcount = Constants.incallcount - 1;
                if (Constants.incallcount < 0) {
                    Constants.incallcount = 0;
                }

                // if incoming call callType equal to video it will call endVideoCall API
                // if incoming call callType equal to audio it will call endVoiceCall API
                if (callType.equals("video")) {
                    CSCallObj.endVideoCall(srcnumber, mCallId, com.ca.Utils.CSConstants.E_UnKnown, com.ca.Utils.CSConstants.UnKnown);
                } else {
                    CSCallObj.endVoiceCall(srcnumber, mCallId, com.ca.Utils.CSConstants.E_UnKnown, com.ca.Utils.CSConstants.UnKnown);
                }

                // this will cancel incoming call notification
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(notificationId);
                callAnsweredFlag = true;
                finish();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This broadcast receiver handle all the SDK events
     */
    public class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    // this block will handle all network related events
                    updateUI("NetworkError");
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    // this block will handle login response
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        // if user login success then only it will enable answer call button to answer incoming call
                        AnswerCall.setAlpha(1);
                        AnswerCall.setEnabled(true);
                    }
                } else if (intent.getAction().equals(CSEvents.CSCALL_CALLTERMINATED)) {

                    if (Constants.lastcallid.equals(intent.getStringExtra("callid"))) {
                        answerCall();
                    }
                } else if (intent.getAction().equals(CSEvents.CSCALL_CALLENDED)) {
                    // if the incoming call ended before lift the call this block will execute
                    if (mCallId.equals(intent.getStringExtra("callid"))) {
                        mPlayerHandler.removeCallbacks(mPlayerRunnable);
                        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
                        Constants.incallcount = Constants.incallcount - 1;
                        if (Constants.incallcount < 0) {
                            Constants.incallcount = 0;
                        }
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(notificationId);
                        callAnsweredFlag = true;
                        finish();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    public void onResume() {
        super.onResume();
        try {
            // register the receivers for SDK events
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter4 = new IntentFilter(CSEvents.CSCALL_CALLENDED);
            IntentFilter filter5 = new IntentFilter(CSEvents.CSCLIENT_LOGIN_RESPONSE);
            IntentFilter filter6 = new IntentFilter(CSEvents.CSCALL_CALLTERMINATED);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter4);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter5);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter6);

            // below logic will start the ringtone for incoming calls
            try {

                if (ringtone != null) {
                    ringtone.play();
                } else {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    ringtone.play();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (!CSCallObj.isCallAnswerable(mCallId)) {
                mPlayerHandler.removeCallbacks(mPlayerRunnable);
                LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
                Constants.incallcount = Constants.incallcount - 1;
                if (Constants.incallcount < 0) {
                    Constants.incallcount = 0;
                }
                callAnsweredFlag = true;
                finish();
            } else {
            }


            // if login status not true below logic will disable the Answer call image
            if (!CSClientObj.getLoginstatus()) {
                AnswerCall.setAlpha(0.5f);
                AnswerCall.setEnabled(false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            // this will stop the media player if it is running
            if (ringtone != null) {
                ringtone.stop();
            }
            // below logic will show missed call notification if call not answered
           /* if (!callAnsweredFlag) {
                if (callType.equals("video")) {
                    notificationId = utils.notifycallinprogress(getApplicationContext(), mContactName, "Incoming Video call", "", "", 0);
                } else {
                    notificationId = utils.notifycallinprogress(getApplicationContext(), mContactName, "Incoming Audio call", "", "", 0);
                }
            }*/
            callAnsweredFlag = false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (NotificationReceiver != null)
            unregisterReceiver(NotificationReceiver);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * This method will handle the answer call button click
     * based in type of call it will navigate to Audio/Video call screen
     */
    public void answerCall() {
        try {
            Intent intent1;
            if (callType.equals("video")) {
                intent1 = new Intent(MainActivity.context, PlayNewVideoCallActivity.class);
            } else {
                intent1 = new Intent(MainActivity.context, PlayNewAudioCallActivity.class);
            }
            intent1.putExtra("isinitiatior", false);
            intent1.putExtra("sDstMobNu", mDestinationNumber);
            intent1.putExtra("callactive", callactive);
            intent1.putExtra("callType", callType);
            intent1.putExtra("srcnumber", srcnumber);
            intent1.putExtra("callid", mCallId);
            callanswerstarted = true;
            startActivity(intent1);
            Constants.incallcount = Constants.incallcount - 1;
            if (Constants.incallcount < 0) {
                Constants.incallcount = 0;
            }
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            callAnsweredFlag = true;
            finish();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getProfilePicture() {
        String nativecontactid = "";
        Cursor cur = CSDataProvider.getContactCursorByNumber(srcnumber);
        if (cur.getCount() > 0) {
            cur.moveToNext();
            nativecontactid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
        }
        cur.close();

        String picid = "";
        Cursor cur1 = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, srcnumber);
        //LOG.info("Yes count:"+cur1.getCount());
        if (cur1.getCount() > 0) {
            cur1.moveToNext();
            picid = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
        }
        cur1.close();

        Log.i(TAG, "Profile Pic ID " + picid + " file path " + CSDataProvider.getImageFilePath(picid));
        Bitmap bitmap = CSDataProvider.getImageBitmap(picid);
        if (bitmap != null)
            calleravathar.setImageBitmap(bitmap);
        String filepath = CSDataProvider.getImageFilePath(picid);
        mImageLoader.clearMemoryCache();
        mImageLoader.clearDiskCache();
        String finalNativecontactid = nativecontactid;
        String finalPicid = picid;
        mImageLoader.loadImage("file://" + filepath, mOptions, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view,
                                          Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                // cache is now warmed up
                if (loadedImage != null) {
                    Log.i(TAG, "setting profile image");
                    calleravathar.setImageBitmap(loadedImage);
                } else {
                    new ImageDownloaderTask(calleravathar).execute("app", finalPicid, finalNativecontactid);
                }

            }
        });

    }

    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        boolean scaleit = false;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap photo = null;
            try {

                if (params[0].equals("app")) {
                    photo = CSDataProvider.getImageBitmap(params[1]);
                    if (photo == null) {
                        photo = utils.loadContactPhoto(Long.parseLong(params[2]));
                    }
                } else if (params[0].equals("native")) {
                    photo = utils.loadContactPhoto(Long.parseLong(params[1]));
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    if (photo == null) {
                        photo = BitmapFactory.decodeResource(getResources(), R.drawable.defaultcontact);
                    }
                } catch (Exception ex) {
                    e.printStackTrace();
                }
            }
            return photo;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }

    }

    BroadcastReceiver NotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("CallScreenActivity", "NotificationReceiver called");
            String action = intent.getAction();
            if (action.equals("NotificationHandleReceiver")) {
                String actionToperFrom = intent.getStringExtra("operationToPerform");
                Log.i("CallScreenActivity", "operationToPerform " + actionToperFrom);
                if (actionToperFrom.equals("EndCall")) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(notificationId);
                    EndCall.performClick();
                } else if (actionToperFrom.equals("AnswerCall")) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(notificationId);
                    AnswerCall.performClick();
                }
            }
        }
    };
}
