package com.ca.callsample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDex;

import com.ca.Utils.CSDbFields;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // below line of code will get the SignUp status from SDK
        boolean isAlreadySignedUp = CSDataProvider.getSignUpstatus();
        // if user already login into application launch the MainActivity
        // if Application opens first time navigate to SignUp Activity
        if (isAlreadySignedUp) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        } else {
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        }
        finish();
    }


    @Override
    public void onResume() {
        super.onResume();
        return;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        return;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
