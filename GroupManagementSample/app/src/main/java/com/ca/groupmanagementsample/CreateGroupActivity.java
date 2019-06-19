package com.ca.groupmanagementsample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSEvents;
import com.ca.adapaters.SimpleImageTextAdapter;

import com.ca.chatsample.R;
import com.ca.utils.Constants;
import com.ca.utils.utils;
import com.ca.views.RoundedImageView;
import com.ca.wrapper.CSGroups;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * This class is help to create new group to server
 */
public class CreateGroupActivity extends Activity {
    private ProgressDialog progressBar;
    private Handler mProgressBarHandler = new Handler();
    private int mTimeDelay = 20000;
    private Runnable mProgressBarRunnable;
    private CSGroups CSGroupsObj = new CSGroups();
    private Button mCreateGroupButton;
    private Button mSelectContactsButton;
    private EditText mGroupNameEdt;
    private EditText mStatusEdt;
    private TextView mContactsCountTv;
    private RoundedImageView mGroupImage;
    private String filepath = "";
    private static String mGroupId;
    private AlertDialog mAlertDialog;
    public static List<String> groupnumbers = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creategroup);
        try {
            mGroupImage = (RoundedImageView) findViewById(R.id.group_image);
            mCreateGroupButton = (Button) findViewById(R.id.button_create_group);
            mGroupNameEdt = (EditText) findViewById(R.id.group_name_edt);
            mStatusEdt = (EditText) findViewById(R.id.group_status_edt);
            mContactsCountTv = (TextView) findViewById(R.id.contacts_count_tv);
            mSelectContactsButton = (Button) findViewById(R.id.select_contacts_tv);

            groupnumbers.clear();
            // crate group button click listener
            mCreateGroupButton.setOnClickListener(new OnClickListener() {
                @SuppressLint("NewApi")
                @Override
                public void onClick(View v) {
                    // if user not provide groupName it will set error message
                    // Group name is mandatory to create group
                    if (mGroupNameEdt.getText().toString().isEmpty()) {
                        mGroupNameEdt.setError(getResources().getString(R.string.error_empty_group));
                        return;
                    }
                    String name = mGroupNameEdt.getText().toString();
                    String presence = mStatusEdt.getText().toString();
                    // below API will create group with given details
                    CSGroupsObj.createGroup(name, presence, filepath);
                    showProgressbar();

                }
            });
            // contact selection button click listener
            mSelectContactsButton.setOnClickListener(new OnClickListener() {
                @SuppressLint("NewApi")
                @Override
                public void onClick(View v) {
                    // by this event contact screen will open to select contact to ad in group
                    Intent intentt = new Intent(MainActivity.context, ShowAppContactsMultiSelectActivity.class);
                    intentt.putExtra("uiaction", Constants.UIACTION_ADDCONTACTSTOGROUP);
                    startActivityForResult(intentt, Constants.SELECT_CONTACT_INTENT);
                }
            });
            // group image add button click listener
            mGroupImage.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // by this event a popUp box will open to add image
                    showOptions();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method will open popUp message to user with options to add image in group
     *
     * @return
     */
    public boolean showOptions() {
        try {
            final ArrayList<String> grpoptions = new ArrayList<>();

            AlertDialog.Builder successfullyLogin = new AlertDialog.Builder(CreateGroupActivity.this);
            successfullyLogin.setTitle("Options");
            successfullyLogin.setCancelable(true);
            grpoptions.clear();

            grpoptions.add("Camera");
            grpoptions.add("Gallery");


            SimpleImageTextAdapter simpleImageTextAdapter = new SimpleImageTextAdapter(MainActivity.context, grpoptions);
            successfullyLogin.setAdapter(simpleImageTextAdapter,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {

                            String finalaction = grpoptions.get(which);
                            // if user select Camera option native camera will open
                            if (finalaction.equals("Camera")) {
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent, Constants.CAMERA_OPEN_INTENT);
                                if (mAlertDialog != null) {
                                    mAlertDialog.cancel();
                                }
                            } else if (finalaction.equals("Gallery")) {
                                // if user user select Galley, galley will open
                                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                i.setType("image/*");
                                startActivityForResult(i, Constants.GALLEY_OPEN_INTENT);
                                if (mAlertDialog != null) {
                                    mAlertDialog.cancel();
                                }
                            }
                        }
                    });


            successfullyLogin.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {


                        }
                    });

            mAlertDialog = successfullyLogin.show();

            return true;
        } catch (Exception ex) {
            utils.logStacktrace(ex);
            return false;
        }

    }

    /**
     * This will handle the Intent results
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // if request for group image add this will execute
            if (requestCode == Constants.GALLEY_OPEN_INTENT || requestCode == Constants.CAMERA_OPEN_INTENT) {
                if (data != null) {
                    // if requestCode is equal to camera this block will execute
                    if (requestCode == Constants.CAMERA_OPEN_INTENT) {
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        Uri tempUri = getImageUri(getApplicationContext(), photo);
                        filepath = utils.getRealPathFromURI(getApplicationContext(), tempUri);
                    } else {
                        // if requestCode is equal to galley this block will execute
                        Uri selectedImageURI = data.getData();
                        filepath = utils.getRealPathFromURI(getApplicationContext(), selectedImageURI);
                    }
                    if (filepath.equals("")) {
                        Toast.makeText(CreateGroupActivity.this, "No Image Set", Toast.LENGTH_SHORT).show();
                    } else {
                        if (new File(filepath).length() > 10000000) {
                            filepath = "";
                            Toast.makeText(CreateGroupActivity.this, "File Size limit excedded", Toast.LENGTH_SHORT).show();
                        } else {
                            Uri temp = Uri.parse(filepath);
                            mGroupImage.setImageURI(temp);
                        }
                    }
                }

            } else if (requestCode == Constants.SELECT_CONTACT_INTENT && resultCode == Activity.RESULT_OK && data != null) {
// if requestCode is equal to contacts this block will execute
                groupnumbers = data.getStringArrayListExtra("contactnumbers");
                mContactsCountTv.setText(groupnumbers.size() + " Contacts Selected");
            }
        } catch (Exception ex) {
            utils.logStacktrace(ex);
        }
    }

    /**
     * This method will get the Image URL from bitmap
     *
     * @param inContext
     * @param inImage
     * @return
     */
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
            return Uri.parse(path);
        } catch (Exception ex) {
            return null;
        }
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

    }

    /**
     * Method will update the UI based on SDK events
     *
     * @param str
     */
    public void updateUI(String str) {

        try {
            if (str.equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                dismissprogressbar();
            } else if (str.equals("Failed")) {
                dismissprogressbar();
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        groupnumbers.clear();
        finish();
        return;
    }

    /**
     * This broadCast receiver will receive events from SDK and will handle accordingly
     */
    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    // this will handle network related events
                    updateUI(CSEvents.CSCLIENT_NETWORKERROR);
                } else if (intent.getAction().equals(CSEvents.CSGROUPS_CREATEGROUP_RESPONSE)) {
                    // this block will handle the create group response
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        mGroupId = intent.getStringExtra("groupid");
                        //LOG.info("mGroupId:" + mGroupId);
                        if (!groupnumbers.isEmpty()) {

                            CSGroupsObj.addMembersToGroup(mGroupId, groupnumbers);

                        } else {
                            //LOG.info("NO ADD CONTACTS TO GROUP IS CALLED");
                            onBackPressed();
                        }
                    } else {
                        updateUI("Failed");
                    }
                } else if (intent.getAction().equals(CSEvents.CSGROUPS_ADDMEMBERS_TOGROUP_RESPONSE)) {
                    // this block will handle add members to group response
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        dismissprogressbar();
                        onBackPressed();
                    } else {
                        updateUI("Failed");
                    }
                } else if (intent.getAction().equals(CSEvents.CSGROUPS_PULLGROUPDETAILS_RESPONSE)) {
                    // this block will handle get group details
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                    } else {
                        updateUI("Failed");
                    }
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
            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter1 = new IntentFilter(CSEvents.CSGROUPS_CREATEGROUP_RESPONSE);
            IntentFilter filter3 = new IntentFilter(CSEvents.CSGROUPS_ADDMEMBERS_TOGROUP_RESPONSE);
            //IntentFilter filter4 = new IntentFilter(CSEvents.addcontactstogroupfailure);
            IntentFilter filter5 = new IntentFilter(CSEvents.CSGROUPS_PULLGROUPDETAILS_RESPONSE);
            //IntentFilter filter6 = new IntentFilter(CSEvents.pullGroupDetailsResfailure);

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter3);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter5);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            // this line will unregister the Registered receivers
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    /**
     * This method will show progressbar while creating group
     */
    public void showProgressbar() {
        try {
            if (MainActivity.context != null) {
                progressBar = new ProgressDialog(CreateGroupActivity.this);
                progressBar.setCancelable(false);
                progressBar.setMessage("Please Wait..");
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.setProgress(0);
                progressBar.show();
                // this handler will close progressbar if response not came within time
                mProgressBarHandler = new Handler();
                mProgressBarRunnable = new Runnable() {

                    public void run() {
                        mProgressBarHandler.postDelayed(this, mTimeDelay);

                        runOnUiThread(new Runnable() {
                            public void run() {
                                dismissprogressbar();
                            }
                        });
                    }
                };
                mProgressBarHandler.postDelayed(mProgressBarRunnable, mTimeDelay);


            }
        } catch (Exception ex) {
            dismissprogressbar();
        }
    }

    /**
     * This method will dismiss the progressbar
     */
    public void dismissprogressbar() {
        try {
            if (progressBar != null) {
                progressBar.dismiss();

            }
            if (mProgressBarHandler != null) {
                mProgressBarHandler.removeCallbacks(mProgressBarRunnable);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
