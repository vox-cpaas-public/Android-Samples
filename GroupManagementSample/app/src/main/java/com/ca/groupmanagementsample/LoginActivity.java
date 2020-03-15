package com.ca.groupmanagementsample;

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
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSEvents;
import com.ca.dao.CSAppDetails;
import com.ca.dao.CSExplicitEventReceivers;
import com.ca.utils.Constants;
import com.ca.utils.PreferenceProvider;
import com.ca.wrapper.CSClient;
import com.ca.chatsample.R;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    ProgressDialog progressBar;
    Handler h = new Handler();
    int delay = 80000;
    Runnable RunnableObj;
    EditText urlobj;
    EditText urlobj1;
    boolean showpassword = false;
    CSClient CSClientObj = new CSClient();
    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();
    private int progressBarStatus = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.direct_login);
        try {
            showpassword = false;
            final Button submitobj = findViewById(R.id.button6);
            urlobj = (EditText) findViewById(R.id.editText2);
            urlobj1 = (EditText) findViewById(R.id.editText1);
            PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
            boolean dontshowagain = pf.getPrefBoolean("registerreceiversnew");
            if (!dontshowagain) {
                CSClientObj.registerExplicitEventReceivers(new CSExplicitEventReceivers("com.ca.receivers.CSUserJoined", "com.ca.receivers.CSCallReceiver", "com.ca.receivers.CSChatReceiver", "com.ca.receivers.CSGroupNotificationReceiver", "com.ca.receivers.CSCallMissed"));
                PreferenceProvider pff = new PreferenceProvider(getApplicationContext());
                pff.setPrefboolean("registerreceiversnew", true);
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                ArrayList<String> allpermissions = new ArrayList<String>();
                allpermissions.add(android.Manifest.permission.CAMERA);
                allpermissions.add(android.Manifest.permission.READ_CONTACTS);
                allpermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
                allpermissions.add(android.Manifest.permission.RECORD_AUDIO);
                allpermissions.add(android.Manifest.permission.READ_PHONE_STATE);
                allpermissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                allpermissions.add(android.Manifest.permission.VIBRATE);
                allpermissions.add(android.Manifest.permission.READ_PHONE_STATE);
                ArrayList<String> requestpermissions = new ArrayList<String>();
                for (String permission : allpermissions) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_DENIED) {
                        requestpermissions.add(permission);
                    }
                }
                if (requestpermissions.size() > 0) {
                    ActivityCompat.requestPermissions(this, requestpermissions.toArray(new String[requestpermissions.size()]), 101);
                }
            }
            submitobj.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (urlobj.getText().toString().equals("")) {
                        urlobj.setError("Username shouldn't be empty");
                    } else if (urlobj1.getText().toString().equals("")) {
                        urlobj1.setError("Password shouldn't be empty");
                    } else if (urlobj.getText().toString().contains("\n")) {
                        urlobj.setError("Username should be valid");
                    } else if (urlobj1.getText().toString().contains("\n")) {
                        urlobj1.setError("Password should be valid");
                    } else {
                        showprogressbar();
                        CSAppDetails csAppDetails = new CSAppDetails(Constants.appname, Constants.pid);
                        CSClientObj.initialize(Constants.server, Constants.port, csAppDetails);
                    }
                }
            });
            urlobj1.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_LEFT = 0;
                    final int DRAWABLE_TOP = 1;
                    final int DRAWABLE_RIGHT = 2;
                    final int DRAWABLE_BOTTOM = 3;
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (event.getRawX() >= (urlobj1.getRight() - urlobj1.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            if (showpassword) {
                                urlobj1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                showpassword = false;
                                urlobj1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eyenot, 0);
                            } else {
                                urlobj1.setTransformationMethod(null);
                                showpassword = true;
                                urlobj1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye, 0);
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
        } catch (Exception ex) {
        }
    }
    public void showprogressbar() {
        try {
            if (getApplicationContext() != null) {
                progressBarStatus = 0;
                progressBar = new ProgressDialog(LoginActivity.this);
                progressBar.setCancelable(false);
                progressBar.setMessage("Please Wait..");
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.setProgress(0);
                progressBar.show();
                h = new Handler();
                RunnableObj = new Runnable() {
                    public void run() {
                        h.postDelayed(this, delay);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                dismissprogressbar();
                                Toast.makeText(getApplicationContext(), "NetworkError", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                };
                h.postDelayed(RunnableObj, delay);
            }
        } catch (Exception ex) {
        }
    }
    public void dismissprogressbar() {
        try {
            if (progressBar != null) {
                progressBar.dismiss();
            }
            if (h != null) {
                h.removeCallbacks(RunnableObj);
            }
        } catch (Exception ex) {
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        try {
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            filter.addAction(CSEvents.CSCLIENT_LOGIN_RESPONSE);
            filter.addAction(CSEvents.CSCLIENT_INITILIZATION_RESPONSE);
            MainActivity.context = getApplicationContext();
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter);
        } catch (Exception ex) {
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
        } catch (Exception ex) {
        }
    }
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
    public class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    dismissprogressbar();
                    Toast.makeText(getApplicationContext(), "NetworkError", Toast.LENGTH_SHORT).show();
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_INITILIZATION_RESPONSE)) {
                    dismissprogressbar();
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        CSClientObj.enableNativeContacts(true, 91);
                        CSClientObj.login(urlobj.getText().toString(), urlobj1.getText().toString());
                    } else {
                        int retcode = intent.getIntExtra(CSConstants.RESULTCODE, 0);
                        if (retcode == CSConstants.E_409_NOINTERNET) {
                            showalert("No Internet Available");
                        } else {
                            Toast.makeText(getApplicationContext(), "INITILIZATIONFAILURE", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    dismissprogressbar();
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        Intent intentt = new Intent(getApplicationContext(), MainActivity.class);
                        startActivityForResult(intentt, 933);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception ex) {
            }
        }
    }
}
