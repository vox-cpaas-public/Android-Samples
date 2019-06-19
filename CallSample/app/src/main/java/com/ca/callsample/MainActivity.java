package com.ca.callsample;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;

import com.ca.dao.CSAppDetails;
import com.ca.dao.CSExplicitEventReceivers;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.Utils.CSConstants;
import com.ca.fragments.Contacts;
import com.ca.fragments.Recents;
import com.ca.utils.Constants;
import com.ca.utils.PrefereceProvider;
import com.ca.utils.utils;
import com.ca.wrapper.CSCall;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static TabLayout mTabLayout;
    public static ViewPager mViewPager;
    public static NotificationManager mNotificationManager;
    public static Context context;
    public static boolean showLogInFailure = false;
    private CSClient CSClientObj = new CSClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        mViewPager = findViewById(R.id.viewpager);
        setupViewPager(mViewPager);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mTabLayout = findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(2);
        showLogInFailure = true;
        android.support.v7.widget.Toolbar toolbar =  findViewById(R.id.toolbar);
        android.support.design.widget.AppBarLayout appBarLayout =  findViewById(R.id.appbarlayout);
        setSupportActionBar(toolbar);
        //toolbar.setTitle("Call sample");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // below logic will register the call receivers to SDK
        CSExplicitEventReceivers ccs = CSClientObj.getRegisteredExplicitEventReceivers();
        if (ccs.getCSChatReceiverReceiver().equals("")) {
            CSClientObj.registerExplicitEventReceivers(new CSExplicitEventReceivers("com.ca.receivers.CSUserJoined", "com.ca.receivers.CSCallReceiver", "com.ca.receivers.CSChatReceiver", "com.ca.receivers.CSGroupNotificationReceiver", "com.ca.receivers.CSCallMissed"));
            PrefereceProvider pff = new PrefereceProvider(getApplicationContext());
            pff.setPrefboolean("registerreceivers", true);
        }

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

        new CSCall().setPreferredAudioCodec(CSConstants.PreferredAudioCodec.G729);
        new CSCall().enableDefaultlocalVideoPreviewUX(true);
        getOptimizerPermissions();


    }

    /**
     * This method will show optimization permission based on device
     */
    public void getOptimizerPermissions() {
        try {
            PrefereceProvider pf = new PrefereceProvider(getApplicationContext());
            boolean dontshowagain = pf.getPrefBoolean("dontshowagain");
            if (!dontshowagain) {
                //boolean checkpermission = false;


                String manufacturer = android.os.Build.MANUFACTURER;
                int apilevel = Build.VERSION.SDK_INT;

                //LOG.info("manufacturer:" + manufacturer);
                //LOG.info("apilevel:" + apilevel);

                Intent intent = new Intent();
                if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
                } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                } else if ("oneplus".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListAct‌​ivity"));
                } else if ("huawei".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                } else if ("samsung".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity"));
                } else if ("asus".equalsIgnoreCase(manufacturer)) {

/*
                    PackageManager pm = getPackageManager();
                    List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                    for (ApplicationInfo packageInfo : packages)
                    {
                        LOG.info("Installed package :" + packageInfo.packageName);
                        LOG.info("Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
                    }*/


                } else if ("sony".equalsIgnoreCase(manufacturer)) {

                } else if ("htc".equalsIgnoreCase(manufacturer)) {

                } else if ("lenovo".equalsIgnoreCase(manufacturer)) {

                } else if (apilevel >= 23) {

                }


                List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (list.size() <= 0) {
                    utils.showSettingsAlert("Enable autostart permission to receive notifications",MainActivity.this);
                    //return;
                } else {
                    utils.showIntentAlert("Enable autostart permission to receive notifications", MainActivity.this, intent);
                }

                //if(!checkpermission) {

                PrefereceProvider pff = new PrefereceProvider(getApplicationContext());
                pff.setPrefboolean("dontshowagain", true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // this will open About screen once user click the about option under menu
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                // if selected item id is equal to about id it will open About activity
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // This will show About option to open About screen
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    /**
     * This method will update the tabs once user
     *
     * @param currenttab
     */
    private void updateTabs(int currenttab) {
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setSelectedTabIndicatorHeight(0);
        switch (currenttab) {
            case 0:
                mTabLayout.getTabAt(0).setCustomView(R.layout.recents_colored_tab);
                mTabLayout.getTabAt(1).setCustomView(R.layout.contacts_tab);
                try {
                    Cursor cursor = CSDataProvider.getCallLogCursorByFilter(CSDbFields.KEY_CALLLOG_MISSEDCALL_NOTIFIED, 0);
                    while (cursor.moveToNext()) {
                        String direction = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DIR));
                        if (direction.contains("MISSED")) {
                            String callid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_CALLID));
                            CSDataProvider.UpdateCallLogbyCallidAndFilter(callid, CSDbFields.KEY_CALLLOG_MISSEDCALL_NOTIFIED, 1);
                        }
                    }
                    cursor.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            case 1:
                mTabLayout.getTabAt(0).setCustomView(R.layout.recents_tab);
                mTabLayout.getTabAt(1).setCustomView(R.layout.contacts_colored_tab);
                break;
            default:
                break;
        }
        mTabLayout.getTabAt(0).setText("Recents");
        updateRecentsBadgeCount();
    }

    /**
     * This method will setup viewpager with tabs
     *
     * @param viewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Recents(), "Recents");
        adapter.addFragment(new Contacts(), "Contacts");
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Constants.tab_selected = position;
                hideKeyboard();
                updateTabs(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    /**
     * This adapter add contacts and recents fragments to Viewpager
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    /**
     * This broadCast receiver will handle all SDK events
     */
    public class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    // This if block will responds for network related evenets
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    // this block will responds for Login response
                } else if (intent.getAction().equals(CSEvents.CSCALL_CALLLOGUPDATED)) {
                    // this block will updated for call logs update
                    updateRecentsBadgeCount();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    public void onResume() {
        super.onResume();
        try {
            // Register the receivers for SDK event
            IntentFilter filter1 = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            filter1.addAction(CSEvents.CSCLIENT_INITILIZATION_RESPONSE);
            filter1.addAction(CSEvents.CSCLIENT_LOGIN_RESPONSE);
            filter1.addAction(CSEvents.CSCALL_CALLLOGUPDATED);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);
            mNotificationManager.cancelAll();

            // if application not login state below logic will initialize the application to server with user details
            if (!CSDataProvider.getLoginstatus()) {
                CSAppDetails csAppDetails = new CSAppDetails("CallSample", "pid_39684a6d_5103_4254_9775_1b923b9b98d5");
                CSClientObj.initialize(Constants.server, Constants.port, csAppDetails);
            }
            updateRecentsBadgeCount();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            // below logic will unregister the registered receivers before closing application
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            getApplicationContext().unregisterReceiver(MainActivityReceiverObj);
            showLogInFailure = false;
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
     * This method will hide the native soft keypad if it open
     */
    private void hideKeyboard() {
        try {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method will update the badge count for missed calls tabLayout
     */
    public static void updateRecentsBadgeCount() {
        try {
            Cursor cr = CSDataProvider.getCallLogCursorByFilter(CSDbFields.KEY_CALLLOG_MISSEDCALL_NOTIFIED, 0);
            int chat_unread_count = cr.getCount();
            cr.close();
            TextView badgecount = (TextView) mTabLayout.getTabAt(0).getCustomView().findViewById(R.id.badge_textView);
            if (chat_unread_count <= 0) {
                badgecount.setVisibility(View.GONE);
            } else {
                badgecount.setVisibility(View.VISIBLE);
                badgecount.setText(String.valueOf(chat_unread_count));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}