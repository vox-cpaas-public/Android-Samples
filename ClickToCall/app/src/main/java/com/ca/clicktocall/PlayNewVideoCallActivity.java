package com.ca.clicktocall;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSEvents;
import com.ca.views.CSPercentFrameLayout;
import com.ca.views.CSSurfaceViewRenderer;
import com.ca.wrapper.CSCall;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;
import com.ca.Utils.CSDbFields;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


//import com.cavox.uiutils.UIActions;

public class PlayNewVideoCallActivity extends Activity {
    boolean mediaconnectedatleastonce = false;
    boolean mediadisconnectionshown = false;
    boolean isnotificationalreadyshown = false;
    String destinationnumbettocall = "";
    CSClient CSClientObj = new CSClient();
    Runnable ringbackRunnableObj;
    long callDate = 0;
    boolean showtimer = false;
    final Handler h = new Handler();
    final Handler h1 = new Handler();
    MediaPlayer mp;
    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    int delay = 1000;
    boolean shownotifiction = true;
    CSSurfaceViewRenderer localRender;
    CSSurfaceViewRenderer remoteRender;
    String r_sdp = "";
    String r_streamid = "";
    String r_number = "";
    String custom_candidate = null;
    String localsdp = "";
    //List<String> candidates = new ArrayList<String>();
    LinkedList<String> candidates = new LinkedList<String>();
    String l_sdp = "";
    String l_streamid = "";

    String streamidforchat = "";
    ManageAudioFocus manageAudioFocus = new ManageAudioFocus();
    boolean isaudioenabled = true;
    boolean isvideoenabled = true;
    boolean ischatenabled = true;
    boolean isspeakerenabled = true;
    final Handler handler = new Handler();
    Runnable RunnableObj;
    ImageButton togglecamera;
    ImageButton sharestream;
    ImageButton closestream;
    ImageView holdbutton;
    ImageView send;
    TextView viewercount;
    String mycallid = "";
    ImageButton muteButton;
    //ImageButton chatButton;
    ImageButton speakerButton;
    View sep_hr;
    EditText commentbox;
    LinearLayout commentboxlayout;
    static ListView comments;
    boolean isholdenabled = true;
    int notificationid = 0;
    String displayname = "UnKnown";
    public static boolean audiomutestateirrespectivehold = false;
    public static boolean videomutestateirrespectivehold = false;
    boolean iceConnected = true;
    private static final int STAT_CALLBACK_PERIOD = 1000;
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;
    TextView reconnecting;
    TextView callednumber;
    TextView timer;
    CSPercentFrameLayout localRenderLayout;
    CSPercentFrameLayout remoteRenderLayout;
    CSCall CSCallsObj = new CSCall();
    private MediaPlayer mConnectingMediaPlayer;
    private TextView mContactNumberTv;
    private boolean isMediaConnected = false;
    
