package com.ca.clicktocall;


import android.app.Activity;
import android.app.NotificationManager;
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
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.multidex.MultiDex;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class PlaySipCallActivity extends Activity implements SensorEventListener
{
	boolean mediaconnectedatleastonce = false;
	boolean mediadisconnectionshown = false;

	public static boolean audiomutestateirrespectivehold = false;
	private SensorManager mSensorManager;
	private Sensor mProximity;
	private static final int SENSOR_SENSITIVITY = 4;
	ManageAudioFocus manageAudioFocus = new ManageAudioFocus();
	boolean isnotificationalreadyshown = false;
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
	boolean showtimer = false;
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

	ImageView muteButton;
	ImageView holdbutton;
	ImageView chatButton;
	boolean isaudioenabled = true;
	boolean isspeakerenabled = false;
	boolean isholdenabled = true;
	boolean isdtmfenabled = true;
	CircularImageView avatharButton;

	RelativeLayout blankscreen;
	RelativeLayout mainscreen;
	CSCall CSCallsObj = new CSCall();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sip_call);
		try {


			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			
			mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			//GlobalVariables.lastcalltype = "audio";


			callDate = CSClientObj.getTime();
			notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			successfullyLogin = new AlertDialog.Builder(PlaySipCallActivity.this);

			reconnecting = (TextView) findViewById(R.id.textView19);

			callednumber = (TextView) findViewById(R.id.textView17);
			timer = (TextView) findViewById(R.id.textView18);
			EndaudiocallButton = (ImageView) findViewById(R.id.callbutton);

			blankscreen = (RelativeLayout) findViewById(R.id.blankscreen);
			mainscreen = (RelativeLayout) findViewById(R.id.mainscreen);
			dtmfButton = (ImageView) findViewById(R.id.dialpad);
			muteButton = (ImageView) findViewById(R.id.mute);
			holdbutton = (ImageView) findViewById(R.id.hold);
			chatButton = (ImageView) findViewById(R.id.speaker);

			avatharButton = (CircularImageView) findViewById(R.id.avathar);
			reconnecting.setVisibility(View.GONE);
			try {

				RunnableObj = new Runnable(){
					int i = 0;
					public void run(){
						h.postDelayed(this, delay);
						////LOG.info("printing at 1 sec");
						PlaySipCallActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								String minutes = "00";
								String seconds = "00";
								int x = i++;
								int mins= (x)/60;
								int sec = (x)%60;
								if(mins<10) {
									minutes = "0"+String.valueOf(mins);
								} else {
									minutes = String.valueOf(mins);
								}
								if(sec<10) {
									seconds = "0"+String.valueOf(sec);
								} else {
									seconds = String.valueOf(sec);
								}

								timer.setText(minutes+":"+seconds);
							}
						});
					}
				};

				IntentFilter filter1 = new IntentFilter(CSEvents.CSCALL_CALLENDED);
				LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj,filter1);

				if(getIntent().getBooleanExtra("isinitiatior", false)) {
timer.setText("Calling..");
					destinationnumbettocall = getIntent().getStringExtra("dstnumber");
					destinationnumbettocall = destinationnumbettocall.replace("+","");
                    System.out.println("connectsdk playsip call");
					mycallid = CSCallsObj.startPstnCall(destinationnumbettocall);
					manageAudioFocus.requestAudioFocus(getApplicationContext(),mycallid,destinationnumbettocall,"pstn");

					/*
					Thread.sleep(1000);
					GlobalVariables.//LOG.info("recordCall:"+CSCallsObj.recordCall(mycallid));
					Thread.sleep(1000);
					GlobalVariables.//LOG.info("pauseCallRecording:"+CSCallsObj.pauseCallRecording(mycallid));
					Thread.sleep(1000);
					GlobalVariables.//LOG.info("resumeCallRecording:"+CSCallsObj.resumeCallRecording(mycallid));
					Thread.sleep(1000);
					GlobalVariables.//LOG.info("cancelCallRecording:"+CSCallsObj.cancelCallRecording(mycallid));
*/


				} else {
					timer.setText("Answering..");
					destinationnumbettocall = getIntent().getStringExtra("srcnumber");
					//h.postDelayed(RunnableObj, delay);
					mycallid = getIntent().getStringExtra("callid");

					//String sdp = getIntent().getStringExtra("sdp");

					CSCallsObj.answerPstnCall(destinationnumbettocall,getIntent().getStringExtra("callid"));

				}
				
				String nativecontactid = "";
				Cursor cur = CSDataProvider.getContactCursorByNumber(destinationnumbettocall);
				if(cur.getCount()>0) {
					cur.moveToNext();
					nativecontactid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
				}
				cur.close();

				String picid  = "";
				Cursor cur1 = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER,destinationnumbettocall);
				if(cur1.getCount()>0) {
					cur1.moveToNext();
					picid = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
				}
				cur1.close();


				new ImageDownloaderTask(avatharButton).execute("app",picid,nativecontactid);




				displayname = destinationnumbettocall;
				String temp_destinationnumbettocall = destinationnumbettocall;
				if(!destinationnumbettocall.contains("+")){
					temp_destinationnumbettocall = "+"+temp_destinationnumbettocall;
				}
				Cursor ccfr = CSDataProvider.getContactCursorByNumber(temp_destinationnumbettocall);
				if(ccfr.getCount()>0) {
					ccfr.moveToNext();
					displayname = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME))+"["+destinationnumbettocall+"]";
				}
				ccfr.close();
				callednumber.setText(displayname);

			} catch (Exception ex) {
				ex.printStackTrace();
			}











			EndaudiocallButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					try {

						stopringbacktone();

						CSCallsObj.endPstnCall(destinationnumbettocall,mycallid);
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

					} catch(Exception ex) {
						//ex.printStackTrace();
					}
				}
			});

			chatButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					try {
						if(!mycallid.equals("")) {
							if (isspeakerenabled) {
								////LOG.info("Came here2");
								isspeakerenabled = false;
								CSCallsObj.enableSpeaker(mycallid,false);
								//audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
								//chatButton.setBackgroundResource(R.drawable.call_bg_selector_prpl));
								chatButton.setBackgroundResource(R.drawable.uispeaker_icon_norm);
								//muteButton.setBackgroundColor(Color.parseColor("#1A000000"));

							} else {
								////LOG.info("Came here3");
								isspeakerenabled = true;
								CSCallsObj.enableSpeaker(mycallid,true);
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


					} catch(Exception ex) {
						//ex.printStackTrace();
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
							showdialpad();
						} else {
							isdtmfenabled = true;
							//dtmfButton.setBackgroundColor(Color.parseColor("#00000000"));
							dtmfButton.setBackgroundResource(R.drawable.ui_keypad_normal);
							dismisssuccessfullyLogin.cancel();

						}

					} catch(Exception ex) {
						//ex.printStackTrace();
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

						if(!mycallid.equals("")) {

							//if (getIntent().getBooleanExtra("isinitiatior", false)) {
							////LOG.info("callid to mute or unmute:"+mycallid);

							if (isaudioenabled) {
								CSCallsObj.muteAudio(mycallid,true);
								audiomutestateirrespectivehold = true;
								//GlobalVariables.peerConnectionClient.setAudioEnabled(false);
								isaudioenabled = false;
								//muteButton.setBackgroundResource(R.drawable.call_bg_selector));
								muteButton.setBackgroundResource(R.drawable.ui_mute_hover);
								//muteButton.setBackgroundColor(Color.parseColor("#1A000000"));
							} else {
								CSCallsObj.muteAudio(mycallid,false);
								audiomutestateirrespectivehold = false;
								//GlobalVariables.peerConnectionClient.setAudioEnabled(true);
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
									GlobalVariables.peerConnectionClient.setAudioEnabled(true);
									isaudioenabled = true;
									muteButton.setBackgroundResource(R.drawable.ui_mute_hover));
									//muteButton.setBackgroundColor(Color.parseColor("#4E3164"));
								}
							}*/
						} else {
							//LOG.info("callid is empty to toggle mute in audio call");
						}
					} catch(Exception ex) {
						//ex.printStackTrace();
					}
				}
			});

			holdbutton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					try {

						if(!mycallid.equals("")) {

							if (isholdenabled) {
								ClickToCallConstants.isintentionalhold = true;
								CSCallsObj.holdPstnCall(mycallid,true);
							} else {
								ClickToCallConstants.isintentionalhold = false;
								CSCallsObj.holdPstnCall(mycallid,false);
							}

						} else {
							//LOG.info("callid is empty to toggle hold in pstn call");
						}
					} catch(Exception ex) {
						//ex.printStackTrace();
					}
				}
			});

		} catch(Exception ex){

			//ex.printStackTrace();

		}


	}



	public boolean showdialpad() {
		try {


			runOnUiThread(new Runnable() {

				@Override
				public void run() {


					LayoutInflater inflater = PlaySipCallActivity.this.getLayoutInflater();
					View dialogView = inflater.inflate(R.layout.firstcall_only_numpad, null);
					successfullyLogin.setView(dialogView);
					TextView zero = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_0);
					TextView one = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_1);
					TextView two = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_2);
					TextView three = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_3);
					TextView four = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_4);
					TextView five = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_5);
					TextView six = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_6);
					TextView seven = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_7);
					TextView eight = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_8);
					TextView nine = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_9);

					TextView plus = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_plus);
					TextView hash = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_hash);


					final EditText edittext = (EditText) dialogView.findViewById(R.id.editText1);
					ImageView backarrow = (ImageView) dialogView.findViewById(R.id.imageView5);

					Button endcall = (Button) dialogView.findViewById(R.id.button6);
					Button hidekeyboard = (Button) dialogView.findViewById(R.id.button7);

					edittext.setFocusable(false);
					edittext.setClickable(true);

					endcall.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							dismisssuccessfullyLogin.cancel();
							EndaudiocallButton.performClick();
						}
					});

					hidekeyboard.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							dtmfButton.setBackgroundResource(R.drawable.ui_keypad_normal);
							if(dismisssuccessfullyLogin!=null) {
								dismisssuccessfullyLogin.cancel();
							}
						}
					});

					backarrow.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							try
							{
								edittext.setText("");

							}catch (Exception ex) {
								//ex.printStackTrace();
							}

							return true;
						}
					});
					backarrow.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								String str = edittext.getText().toString();
								str = str.substring(0, (str.length() - 1));
								edittext.setText(str);
							} catch (Exception ex) {}
						}
					});
					zero.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String str = edittext.getText().toString();
							str = str+"0";
							edittext.setText(str);
