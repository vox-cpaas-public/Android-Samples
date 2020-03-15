package com.ca.clicktocall;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.multidex.MultiDex;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This class will initialize the application to server and generate OTP
 */
public class ClickToCallActivity extends Activity {

    private TextView audiocall,videocall,pstncall;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.clickactivity);
            audiocall = findViewById(R.id.sign_up_button_tv);
            videocall = findViewById(R.id.sign_up_button_tv1);
            pstncall = findViewById(R.id.sign_up_button_tv2);

            audiocall.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ClickToCallApi.isAppReady()) {
                    ClickToCallApi.doNewVoiceCall(ClickToCallActivity.this,ClickToCallConstants.destinationid);
                    } else {
                        Utils.showSimpleAlert(ClickToCallActivity.this,"Plz wait. Login is in progress");
                    }
                }
            });

            videocall.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ClickToCallApi.isAppReady()) {
                        ClickToCallApi.doNewVideoCall(ClickToCallActivity.this, ClickToCallConstants.destinationid);
                    } else {
                        Utils.showSimpleAlert(ClickToCallActivity.this,"Plz wait. Login is in progress");
                    }
                }
            });
            pstncall.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ClickToCallApi.isAppReady()) {
                    ClickToCallApi.doNewPSTNCall(ClickToCallActivity.this,ClickToCallConstants.destinationid);
                    } else {
                        Utils.showSimpleAlert(ClickToCallActivity.this,"Plz wait. Login is in progress");
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



    }