    float unitvolume = 0;// =(float)(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))/100;
    float currentvolumeinfloat  = 0;//= (float)(((float)(am.getStreamVolume(AudioManager.STREAM_MUSIC)/am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)))*100);
    AudioManager am;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        try {
            //LOG.info("RenderLayout: oncreate");
            //mPreferenceProvider = new PreferenceProvider(getApplicationContext());
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                  callDate = CSClientObj.getTime();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            shownotifiction = true;
            reconnecting = (TextView) findViewById(R.id.textView3);
            callednumber = (TextView) findViewById(R.id.textView1);
            mContactNumberTv = findViewById(R.id.contact_number_tv);
            timer = (TextView) findViewById(R.id.textView2);

            localRender = (CSSurfaceViewRenderer) findViewById(R.id.local_video_view);
            remoteRender = (CSSurfaceViewRenderer) findViewById(R.id.remote_video_view);
            localRenderLayout = (CSPercentFrameLayout) findViewById(R.id.local_video_layout);
            remoteRenderLayout = (CSPercentFrameLayout) findViewById(R.id.remote_video_layout);
            speakerButton = (ImageButton) findViewById(R.id.speakerButton);
            muteButton = (ImageButton) findViewById(R.id.muteButton);
            //chatButton = (ImageButton) findViewById(R.id.chatButton);
            sep_hr = findViewById(R.id.sep_hr);
            commentboxlayout = (LinearLayout) findViewById(R.id.commentboxlayout);
            holdbutton = (ImageView) findViewById(R.id.hold);
            togglecamera = (ImageButton) findViewById(R.id.imageButton1);
            sharestream = (ImageButton) findViewById(R.id.shareButton);
            closestream = (ImageButton) findViewById(R.id.closeButton);
            send = (ImageView) findViewById(R.id.send);
            viewercount = (TextView) findViewById(R.id.viewercount);
            commentbox = (EditText) findViewById(R.id.editText1);
            commentbox.setText("");
            commentbox.setLongClickable(false);
            comments = (ListView) findViewById(R.id.comments);
            //comments.setAdapter(ChannelCommentsAdapterObj);
            //comments.setSelection(ChannelCommentsAdapterObj.getCount()-1);
            //sharestream.setVisibility(View.INVISIBLE);
            //chatButton.setVisibility(View.INVISIBLE);

            am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            unitvolume =(float)(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))/100;
            float currentvolumepercent = (float)(((float)(am.getStreamVolume(AudioManager.STREAM_MUSIC)/am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)))*100);
            currentvolumeinfloat =(float)(currentvolumepercent)/100;


            commentboxlayout.setVisibility(View.INVISIBLE);
            comments.setVisibility(View.INVISIBLE);
            reconnecting.setVisibility(View.GONE);
            callednumber.setVisibility(View.GONE);
/*
			if(getIntent().getBooleanExtra("isinitiatior", false)) {
				destinationnumbettocall = getIntent().getStringExtra("dstnumber");
				localRender.init(rootEglBase.getEglBaseContext(), null);
				remoteRender.init(rootEglBase.getEglBaseContext(), null);
				localRender.setZOrderMediaOverlay(true);
				updateVideoView();

			} else {
				destinationnumbettocall = getIntent().getStringExtra("srcnumber");
				localRender = (SurfaceViewRenderer) findViewById(R.id.remote_video_view);
				remoteRender = (SurfaceViewRenderer) findViewById(R.id.local_video_view);
				localRenderLayout = (PercentFrameLayout) findViewById(R.id.remote_video_layout);
				remoteRenderLayout = (PercentFrameLayout) findViewById(R.id.local_video_layout);


				localRender.init(rootEglBase.getEglBaseContext(), null);
				remoteRender.init(rootEglBase.getEglBaseContext(), null);
				//togglecamera.setVisibility(View.INVISIBLE);

				localRender.setZOrderMediaOverlay(true);
				updateVideoView();
			}
			*/

            RunnableObj = new Runnable() {
                int i = 0;

                public void run() {
                    h.postDelayed(this, delay);
                    ////LOG.info("printing at 1 sec");
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

                            timer.setText(minutes + ":" + seconds);
                        }
                    });
                }
            };

            try {

                IntentFilter filter1 = new IntentFilter(CSEvents.CSCALL_CALLENDED);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);

                if (getIntent().getBooleanExtra("isinitiatior", false)) {
                    playConnectingTone();
                    destinationnumbettocall = getIntent().getStringExtra("dstnumber");
                    mycallid = CSCallsObj.startVideoCall(destinationnumbettocall, localRender, remoteRender,ClickToCallConstants.callrecord);
                    manageAudioFocus.requestAudioFocus(getApplicationContext(),mycallid, destinationnumbettocall,"video");
                } else {
                    timer.setText("Connecting..");
                    destinationnumbettocall = getIntent().getStringExtra("srcnumber");
                    //h.postDelayed(RunnableObj, delay); ////tmp
                    mycallid = getIntent().getStringExtra("callid");
                    //String sdp = getIntent().getStringExtra("sdp");
                    CSCallsObj.answerVideoCall(destinationnumbettocall, getIntent().getStringExtra("callid"), localRender, remoteRender);

                }


                mContactNumberTv.setText(destinationnumbettocall);
                Cursor ccfr = CSDataProvider.getContactCursorByNumber(destinationnumbettocall);
                if (ccfr.getCount() > 0) {
                    ccfr.moveToNext();
                    displayname = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
                }
                ccfr.close();
                callednumber.setText(displayname);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
