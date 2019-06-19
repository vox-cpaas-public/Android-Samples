package com.ca.clicktocall;

import android.content.Context;
import android.media.AudioManager;

import com.ca.wrapper.CSCall;
public class ManageAudioFocus {

    private static AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private static AudioManager am = null;
    private static String callid = "";
    private static String remoteid = "";

    CSCall CSCallObj = new CSCall();


//hold only if callid available and call is active-->sdk will take care.


    public boolean requestAudioFocus(Context context, String acallid, String aremoteid,String calltype) {
        try {


                am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                //abandonAudioFocus();

                callid = acallid;
                remoteid = aremoteid;


                audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int focusChange) {
                        if (!callid.equals("") && !remoteid.equals("")) {
                            final String typeOfChange;
                            switch (focusChange) {
                                case AudioManager.AUDIOFOCUS_GAIN:
                                    typeOfChange = "AUDIOFOCUS_GAIN";
                                    //LOG.info("unholding the call on AUDIOFOCUS_GAIN hold status:" + GlobalVariables.hold);
                                    if (ClickToCallConstants.iSHoldCalled&&!ClickToCallConstants.isintentionalhold) {
                                        //LOG.info("unholding the call on AUDIOFOCUS_GAIN");
                                        if(calltype.equals("pstn")) {
                                            CSCallObj.holdPstnCall(callid, false);
                                        } else {
                                            CSCallObj.holdAVCall(remoteid, callid, false);
                                        }

                                    } /*else {
								GlobalVariables.hold = true;
							}*/
                                    break;
                                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                                    typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT";
                                    //if (GlobalVariables.hold) {
                                    if (ClickToCallConstants.iSHoldCalled&&!ClickToCallConstants.isintentionalhold) {
                                        if(calltype.equals("pstn")) {
                                            CSCallObj.holdPstnCall(callid, false);
                                        } else {
                                            CSCallObj.holdAVCall(remoteid, callid, false);
                                        }

                                    }
							/*} else {
								GlobalVariables.hold = true;
							}*/
                                    break;
                                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                                    typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE";
                                    //if (GlobalVariables.hold) {
                                    if (ClickToCallConstants.iSHoldCalled&&!ClickToCallConstants.isintentionalhold) {
                                        if(calltype.equals("pstn")) {
                                            CSCallObj.holdPstnCall(callid, false);
                                        } else {

                                            CSCallObj.holdAVCall(remoteid, callid, false);
                                        }
                                    }
							/*} else {
								GlobalVariables.hold = true;
							}*/
                                    break;
                                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                                    typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK";
                                    break;
                                case AudioManager.AUDIOFOCUS_LOSS:
                                    typeOfChange = "AUDIOFOCUS_LOSS";
                                    ClickToCallConstants.iSHoldCalled = true;
                                    //hold the call
                                    if(calltype.equals("pstn")) {
                                        CSCallObj.holdPstnCall(callid, true);
                                    } else {
                                        CSCallObj.holdAVCall(remoteid, callid, true);
                                    }

                                    break;
                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                    typeOfChange = "AUDIOFOCUS_LOSS_TRANSIENT";
                                    ClickToCallConstants.iSHoldCalled = true;
                                    //hold the call
                                    if(calltype.equals("pstn")) {
                                        CSCallObj.holdPstnCall(callid, true);
                                    } else {
                                        CSCallObj.holdAVCall(remoteid, callid, true);
                                    }

                                    break;
                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                    typeOfChange = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";

                                    //hold the call

                                    break;
                                default:
                                    typeOfChange = "AUDIOFOCUS_INVALID";
                                    break;
                            }
                            //LOG.info("App onAudioFocusChange: " + typeOfChange);
                        }
                    }
                };

                //LOG.info("here2:" + audioFocusChangeListener);
                int result = am.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);
                //LOG.info("here3");
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    //LOG.info("App Audio focus request granted for VOICE_CALL streams");
                    return true;
                } else {
                    //LOG.info("App Audio focus request failed");
                    return false;
                }

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public void abandonAudioFocus() {
        try {
            if (am != null && audioFocusChangeListener != null) {
                //LOG.info("here22:" + audioFocusChangeListener);
                //LOG.info("am and audioFocusChangeListener are not null..abandoning..");
                am.abandonAudioFocus(audioFocusChangeListener);
                audioFocusChangeListener = null;
                am = null;
                callid = "";
                remoteid = "";
            } else {
                //LOG.info("am or audioFocusChangeListener is null");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
