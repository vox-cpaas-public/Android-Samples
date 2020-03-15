package com.ca.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSEvents;
import com.ca.adapaters.RecentsAdapter;
import com.ca.adapaters.SimpleTextAdapter;
import com.ca.callsample.MainActivity;
import com.ca.callsample.R;
import com.ca.utils.utils;
import com.ca.wrapper.CSDataProvider;

import java.util.ArrayList;
import java.util.List;

public class Recents extends Fragment {
    private ListView mCallLogsListView;
    private FloatingActionButton mDeleteCallLogsImg;
    private RecentsAdapter appContactsAdapter;
    private static FragmentActivity mActivity;
    private TextView mNoLogsFoundTv;

    public Recents() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recents, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDeleteCallLogsImg = view.findViewById(R.id.delete_call_log_img);
        mNoLogsFoundTv = view.findViewById(R.id.no_call_logs_found_tv);
        mCallLogsListView = view.findViewById(R.id.call_logs_listView);

        // below logic will get all call logs from callLogs DB and show it in Adapter
        appContactsAdapter = new RecentsAdapter(MainActivity.context, CSDataProvider.getCallLogCursorGroupedByNumberAndDirection(), 0);
        mCallLogsListView.setAdapter(appContactsAdapter);

        // ListView click listener
        mCallLogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    // below logic will get list item numbers based on item position from Cursor
                    Cursor cur = CSDataProvider.getCallLogCursorGroupedByNumberAndDirection();
                    cur.moveToPosition(position);
                    String number = "";
                    number = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_NUMBER));
                    cur.close();
                    if (!number.equals("")) {
                        showCallOptions(number);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        // listView LongClick listener
        mCallLogsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                // below logic will get the list item details based on item position from cursor
                Cursor cur = CSDataProvider.getCallLogCursorGroupedByNumberAndDirection();
                cur.moveToPosition(pos);
                String number = "";
                String direction = "";
                number = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_NUMBER));
                direction = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DIR));
                cur.close();
                // after getting all the details below method will show delete and add contact options to user
                showoptions(pos, number, direction);
                return true;
            }
        });
        // call logs delete image click listener
        mDeleteCallLogsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // below logic will get call logs cursor from CSDataProvider
                Cursor ccr = CSDataProvider.getCallLogCursorGroupedByNumberAndDirection();
                // if call logs count greater that zero it will show popup message to user to delete all call logs
                if (ccr.getCount() > 0) {
                    showAlertForDeleteAllCallLogs();
                }
                ccr.close();
            }
        });
    }

    /**
     * This method will show the confirmation dialog to user to delete all call history
     *
     * @return
     */
    public boolean showAlertForDeleteAllCallLogs() {
        try {
            AlertDialog.Builder successfullyLogin = new AlertDialog.Builder(getActivity());
            successfullyLogin.setTitle(getString(R.string.user_alert_dialog_tittle));
            successfullyLogin.setMessage("Delete All History?");
            successfullyLogin.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            CSDataProvider.deleteAllCallLog();
                            appContactsAdapter.changeCursor(CSDataProvider.getCallLogCursorGroupedByNumberAndDirection());
                            appContactsAdapter.notifyDataSetChanged();
                            updateUI("updatenologtext");
                            MainActivity.updateRecentsBadgeCount();
                        }
                    });
            successfullyLogin.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                        }
                    });
            successfullyLogin.show();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * This method will show call options dialog with Audio and Video Call options
     *
     * @param number
     * @return
     */
    public boolean showCallOptions(final String number) {
        try {
            final ArrayList<String> grpoptions = new ArrayList<>();
            android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(getActivity());
            successfullyLogin.setTitle("Options");
            successfullyLogin.setCancelable(true);
            grpoptions.clear();
            grpoptions.add("Audio Call");
            grpoptions.add("Video Call");
            SimpleTextAdapter simpleTextAdapter = new SimpleTextAdapter(MainActivity.context, grpoptions);
            successfullyLogin.setAdapter(simpleTextAdapter,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            String finalaction = grpoptions.get(which);
                            if (finalaction.equals("Audio Call")) {
                                // if user select Audio call it will initiate audio call
                                utils.doNewVoiceCall(number, mActivity);
                            } else if (finalaction.equals("Video Call")) {
                                // if user select Audio call it will initiate video call
                                utils.doNewVideoCall(number, mActivity);
                            }
                        }
                    });
            successfullyLogin.show();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * This method will show popup message to user delete single call log/ add contact options
     *
     * @param position
     * @param number
     * @param direction
     * @return
     */
    public boolean showoptions(final int position, final String number, final String direction) {
        try {
            final ArrayList<String> grpoptions = new ArrayList<>();
            AlertDialog.Builder successfullyLogin = new AlertDialog.Builder(getActivity());
            successfullyLogin.setTitle("Options");
            successfullyLogin.setCancelable(true);
            grpoptions.clear();
            grpoptions.add("Delete");
            // below line will get cursor from DB with selected number
            Cursor csr = CSDataProvider.getContactCursorByNumber(number);
            // if cursor count available get the contact ID and check if contact exist or not in device
            // if count not available add "Add To Contact" option
            if (csr.getCount() > 0) {
                csr.moveToNext();
                String idd = csr.getString(csr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
                String rawnumber = csr.getString(csr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_RAW_NUMBER));
                // if selected call logs number not saved in device it will show "Add To Contacts" message along with "delete call log"
                if (!iscontactexists(idd, rawnumber)) {
                    grpoptions.add("Add To Contacts");
                }
            } else {
                // if selected call logs number not saved in device it will show "Add To Contacts" message along with "delete call log"
                grpoptions.add("Add To Contacts");
            }
            csr.close();
            SimpleTextAdapter simpleTextAdapter = new SimpleTextAdapter(MainActivity.context, grpoptions);
            successfullyLogin.setAdapter(simpleTextAdapter,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            String finalaction = grpoptions.get(which);
                            if (finalaction.equals("Delete")) {
                                // if user select "Delete" option delete the call logs from DB with bellow API from CSDataProvider and refresh the call logs from DB again
                                Cursor ccr = CSDataProvider.getCallLogCursorByTwoFilters(CSDbFields.KEY_CALLLOG_NUMBER, number, CSDbFields.KEY_CALLLOG_DIR, direction);
                                while (ccr.moveToNext()) {
                                    int rowid = ccr.getInt(ccr.getColumnIndexOrThrow(CSDbFields.KEY_ID));
                                    CSDataProvider.deleteCallLogByRowId(rowid);
                                }
                                ccr.close();
                                appContactsAdapter.changeCursor(CSDataProvider.getCallLogCursorGroupedByNumberAndDirection());
                                appContactsAdapter.notifyDataSetChanged();
                                updateUI("updatenologtext");
                                MainActivity.updateRecentsBadgeCount();
                            } else if (finalaction.equals("Add To Contacts")) {
                                // if user select "Add To Contacts" option Call Contact insertion Intent to Native
                                Intent intent = new Intent(Intent.ACTION_INSERT);
                                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                                intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
                                startActivity(intent);
                            }
                        }
                    });
            successfullyLogin.show();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Below method will update the UI accordingly SDK events
     *
     * @param str
     */
    public void updateUI(String str) {
        try {
            if (str.equals("NetworkError")) {
                // this block will execute for network related events
            } else if (str.equals("isAppContactRessuccess") || str.equals("contactsupdated") || str.equals("ChatUpdated") || str.equals("UserProfileChangedReq") || str.equals("imagesdbupdated")) {
                // this block will execute if user details changed case and new entry in Call logs DB
                appContactsAdapter.changeCursor(CSDataProvider.getCallLogCursorGroupedByNumberAndDirection());
                appContactsAdapter.notifyDataSetChanged();
                updateUI("updatenologtext");
            } else if (str.equals("updatenologtext")) {
                // this block will execute for no callLogs event
                Cursor ccr = CSDataProvider.getCallLogCursorGroupedByNumberAndDirection();
                // if CallLogs cursor count available it will hide No Log text and show Delete call logs image
                // if CallLogs cursor count not available it will show No Log text and hide Delete call logs image
                if (ccr.getCount() <= 0) {
                    mNoLogsFoundTv.setVisibility(View.VISIBLE);
                    mDeleteCallLogsImg.setVisibility(View.INVISIBLE);
                } else {
                    mNoLogsFoundTv.setVisibility(View.INVISIBLE);
                    mDeleteCallLogsImg.setVisibility(View.VISIBLE);
                }
                ccr.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This BroadCastReceiver will handle all the events from SDK
     */
    public class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    // this will handle network related events
                    updateUI("NetworkError");
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_IMAGESDBUPDATED)) {
                    // this block will handle profile image update events
                    updateUI("imagesdbupdated");
                } else if (intent.getAction().equals(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE)) {
                    // this block will handle given contact is app contact or not events
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        updateUI("isAppContactRessuccess");
                    }
                } else if (intent.getAction().equals(CSEvents.CSCONTACTS_CONTACTSUPDATED)) {
                    // this block will handle if contact details updated Case
                    updateUI("contactsupdated");
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_USERPROFILECHANGED)) {
                    // this block will handle for user profile change events
                    updateUI("UserProfileChangedReq");
                } else if (intent.getAction().equals(CSEvents.CSCALL_CALLLOGUPDATED)) {
                    // this block will handle for callLogs change events
                    appContactsAdapter.changeCursor(CSDataProvider.getCallLogCursorGroupedByNumberAndDirection());
                    appContactsAdapter.notifyDataSetChanged();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (FragmentActivity) context;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mActivity = (FragmentActivity) activity;
        }
    }

    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    public void onResume() {
        super.onResume();
        try {
            // Register the receivers for callLogs SDK events
            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter5 = new IntentFilter(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE);
            IntentFilter filter6 = new IntentFilter(CSEvents.CSCLIENT_IMAGESDBUPDATED);
            IntentFilter filter7 = new IntentFilter(CSEvents.CSCONTACTS_CONTACTSUPDATED);
            IntentFilter filter8 = new IntentFilter(CSEvents.CSCALL_CALLLOGUPDATED);
            IntentFilter filter10 = new IntentFilter(CSEvents.CSCLIENT_USERPROFILECHANGED);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter5);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter6);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter7);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter8);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter10);
            appContactsAdapter.changeCursor(CSDataProvider.getCallLogCursorGroupedByNumberAndDirection());
            appContactsAdapter.notifyDataSetChanged();
            updateUI("updatenologtext");
            Cursor ccr = CSDataProvider.getCallLogCursorGroupedByNumberAndDirection();
            // below logic will show/hide deleteCallLog image based on callLogs count
            if (ccr.getCount() <= 0) {
                mDeleteCallLogsImg.setVisibility(View.INVISIBLE);
            } else {
                mDeleteCallLogsImg.setVisibility(View.VISIBLE);
            }
            ccr.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            // below line will unregister the SDK event receivers
            LocalBroadcastManager.getInstance(MainActivity.context).unregisterReceiver(MainActivityReceiverObj);
        } catch (Exception ex) {
        }
    }

    /**
     * This method will check if given contact number is exist or not in device contacts
     *
     * @param id
     * @param number
     * @return
     */
    public boolean iscontactexists(String id, String number) {
        boolean retvalue = false;
        String phone = "";
        try {
            List numbers = new ArrayList<String>();
            Cursor cursor = MainActivity.context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{id}, null);
            while (cursor.moveToNext()) {
                phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                numbers.add(phone);
            }
            cursor.close();
            if (numbers.contains(number)) {
                retvalue = true;
            } else {
                retvalue = false;
            }
            numbers.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retvalue;
    }
}