CSCallsObj.sendDtmfInPstnCall("0",mycallid);
						}
					});

					one.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String str = edittext.getText().toString();
							str = str+"1";
							edittext.setText(str);
							CSCallsObj.sendDtmfInPstnCall("1",mycallid);
						}
					});
					two.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String str = edittext.getText().toString();
							str = str+"2";
							edittext.setText(str);
							CSCallsObj.sendDtmfInPstnCall("2",mycallid);
						}
					});
					three.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String str = edittext.getText().toString();
							str = str+"3";
							edittext.setText(str);
							CSCallsObj.sendDtmfInPstnCall("3",mycallid);
						}
					});
					four.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String str = edittext.getText().toString();
							str = str+"4";
							edittext.setText(str);
							CSCallsObj.sendDtmfInPstnCall("4",mycallid);
						}
					});
					five.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String str = edittext.getText().toString();
							str = str+"5";
							edittext.setText(str);
							CSCallsObj.sendDtmfInPstnCall("5",mycallid);
						}
					});
					six.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String str = edittext.getText().toString();
							str = str+"6";
							edittext.setText(str);
							CSCallsObj.sendDtmfInPstnCall("6",mycallid);
						}
					});
					seven.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String str = edittext.getText().toString();
							str = str+"7";
							edittext.setText(str);
							CSCallsObj.sendDtmfInPstnCall("7",mycallid);
						}
					});
					eight.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String str = edittext.getText().toString();
							str = str+"8";
							edittext.setText(str);
							CSCallsObj.sendDtmfInPstnCall("8",mycallid);
						}
					});
					nine.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String str = edittext.getText().toString();
							str = str+"9";
							edittext.setText(str);
							CSCallsObj.sendDtmfInPstnCall("9",mycallid);
						}
					});
					plus.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String str = edittext.getText().toString();
							str = str+"*";
							edittext.setText(str);
							CSCallsObj.sendDtmfInPstnCall("*",mycallid);
						}
					});
					hash.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String str = edittext.getText().toString();
							str = str+"#";
							edittext.setText(str);
							CSCallsObj.sendDtmfInPstnCall("#",mycallid);
						}
					});
					dismisssuccessfullyLogin = successfullyLogin.show();

				}
			});
			return true;
		} catch(Exception ex) {
			return false;
		}

	}

	public class MainActivityReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			try {
				//LOG.info("Audio call callback:"+intent.getAction().toString());
				//LOG.info("callback callid:"+intent.getStringExtra("callid"));
				////LOG.info("mycallid:"+mycallid);
				////LOG.info("Audio call callback:"+intent.getStringExtra("callid"));

				if(mycallid.equals(intent.getStringExtra("callid"))) {
					if (intent.getAction().equals(CSEvents.CSCALL_CALLENDED)) {

						if(intent.getStringExtra("endReason").toString().equals(CSConstants.UserBusy)) {
							showStreamStoppedAlert("callEnded! UserBusy!");
						} else {
							showStreamStoppedAlert("callEnded");
						}
						//LOG.info("endReason:"+intent.getStringExtra("endReason"));

					}
					else if (intent.getAction().equals(CSEvents.CSCALL_NOANSWER)) {

						showStreamStoppedAlert("NoAnswer");
					} else if (intent.getAction().equals(CSEvents.CSCALL_CALLANSWERED)) {
						//LOG.info("test CSCALL_CALLANSWERED receieved");
						//stopringbacktone(); //tmp
						//h.postDelayed(RunnableObj, delay); //tmp
						if(getIntent().getBooleanExtra("isinitiatior", false)) {
							h.postDelayed(RunnableObj, delay);
						}
					} else if (intent.getAction().equals(CSEvents.CSCALL_NOMEDIA)) {

						showStreamStoppedAlert("NoMedia! CallEnded!");
					} else if (intent.getAction().equals(CSEvents.CSCALL_RINGING)) {
						//LOG.info("test ringing receieved");
						timer.setText("Ringing");
						playringbacktone();
					}
					else if (intent.getAction().equals(CSEvents.CSCALL_MEDIACONNECTED)) {
						mediaconnectedatleastonce = true;
						//LOG.info("test MediaConnected receieved");

						if(!showtimer) {
							showtimer = true;
							stopringbacktone();
							if(!getIntent().getBooleanExtra("isinitiatior", false)) {
								h.postDelayed(RunnableObj, delay);
							}
						}


						reconnecting.setVisibility(View.GONE);
						stopringbacktone();
					}
					else if (intent.getAction().equals(CSEvents.CSCALL_MEDIADISCONNECTED)) {
						//LOG.info("test MediaDisConnected receieved");
						reconnecting.setVisibility(View.VISIBLE);
					}
					else if (intent.getAction().equals("TerminateForSecondCall")) {
						//LOG.info("test TerminateForSecondCall receieved");
						EndaudiocallButton.performClick();
					}

				}
				if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
					stopringbacktone();
					if(mediaconnectedatleastonce&&!Utils.isNetworkAvailable(PlaySipCallActivity.this)) {
						reconnecting.setVisibility(View.VISIBLE);
						mediadisconnectionshown = true;
					}
				} else if(intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
					if (mediadisconnectionshown && intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
						reconnecting.setVisibility(View.GONE);
						mediadisconnectionshown = false;
					}

				} else if(mycallid.equals(intent.getStringExtra("callid"))&&intent.getAction().equals(CSEvents.CSCALL_RECORDING_AT_SERVER)) {
					//Toast.makeText(getApplicationContext(), "Call is being recorded at server", Toast.LENGTH_SHORT).show();
				}
				else if (intent.getAction().equals(CSEvents.CSCALL_SEND_DTMF_TONE_RESPONSE)) {
					if(intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
						Toast.makeText(getApplicationContext(), "DTMF Success", Toast.LENGTH_SHORT).show();

					} else {
						Toast.makeText(getApplicationContext(), "DTMF Failure", Toast.LENGTH_SHORT).show();

					}

				}
				else if (intent.getAction().equals(CSEvents.CSCALL_HOLD_UNHOLD_RESPONSE)) {

					//if(intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)&&mycallid.equals(intent.getStringExtra("callid"))) {
						if(mycallid.equals(intent.getStringExtra("callid"))) {


							//Toast.makeText(getApplicationContext(), "HoldUnhold Success", Toast.LENGTH_SHORT).show();
							boolean ishold = intent.getBooleanExtra("hold", false);


						if (ishold) {
							ClickToCallConstants.iSHoldCalled = true;
							//CSCallsObj.holdPstnCall(mycallid,true);
							isholdenabled = false;
							holdbutton.setBackgroundResource(R.drawable.ui_hold_hover);

							muteButton.setEnabled(false);


						} else {
							ClickToCallConstants.iSHoldCalled = false;
							//CSCallsObj.holdPstnCall(mycallid,false);
							isholdenabled = true;
							holdbutton.setBackgroundResource(R.drawable.ui_hold_normal);
							muteButton.setEnabled(true);

						}


							if(audiomutestateirrespectivehold) {
								//CSCallsObj.muteAudio(mycallid,true);
								isaudioenabled = false;
								muteButton.setBackgroundResource(R.drawable.ui_mute_hover);
							}


					} else {
						//Toast.makeText(getApplicationContext(), "HoldUnhold Failure", Toast.LENGTH_SHORT).show();
					}
				}
				else if (intent.getAction().equals(CSEvents.CSCALL_SESSION_IN_PROGRESS)) {
					//if(intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
					if(intent.getStringExtra("callid").equals(mycallid)) {
						timer.setText("Session in progress");
						stopringbacktone();
					}
									}

			} catch(Exception ex){
				//ex.printStackTrace();
			}
		}
	}



	MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();
	@Override
	public void onResume() {
		super.onResume();

		try{


			IntentFilter filter = new IntentFilter();
			filter.addAction(CSEvents.CSCALL_NOANSWER);
			filter.addAction(CSEvents.CSCALL_CALLANSWERED);
			filter.addAction(CSEvents.CSCALL_NOMEDIA);
			filter.addAction(CSEvents.CSCLIENT_NETWORKERROR);
			filter.addAction(CSEvents.CSCALL_RINGING);
			filter.addAction(CSEvents.CSCALL_MEDIACONNECTED);
			filter.addAction(CSEvents.CSCALL_MEDIADISCONNECTED);
			filter.addAction("TerminateForSecondCall");
			filter.addAction(CSEvents.CSCALL_SEND_DTMF_TONE_RESPONSE);
			filter.addAction(CSEvents.CSCALL_HOLD_UNHOLD_RESPONSE);
			filter.addAction(CSEvents.CSCALL_SESSION_IN_PROGRESS);
			filter.addAction(CSEvents.CSCALL_RECORDING_AT_SERVER);
			filter.addAction(CSEvents.CSCLIENT_LOGIN_RESPONSE);

			LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj,filter);

			if(!isnotificationalreadyshown) {
				notificationid = Utils.notifycallinprogress(getApplicationContext(), displayname, "Pstn call is in progress");
				isnotificationalreadyshown = true;
			}
			mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

			//LOG.info("Registering audio call call backs done");

		} catch(Exception ex) {}

	}
	@Override
	public void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		try {
			notificationManager.cancel(notificationid);
		} catch(Exception ex) {}
	}


	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		try {

			showstreamstopalert();

		} catch(Exception ex) {};
		//return;
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
				WindowManager.LayoutParams  params = getWindow().getAttributes();
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
								PlaySipCallActivity.this);
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
		} catch(Exception ex) {
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
			if(!isFinishing()) {
				android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(PlaySipCallActivity.this);
				successfullyLogin.setTitle("Confirmation");
				successfullyLogin.setCancelable(false);
				successfullyLogin.setMessage("End Call?");
				successfullyLogin.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
												int which) {

								stopringbacktone();
								CSCallsObj.endPstnCall(destinationnumbettocall,mycallid);
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
				if(!isFinishing()) {
					successfullyLogin.show();
				}
			}
			return true;
		} catch(Exception ex) {
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
				if(params[0].equals("app")) {
					photo = CSDataProvider.getImageBitmap(params[1]);
				} else {
					photo = loadContactPhoto(Long.parseLong(params[1]));
				}
				if(params[0].equals("app")&&photo == null) {
					photo = loadContactPhoto(Long.parseLong(params[2]));
				}
				if(photo == null) {
					photo = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.defaultcontact);
				}

			} catch (Exception e) {
				photo = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.defaultcontact);
				//utils.logStacktrace(e);
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
	public  Bitmap loadContactPhoto(long  id) {
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
			notificationManager.cancel(notificationid);
			stopringbacktone();
			manageAudioFocus.abandonAudioFocus();

			ClickToCallConstants.iSHoldCalled = false;
			ClickToCallConstants.isintentionalhold = false;
			finish();


		} catch(Exception ex) {

		}
	}
	public void playringbacktone() {

		if(getIntent().getBooleanExtra("isinitiatior", false)) {
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
				//mp = MediaPlayer.create(PlaySipCallActivity.this, notification);
				int resID = R.raw.standard_6_ringback;
				//int resID = getResources().getIdentifier("standard_6_ringback.mp3", "raw", getApplicationContext().getPackageName());
				mp = MediaPlayer.create(PlaySipCallActivity.this, resID);
				//mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
				if (mp != null) {
					mp.setLooping(true);
					mp.start();
				}
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		}

	}

	public void stopringbacktone() {
		//if(getIntent().getBooleanExtra("isinitiatior", false)) {
		try {
			if(mp!=null) {
				mp.stop();
				mp.reset();
				mp.release();
				mp = null;
			}
		} catch (Exception ex) {

		}
		//}
	}
}