/*
            String notoficationName = "";
            if (displayname.equals("UnKnown")) {
                notoficationName = destinationnumbettocall;
            } else {
                notoficationName = displayname;
            }
            notificationid = utils.notifycallinprogress(getApplicationContext(), notoficationName, "Video call is in progress", "", "", 0);
       */

        } catch (Exception ex) {
        }

        togglecamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!mycallid.equals("")) {
                        CSCallsObj.toggleCamera(mycallid);
                    } else {
                        //LOG.info("callid is empty to toggle camera in video call");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        speakerButton.setBackgroundResource(R.drawable.call_bg_selector_prpl);
        speakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!mycallid.equals("")) {
                        if (isspeakerenabled) {
                            isspeakerenabled = false;
                            speakerButton.setBackgroundResource(R.drawable.call_bg_selector);
                            CSCallsObj.enableSpeaker(mycallid, false);
                        } else {

                            isspeakerenabled = true;
                            speakerButton.setBackgroundResource(R.drawable.call_bg_selector_prpl);
                            CSCallsObj.enableSpeaker(mycallid, true);
                        }
                    } else {
                        //LOG.info("callid is empty to toggle speaker in video call");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!mycallid.equals("")) {
                        if (isaudioenabled) {
                            CSCallsObj.muteAudio(mycallid, true);
                            audiomutestateirrespectivehold = true;
                            isaudioenabled = false;
                            muteButton.setBackgroundResource(R.drawable.call_bg_selector_prpl);
                            //muteButton.setBackgroundColor(Color.parseColor("#1A000000"));
                            //CSCallsObj.enableOrDisableSpeakerInVideoCall(false);
                        } else {
                            CSCallsObj.muteAudio(mycallid, false);
                            audiomutestateirrespectivehold = false;
                            isaudioenabled = true;
                            muteButton.setBackgroundResource(R.drawable.call_bg_selector);
                            //muteButton.setBackgroundColor(Color.parseColor("#4E3164"));
                            //CSCallsObj.enableOrDisableSpeakerInVideoCall(true);
                        }
                    } else {
                        //LOG.info("callid is empty to toggle mute in video call");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        holdbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    if (!mycallid.equals("")) {

                        if (isholdenabled) {
                            ClickToCallConstants.isintentionalhold = true;
                            CSCallsObj.holdAVCall(destinationnumbettocall, mycallid, true);
                            //isholdenabled = false;
                            //holdbutton.setBackgroundResource(R.drawable.ui_hold_hover);
                        } else {
                            ClickToCallConstants.isintentionalhold = false;
                            CSCallsObj.holdAVCall(destinationnumbettocall, mycallid, false);

                            //isholdenabled = true;
                            //holdbutton.setBackgroundResource(R.drawable.ui_hold_normal);
                        }

                    } else {
                        //LOG.info("callid is empty to toggle hold in video call");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        sharestream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!mycallid.equals("")) {
                        if (isvideoenabled) {
                            CSCallsObj.muteVideo(mycallid, true);
                            videomutestateirrespectivehold = true;
                            isvideoenabled = false;
                            sharestream.setBackgroundResource(R.drawable.call_bg_selector_prpl);
                            //muteButton.setBackgroundColor(Color.parseColor("#1A000000"));
                            //CSCallsObj.enableOrDisableSpeakerInVideoCall(false);
                        } else {
                            CSCallsObj.muteVideo(mycallid, false);
                            videomutestateirrespectivehold = false;
                            isvideoenabled = true;
                            sharestream.setBackgroundResource(R.drawable.call_bg_selector);
                            //muteButton.setBackgroundColor(Color.parseColor("#4E3164"));
                            //CSCallsObj.enableOrDisableSpeakerInVideoCall(true);
                        }
                    } else {
                        //LOG.info("callid is empty to toggle mute in video call");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        closestream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    shownotifiction = false;
                    //onBackPressed();
                    try {
                        stopConnectingTone();
                        stopringbacktone();
                        CSCallsObj.endVideoCall(destinationnumbettocall, mycallid, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
                        endTheCall();
                    } catch (Exception ex) {
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


    }


    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //LOG.info("Yes Something receieved in video call screen:" + intent.getAction().toString());
                //LOG.info("Yes Something receieved in video call screen callid:" + intent.getStringExtra("callid"));
                //LOG.info("Yes Something receieved in video call screen mycallid:" + mycallid);
                if (mycallid.equals(intent.getStringExtra("callid"))) {
                    if (intent.getAction().equals(CSEvents.CSCALL_CALLENDED)) {
                        //  playCallEndTone();
                        shownotifiction = false;
                        if (intent.getStringExtra("endReason").equals(CSConstants.UserBusy)) {
                            // showStreamStoppedAlert("callEnded! UserBusy!");
                            releaseCall("UserBusy");
                        } else {
                            //   showStreamStoppedAlert("callEnded");
                            releaseCall("Call Ended");
                        }
                    } else if (intent.getAction().equals(CSEvents.CSCALL_NOANSWER)) {
                        //playCallEndTone();
                        shownotifiction = false;
                        //   showStreamStoppedAlert("NoAnswer");
                        releaseCall("NoAnswer");
                    } else if (intent.getAction().equals(CSEvents.CSCALL_CALLANSWERED)) {
                        //LOG.info("test CSCALL_CALLANSWERED receieved");
                        timer.setText("Connecting..");
                        stopConnectingTone();
                        stopringbacktone(); ////tmp
                        //h.postDelayed(RunnableObj, delay); ////tmp
                    } else if (intent.getAction().equals(CSEvents.CSCALL_NOMEDIA)) {
                        // playCallEndTone();
                        shownotifiction = false;
                        // showStreamStoppedAlert("NoMedia! CallEnded!");
                        releaseCall("NoMedia");
                    } else if (intent.getAction().equals(CSEvents.CSCALL_RINGING)) {
                        //LOG.info("test ringing receieved");
                        boolean iscallwaiting = intent.getBooleanExtra("iscallwaiting", false);
                        if (iscallwaiting) {
                            timer.setText("call waiting");
                        } else {
                            timer.setText("Ringing");
                        }
                        stopConnectingTone();
                        playringbacktone(iscallwaiting);
                    } else if (intent.getAction().equals(CSEvents.CSCALL_MEDIACONNECTED)) {
                        mediaconnectedatleastonce = true;
                        //LOG.info("test MediaConnected receieved");
                        isMediaConnected = true;
                        if (!showtimer) {
                            showtimer = true;
                            stopConnectingTone();
                            stopringbacktone();
                            h.postDelayed(RunnableObj, delay);
                        }
                        stopConnectingTone();
                        stopringbacktone();
                        reconnecting.setVisibility(View.GONE);
                    } else if (intent.getAction().equals(CSEvents.CSCALL_MEDIADISCONNECTED)) {
                        //LOG.info("test MediaDisConnected receieved");
                        reconnecting.setVisibility(View.VISIBLE);
                    }
                    else if(mycallid.equals(intent.getStringExtra("callid"))&&intent.getAction().equals(CSEvents.CSCALL_RECORDING_STATUS)) {
                        //Toast.makeText(getApplicationContext(), "Call is being recorded status:"+intent.getIntExtra("status",0) , Toast.LENGTH_SHORT).show();
                    }
                    else if(mycallid.equals(intent.getStringExtra("callid"))&&intent.getAction().equals(CSEvents.CSCALL_RECORDING_AT_SERVER)) {
                        //Toast.makeText(getApplicationContext(), "Call is being recorded at server", Toast.LENGTH_SHORT).show();
                    }
                    else if (intent.getAction().equals("TerminateForSecondCall")) {
                        playCallEndTone();
                        //LOG.info("test TerminateForSecondCall receieved");
                        closestream.performClick();
                    } else if (intent.getAction().equals(CSEvents.CSCALL_HOLD_UNHOLD_RESPONSE)) {

                        //if(intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        if (mycallid.equals(intent.getStringExtra("callid"))) {


                            //Toast.makeText(getApplicationContext(), "HoldUnhold Success", Toast.LENGTH_SHORT).show();
                            boolean ishold = intent.getBooleanExtra("hold", false);

                            if (ishold) {
                                isholdenabled = false;
                                ClickToCallConstants.iSHoldCalled = true;
                                boolean isremotehold = intent.getBooleanExtra("isremotehold", false);
                                if (isremotehold) {
                                    holdbutton.setBackgroundResource(R.drawable.ui_hold_r_hover);
                                } else {
                                    holdbutton.setBackgroundResource(R.drawable.ui_hold_hover);
                                }

                                //muteButton.performClick();
                                //sharestream.performClick();


                                //isaudioenabled = false;
                                //muteButton.setBackgroundResource(R.drawable.call_bg_selector_prpl);
                                muteButton.setEnabled(false);

                                //isvideoenabled = false;
                                //sharestream.setBackgroundResource(R.drawable.call_bg_selector_prpl);
                                sharestream.setEnabled(false);

                            } else {
                                ClickToCallConstants.iSHoldCalled = false;
                                isholdenabled = true;
                                //holdbutton.setBackgroundResource(R.drawable.ui_hold_normal1);
                                holdbutton.setBackgroundResource(R.drawable.call_bg_selector);

                                //isaudioenabled = true;
                                //muteButton.setBackgroundResource(R.drawable.call_bg_selector);
                                muteButton.setEnabled(true);

                                //isvideoenabled = true;
                                //sharestream.setBackgroundResource(R.drawable.call_bg_selector);
                                sharestream.setEnabled(true);
                            }

                            if (audiomutestateirrespectivehold) {
                                //CSCallsObj.muteAudio(mycallid,true);
                                isaudioenabled = false;
                                muteButton.setBackgroundResource(R.drawable.ui_mute_hover);
                            }

                            if (videomutestateirrespectivehold) {
                                //CSCallsObj.muteVideo(mycallid,true);
                                isvideoenabled = false;
                                sharestream.setBackgroundResource(R.drawable.call_bg_selector_prpl);
                            }

                        } else {
                            //Toast.makeText(getApplicationContext(), "HoldUnhold Failure", Toast.LENGTH_SHORT).show();
                        }
                    }


                }

                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    stopringbacktone();
                    stopConnectingTone();
                    if(mediaconnectedatleastonce&&!Utils.isNetworkAvailable(PlayNewVideoCallActivity.this)) {
                        reconnecting.setVisibility(View.VISIBLE);
                        mediadisconnectionshown = true;
                    }
                } else if(intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    if (mediadisconnectionshown && intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        reconnecting.setVisibility(View.GONE);
                        mediadisconnectionshown = false;
                    }

                }
            } catch (Exception ex) {
            }
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        super.onKeyUp(keyCode, event);
        try {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                //AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                if (am != null && mp != null && mp.isPlaying()) {
/*
                    //int ringervoulume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                    ////LOG.info("am ringervoulume up:" + ringervoulume);
                    //int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    //int currentVolumePercentage = 100 * currentVolume/maxVolume;
                    //am.
//mp.setVolume();
                    int ringervoulume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                    //LOG.info("am ringervoulume up:" + ringervoulume);
                    //LOG.info("am ringervoulume max:" + am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                    ////LOG.info("am ringervoulume down:" + am.getStreamMinVolume(AudioManager.STREAM_MUSIC));
                    //LOG.info("am ringervoulume current:" + am.getStreamVolume(AudioManager.STREAM_MUSIC));

                    float unitvolume =(float)(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))/100;

                    float currentvolumepercent = (float)(((float)(am.getStreamVolume(AudioManager.STREAM_MUSIC)/am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)))*100);
                    //LOG.info("am ringervoulume currentvolumepercent:" + currentvolumepercent);


                    float currentvolumeinfloat =(float)(currentvolumepercent)/100;
*/


                    //LOG.info("am ringervoulume unitvolume:" + unitvolume);
                    //LOG.info("am ringervoulume currentvolumeinfloat:" + currentvolumeinfloat);
                    mp.setVolume((float)(currentvolumeinfloat+unitvolume),(float)(currentvolumeinfloat+unitvolume));
                    //am.getStreamVolume(AudioManager.STREAM_MUSIC);
                    currentvolumeinfloat = currentvolumeinfloat+unitvolume;
                    if(currentvolumeinfloat>1) {
                        currentvolumeinfloat = 1;
                    } else if(currentvolumeinfloat<0) {
                        currentvolumeinfloat = 0;
                    }


                } else if (am != null && mConnectingMediaPlayer != null && mConnectingMediaPlayer.isPlaying()) {
                    //LOG.info("am ringervoulume unitvolume:" + unitvolume);
                    //LOG.info("am ringervoulume currentvolumeinfloat:" + currentvolumeinfloat);
                    mConnectingMediaPlayer.setVolume((float)(currentvolumeinfloat+unitvolume),(float)(currentvolumeinfloat+unitvolume));
                    //am.getStreamVolume(AudioManager.STREAM_MUSIC);
                    currentvolumeinfloat = currentvolumeinfloat+unitvolume;
                    if(currentvolumeinfloat>1) {
                        currentvolumeinfloat = 1;
                    } else if(currentvolumeinfloat<0) {
                        currentvolumeinfloat = 0;
                    }
                }



                //Toast.makeText(CallScreenActivity.this,"Up working",Toast.LENGTH_SHORT).show();
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);

        try {

            if(keyCode==KeyEvent.KEYCODE_BACK)   {
onBackPressed();
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                //AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                if (am != null && mp != null && mp.isPlaying()) {
                  /*
                   //LOG.info("am ringervoulume am.getMode():" + am.getMode());

                   int ringervoulume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                   //LOG.info("am ringervoulume up:" + ringervoulume);
                   //LOG.info("am ringervoulume max:" + am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                   ////LOG.info("am ringervoulume down:" + am.getStreamMinVolume(AudioManager.STREAM_MUSIC));
                   //LOG.info("am ringervoulume current:" + am.getStreamVolume(AudioManager.STREAM_MUSIC));
                   //am.getStreamVolume(AudioManager.STREAM_MUSIC);
                   float unitvolume =(float)(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))/100;
                   float currentvolumepercent = (float)(((float)(am.getStreamVolume(AudioManager.STREAM_MUSIC)/am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)))*100);
                   //LOG.info("am ringervoulume currentvolumepercent:" + currentvolumepercent);

                   float currentvolumeinfloat =(float)(currentvolumepercent)/100;
*/
                    //LOG.info("am ringervoulume unitvolume:" + unitvolume);
                    //LOG.info("am ringervoulume currentvolumeinfloat:" + currentvolumeinfloat);
                    mp.setVolume((float)(currentvolumeinfloat-unitvolume),(float)(currentvolumeinfloat-unitvolume));
                    currentvolumeinfloat = currentvolumeinfloat-unitvolume;
                    if(currentvolumeinfloat>1) {
                        currentvolumeinfloat = 1;
                    } else if(currentvolumeinfloat<0) {
                        currentvolumeinfloat = 0;
                    }
                } else if (am != null && mConnectingMediaPlayer != null && mConnectingMediaPlayer.isPlaying()) {
                    //LOG.info("am ringervoulume unitvolume:" + unitvolume);
                    //LOG.info("am ringervoulume currentvolumeinfloat:" + currentvolumeinfloat);
                    mConnectingMediaPlayer.setVolume((float)(currentvolumeinfloat-unitvolume),(float)(currentvolumeinfloat-unitvolume));
                    currentvolumeinfloat = currentvolumeinfloat-unitvolume;
                    if(currentvolumeinfloat>1) {
                        currentvolumeinfloat = 1;
                    } else if(currentvolumeinfloat<0) {
                        currentvolumeinfloat = 0;
                    }
                }
                //Toast.makeText(CallScreenActivity.this,"Down working", Toast.LENGTH_SHORT).show();
                return true;
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }


    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    public void onResume() {
        super.onResume();

        try {

            shownotifiction = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(CSEvents.CSCALL_NOANSWER);
            filter.addAction(CSEvents.CSCALL_CALLANSWERED);
            filter.addAction(CSEvents.CSCALL_NOMEDIA);
            filter.addAction(CSEvents.CSCLIENT_NETWORKERROR);
            filter.addAction(CSEvents.CSCALL_RINGING);
            filter.addAction(CSEvents.CSCALL_MEDIACONNECTED);
            filter.addAction(CSEvents.CSCALL_MEDIADISCONNECTED);
            filter.addAction("TerminateForSecondCall");
            filter.addAction(CSEvents.CSCALL_HOLD_UNHOLD_RESPONSE);
            filter.addAction(CSEvents.CSCALL_RECORDING_AT_SERVER);
            filter.addAction(CSEvents.CSCALL_RECORDING_STATUS);
            filter.addAction(CSEvents.CSCLIENT_LOGIN_RESPONSE);

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter);

            if (isMediaConnected) {
                CSCallsObj.muteVideo(mycallid, false);
            }

            if(!isnotificationalreadyshown) {
            String notoficationName = "";
            if (displayname.equals("UnKnown")) {
                notoficationName = destinationnumbettocall;
            } else {
                notoficationName = displayname;
            }
            notificationid = Utils.notifycallinprogress(getApplicationContext(), getIntent().getStringExtra("dstnumber"), "Video call is in progress");
            isnotificationalreadyshown = true;
            }



        } catch (Exception ex) {
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (shownotifiction) {
            //GlobalVariables.hold = false;
        }
        if (isMediaConnected) {
            CSCallsObj.muteVideo(mycallid, true);
        }
        //LOG.info("yes on pause called:" + notificationid);
        //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.cancel(notificationid);
        //Intent intent = new Intent(getApplicationContext(), EmptyActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(intent);

    }

    @Override
    protected void onDestroy() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationid);
        stopConnectingTone();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.cancel(notificationid);
        ////LOG.info("yes onStop called:"+notificationid);
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        try {
            System.out.println("connectsdk playvideocall onBackPressed");
            showstreamstopalert();
        } catch (Exception ex) {
        }
        //return;
    }


    public boolean showStreamStoppedAlert(final String message) {
        try {
            try {
                stopringbacktone();
            } catch (Exception ex) {
            }
            endTheCall();
			/*
			if(!isFinishing()) {
				//LOG.info("Yes in alert..");
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						AlertDialog.Builder successfullyLogin = new AlertDialog.Builder(
								PlayNewVideoCallActivity.this);
						successfullyLogin.setCancelable(false);
						successfullyLogin.setMessage(message);

						successfullyLogin.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
														int which) {
									endTheCall();
									}
								});
						if(!isFinishing()) {
							successfullyLogin.show();
						}
					}
				});
			}*/
            return true;
        } catch (Exception ex) {
            return false;
        }

    }


    public boolean showNativeCameraError(final String message) {
        try {
            if (!isFinishing()) {
                //cleanimagesdbbyremovingcommontors();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        AlertDialog.Builder successfullyLogin = new AlertDialog.Builder(
                                PlayNewVideoCallActivity.this);
                        successfullyLogin.setCancelable(true);
                        successfullyLogin.setMessage(message);

                        successfullyLogin.setPositiveButton("Ok",
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
                });
            }
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

    public boolean showstreamstopalert() {
        try {
            if (!isFinishing()) {
                android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(PlayNewVideoCallActivity.this);
                successfullyLogin.setTitle("Confirmation");
                successfullyLogin.setCancelable(false);
                successfullyLogin.setMessage("End Call?");
                successfullyLogin.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                try {
                                    shownotifiction = false;
                                    stopConnectingTone();
                                    stopringbacktone();
                                } catch (Exception ex) {
                                }
                                CSCallsObj.endVideoCall(destinationnumbettocall, mycallid, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
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

    public void endTheCall() {
        try {

            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            h.removeCallbacks(RunnableObj);
            //Intent intent = new Intent(UIActions.VIDEOCALLDONE.getKey());
            //LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(intent);
            stopConnectingTone();
            stopringbacktone();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationid);
            //LOG.info("yes endcall called:" + notificationid);
            manageAudioFocus.abandonAudioFocus();
            finish();
        } catch (Exception ex) {

        }
    }

    public void playringbacktone(boolean iscallwaiting) {
        if (getIntent().getBooleanExtra("isinitiatior", false)) {
            try {
                if (mp != null) {
                    if (mp.isPlaying())
                        mp.stop();
                    mp.reset();
                    mp.release();
                    mp = null;
                }
            } catch (Exception ex) {
            }


            ringbackRunnableObj = new Runnable() {
                public void run() {
                    //handler.postDelayed(this, 2000);
                    int resID = R.raw.standard_6_ringback;
                    if (iscallwaiting) {
                        resID = R.raw.busy_remote_end;
                    } else {
                        resID = R.raw.standard_6_ringback;
                    }

                    mp = MediaPlayer.create(PlayNewVideoCallActivity.this, resID);
                    if (mp != null) {
                        mp.setLooping(true);
                        mp.start();
                    }

                }
            };
            handler.postDelayed(ringbackRunnableObj, 2000);

        }
    }

    public void stopringbacktone() {
        //if(getIntent().getBooleanExtra("isinitiatior", false)) {
        try {
            if (mp != null) {
                mp.stop();
                mp.reset();
                mp.release();
                mp = null;
            }
            handler.removeCallbacks(ringbackRunnableObj);
        } catch (Exception ex) {

        }
        //}
    }

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

    private void playCallEndTone() {
        try {
            mConnectingMediaPlayer = MediaPlayer.create(this, R.raw.call_end_tone);
            mConnectingMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mConnectingMediaPlayer != null)
                        mConnectingMediaPlayer.start();
                }
            });
            mConnectingMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseCall(String endReson) {
        timer.setText(endReson);
        stopConnectingTone();
        stopringbacktone();
        playCallEndTone();
        holdbutton.setEnabled(false);
        muteButton.setEnabled(false);
        speakerButton.setEnabled(false);
        sharestream.setEnabled(false);
        closestream.setEnabled(false);
        togglecamera.setEnabled(false);
        h.removeCallbacks(RunnableObj);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showStreamStoppedAlert(endReson);
            }
        }, 1000);
    }
}
