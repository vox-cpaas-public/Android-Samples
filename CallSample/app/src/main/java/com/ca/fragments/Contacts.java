package com.ca.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.adapaters.ContactsAdapter;
import com.ca.adapaters.SimpleTextAdapter;
import com.ca.callsample.MainActivity;
import com.ca.callsample.R;
import com.ca.utils.utils;
import com.ca.wrapper.CSDataProvider;

import java.util.ArrayList;

/**
 * This class is handle All registered contacts and to perform calls
 */
public class Contacts extends Fragment {
    private ListView mContactListView;
    private EditText mContactSearchEdt;
    private ContactsAdapter appContactsAdapter;
    private static FragmentActivity mActivity;
    private TextView mNoContactFoundTv;
    private ImageView mSearchCancelImg;

    public Contacts() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContactListView = view.findViewById(R.id.contact_listView);
        mContactSearchEdt = view.findViewById(R.id.contact_search_edt);
        mNoContactFoundTv = view.findViewById(R.id.no_contact_found_tv);
        mSearchCancelImg = view.findViewById(R.id.contacts_search_cancel_img);
        try {
            // below logic will get all registered appContacts saved in Device and show it in listView
            appContactsAdapter = new ContactsAdapter(MainActivity.context, CSDataProvider.getAppContactsCursor(), 0);
            mContactListView.setAdapter(appContactsAdapter);

            // Contact listView item Click listener
            mContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        // below logic will get contact details from item position and show call options
                        Cursor cur;
                        String searchstring = mContactSearchEdt.getText().toString();
                        if (searchstring.equals("")) {
                            cur = CSDataProvider.getAppContactsCursor();
                        } else {
                            cur = CSDataProvider.getSearchAppContactsCursor(searchstring);
                        }
                        cur.moveToPosition(position);
                        String number = "";
                        number = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NUMBER));
                        cur.close();
                        if (!number.equals("")) {
                            showCallOptions(number);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

             // search cancel image click listener
            mSearchCancelImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mContactSearchEdt != null && mContactSearchEdt.getText().toString().length() > 0) {
                        mContactSearchEdt.setText("");
                    }
                }
            });
            // contact search editText text changed listener
            mContactSearchEdt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() > 0) {
                        mSearchCancelImg.setVisibility(View.VISIBLE);
                    } else {
                        mSearchCancelImg.setVisibility(View.GONE);
                    }
                    searchContact();
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
        } catch (Exception ex) {
        }
    }

    /**
     * This method will search the contact with given name and show the result in listView
     */
    public void searchContact() {
        try {
            appContactsAdapter.changeCursor(CSDataProvider.getSearchAppContactsCursor(mContactSearchEdt.getText().toString()));
            appContactsAdapter.notifyDataSetChanged();
            updateUI("updatenologtext");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method will update the UI accordingly SDK event
     *
     * @param str
     */
    public void updateUI(String str) {
        try {
            if (str.equals("NetworkError")) {
                // this block will handle network related events
            } else if (str.equals("shareStreamRessuccess")) {

            } else if (str.equals("updatenologtext")) {
                // this block will execute for contact update events
                Cursor ccr;
                if (!mContactSearchEdt.getText().toString().equals("")) {
                    ccr = CSDataProvider.getSearchAppContactsCursor(mContactSearchEdt.getText().toString());
                } else {
                    ccr = CSDataProvider.getAppContactsCursor();
                }
                // if contact cursor count is available it will hide no contact textView
                // if contact cursor count is not available it will show no contact textView
                if (ccr.getCount() <= 0) {
                    mNoContactFoundTv.setVisibility(View.VISIBLE);
                } else {
                    mNoContactFoundTv.setVisibility(View.INVISIBLE);
                }
                ccr.close();
            } else if (str.equals("isAppContactRessuccess") || str.equals("contactsupdated") || str.equals("imagesdbupdated") || str.equals("UserProfileChangedReq") || str.equals(CSEvents.CSCONTACTSANDGROUPS_CANDGUPDATED)) {

                // this block will execute for contact profile change events
                searchContact();
                updateUI("updatenologtext");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method will show the call options to user Audio/Video call
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
                                // if user select "Audio Call" below method will initiate AudioCall
                                utils.doNewVoiceCall(number, mActivity);
                            } else if (finalaction.equals("Video Call")) {
                                // if user select "Video Call" below method will initiate AudioCall
                                utils.doNewVideoCall(number, mActivity);
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
     * This BroadCast Receiver will handle all the SDK events related to contacts
     */
    public class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    updateUI("NetworkError");
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_IMAGESDBUPDATED)) {
                    updateUI("imagesdbupdated");
                } else if (intent.getAction().equals(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE)) {
                    updateUI("isAppContactRessuccess");
                } else if (intent.getAction().equals(CSEvents.CSCONTACTSANDGROUPS_CANDGUPDATED)) {
                    updateUI(CSEvents.CSCONTACTSANDGROUPS_CANDGUPDATED);
                } else if (intent.getAction().equals(CSEvents.CSCONTACTS_CONTACTSUPDATED)) {
                    updateUI("contactsupdated");
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_USERPROFILECHANGED)) {
                    updateUI("UserProfileChangedReq");
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
            // Register the receivers for Contacts Events from SDK
            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter5 = new IntentFilter(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE);
            IntentFilter filter6 = new IntentFilter(CSEvents.CSCLIENT_IMAGESDBUPDATED);
            IntentFilter filter7 = new IntentFilter(CSEvents.CSCONTACTS_CONTACTSUPDATED);
            IntentFilter filter8 = new IntentFilter(CSEvents.CSCLIENT_USERPROFILECHANGED);
            IntentFilter filter9 = new IntentFilter(CSEvents.CSCONTACTSANDGROUPS_CANDGUPDATED);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter6);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter7);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter8);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter9);
            searchContact();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            // this line will unregister the registered receivers
            LocalBroadcastManager.getInstance(MainActivity.context).unregisterReceiver(MainActivityReceiverObj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
