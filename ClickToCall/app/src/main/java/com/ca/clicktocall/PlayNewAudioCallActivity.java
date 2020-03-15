package com.ca.clicktocall;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import androidx.multidex.MultiDex;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.wrapper.CSCall;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Random;

public class PlayNewAudioCallActivity extends Activity implements SensorEventListener {
boolean mediaconnectedatleastonce = false;
    boolean mediadisconnectionshown = false;
    boolean isnotificationalreadyshown = false;
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private static final int SENSOR_SENSITIVITY = 4;
    boolean isholdenabled = true;
    boolean shownotifiction = true;
    boolean showtimer = false;
    private MediaPlayer mConnectingMediaPlayer;
    NotificationManager notificationManager;
    AlertDialog.Builder successfullyLogin;
    AlertDialog dismisssuccessfullyLogin;
    int notificationid = 0;
    int audiomodechangecount = 0;
    CSClient CSClientObj = new CSClient();
    long callDate = 0;
    final Handler h = new Handler();
    final Handler h1 = new Handler();
    Runnable RunnableObj;
    MediaPlayer mp;
    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    String mycallid = "";
    boolean isdtmfenabled = true;
    String displayname = "";
    //PeerConnectionParameters peerConnectionParameters;
    String destinationnumbettocall = "";
    //SignalingParameters signalingParameters;
    //PercentFrameLayout localRenderLayout;
    //PercentFrameLayout remoteRenderLayout;
    ImageView dtmfButton;
    String custom_candidate = null;
    String localsdp = "";
    LinkedList<String> candidates = new LinkedList<String>();
    //List<String> candidates = new ArrayList<String>();
    ManageAudioFocus manageAudioFocus = new ManageAudioFocus();

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
    ImageView EndaudiocallButton;
    int delay = 1000;
    ImageView holdbutton;
    ImageView muteButton;
    ImageView chatButton;
    boolean isaudioenabled = true;
    public static boolean audiomutestateirrespectivehold = false;
    boolean isspeakerenabled = false;

    CircularImageView avatharButton;

    RelativeLayout blankscreen;
    RelativeLayout mainscreen;
    CSCall CSCallsObj = new CSCall();
    private TextView mNumberTV;
    //AudioManager am;// = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    float unitvolume = 0;// =(float)(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))/100;
    float currentvolumeinfloat  = 0;//= (float)(((float)(am.getStreamVolume(AudioManager.STREAM_MUSIC)/am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)))*100);
    AudioManager am;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_call);
        try {
            //setVolumeControlStream(AudioManager.STREAM_MUSIC);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
           
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            //ClickToCallConstants.lastcalltype = "audio";
            shownotifiction = true;
            callDate = CSClientObj.getTime();
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            successfullyLogin = new AlertDialog.Builder(PlayNewAudioCallActivity.this);

            reconnecting = (TextView) findViewById(R.id.textView19);
            callednumber = (TextView) findViewById(R.id.textView17);
            mNumberTV = findViewById(R.id.contact_number_tv);
            callednumber.setSelected(true);
            mNumberTV.setSelected(true);
            timer = (TextView) findViewById(R.id.textView18);
            EndaudiocallButton = (ImageView) findViewById(R.id.callbutton);

            blankscreen = (RelativeLayout) findViewById(R.id.blankscreen);
            mainscreen = (RelativeLayout) findViewById(R.id.mainscreen);
            dtmfButton = (ImageView) findViewById(R.id.dialpad);
            muteButton = (ImageView) findViewById(R.id.mute);
            chatButton = (ImageView) findViewById(R.id.speaker);
            holdbutton = (ImageView) findViewById(R.id.view11);
            avatharButton = (CircularImageView) findViewById(R.id.avathar);
            reconnecting.setVisibility(View.GONE);
            callednumber.setVisibility(View.GONE);
            try {

                RunnableObj = new Runnable() {
                    int i = 0;

                    public void run() {
                        h.postDelayed(this, delay);
                        ////LOG.info("printing at 1 sec");
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
                                timer.setText(minutes + ":" + seconds);
                            }
                        });
                    }
                };

                am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                unitvolume =(float)(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))/100;
                float currentvolumepercent = (float)(((float)(am.getStreamVolume(AudioManager.STREAM_MUSIC)/am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)))*100);
                currentvolumeinfloat =(float)(currentvolumepercent)/100;





                IntentFilter filter1 = new IntentFilter(CSEvents.CSCALL_CALLENDED);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);

                if (getIntent().getBooleanExtra("isinitiatior", false)) {
                    playConnectingTone();
                    destinationnumbettocall = getIntent().getStringExtra("dstnumber");
                    mycallid = CSCallsObj.startVoiceCall(destinationnumbettocall,ClickToCallConstants.callrecord);
                    manageAudioFocus.requestAudioFocus(getApplicationContext(),mycallid, destinationnumbettocall,"audio");
                } else {
                    timer.setText("Connecting..");
                    destinationnumbettocall = getIntent().getStringExtra("srcnumber");
                    //h.postDelayed(RunnableObj, delay); ////tmp
                    mycallid = getIntent().getStringExtra("callid");

                    //String sdp = getIntent().getStringExtra("sdp");

                    CSCallsObj.answerVoiceCall(destinationnumbettocall, getIntent().getStringExtra("callid"));
                }
              
                String nativecontactid = "";
                Cursor cur = CSDataProvider.getContactCursorByNumber(destinationnumbettocall);
                if (cur.getCount() > 0) {
                    cur.moveToNext();
                    nativecontactid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
                }
                cur.close();

                String picid = "";
                Cursor cur1 = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, destinationnumbettocall);
                if (cur1.getCount() > 0) {
                    cur1.moveToNext();
                    picid = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                }
                cur1.close();


                new ImageDownloaderTask(avatharButton).execute("app", picid, nativecontactid);


                displayname = "UnKnown";
                mNumberTV.setText(destinationnumbettocall);
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
            notificationid = ClickToCallApi.notifycallinprogress(getApplicationContext(), notoficationName, "Audio call is in progress", "", "", 0);
