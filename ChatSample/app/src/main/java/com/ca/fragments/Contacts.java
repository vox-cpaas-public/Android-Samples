package com.ca.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.adapaters.ContactsAdapter;
import com.ca.chatsample.ChatAdvancedActivity;
import com.ca.chatsample.MainActivity;
import com.ca.chatsample.R;
import com.ca.utils.utils;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Contacts extends Fragment {
    public static long mLastClickTime = 0;
    //ListView mListView;

    static EditText editText;
    private ImageView mSearchCancelImg;
    ContactsAdapter appContactsAdapter;
    static FragmentActivity mActivity;
    TextView nocontactsview;


    RecyclerView rv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getActivity().getWindow().setBackgroundDrawableResource(R.color.background);
        } catch (Exception ex) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
          return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //LOG.info("onViewCreated:");
        //mListView = (ListView) view.findViewById(R.id.appcontacts1);
        //addcontact = view.findViewById(R.id.addcontact);
        editText = (EditText) view.findViewById(R.id.editText);
        nocontactsview = (TextView) view.findViewById(R.id.no_contact_found_tv);
        mSearchCancelImg = view.findViewById(R.id.contacts_search_cancel_img);

        rv = (RecyclerView) view.findViewById(R.id.chat_layout);

        appContactsAdapter = new ContactsAdapter(MainActivity.context, CSDataProvider.getAppContactsCursor());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.context);
        rv.setNestedScrollingEnabled(false);
        rv.setLayoutManager(mLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(appContactsAdapter);

        mSearchCancelImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText != null && editText.getText().toString().length() > 0) {
                    editText.setText("");
                }
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                System.out.println("Yes on touch up:" + editText.getText().toString());
                if (charSequence.length() > 0) {
                    mSearchCancelImg.setVisibility(View.VISIBLE);
                } else {
                    mSearchCancelImg.setVisibility(View.GONE);
                }
                refreshview();
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });





    }

    public void refreshview() {

        try {




            if (!editText.getText().toString().equals("")) {
                appContactsAdapter.swapCursorAndNotifyDataSetChanged(CSDataProvider.getSearchAppContactsCursor(editText.getText().toString()));

            } else {
                appContactsAdapter.swapCursorAndNotifyDataSetChanged(CSDataProvider.getAppContactsCursor());

            }
            updateUI("updatenologtext");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateUI(String str) {

        try {

            if (str.equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                ////LOG.info("NetworkError receieved");
                ////Toast.makeText(MainActivity.context, "NetworkError", Toast.LENGTH_SHORT).show();
            } else if (str.equals("updatenologtext")) {
                //LOG.info("updatenologtext");
                Cursor ccr;
                if (!editText.getText().toString().equals("")) {

                        ccr = CSDataProvider.getSearchAppContactsCursor(editText.getText().toString());

                } else {

                        ccr = CSDataProvider.getAppContactsCursor();

                }

                if (ccr.getCount() <= 0) {
                    nocontactsview.setVisibility(View.VISIBLE);
                } else {
                    nocontactsview.setVisibility(View.INVISIBLE);
                }
                ccr.close();

            } else if (str.equals(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE) || str.equals(CSEvents.CSCONTACTS_CONTACTSUPDATED) || str.equals(CSEvents.CSCLIENT_IMAGESDBUPDATED) || str.equals(CSEvents.CSCLIENT_USERPROFILECHANGED) || str.equals(CSEvents.CSCONTACTSANDGROUPS_CANDGUPDATED)) {
                refreshview();
                updateUI("updatenologtext");
            }
        } catch (Exception ex) {
        }

    }





    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                //LOG.info("Yes Something receieved in RecentReceiver in contacts:" + intent.getAction().toString());


                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    updateUI(CSEvents.CSCLIENT_NETWORKERROR);
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_IMAGESDBUPDATED)) {
                    updateUI(CSEvents.CSCLIENT_IMAGESDBUPDATED);
                } else if (intent.getAction().equals(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        updateUI(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE);
                    }
                } else if (intent.getAction().equals(CSEvents.CSCONTACTSANDGROUPS_CANDGUPDATED)) {
                    updateUI(CSEvents.CSCONTACTSANDGROUPS_CANDGUPDATED);
                } else if (intent.getAction().equals(CSEvents.CSCONTACTS_CONTACTSUPDATED)) {
                    updateUI(CSEvents.CSCONTACTS_CONTACTSUPDATED);
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_USERPROFILECHANGED)) {
                    updateUI(CSEvents.CSCLIENT_USERPROFILECHANGED);
                }


            } catch (Exception ex) {
            }
        }


    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Code here
        ////LOG.info("On attach called9");

        if (context instanceof Activity) {
            mActivity = (FragmentActivity) context;
        }


    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Code here
            ////LOG.info("On attach called3");

            mActivity = (FragmentActivity) activity;

        }
    }


    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    public void onResume() {
        super.onResume();

        try {

            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter5 = new IntentFilter(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE);
            IntentFilter filter6 = new IntentFilter(CSEvents.CSCLIENT_IMAGESDBUPDATED);
            IntentFilter filter7 = new IntentFilter(CSEvents.CSCONTACTS_CONTACTSUPDATED);
            IntentFilter filter8 = new IntentFilter(CSEvents.CSCLIENT_USERPROFILECHANGED);
            IntentFilter filter9 = new IntentFilter(CSEvents.CSCONTACTSANDGROUPS_CANDGUPDATED);

            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter);
            //LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj,filter5);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter6);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter7);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter8);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter9);



        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            LocalBroadcastManager.getInstance(MainActivity.context).unregisterReceiver(MainActivityReceiverObj);
        } catch (Exception ex) {
        }


    }

    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getApplicationWindowToken(), 0);
        editText.clearFocus();
        editText.setText("");
    }



    public static void handleclick(int position) {
        try {

            String searchstring = editText.getText().toString();


    Cursor cur;

        if (searchstring.equals("")) {
            cur = CSDataProvider.getAppContactsCursor();
        } else {
            cur = CSDataProvider.getSearchAppContactsCursor(searchstring);
        }
        //LOG.info("Number of contacts:" + cur.getCount());
        cur.moveToPosition(position);

        String number = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NUMBER));

        cur.close();

        if (!number.equals("")) {
            Intent intent = new Intent(mActivity, ChatAdvancedActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("Sender", number);
            intent.putExtra("IS_GROUP", false);
            mActivity.startActivity(intent);
    }

                View view = mActivity.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

        } catch (Exception ex) {
            utils.logStacktrace(ex);
        }
    }






}
