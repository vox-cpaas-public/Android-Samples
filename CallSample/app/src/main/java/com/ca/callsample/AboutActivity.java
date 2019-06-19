package com.ca.callsample;

import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.ca.wrapper.CSClient;

public class AboutActivity extends AppCompatActivity {
    private TextView mAppVersionTv, mSDKVersionTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mAppVersionTv = findViewById(R.id.app_version_tv);
        mSDKVersionTv = findViewById(R.id.sdk_version_tv);
        CSClient CSClientObj = new CSClient();
        // this will get the nSDK version
        mSDKVersionTv.setText("SDK Version: " + CSClientObj.getVersion());
        // below logic will get the App version
        try {
            mAppVersionTv.setText("App Version: " + this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
