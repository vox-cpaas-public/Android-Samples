package com.ca.callsample;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.multidex.MultiDex;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSEvents;
import com.ca.dao.CSAppDetails;
import com.ca.utils.Constants;
import com.ca.utils.utils;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;

import java.util.ArrayList;
import java.util.Random;

/**
 * This class will initialize the application to server and generate OTP
 */
public class SignUpActivity extends Activity {

    private TextView mSignUpButtonTv;
    private EditText mMobileNumberEdt;
    private EditText mUserPassWordEdt;
    private String countryCode = "+91";
    private ProgressDialog mProgressDialog;
    private Handler mProgressBarHandler = new Handler();
    private int mProgressBarDelay = 40000;
    private Runnable mProgressBarRunnable;
    private CSClient CSClientObj = new CSClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_signup);

            mMobileNumberEdt = findViewById(R.id.mobile_number_edt);
            mUserPassWordEdt = findViewById(R.id.password_edt);
            mSignUpButtonTv = findViewById(R.id.sign_up_button_tv);


            //Sign Up button click listener
            mSignUpButtonTv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    // need to check number is empty or not to proceed registration

                    if (mMobileNumberEdt.getText().toString().trim().equals("")) {
                        mMobileNumberEdt.setError(getString(R.string.error_empty_number));
                    } else {
                        // need to check network available or not before call API
                        if (!utils.isNetworkAvailable(getApplicationContext())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.network_unavailable_message), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // need to check entered number is valid or not for selected country  this method will return null if given number is not belongs to selected country

                        Constants.phoneNumber = CSClientObj.getInternationalFormatNumber(mMobileNumberEdt.getText().toString(), countryCode);

                        // if given number is valid we will proceed for registration otherwise show error message to user
                        if (Constants.phoneNumber != null) {
                            // if user not gave any password we have to create random password
                            if (mUserPassWordEdt.getText().toString().equals("")) {
                                Constants.password = String.valueOf(new Random().nextInt(1000000));
                            } else {
                                Constants.password = mUserPassWordEdt.getText().toString();
                            }
                            // need to show confirmation message to user to check the number once
                            showAlertToUser();
                        } else {
                            mMobileNumberEdt.setError(getString(R.string.error_empty_number));
                        }
                    }
                }
            });




            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                ArrayList<String> allpermissions = new ArrayList<String>();
                allpermissions.add(android.Manifest.permission.CAMERA);
                allpermissions.add(android.Manifest.permission.READ_CONTACTS);
                allpermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
                allpermissions.add(android.Manifest.permission.RECORD_AUDIO);
                allpermissions.add(android.Manifest.permission.READ_PHONE_STATE);
                allpermissions.add(android.Manifest.permission.READ_SMS);
                allpermissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                allpermissions.add(android.Manifest.permission.VIBRATE);
                allpermissions.add(android.Manifest.permission.READ_PHONE_STATE);

                ArrayList<String> requestpermissions = new ArrayList<String>();

                for (String permission : allpermissions) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_DENIED) {
                        requestpermissions.add(permission);
                    }
                }
                if(requestpermissions.size()>0) {
                    ActivityCompat.requestPermissions(this, requestpermissions.toArray(new String[requestpermissions.size()]), 101);
                }
            }






        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ACTIVATION_SCREEN_INTENT_CODE && Constants.isAlreadySignedUp) {
            finish();
        }
    }


    /**
     * This method will show the confirmation message to user to check the entered message
     *
     * @return
     */
    public boolean showAlertToUser() {
        try {
            Builder builderDialog = new Builder(SignUpActivity.this);
            builderDialog.setTitle(getString(R.string.user_alert_dialog_tittle));

            builderDialog.setMessage(getString(R.string.user_alert_confirm_message) + Constants.phoneNumber);

            builderDialog.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            // need to check network available or not before call API
                            if (!utils.isNetworkAvailable(getApplicationContext())) {
                                Toast.makeText(getApplicationContext(), getString(R.string.network_unavailable_message), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            showProgressbar();
                            CSAppDetails csAppDetails = new CSAppDetails("CallSample", "pid_39684a6d_5103_4254_9775_1b923b9b98d5");
                            CSClientObj.initialize(Constants.server, Constants.port, csAppDetails);
                        }
                    });

            builderDialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                        }
                    });
            builderDialog.show();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    /**
     * This broadcast receiver wil, catch the events coming from SDK
     */
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {

                    // if network fluctuation came in between registration  dismiss the progressbar and show netwrok error message to user

                    Toast.makeText(getApplicationContext(), getString(R.string.network_error_message), Toast.LENGTH_SHORT).show();
                    dismissProgressbar();

                } else if (intent.getAction().equals(CSEvents.CSCLIENT_SIGNUP_RESPONSE)) {

                    // if SignUp is success dismiss the progressbar and open Activation screen
                    // if SignUp failure show the error message to user

                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        dismissProgressbar();
                        Intent activationIntent = new Intent(getApplicationContext(), ActivationActivity.class);
                        activationIntent.putExtra(Constants.INTENT_MOBILE_NUMBER, Constants.phoneNumber);
                        activationIntent.putExtra(Constants.INTENT_REGION, countryCode);
                        startActivityForResult(activationIntent, Constants.ACTIVATION_SCREEN_INTENT_CODE);
                    } else {
                        dismissProgressbar();
                        Toast.makeText(SignUpActivity.this, getString(R.string.sign_up_failure), Toast.LENGTH_SHORT).show();
                    }

                } else if (intent.getAction().equals(CSEvents.CSCLIENT_INITILIZATION_RESPONSE)) {

                    // if Initialization to server is success call SignUp API and call contacts loading API
                    // if initialization to server not success dismiss the progressbar and show the error response to user
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        if (!CSDataProvider.getSignUpstatus()) {

                            // this API will enable contacts loading method in SDK
                            CSClientObj.enableNativeContacts(true, 91);

                            // This will call SignUp API to generate OTP
                            CSClientObj.signUp(Constants.phoneNumber, Constants.password, false);
                        }
                    } else {
                        dismissProgressbar();
                        int returnCode = intent.getIntExtra(CSConstants.RESULTCODE, 0);
                        if (returnCode == CSConstants.E_409_NOINTERNET) {
                            Toast.makeText(getApplicationContext(), getString(R.string.network_unavailable_message), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.initialization_failure_message), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    MyReceiver MyReceiverObj = new MyReceiver();

    @Override
    public void onResume() {
        super.onResume();

        try {

            MyReceiverObj = new MyReceiver();
            // register the receivers to catch the events coming from SDK
            IntentFilter filter = new IntentFilter();
            filter.addAction(CSEvents.CSCLIENT_NETWORKERROR);
            filter.addAction(CSEvents.CSCLIENT_SIGNUP_RESPONSE);
            filter.addAction(CSEvents.CSCLIENT_INITILIZATION_RESPONSE);

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MyReceiverObj, filter);
            // it will clear the user db for fresh registration
            //new CSClient().reset();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            // unregister the receiver
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MyReceiverObj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * This method will show the progressbar while doing registration
     */
    public void showProgressbar() {
        try {
            if (getApplicationContext() != null) {
                mProgressDialog = new ProgressDialog(SignUpActivity.this);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage("Please Wait..");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setProgress(0);
                mProgressDialog.show();
                // this handler will close the progressbar if response not came with in specified time
                mProgressBarHandler = new Handler();
                mProgressBarRunnable = new Runnable() {
                    public void run() {
                        mProgressBarHandler.postDelayed(this, mProgressBarDelay);

                        runOnUiThread(new Runnable() {
                            public void run() {
                                dismissProgressbar();
                                Toast.makeText(getApplicationContext(), getString(R.string.network_error_message), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                };
                mProgressBarHandler.postDelayed(mProgressBarRunnable, mProgressBarDelay);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * This will dismiss the progressbar  once got any response from server
     */
    public void dismissProgressbar() {
        try {

            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            if (mProgressBarHandler != null) {
                mProgressBarHandler.removeCallbacks(mProgressBarRunnable);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