*/
            EndaudiocallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        shownotifiction = false;
                        stopringbacktone();
                        stopConnectingTone();

                        CSCallsObj.endVoiceCall(destinationnumbettocall, mycallid, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
                        endTheCall();
						/*if(getIntent().getBooleanExtra("isinitiatior", false)) {

							CSCallsObj.endAudioCall("",mycallid);
							h.removeCallbacks(RunnableObj);
							finish();
							//CSPaymentsObj.directAudioVideoCallEndReq(destinationnumbettocall, 1, mycallid,CSClientObj.getTime());
						} else {
							CSCallsObj.endAudioCall(destinationnumbettocall,mycallid);
							h.removeCallbacks(RunnableObj);
							finish();
							//CSPaymentsObj.directAudioVideoCallEndReq(getIntent().getStringExtra("srcnumber"), 1, getIntent().getLongExtra("callid",0),CSClientObj.getTime());
						}
						*/

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            chatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (!mycallid.equals("")) {
                            if (isspeakerenabled) {
                                ////LOG.info("Came here2");
                                isspeakerenabled = false;
                                CSCallsObj.enableSpeaker(mycallid, false);
                                //audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
                                //chatButton.setBackgroundResource(R.drawable.call_bg_selector_prpl));
                                chatButton.setBackgroundResource(R.drawable.uispeaker_icon_norm);
                                //muteButton.setBackgroundColor(Color.parseColor("#1A000000"));

                            } else {
                                //LOG.info("Came here3");
                                isspeakerenabled = true;
                                CSCallsObj.enableSpeaker(mycallid, true);
                                //audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
                                //chatButton.setBackgroundResource(R.drawable.call_bg_selector));
                                ////LOG.info("Came here4");
                                chatButton.setBackgroundResource(R.drawable.uispeaker_icon_hover);
                                ////LOG.info("Came here5");
                                //muteButton.setBackgroundColor(Color.parseColor("#4E3164"));
                            }


                        } else {
                            //LOG.info("callid is empty to toggle speaker in audio call");
                        }


                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            dtmfButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {

                        if (isdtmfenabled) {
                            isdtmfenabled = false;
                            //dtmfButton.setBackgroundResource(R.drawable.call_bg_selector_prpl));
                            dtmfButton.setBackgroundResource(R.drawable.ui_keypad_hover);
                            //showdialpad();
                        } else {
                            isdtmfenabled = true;
                            //dtmfButton.setBackgroundColor(Color.parseColor("#00000000"));
                            dtmfButton.setBackgroundResource(R.drawable.ui_keypad_normal);
                            dismisssuccessfullyLogin.cancel();

                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            successfullyLogin.setOnCancelListener(new DialogInterface.OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    isdtmfenabled = true;
                    //dtmfButton.setBackgroundColor(Color.parseColor("#00000000"));
                    dtmfButton.setBackgroundResource(R.drawable.ui_keypad_normal);
                    dismisssuccessfullyLogin.cancel();
                }
            });
            muteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {

                        if (!mycallid.equals("")) {

                            //if (getIntent().getBooleanExtra("isinitiatior", false)) {
                            ////LOG.info("callid to mute or unmute:"+mycallid);

                            if (isaudioenabled) {
                                CSCallsObj.muteAudio(mycallid, true);
                                audiomutestateirrespectivehold = true;
                                //ClickToCallConstants.peerConnectionClient.setAudioEnabled(false);
                                isaudioenabled = false;
                                //muteButton.setBackgroundResource(R.drawable.call_bg_selector));
                                muteButton.setBackgroundResource(R.drawable.ui_mute_hover);
                                //muteButton.setBackgroundColor(Color.parseColor("#1A000000"));
                            } else {
                                CSCallsObj.muteAudio(mycallid, false);
                                audiomutestateirrespectivehold = false;
                                //ClickToCallConstants.peerConnectionClient.setAudioEnabled(true);
                                isaudioenabled = true;
                                //muteButton.setBackgroundResource(R.drawable.call_bg_selector_prpl));
                                muteButton.setBackgroundResource(R.drawable.ui_mute_normal);

                                //muteButton.setBackgroundColor(Color.parseColor("#4E3164"));
                            }
							/*} else {

								if (isaudioenabled) {
									setVolume(false);
									isaudioenabled = false;
									muteButton.setBackgroundResource(R.drawable.ui_mute_normal));
									//muteButton.setBackgroundColor(Color.parseColor("#1A000000"));

								} else {
									setVolume(true);
									ClickToCallConstants.peerConnectionClient.setAudioEnabled(true);
									isaudioenabled = true;
									muteButton.setBackgroundResource(R.drawable.ui_mute_hover));
									//muteButton.setBackgroundColor(Color.parseColor("#4E3164"));
								}
							}*/
                        } else {
                            //LOG.info("callid is empty to toggle mute in audio call");
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
                            //LOG.info("callid is empty to toggle hold in audio call");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });


        } catch (Exception ex) {

            ex.printStackTrace();

        }


    }



    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //LOG.info("Audio call callback:" + intent.getAction().toString());
                ////LOG.info("mycallid:"+mycallid);
                //LOG.info("Audio call callback:" + intent.getStringExtra("callid"));

                if (mycallid.equals(intent.getStringExtra("callid"))) {
                    if (intent.getAction().equals(CSEvents.CSCALL_CALLENDED)) {
                        //playCallEndTone();
                        shownotifiction = false;
                        if (intent.getStringExtra("endReason").toString().equals(CSConstants.UserBusy)) {
                            // showStreamStoppedAlert("callEnded! UserBusy!");
                            releaseCall("UserBusy");
                        } else {
                            //  showStreamStoppedAlert("callEnded");
                            releaseCall("Call Ended");
                        }

                    } else if (intent.getAction().equals(CSEvents.CSCALL_NOANSWER)) {
                        //  playCallEndTone();
                        shownotifiction = false;
                        // showStreamStoppedAlert("NoAnswer");
                        releaseCall("NoAnswer");

                    } else if (intent.getAction().equals(CSEvents.CSCALL_CALLANSWERED)) {
                        //LOG.info("test CSCALL_CALLANSWERED receieved");
                        stopringbacktone(); ////tmp
                        stopConnectingTone();
                        //h.postDelayed(RunnableObj, delay); ////tmp
                        timer.setText("Connecting..");
                    } else if (intent.getAction().equals(CSEvents.CSCALL_NOMEDIA)) {
                        //  playCallEndTone();
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
                        //LOG.info("test MediaConnected receieved");
                        mediaconnectedatleastonce = true;
                        if (!showtimer) {
                            showtimer = true;
                            stopringbacktone();
                            h.postDelayed(RunnableObj, delay);
                        }

                        reconnecting.setVisibility(View.GONE);
                        stopringbacktone();
                        stopConnectingTone();
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
                        //LOG.info("test TerminateForSecondCall receieved");
                        EndaudiocallButton.performClick();
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
                                //isaudioenabled = false;
                                //muteButton.setBackgroundResource(R.drawable.ui_mute_hover);
                                muteButton.setEnabled(false);

                            } else {
                                isholdenabled = true;
                                ClickToCallConstants.iSHoldCalled = false;
                                holdbutton.setBackgroundResource(R.drawable.ui_hold_normal);


                                //isaudioenabled = true;
                                //muteButton.setBackgroundResource(R.drawable.ui_mute_normal);
                                muteButton.setEnabled(true);

                                //LOG.info("audiomutestateirrespectivehold:" + audiomutestateirrespectivehold);
                                if (audiomutestateirrespectivehold) {
                                    //CSCallsObj.muteAudio(mycallid,true);
                                    isaudioenabled = false;
                                    muteButton.setBackgroundResource(R.drawable.ui_mute_hover);
                                }
                            }

                        } else {
                            //Toast.makeText(getApplicationContext(), "HoldUnhold Failure", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                if(intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    stopringbacktone();
                    stopConnectingTone();
                    if(mediaconnectedatleastonce&&!Utils.isNetworkAvailable(PlayNewAudioCallActivity.this)) {
                        reconnecting.setVisibility(View.VISIBLE);
                        mediadisconnectionshown = true;
                    }
                } else if(intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    if (mediadisconnectionshown && intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        reconnecting.setVisibility(View.GONE);
                        mediadisconnectionshown = false;
                    }

                }

				  /*if (intent.getAction().equals(CSEvents.CSCALL_PROXIMITYSENSOREVENT)) {
					try {
						String action = intent.getStringExtra("event");
						if (action.equals("near")) {
							WindowManager.LayoutParams  params = getWindow().getAttributes();
							params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
							params.screenBrightness = 0;
							getWindow().setAttributes(params);

							blankscreen.setVisibility(View.VISIBLE);
							mainscreen.setVisibility(View.INVISIBLE);

						} else {
							WindowManager.LayoutParams params = getWindow().getAttributes();
							params.screenBrightness = -1;
							getWindow().setAttributes(params);
							blankscreen.setVisibility(View.INVISIBLE);
							mainscreen.setVisibility(View.VISIBLE);
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}

				}
*/


            } catch (Exception ex) {
                ex.printStackTrace();
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

            //LOG.info("Registering audio call call backs");
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



            /* NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationid);*/


            mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

            //LOG.info("Registering audio call call backs done");


if(!isnotificationalreadyshown) {
    String notoficationName = "";
    if (displayname.equals("UnKnown")) {
        notoficationName = destinationnumbettocall;
    } else {
        notoficationName = displayname;
    }
    notificationid = Utils.notifycallinprogress(getApplicationContext(), notoficationName, "Audio call is in progress");
    isnotificationalreadyshown = true;
}

        } catch (Exception ex) {
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(this);

        //notificationManager.cancel(notificationid);
        //Intent intent = new Intent(getApplicationContext(), EmptyActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(intent);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            notificationManager.cancel(notificationid);
            stopConnectingTone();
        } catch (Exception ex) {
        }
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        try {
            showstreamstopalert();

        } catch (Exception ex) {
        }
        ;
        //return;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                //near
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                params.screenBrightness = 0;
                getWindow().setAttributes(params);
                //LOG.info("App proximity sensor near");
                blankscreen.setVisibility(View.VISIBLE);
                mainscreen.setVisibility(View.INVISIBLE);

            } else {

                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.screenBrightness = -1;
                getWindow().setAttributes(params);
                //LOG.info("App proximity sensor far");
                blankscreen.setVisibility(View.INVISIBLE);
                mainscreen.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public boolean showStreamStoppedAlert(final String message) {
        try {


            stopringbacktone();

            endTheCall();

			/*
			if(!isFinishing()) {
				//LOG.info("Yes in alert..");
				//cleanimagesdbbyremovingcommontors();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						AlertDialog.Builder successfullyLogin = new AlertDialog.Builder(
								PlayNewAudioCallActivity.this);
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public boolean showstreamstopalert() {
        try {
            if (!isFinishing()) {
                android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(PlayNewAudioCallActivity.this);
                successfullyLogin.setTitle("Confirmation");
                successfullyLogin.setCancelable(false);
                successfullyLogin.setMessage("End Call?");
                successfullyLogin.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                shownotifiction = false;
                                stopringbacktone();
                                stopConnectingTone();
                                CSCallsObj.endVoiceCall(destinationnumbettocall, mycallid, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
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

    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap photo = null;
            try {
                if (params[0].equals("app")) {
                    photo = CSDataProvider.getImageBitmap(params[1]);
                } else {
                    photo = loadContactPhoto(Long.parseLong(params[1]));
                }
                if (params[0].equals("app") && photo == null) {
                    photo = loadContactPhoto(Long.parseLong(params[2]));
                }
                if (photo == null) {
                    photo = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.defaultcontact);
                }

            } catch (Exception e) {
                photo = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.defaultcontact);
                //ClickToCallApi.logStacktrace(e);
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
                    } else {
						/*TextDrawable drawable2 = TextDrawable.builder()
				                .buildRound("A", Color.RED);*/
						/*Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.placeholder);
						imageView.setImageDrawable(placeholder);*/
                    }
                }
            }
        }
    }

    public Bitmap loadContactPhoto(long id) {
        try {
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
            InputStream stream = ContactsContract.Contacts.openContactPhotoInputStream(
                    getApplicationContext().getContentResolver(), uri);
            return BitmapFactory.decodeStream(stream);
        } catch (Exception ex) {
            return null;
        }


    }

    public void endTheCall() {
        try {

            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            h.removeCallbacks(RunnableObj);
            //Intent intent = new Intent(UIActions.AUDIOCALLDONE.getKey());
            //LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(intent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationid);
            stopringbacktone();
            stopConnectingTone();
            manageAudioFocus.abandonAudioFocus();
            ClickToCallConstants.iSHoldCalled = false;
            ClickToCallConstants.isintentionalhold = false;
            finish();
        } catch (Exception ex) {
            ex.printStackTrace();
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

            try {

                //notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                //mp = MediaPlayer.create(PlayNewAudioCallActivity.this, notification);
                int resID = R.raw.standard_6_ringback;
                if (iscallwaiting) {
                    resID = R.raw.busy_remote_end;
                } else {
                    resID = R.raw.standard_6_ringback;
                }

                //int resID = getResources().getIdentifier("standard_6_ringback.mp3", "raw", getApplicationContext().getPackageName());
                mp = MediaPlayer.create(PlayNewAudioCallActivity.this, resID);
                //mp.setAudioStreamType(AudioManager.STREAM_RING);
                if (mp != null) {
                    //mp.prepare();
                    mp.setLooping(true);
                    mp.start();
                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
        chatButton.setEnabled(false);
        EndaudiocallButton.setEnabled(false);
        dtmfButton.setEnabled(false);
        h.removeCallbacks(RunnableObj);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showStreamStoppedAlert(endReson);
            }
        }, 1000);
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
    public int notifycallinprogress(Context context, String calle, String description, String callee, String calltype, int reqcode) {
        Random rand = new Random();
        int notificationid = rand.nextInt(1000001);
        try {
            createNotificationChannel(context);
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

    private  void createNotificationChannel(Context context) {
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
