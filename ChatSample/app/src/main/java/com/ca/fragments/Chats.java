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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.Utils.CSExplicitEvents;
import com.ca.adapaters.ChatsAdapter;
import com.ca.chatsample.ChatAdvancedActivity;
import com.ca.chatsample.MainActivity;
import com.ca.chatsample.R;
import com.ca.utils.utils;
import com.ca.wrapper.CSChat;
import com.ca.wrapper.CSDataProvider;

public class Chats extends Fragment {

    //ListView mListView;

    public  static EditText editText;

    private ImageView mSearchCancelImg;
    RecyclerView rv;
    public static int contactstypetoload = 0;//0- normal contacts 1 for app contacts
    static ChatsAdapter appContactsAdapter;
    static FragmentActivity mActivity;
    static TextView mytextview;
    FloatingActionButton newchat;
   private String TAG="FirstCallChats";
    public Chats() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //LOG.info("onViewCreated:");
        //mListView = (ListView) view.findViewById(R.id.appcontacts1);
        editText = (EditText) view.findViewById(R.id.editText);
        mytextview = (TextView) view.findViewById(R.id.textview);
        newchat = view.findViewById(R.id.newchat);
        mSearchCancelImg = view.findViewById(R.id.chat_search_cancel_img);
        rv = (RecyclerView) view.findViewById(R.id.chat_layout);

        try {

/*
            newchat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.context, ShowAppContactsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("uiaction", UIACTION_NEWCHAT);
                    MainActivity.context.startActivity(intent);
                }
            });
*/

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

                    //LOG.info("Yes on touch up:" + editText.getText().toString());
                    if (charSequence.length() > 0) {
                        mSearchCancelImg.setVisibility(View.VISIBLE);
                    } else {
                        mSearchCancelImg.setVisibility(View.GONE);
                    }
                    refreshView();
                }

