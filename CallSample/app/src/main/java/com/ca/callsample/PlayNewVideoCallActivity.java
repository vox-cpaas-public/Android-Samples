package com.ca.callsample;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.utils.utils;
import com.ca.views.CSPercentFrameLayout;
import com.ca.views.CSSurfaceViewRenderer;
import com.ca.wrapper.CSCall;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;

public class PlayNewVideoCallActivity extends Activity {
    private String mDestinationNumberToCall = "";
    private CSClient CSClientObj = new CSClient();
    private Runnable mRingBackRunnable;
    private long callDate = 0;
    private final Handler mTimerHandler = new Handler();
    private MediaPlayer mMediaPlayer;
    private int mTimerDelay = 1000;
    private boolean showNotification = true;
    private CSSurfaceViewRenderer localRender;
    private CSSurfaceViewRenderer remoteRender;
    private boolean isAudioEnabled = false;
    private boolean isSpeakerEnabled = true;
    private final Handler mRingBackHandler = new Handler();
    private Runnable mTimerRunnable;
    private ImageButton mCameraSwitchImg, mVideoCallEndImg, mMuteImg, mSpeakerImg;
    private String myCallId = "";
    private int notificationId = 0;
    private String mContactName = "";
    private TextView mReconnectingTv, mCallInfoTv, mVideoCallStatusTv;
    private CSPercentFrameLayout localRenderLayout;
    private CSPercentFrameLayout remoteRenderLayout;
    private CSCall CSCallObj = new CSCall();
    private MediaPlayer mConnectingMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            com.ca.utils.Constants.incallcount = com.ca.utils.Constants.incallcount + 1;
            com.ca.utils.Constants.answeredcallcount = com.ca.utils.Constants.answeredcallcount + 1;
            callDate = CSClientObj.getTime();
            showNotification = true;
            mReconnectingTv = findViewById(R.id.video_call_reconnecting_tv);
            mCallInfoTv = findViewById(R.id.video_call_caller_info_tv);
            mVideoCallStatusTv = findViewById(R.id.video_call_status_tv);
            localRender = findViewById(R.id.local_video_view);
            remoteRender = findViewById(R.id.remote_video_view);
            localRenderLayout = findViewById(R.id.local_video_layout);
            remoteRenderLayout = findViewById(R.id.remote_video_layout);
            mSpeakerImg = findViewById(R.id.video_call_speaker_img);
            mMuteImg = findViewById(R.id.video_call_mute_img);
            mCameraSwitchImg = findViewById(R.id.camera_switch_img);
            mVideoCallEndImg = findViewById(R.id.video_call_end_img);
            mReconnectingTv.setVisibility(View.GONE);
            mTimerRunnable = new Runnable() {
                int i = 0;

                public void run() {
                    mTimerHandler.postDelayed(this, mTimerDelay);
                    PlayNewVideoCallActivity.this.runOnUiThread(new Runnable() {
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
                            mVideoCallStatusTv.setText(minutes + ":" + seconds);
                        }
                    });
                }
            };
            try {
                IntentFilter filter1 = new IntentFilter(CSEvents.CSCALL_CALLENDED);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);
                if (getIntent().getBooleanExtra("isinitiatior", false)) {
                    playConnectingTone();
                    mDestinationNumberToCall = getIntent().getStringExtra("dstnumber");
                    myCallId = CSCallObj.startVideoCall(mDestinationNumberToCall, localRender, remoteRender,CSConstants.CALLRECORD.DONTRECORD);
                } else {
                    mVideoCallStatusTv.setText("Connecting..");
                    mDestinationNumberToCall = getIntent().getStringExtra("srcnumber");
                    mTimerHandler.postDelayed(mTimerRunnable, mTimerDelay);
                    myCallId = getIntent().getStringExtra("callid");
                    CSCallObj.answerVideoCall(mDestinationNumberToCall, getIntent().getStringExtra("callid"), localRender, remoteRender);
                }
                com.ca.utils.Constants.lastcallid = myCallId;
                String displayName = "UnKnown";
                Cursor ccfr = CSDataProvider.getContactCursorByNumber(mDestinationNumberToCall);
                if (ccfr.getCount() > 0) {
                    ccfr.moveToNext();
                    displayName = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
                }
                ccfr.close();
                displayName = displayName + "\n" + mDestinationNumberToCall;
                mCallInfoTv.setText(displayName);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // camera switch image click listener
        mCameraSwitchImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!myCallId.equals("")) {
                        // this API will toggle the device camera
                        CSCallObj.toggleCamera(myCallId);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        // initially speaker should be in ON state
        mSpeakerImg.setBackgroundResource(R.drawable.call_bg_selector_prpl);
        // Speaker image click listener
        mSpeakerImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!myCallId.equals("")) {
                        // based on speaker state we need to enable and disable speaker and update UI correspondingly
                        if (isSpeakerEnabled) {
                            isSpeakerEnabled = false;
                            mSpeakerImg.setBackgroundResource(R.drawable.call_bg_selector);
                            // this API will disable speaker
                            CSCallObj.enableSpeaker(myCallId, false);
                        } else {
                            isSpeakerEnabled = true;
                            mSpeakerImg.setBackgroundResource(R.drawable.call_bg_selector_prpl);
                            // This API will enable speaker
                            CSCallObj.enableSpeaker(myCallId, true);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        // Mute image click listener
        mMuteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!myCallId.equals("")) {
                        // based on previous mute state need to enable/disable mute using SDK API's and update UI accordingly
                        if (isAudioEnabled) {
                            // This API will disable the Audio Mute
                            CSCallObj.muteAudio(myCallId, false);
                            isAudioEnabled = false;
                            mMuteImg.setBackgroundResource(R.drawable.call_bg_selector);
                        } else {
                            // This API will enable the Audio Mute
                            CSCallObj.muteAudio(myCallId, true);
                            isAudioEnabled = true;
                            mMuteImg.setBackgroundResource(R.drawable.call_bg_selector_prpl);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        // EndCall image click listener
        mVideoCallEndImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    showNotification = false;
                    stopConnectingTone();
                    stopRingBackTone();
                    // This API will end the Running call
                    CSCallObj.endVideoCall(mDestinationNumberToCall, myCallId, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
                    // after API call need to close the Video call screen below method have that logic
                    endTheCall();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * This BroadCast Receiver will handle all the SDK events
     */
    public class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (myCallId.equals(intent.getStringExtra("callid"))) {
                    if (intent.getAction().equals(CSEvents.CSCALL_CALLENDED)) {
                        showNotification = false;
                        if (intent.getStringExtra("endReason").equals(com.ca.Utils.CSConstants.UserBusy)) {
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
                        mVideoCallStatusTv.setText("Connecting..");
                        mTimerHandler.postDelayed(mTimerRunnable, mTimerDelay);
                    } else if (intent.getAction().equals(CSEvents.CSCALL_NOMEDIA)) {
                        showNotification = false;
                        releaseCall("NoMedia");
                    } else if (intent.getAction().equals(CSEvents.CSCALL_RINGING)) {
                        mVideoCallStatusTv.setText("Ringing");
                        stopConnectingTone();
                        playRingBackTone();
                    } else if (intent.getAction().equals(CSEvents.CSCALL_MEDIACONNECTED)) {
                        stopConnectingTone();
                        stopRingBackTone();
                        mReconnectingTv.setVisibility(View.GONE);
                    } else if (intent.getAction().equals(CSEvents.CSCALL_MEDIADISCONNECTED)) {
                        mReconnectingTv.setVisibility(View.VISIBLE);
                    } else if (intent.getAction().equals("TerminateForSecondCall")) {
                        mVideoCallEndImg.performClick();
                    }
                }
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                   stopConnectingTone();
                    stopRingBackTone();
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

            //Register the SDK receivers with Video call events
            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter3 = new IntentFilter(CSEvents.CSCALL_NOANSWER);
            IntentFilter filter4 = new IntentFilter(CSEvents.CSCALL_CALLANSWERED);
            IntentFilter filter5 = new IntentFilter(CSEvents.CSCALL_NOMEDIA);
            IntentFilter filter6 = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter7 = new IntentFilter(CSEvents.CSCALL_RINGING);
            IntentFilter filter8 = new IntentFilter(CSEvents.CSCALL_MEDIACONNECTED);
            IntentFilter filter9 = new IntentFilter(CSEvents.CSCALL_MEDIADISCONNECTED);
            IntentFilter filter10 = new IntentFilter("TerminateForSecondCall");
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter3);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter4);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter5);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter6);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter7);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter8);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter9);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter10);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // this will close the video call running notification  once App is in foreground
            notificationManager.cancel(notificationId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // below logic will show the Video call running notification if application went to background state
        if (showNotification) {
            notificationId = utils.notifycallinprogress(getApplicationContext(), mContactName, "Video call is in progress", "", "", 0);
        }
    }


    @Override
    public void onBackPressed() {
        try {
            // below method will handle the backPress event
            showStreamStopAlert();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method will close the audio call screen when got callEnd event from SDK
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
            endTheCall();
            return false;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * This method will show popup dialog to user
     *
     * @return
     */
    public boolean showStreamStopAlert() {
        try {
            if (!isFinishing()) {
                android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(PlayNewVideoCallActivity.this);
                successfullyLogin.setTitle(getString(R.string.user_alert_dialog_tittle));
                successfullyLogin.setCancelable(false);
                successfullyLogin.setMessage("End Call?");
                successfullyLogin.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                try {
                                    showNotification = false;
                                    // if call not confirmed stop the ringBackTone
                                  stopConnectingTone();
                                    stopRingBackTone();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                // This API will end the Running call once user confirmed to go with back button press
                                CSCallObj.endVideoCall(mDestinationNumberToCall, myCallId, com.ca.Utils.CSConstants.E_UserTerminated, com.ca.Utils.CSConstants.UserTerminated);
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
     * This method will close the VideoCall screen once got Call end event from SDK or user ends the call him self
     * This Method will reset the all call related values to default values
     */
    public void endTheCall() {
        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            mTimerHandler.removeCallbacks(mTimerRunnable);
            stopConnectingTone();
            stopRingBackTone();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
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
            ex.printStackTrace();
        }
    }

    /**
     * This method will play the ringBackTone once got Ring event from SDK
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
            mRingBackRunnable = new Runnable() {
                public void run() {
                    int resID = R.raw.standard_6_ringback;
                    mMediaPlayer = MediaPlayer.create(PlayNewVideoCallActivity.this, resID);
                    if (mMediaPlayer != null) {
                        mMediaPlayer.setLooping(true);
                        mMediaPlayer.start();
                    }
                }
            };
            mRingBackHandler.postDelayed(mRingBackRunnable, 2000);
        }
    }

    /**
     * This method will stop the ringBackTone if it plays
     */
    public void stopRingBackTone() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            mRingBackHandler.removeCallbacks(mRingBackRunnable);
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
        mVideoCallStatusTv.setText(endReson);
        stopConnectingTone();
        stopRingBackTone();
        playCallEndTone();
        mMuteImg.setEnabled(false);
        mSpeakerImg.setEnabled(false);
        mVideoCallEndImg.setEnabled(false);
        mCameraSwitchImg.setEnabled(false);
        mTimerHandler.removeCallbacks(mTimerRunnable);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showStreamStoppedAlert(endReson);
            }
        }, 1000);
    }
}
