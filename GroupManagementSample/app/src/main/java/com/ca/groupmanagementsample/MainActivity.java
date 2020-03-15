package com.ca.groupmanagementsample;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.ca.dao.CSAppDetails;
import com.ca.dao.CSExplicitEventReceivers;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.multidex.MultiDex;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.Utils.CSConstants;
import com.ca.adapaters.GroupsAdapter;
import com.ca.chatsample.R;
import com.ca.utils.Constants;
import com.ca.utils.PreferenceProvider;
import com.ca.utils.utils;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;
import com.ca.wrapper.CSGroups;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static public TabLayout mTabLayout;
    public static ViewPager mViewPager;
    public static NotificationManager notificationManager;
    public static Context context;
    private CSClient CSClientObj = new CSClient();
    private GroupsAdapter mGroupsAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // this will register the receivers for SDK
        CSExplicitEventReceivers ccs = CSClientObj.getRegisteredExplicitEventReceivers();
        if (ccs.getCSChatReceiverReceiver().equals("")) {
            CSClientObj.registerExplicitEventReceivers(new CSExplicitEventReceivers("com.ca.receivers.CSUserJoined", "com.ca.receivers.CSCallReceiver", "com.ca.receivers.CSChatReceiver", "com.ca.receivers.CSGroupNotificationReceiver", "com.ca.receivers.CSCallMissed"));
            PreferenceProvider pff = new PreferenceProvider(getApplicationContext());
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


        // this method will get the permissions from native
        getOptimizerPermissions();


        ImageView addcontact = (ImageView) findViewById(R.id.addcontact);
        ListView mListView = (ListView) findViewById(R.id.appcontacts1);

        // create group button click listener
        addcontact.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // by clicking this button it will open create group screen
                Intent creategroupintent = new Intent(MainActivity.context, CreateGroupActivity.class);
                creategroupintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.context.startActivity(creategroupintent);
            }
        });

        // this will get the list of your groups
        mGroupsAdaptor = new GroupsAdapter(MainActivity.context, CSDataProvider.getGroupsCursor(), 0);
        mListView.setAdapter(mGroupsAdaptor);

        // list view lick listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    // by clicking any item in listView it will navigate to manage group screen
                    String groupid = "";

                    Cursor cur = CSDataProvider.getGroupsCursor();
                    cur.moveToPosition(position);
                    groupid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_ID));
                    cur.close();

                    if (!groupid.equals("")) {
                        Intent intentt = new Intent(MainActivity.context, ManageGroupActivity.class);
                        intentt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intentt.putExtra("grpid", groupid);
                        startActivity(intentt);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


    }

    /**
     * This method will show popup message to user to get optimized permissions based on device model
     */
    public void getOptimizerPermissions() {
        try {
            PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
            boolean dontshowagain = pf.getPrefBoolean("dontshowagain");
            if (!dontshowagain) {
                String manufacturer = Build.MANUFACTURER;
                int apilevel = Build.VERSION.SDK_INT;
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
                } else if ("sony".equalsIgnoreCase(manufacturer)) {
                } else if ("htc".equalsIgnoreCase(manufacturer)) {
                } else if ("lenovo".equalsIgnoreCase(manufacturer)) {
                } else if (apilevel >= 23) {
                }
                List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (list.size() <= 0) {
                    return;
                }
                utils.showIntentAlert("Enable autostart permission to receive notifications", MainActivity.this, intent);
                PreferenceProvider pff = new PreferenceProvider(getApplicationContext());
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
     * This method will get all group list and show it in list view
     */
    public void refreshView() {
        try {
            mGroupsAdaptor.changeCursor(CSDataProvider.getGroupsCursor());
            mGroupsAdaptor.notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This broadCast receiver will receive the events form SDK
     */
    public class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    // this block will handle the network events
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    // this block will handle the login response
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        CSGroups CSGroupsObj = new CSGroups();
                        CSGroupsObj.pullMyGroupsList();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    refreshView();

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
            // register the receivers for SDK events
            IntentFilter filter1 = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            filter1.addAction(CSEvents.CSCLIENT_LOGIN_RESPONSE);
            filter1.addAction(CSEvents.CSCLIENT_IMAGESDBUPDATED);
            filter1.addAction(CSEvents.CSGROUPS_CREATEGROUP_RESPONSE);
            filter1.addAction(CSEvents.CSGROUPS_PULLGROUPDETAILS_RESPONSE);
            filter1.addAction(CSEvents.CSGROUPS_PULLMYGROUPLIST_RESPONSE);

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);
            notificationManager.cancelAll();
            // if application not login state below logic will initialize the application to server with user details
            if (!CSDataProvider.getLoginstatus()) {
                CSAppDetails csAppDetails = new CSAppDetails("GroupManagementSample",  "pid_39684a6d_5103_4254_9775_1b923b9b98d5");
                CSClientObj.initialize(Constants.server, Constants.port, csAppDetails);
            } else {
                // if app already in registered state it will get all list of groups from below API
                CSGroups CSGroupsObj = new CSGroups();
                CSGroupsObj.pullMyGroupsList();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            // this will unregister the registered receivers
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            getApplicationContext().unregisterReceiver(MainActivityReceiverObj);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}