                @Override
                public void afterTextChanged(Editable editable) {


                }
            });

        } catch (Exception ex) {

        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
/*
        if(requestCode == 9199) {
            //LOG.info("onActivityResult called here");
            new Thread(new Runnable() {
                public void run() {
                    IAmLiveCore IAmLiveCoreObj = new IAmLiveCore();
                    IAmLiveCoreObj.readRawContacts();
                }
            }).start();
        }
        */
    }

    public static void refreshView() {
        try {
            if (!editText.getText().toString().equals("")) {

                appContactsAdapter.swapCursorAndNotifyDataSetChanged(CSDataProvider.getSearchInChatNamesnMessagesCursor(editText.getText().toString()));
                //appContactsAdapter.notifyDataSetChanged();

            } else {
                Log.i("", "refreshView: ");
                appContactsAdapter.swapCursorAndNotifyDataSetChanged(CSDataProvider.getChatCursorGroupedByRemoteId());
                //appContactsAdapter.notifyDataSetChanged();

            }

            Cursor ccr;
            if (!editText.getText().toString().equals("")) {

                ccr = CSDataProvider.getSearchInChatNamesnMessagesCursor(editText.getText().toString());


            } else {
                ccr = CSDataProvider.getChatCursorGroupedByRemoteId();

            }

            if (ccr.getCount() <= 0) {
                mytextview.setVisibility(View.VISIBLE);
            } else {
                mytextview.setVisibility(View.INVISIBLE);
            }
            ccr.close();


            //updateUI("updatenologtext");
        } catch (Exception ex) {
            utils.logStacktrace(ex);
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

                    ccr = CSDataProvider.getSearchInChatNamesnMessagesCursor(editText.getText().toString());


                } else {
                    ccr = CSDataProvider.getChatCursorGroupedByRemoteId();

                }

                if (ccr.getCount() <= 0) {
                    mytextview.setVisibility(View.VISIBLE);
                } else {
                    mytextview.setVisibility(View.INVISIBLE);
                }
                ccr.close();

            } else if (str.equals(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE) || str.equals(CSEvents.CSCONTACTS_CONTACTSUPDATED) || str.equals(CSEvents.CSCHAT_CHATUPDATED) || str.equals(CSEvents.CSCLIENT_USERPROFILECHANGED) || str.equals(CSEvents.CSCLIENT_IMAGESDBUPDATED)||str.equals(CSEvents.CSGROUPS_GROUPINFO_UPDATED)) {
                //LOG.info("isAppContactRessuccess or imagesdbupdated or contactsupdated");
                refreshView();
                updateUI("updatenologtext");
            }
        } catch (Exception ex) {
        }

    }

    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                //LOG.info("Yes Something receieved in RecentReceiver "+intent.getAction());


                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    updateUI(CSEvents.CSCLIENT_NETWORKERROR);
                } else if (intent.getAction().equals(CSEvents.CSGROUPS_GROUPINFO_UPDATED)) {
                    updateUI(CSEvents.CSGROUPS_GROUPINFO_UPDATED);
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_IMAGESDBUPDATED)) {
                    updateUI(CSEvents.CSCLIENT_IMAGESDBUPDATED);
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_USERPROFILECHANGED)) {
                    updateUI(CSEvents.CSCLIENT_USERPROFILECHANGED);
                } else if (intent.getAction().equals(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE)) {

                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        updateUI(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE);
                    }
                } else if (intent.getAction().equals(CSEvents.CSCONTACTS_CONTACTSUPDATED)) {
                    updateUI(CSEvents.CSCONTACTS_CONTACTSUPDATED);
                } else if (intent.getAction().equals(CSEvents.CSCHAT_CHATUPDATED) || intent.getAction().equals(CSExplicitEvents.CSChatReceiver)) {
                    updateUI(CSEvents.CSCHAT_CHATUPDATED);
                    MainActivity.updateChatBadgeCount();
                } else if (intent.getAction().equals(CSEvents.CSGROUPS_PULLGROUPDETAILS_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        refreshView();
                    }
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
            IntentFilter filter8 = new IntentFilter(CSEvents.CSCHAT_CHATUPDATED);
            IntentFilter filter9 = new IntentFilter(CSExplicitEvents.CSChatReceiver);
            IntentFilter filter10 = new IntentFilter(CSEvents.CSCLIENT_USERPROFILECHANGED);
            IntentFilter filter11 = new IntentFilter(CSEvents.CSGROUPS_PULLGROUPDETAILS_RESPONSE);
            IntentFilter filter12=new IntentFilter(CSEvents.CSGROUPS_GROUPINFO_UPDATED);


            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter5);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter6);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter7);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter8);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter9);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter10);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter11);
            LocalBroadcastManager.getInstance(MainActivity.context).registerReceiver(MainActivityReceiverObj, filter12);
            MainActivity.context.registerReceiver(MainActivityReceiverObj, filter9);


            //appContactsAdapter = new ChatsAdapter(MainActivity.context, CSDataProvider.getChatCursorGroupedByRemoteId(), 0);
            //mListView.setAdapter(appContactsAdapter);
            //updateUI(CSEvents.updatenologtext");
            if (!editText.getText().toString().equals("")) {
                appContactsAdapter = new ChatsAdapter(mActivity, CSDataProvider.getSearchInChatNamesnMessagesCursor(editText.getText().toString()));
               setAdapter(appContactsAdapter);

            } else {

                appContactsAdapter = new ChatsAdapter(mActivity, CSDataProvider.getChatCursorGroupedByRemoteId());
                Log.i(TAG, "onResume: cursor "+ CSDataProvider.getChatCursorGroupedByRemoteId()+" count "+ CSDataProvider.getChatCursorGroupedByRemoteId().getCount());
                setAdapter(appContactsAdapter);

            }
            updateUI("updatenologtext");
          hideKeyBoard();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            LocalBroadcastManager.getInstance(MainActivity.context).unregisterReceiver(MainActivityReceiverObj);
            MainActivity.context.unregisterReceiver(MainActivityReceiverObj);

        } catch (Exception ex) {
        }


    }

    public static boolean showoptions(final int isgroupmessage, final String destinationnumber, final String name) {
        try {

            AlertDialog.Builder successfullyLogin = new AlertDialog.Builder(mActivity);
            successfullyLogin.setMessage("Delete all Chat with " + name + "?");
            successfullyLogin.setCancelable(true);


            successfullyLogin.setPositiveButton("Delete",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            CSChat CSChatObj = new CSChat();
                            if (isgroupmessage == 0) {
                                CSChatObj.deleteChatMessagebyfilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destinationnumber);
                            } else {
                                CSChatObj.deleteChatMessagebyfilter(CSDbFields.KEY_CHAT_DESTINATION_GROUPID, destinationnumber);
                            }
                            refreshView();






                            MainActivity.updateChatBadgeCount();

                        }
                    });

            successfullyLogin.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {


                        }
                    });

            //AlertDialog ad =
                    successfullyLogin.show();

            return true;
        } catch (Exception ex) {
            utils.logStacktrace(ex);
            return false;
        }

    }
    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getApplicationWindowToken(), 0);
        editText.clearFocus();
        editText.setText("");
    }
    private void setAdapter(ChatsAdapter appContactsAdapter) {
        try {
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.context);
            //rv.addItemDecoration(new DividerItemDecoration(ChatAdvancedActivity.this, LinearLayoutManager.VERTICAL));
            //rv.addItemDecoration(new MyDividerItemDecoration(ChatAdvancedActivity.this, DividerItemDecoration.VERTICAL, 36));

            DividerItemDecoration divider = new DividerItemDecoration(MainActivity.context, DividerItemDecoration.VERTICAL);
            divider.setDrawable(ContextCompat.getDrawable(MainActivity.context, R.drawable.custom_divider1));
            rv.addItemDecoration(divider);
            rv.setNestedScrollingEnabled(false);
            rv.setLayoutManager(mLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());
            rv.setAdapter(appContactsAdapter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static void handleclick(int position) {
        try {
            ////LOG.info("TEST DELAY:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));
            Cursor cur;
            String searchstring = editText.getText().toString();

            if (searchstring.equals("")) {
                cur = CSDataProvider.getChatCursorGroupedByRemoteId();
            } else {
                cur = CSDataProvider.getSearchInChatNamesnMessagesCursor(searchstring);
            }

            cur.moveToPosition(position);
            int isgroupmessage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_IS_GROUP_MESSAGE));
            ////LOG.info("TEST DELAY 1:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));


            if (isgroupmessage == 0) {
                String number = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_LOGINID));
                //String name=cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_NAME));
                //Cursor ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(number);
                //int unreadCount = ccr.getCount();
                if (!number.equals("")) {
                    //Intent intent = new Intent(MainActivity.context, ChatActivity.class);
                    Intent intent = new Intent(mActivity, ChatAdvancedActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("Sender", number);
                    intent.putExtra("IS_GROUP", false);
                    mActivity.startActivity(intent);
                    ////LOG.info("TEST DELAY 2:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));

                }
            } else {
                String groupid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_GROUPID));
                String name = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_NAME));

                /*
                if (!groupid.equals("")) {
                    Intent intent = new Intent(mActivity, ChatActivityGroup.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("Sender", groupid);
                    intent.putExtra("IS_GROUP", true);
                    intent.putExtra("grpname", name);
                    MainActivity.context.startActivity(intent);
                }
                */
            }


            cur.close();


            try {

                View cview = mActivity.getCurrentFocus();
                if (cview != null) {
                    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(cview.getWindowToken(), 0);
                } else {
                    //LOG.info("but view is null hideKeyboard");
                }
            } catch (Exception ex) {
                utils.logStacktrace(ex);
            }

        } catch (Exception ex) {
ex.printStackTrace();
        }
    }
    public static void handlelongclick(int position) {
        try {
            String searchstring = editText.getText().toString();
            if (searchstring.equals("")) {


                Cursor cur = CSDataProvider.getChatCursorGroupedByRemoteId();
                cur.moveToPosition(position);
                int isgroupmessage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_IS_GROUP_MESSAGE));


                if (isgroupmessage == 0) {
                    String number = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_LOGINID));
                    String contactname = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_NAME));
                    if (contactname.equals("")) {
                        contactname = number;
                    }
                    showoptions(isgroupmessage, number, contactname);
                } else {
                    String grpid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_GROUPID));
                    String groupname = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_NAME));
                    if (groupname.equals("")) {
                        groupname = "Group";
                    }
                    showoptions(isgroupmessage, grpid, groupname);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
