package com.ca.callsample;

import android.app.Activity;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.multidex.MultiDex;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.utils.utils;
import com.ca.wrapper.CSCall;
import com.ca.wrapper.CSDataProvider;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.lang.ref.WeakReference;

public class PlayNewAudioCallActivity extends Activity {
    private boolean showNotification = true;
    private NotificationManager notificationManager;
    private AlertDialog.Builder successfullyLogin;
    private AlertDialog dismissSuccessfullyLogin;
    private int notificationId = 0;
    private final Handler mTimerHandler = new Handler();
    private Runnable mTimerRunnable;
    private MediaPlayer mMediaPlayer;
    private String myCallId = "";
    private boolean isDTMFEnabled = true;
    private String mContactName = "";
    private String mDestinationNumberToCall = "";
    private ImageView mDTMFImg, mAudioEndCallImg, mAudioMuteImg, mAudioSpeakerImg, mHoldImg;
    private TextView mReconnectingTv, mCallerInfoTv, mCallStatusTv;
    private int mTimerDelay = 1000;
    private boolean isMuteEnabled = true;
    private boolean isSpeakerEnabled = false;
    private CircularImageView mContactAvatarImg;
    private RelativeLayout mBlankScreenLayout, mMainScreenLayout;
    private CSCall CSCallObj = new CSCall();
    private boolean isHoldEnabled = true;
    public static boolean audioMuteStateIrrespectiveHold = false;
    private MediaPlayer mConnectingMediaPlayer;
    private String TAG = "PlayNewAudioCallActivity";
    /**
     * ImageLoader variables
     */
    DisplayImageOptions mOptions;
    ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_call);
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            com.ca.utils.Constants.incallcount = com.ca.utils.Constants.incallcount + 1;
            com.ca.utils.Constants.answeredcallcount = com.ca.utils.Constants.answeredcallcount + 1;
            showNotification = true;
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            successfullyLogin = new AlertDialog.Builder(PlayNewAudioCallActivity.this);
            mReconnectingTv = findViewById(R.id.audio_call_reconnecting_tv);
            mCallerInfoTv = findViewById(R.id.audio_call_contact_info_tv);
            mCallStatusTv = findViewById(R.id.audio_call_status_tv);
            mAudioEndCallImg = findViewById(R.id.end_call_img);
            mBlankScreenLayout = findViewById(R.id.blank_screen_layout);
            mMainScreenLayout = findViewById(R.id.main_screen_layout);
            mDTMFImg = findViewById(R.id.audio_call_keypad_img);
            mAudioMuteImg = findViewById(R.id.audio_call_mute_img);
            mAudioSpeakerImg = findViewById(R.id.audio_call_speaker_img);
            mHoldImg = findViewById(R.id.audio_call_hold_img);
            mContactAvatarImg = findViewById(R.id.contact_avatar);
            mReconnectingTv.setVisibility(View.GONE);
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
            try {
                mTimerRunnable = new Runnable() {
                    int i = 0;

                    public void run() {
                        mTimerHandler.postDelayed(this, mTimerDelay);
                        PlayNewAudioCallActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                String minutes = "00";
                                String seconds = "00";
                                int x = i++;
                                int mins = (x) / 60;
                                int sec = (x) % 60;
                                if (mins < 10) {
                                    minutes = "0" + String.valueOf(mins);
                                } else {
                                    minutes = String.valueOf(mins);
                                }
                                if (sec < 10) {
                                    seconds = "0" + String.valueOf(sec);
                                } else {
                                    seconds = String.valueOf(sec);
                                }
                                mCallStatusTv.setText(minutes + ":" + seconds);
                            }
                        });
                    }
                };
                IntentFilter filter1 = new IntentFilter(CSEvents.CSCALL_CALLENDED);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);
                if (getIntent().getBooleanExtra("isinitiatior", false)) {
                    playConnectingTone();
                    mDestinationNumberToCall = getIntent().getStringExtra("dstnumber");
                    myCallId = CSCallObj.startVoiceCall(mDestinationNumberToCall,CSConstants.CALLRECORD.DONTRECORD);
                } else {
                    mCallStatusTv.setText("Connecting..");
                    mDestinationNumberToCall = getIntent().getStringExtra("srcnumber");
                    mTimerHandler.postDelayed(mTimerRunnable, mTimerDelay);
                    myCallId = getIntent().getStringExtra("callid");
                    CSCallObj.answerVoiceCall(mDestinationNumberToCall, getIntent().getStringExtra("callid"));
                }
                com.ca.utils.Constants.lastcallid = myCallId;

                String displayName = "UnKnown";
                Cursor ccfr = CSDataProvider.getContactCursorByNumber(mDestinationNumberToCall);
                if (ccfr.getCount() > 0) {
                    ccfr.moveToNext();
                    displayName = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
                }
                ccfr.close();
                if(displayName.equals("UnKnown")){
                    mContactName=mDestinationNumberToCall;
                }else {
                    mContactName=displayName;
                }
                displayName = displayName + "\n" + mDestinationNumberToCall;
                mCallerInfoTv.setText(displayName);

                getProfilePicture();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // EndCall button click listener
            mAudioEndCallImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        showNotification = false;
                        stopConnectingTone();
                        stopRingBackTone();

                        // API call to end the running call
                        CSCallObj.endVoiceCall(mDestinationNumberToCall, myCallId, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
                        endTheCall();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            // Speaker image click listener
            mAudioSpeakerImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (!myCallId.equals("")) {
                            if (isSpeakerEnabled) {
                                isSpeakerEnabled = false;
                                // this API will disable the speaker
                                CSCallObj.enableSpeaker(myCallId, false);
                                mAudioSpeakerImg.setBackgroundResource(R.drawable.uispeaker_icon_norm);
                            } else {
                                isSpeakerEnabled = true;

                                // this API will enable the speaker
                                CSCallObj.enableSpeaker(myCallId, true);
                                mAudioSpeakerImg.setBackgroundResource(R.drawable.uispeaker_icon_hover);
                            }
                        }
                    } catch (Exception ex) {
                        utils.logStacktrace(ex);
                    }
                }
            });

            // Hold Image click listener
            mHoldImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {

                        if (!myCallId.equals("")) {

                            if (isHoldEnabled) {
                                // this will Hold the running Audio call
                                CSCallObj.holdAVCall(mDestinationNumberToCall, myCallId, true);
                            } else {
                                // This will UnHold the running Audio call
                                CSCallObj.holdAVCall(mDestinationNumberToCall, myCallId, false);
                            }

                        }
                    } catch (Exception ex) {
                        utils.logStacktrace(ex);
                    }
                }
            });

            // keypad button click listener
            mDTMFImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (isDTMFEnabled) {
                            isDTMFEnabled = false;
                            mDTMFImg.setBackgroundResource(R.drawable.ui_keypad_hover);
                            // this will show the keypad
                            showDTMFDialog();
                        } else {
                            isDTMFEnabled = true;
                            mDTMFImg.setBackgroundResource(R.drawable.ui_keypad_normal);
                            dismissSuccessfullyLogin.cancel();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });


            // this will close the keypad if user click back button
            successfullyLogin.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    isDTMFEnabled = true;
                    mDTMFImg.setBackgroundResource(R.drawable.ui_keypad_normal);
                    dismissSuccessfullyLogin.cancel();
                }
            });

            // Mute image click listener
            mAudioMuteImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (!myCallId.equals("")) {
                            if (isMuteEnabled) {
                                // This will disable the Audio mute
                                CSCallObj.muteAudio(myCallId, false);
                                audioMuteStateIrrespectiveHold = false;
                                isMuteEnabled = false;
                                mAudioMuteImg.setBackgroundResource(R.drawable.ui_mute_normal);
                            } else {
                                // thin API will enable the Audio mute
                                CSCallObj.muteAudio(myCallId, true);
                                audioMuteStateIrrespectiveHold = true;
                                isMuteEnabled = true;
                                mAudioMuteImg.setBackgroundResource(R.drawable.ui_mute_hover);
                            }
                        }
                    } catch (Exception ex) {
                        utils.logStacktrace(ex);
                    }
                }
            });
        } catch (Exception ex) {
            utils.logStacktrace(ex);
        }
    }

    /**
     * This method will show the DTMF dialog to user for communication
     *
     * @return
     */
    public boolean showDTMFDialog() {
        return true;
    }

    /**
     * Below broadcast receiver will handle the call backs coming from SDK
     */
    public class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (myCallId.equals(intent.getStringExtra("callid"))) {
                    if (intent.getAction().equals(CSEvents.CSCALL_CALLENDED)) {
                        showNotification = false;
                        if (intent.getStringExtra("endReason").toString().equals(CSConstants.UserBusy)) {
                            releaseCall("UserBusy");
                        } else {
                            releaseCall("Call Ended");
                        }
                    } else if (intent.getAction().equals(CSEvents.CSCALL_NOANSWER)) {
                        showNotification = false;
                        releaseCall("NoAnswer");
                    } else if (intent.getAction().equals(CSEvents.CSCALL_CALLANSWERED)) {
                        stopConnectingTone();
                        stopRingBackTone();
                        mCallStatusTv.setText("Connecting..");
                        mTimerHandler.postDelayed(mTimerRunnable, mTimerDelay);
                    } else if (intent.getAction().equals(CSEvents.CSCALL_NOMEDIA)) {
                        showNotification = false;
                        releaseCall("NoMedia");
                    } else if (intent.getAction().equals(CSEvents.CSCALL_RINGING)) {
                        mCallStatusTv.setText("Ringing");
                        stopConnectingTone();
                        playRingBackTone();
                    } else if (intent.getAction().equals(CSEvents.CSCALL_MEDIACONNECTED)) {
                        mReconnectingTv.setVisibility(View.GONE);
                        stopConnectingTone();
                        stopRingBackTone();
                    } else if (intent.getAction().equals(CSEvents.CSCALL_MEDIADISCONNECTED)) {
                        mReconnectingTv.setVisibility(View.VISIBLE);
                    } else if (intent.getAction().equals("TerminateForSecondCall")) {
                        mAudioEndCallImg.performClick();
                    } else if (intent.getAction().equals(CSEvents.CSCALL_HOLD_UNHOLD_RESPONSE)) {
                        if (myCallId.equals(intent.getStringExtra("callid"))) {
                            boolean ishold = intent.getBooleanExtra("hold", false);

                            if (ishold) {
                                isHoldEnabled = false;
                                mHoldImg.setBackgroundResource(R.drawable.ui_hold_hover);
                                mAudioMuteImg.setEnabled(false);

                            } else {
                                isHoldEnabled = true;
                                mHoldImg.setBackgroundResource(R.drawable.ui_hold_normal);
                                mAudioMuteImg.setEnabled(true);
                                if (audioMuteStateIrrespectiveHold) {
                                    isMuteEnabled = false;
                                    mAudioMuteImg.setBackgroundResource(R.drawable.ui_mute_hover);
                                }
                            }

                        }
                    }
                }
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    stopConnectingTone();
                    stopRingBackTone();
                }
                if (intent.getAction().equals("proximitySensorevent")) {
                    try {
                        // below logic will show the black screen if app got Proximity sensor event
                        String action = intent.getStringExtra("event");
                        if (action.equals("near")) {
                            // if proximity sensor event is equal to near it will show blank black screen and hide Audio call options screen
                            WindowManager.LayoutParams params = getWindow().getAttributes();
                            params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                            params.screenBrightness = 0;
                            getWindow().setAttributes(params);
                            mBlankScreenLayout.setVisibility(View.VISIBLE);
                            mMainScreenLayout.setVisibility(View.INVISIBLE);
                        } else {
                            // if proximity sensor event is not equal to near it will hide blank black screen and show Audio call options screen
                            WindowManager.LayoutParams params = getWindow().getAttributes();
                            params.screenBrightness = -1;
                            getWindow().setAttributes(params);
                            mBlankScreenLayout.setVisibility(View.INVISIBLE);
                            mMainScreenLayout.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
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
            showNotification = true;

            // resister the receivers to handle SDK events for Audio calls
            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter2 = new IntentFilter("proximitySensorevent");
            IntentFilter filter3 = new IntentFilter(CSEvents.CSCALL_NOANSWER);
            IntentFilter filter4 = new IntentFilter(CSEvents.CSCALL_CALLANSWERED);
            IntentFilter filter5 = new IntentFilter(CSEvents.CSCALL_NOMEDIA);
            IntentFilter filter6 = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter7 = new IntentFilter(CSEvents.CSCALL_RINGING);
            IntentFilter filter8 = new IntentFilter(CSEvents.CSCALL_MEDIACONNECTED);
            IntentFilter filter9 = new IntentFilter(CSEvents.CSCALL_MEDIADISCONNECTED);
            IntentFilter filter10 = new IntentFilter("TerminateForSecondCall");
            IntentFilter filter11 = new IntentFilter(CSEvents.CSCALL_HOLD_UNHOLD_RESPONSE);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter2);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter3);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter4);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter5);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter6);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter7);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter8);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter9);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter10);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter11);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        } catch (Exception ex) {
            utils.logStacktrace(ex);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // below logic will show the notification if user place the app in background while call running
        if (showNotification) {
            notificationId = utils.notifycallinprogress(getApplicationContext(), mContactName, "Audio call is in progress", "", "", 0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            // cancel all notification while closing Audio Call screen
            if (showNotification) {
                notificationManager.cancelAll();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            // handle the back button events
            showStreamStopAlert();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method will close the Audio call screen when Call end events came from SDK
     *
     * @param message
     * @return
     */
    public boolean showStreamStoppedAlert(final String message) {
        try {
            stopConnectingTone();
            stopRingBackTone();
            endTheCall();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * This method will show popup dialog to user when back button was pressed
     *
     * @return
     */
    public boolean showStreamStopAlert() {
        try {
            if (!isFinishing()) {
                android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(PlayNewAudioCallActivity.this);
                successfullyLogin.setTitle(getString(R.string.user_alert_dialog_tittle));
                successfullyLogin.setCancelable(false);
                successfullyLogin.setMessage("End Call?");
                successfullyLogin.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                showNotification = false;
                                stopConnectingTone();
                                stopRingBackTone();
                                // if user press OK end the running call
                                CSCallObj.endVoiceCall(mDestinationNumberToCall, myCallId, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
                                endTheCall();
                            }
                        });
                successfullyLogin.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        });
                if (!isFinishing()) {
                    successfullyLogin.show();
                }
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * This method will close the Audio call screen when user end the running call
     * and it will reset the all call related values to default values
     */
    public void endTheCall() {
        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            mTimerHandler.removeCallbacks(mTimerRunnable);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
            stopConnectingTone();
            stopRingBackTone();
            com.ca.utils.Constants.incallcount = com.ca.utils.Constants.incallcount - 1;
            com.ca.utils.Constants.answeredcallcount = com.ca.utils.Constants.answeredcallcount - 1;
            if (com.ca.utils.Constants.answeredcallcount < 0) {
                com.ca.utils.Constants.answeredcallcount = 0;
            }
            if (com.ca.utils.Constants.incallcount < 0) {
                com.ca.utils.Constants.incallcount = 0;
            }
            finish();
        } catch (Exception ex) {
            utils.logStacktrace(ex);
        }
    }

    /**
     * This method will play the ringbackTone once got Ringing event from SDK
     */
    public void playRingBackTone() {
        if (getIntent().getBooleanExtra("isinitiatior", false)) {
            try {
                if (mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying())
                        mMediaPlayer.stop();
                    mMediaPlayer.reset();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                int resID = R.raw.standard_6_ringback;
                mMediaPlayer = MediaPlayer.create(PlayNewAudioCallActivity.this, resID);
                if (mMediaPlayer != null) {
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This method will stop the ring back tone once got call confirmed event or end call click
     */
    public void stopRingBackTone() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method will stop the call connecting tone once got Ringing event
     */
    private void stopConnectingTone() {
        try {
            if (mConnectingMediaPlayer != null) {
                mConnectingMediaPlayer.stop();
                mConnectingMediaPlayer.reset();
                mConnectingMediaPlayer.release();
                mConnectingMediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will play connecting tone until receive Ringing evnt
     */
    private void playConnectingTone() {
        try {
            mConnectingMediaPlayer = MediaPlayer.create(this, R.raw.call_connecting);
            mConnectingMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    mConnectingMediaPlayer.start();
                }
            });
            mConnectingMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will play call end tone if app receiver call end event
     */
    private void playCallEndTone() {
        try {
            mConnectingMediaPlayer = MediaPlayer.create(this, R.raw.call_end_tone);
            mConnectingMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    mConnectingMediaPlayer.start();
                }
            });
            mConnectingMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will clear the running call once receive the call end event
     *
     * @param endReson
     */
    private void releaseCall(String endReson) {
        mCallStatusTv.setText(endReson);
        stopConnectingTone();
        stopRingBackTone();
        playCallEndTone();
        mHoldImg.setEnabled(false);
        mAudioMuteImg.setEnabled(false);
        mAudioSpeakerImg.setEnabled(false);
        mAudioEndCallImg.setEnabled(false);
        mDTMFImg.setEnabled(false);
        mTimerHandler.removeCallbacks(mTimerRunnable);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showStreamStoppedAlert(endReson);
            }
        }, 1000);
    }

    private void getProfilePicture() {
        String nativecontactid = "";
        Cursor cur = CSDataProvider.getContactCursorByNumber(mDestinationNumberToCall);
        if (cur.getCount() > 0) {
            cur.moveToNext();
            nativecontactid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
        }
        cur.close();

        String picid = "";
        Cursor cur1 = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, mDestinationNumberToCall);
        //LOG.info("Yes count:"+cur1.getCount());
        if (cur1.getCount() > 0) {
            cur1.moveToNext();
            picid = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
        }
        cur1.close();

        Log.i(TAG, "Profile Pic ID " + picid + " file path " + CSDataProvider.getImageFilePath(picid));
        Bitmap bitmap = CSDataProvider.getImageBitmap(picid);
        if (bitmap != null)
            mContactAvatarImg.setImageBitmap(bitmap);
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
                    mContactAvatarImg.setImageBitmap(loadedImage);
                } else {
                    new ImageDownloaderTask(mContactAvatarImg).execute("app", finalPicid, finalNativecontactid);
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
}
