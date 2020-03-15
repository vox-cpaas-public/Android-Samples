package com.ca.chatsample;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSEvents;
import com.ca.dao.CSAppDetails;
import com.ca.utils.Constants;
import com.ca.utils.utils;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSGroups;

public class LoginActivity extends AppCompatActivity {
    private EditText mLoginMobileNumberEdt, mLoginPasswordEdt;
    private TextView mLoginTv;
    private CSClient CSClientObj = new CSClient();
    private String mUserName = "";
    private String mPassWord = "";
    private ProgressDialog mProgressDialog;
    private Handler mProgressBarHandler = new Handler();
    private int mProgressBarDelay = 40000;
    private Runnable mProgressBarRunnable;
    private String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mLoginMobileNumberEdt = findViewById(R.id.mobile_number_edt);
        mLoginPasswordEdt = findViewById(R.id.password_edt);
        mLoginTv = findViewById(R.id.login_button_tv);


        mLoginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoginMobileNumberEdt.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.context, "Username shouldn't be empty", Toast.LENGTH_SHORT).show();
                } else if (mLoginPasswordEdt.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.context, "Password shouldn't be empty", Toast.LENGTH_SHORT).show();
                } else if (mLoginMobileNumberEdt.getText().toString().contains("\n")) {
                    Toast.makeText(MainActivity.context, "Username should be valid", Toast.LENGTH_SHORT).show();
                } else if (mLoginPasswordEdt.getText().toString().contains("\n")) {
                    Toast.makeText(MainActivity.context, "Password should be valid", Toast.LENGTH_SHORT).show();
                } else if (!utils.isinternetavailable(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.network_unavailable_message), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    mUserName = mLoginMobileNumberEdt.getText().toString().trim();
                    mPassWord = mLoginPasswordEdt.getText().toString().trim();
                    showProgressbar();
                    CSAppDetails csAppDetails = new CSAppDetails("ChatSample", "pid_39684a6d_5103_4254_9775_1b923b9b98d5");
                    CSClientObj.initialize(Constants.server, Constants.port, csAppDetails);
                }
            }
        });
    }

    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    protected void onResume() {
        super.onResume();
        try {
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter1 = new IntentFilter(CSEvents.CSCLIENT_SIGNUP_RESPONSE);
            IntentFilter filter2 = new IntentFilter(CSEvents.CSCLIENT_LOGIN_RESPONSE);
            IntentFilter filter3 = new IntentFilter(CSEvents.CSCLIENT_INITILIZATION_RESPONSE);
            MainActivity.context = getApplicationContext();

            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter1);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter2);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter3);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        try {

            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.i(TAG, "onReceive: Yes Somthing Received in Login " + intent.getAction());
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    dismissProgressbar();
                    Toast.makeText(getApplicationContext(), "NetworkError", Toast.LENGTH_SHORT).show();
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_INITILIZATION_RESPONSE)) {
                    dismissProgressbar();
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {

                        CSClientObj.enableNativeContacts(true, 91);
                        CSClientObj.login(mUserName, mPassWord);

                    } else {

                        int retcode = intent.getIntExtra(CSConstants.RESULTCODE, 0);
                        Log.i(TAG, "INITILIZATIONFAILURE CODE:" + retcode);
                        if (retcode == CSConstants.E_409_NOINTERNET) {
                            showalert("No Internet Available");
                        } else {
                            Toast.makeText(getApplicationContext(), "INITILIZATIONFAILURE", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    dismissProgressbar();
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        CSGroups CSGroupsObj = new CSGroups();
                        CSGroupsObj.pullMyGroupsList();
                        Intent intentt = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intentt);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * This method will show the progressbar while doing registration
     */
    public void showProgressbar() {
        try {
            if (getApplicationContext() != null) {
                mProgressDialog = new ProgressDialog(LoginActivity.this);
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

    /**
     * This method will show popup message for login failure
     *
     * @param result
     * @return
     */
    public boolean showalert(String result) {
        try {
            android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(LoginActivity.this);
            successfullyLogin.setMessage(result);
            successfullyLogin.setPositiveButton("Settings",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                        }
                    });
            successfullyLogin.setNegativeButton("Ok",
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
}
