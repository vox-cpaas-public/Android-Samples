package com.ca.clicktocall;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.telephony.TelephonyManager;

import com.ca.wrapper.CSClient;

public class ClickToCallApi {

    /**
     * This method returns app logged in status for calls
     */
public static boolean isAppReady() {
    return new CSClient().getLoginstatus();
}
    /**
     * This method will initiate new video call
     * @param calle
     * @param context
     */
    public static void doNewVideoCall(Activity context, String calle) {
        try {

            if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if(telManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                        if(Utils.isNetworkAvailable(context)) {
                            if (new CSClient().getLoginstatus()) {
                            if (!calle.equals("") && !calle.equals(ClickToCallConstants.loginid)) {
                                Intent intent = new Intent(context, PlayNewVideoCallActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("dstnumber", calle);
                                intent.putExtra("isinitiatior", true);
                                context.startActivity(intent);
                            }  else {
                                 Utils.showSettingsAlert(context, "No valid Caller");
                            }
                            } else {
                                Utils.showSettingsAlert(context,"Couldn't place call. Please check internet connection and try again");
                            }

                        } else {
                            Utils.showSettingsAlert(context, context.getString(R.string.network_unavailable_message));
                        }

                     } else {
                        Utils.showSimpleAlert(context,"GSM Call is in progress.Couldn't place call");
                    }
                } else {
                    Utils.showSimpleAlert(context,"READ_PHONE_STATE Permission Needed");
                }
            } else {

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    Utils.showSimpleAlert(context, "RECORD_AUDIO Permission Needed");
                }
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Utils.showSimpleAlert(context, "CAMERA Permission Needed");
                }


            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method will initiate new Audio Call
     * @param calle
     * @param context
     */
    public static void doNewVoiceCall(Activity context, String calle) {
        try {
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if(telManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                        if(Utils.isNetworkAvailable(context)) {
                            if (new CSClient().getLoginstatus()) {
                                if (!calle.equals("") && !calle.equals(ClickToCallConstants.loginid)) {
                                    Intent intent = new Intent(context, PlayNewAudioCallActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("dstnumber", calle);
                                    intent.putExtra("isinitiatior", true);
                                    context.startActivity(intent);
                                }  else {
                                    Utils.showSettingsAlert(context, "No valid Caller");
                                }
                            } else {
                                Utils.showSettingsAlert(context,"Couldn't place call. Please check internet connection and try again");
                            }

                        } else {
                            Utils.showSettingsAlert(context, context.getString(R.string.network_unavailable_message));
                        }

                    } else {
                        Utils.showSimpleAlert(context,"GSM Call is in progress.Couldn't place call");
                    }
                } else {
                    Utils.showSimpleAlert(context,"READ_PHONE_STATE Permission Needed");
                }
            } else {
                Utils.showSimpleAlert(context, "RECORD_AUDIO Permission Needed");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method will initiate new app to PSTN Call
     * @param calle
     * @param context
     */
    public static void doNewPSTNCall(Activity context, String calle) {
        try {
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if(telManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                        if(Utils.isNetworkAvailable(context)) {
                            if (new CSClient().getLoginstatus()) {
                                if (!calle.equals("") && !calle.equals(ClickToCallConstants.loginid)) {
                                    Intent intent = new Intent(context, PlaySipCallActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("dstnumber", calle);
                                    intent.putExtra("isinitiatior", true);
                                    context.startActivity(intent);
                                }  else {
                                    Utils.showSettingsAlert(context, "No valid Caller");
                                }
                            } else {
                                Utils.showSettingsAlert(context,"Couldn't place call. Please check internet connection and try again");
                            }

                        } else {
                            Utils.showSettingsAlert(context, context.getString(R.string.network_unavailable_message));
                        }

                    } else {
                        Utils.showSimpleAlert(context,"GSM Call is in progress.Couldn't place call");
                    }
                } else {
                    Utils.showSimpleAlert(context,"READ_PHONE_STATE Permission Needed");
                }
            } else {
                Utils.showSimpleAlert(context, "RECORD_AUDIO Permission Needed");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
