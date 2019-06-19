package com.ca.groupmanagementsample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.adapaters.SimpleImageTextAdapter;
import com.ca.chatsample.R;
import com.ca.utils.Constants;
import com.ca.utils.utils;
import com.ca.views.RoundedImageView;
import com.ca.wrapper.CSDataProvider;
import com.ca.wrapper.CSGroups;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

/**
 * With this class we can edit the group details
 */
public class EditGroupInfoActivity extends Activity {
    private CSGroups CSGroupsObj = new CSGroups();
    private Button mCreateGroupButton;
    private EditText mGroupNameEdt;
    private EditText mGroupStatusEdt;
    private RoundedImageView mGroupProfileImage;
    private String filepath = "";
    private String mGroupId;
    private String imageid = "";
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_group);
        try {
            mGroupProfileImage = findViewById(R.id.group_image);
            mCreateGroupButton = findViewById(R.id.button_create_group);
            mGroupNameEdt = findViewById(R.id.group_name_edt);
            mGroupStatusEdt = findViewById(R.id.group_status_edt);
            mCreateGroupButton.setText("Done");
            mGroupId = getIntent().getStringExtra("grpid");
            // below logic will get the group details and show it in corresponding fields
            Cursor cur = CSDataProvider.getGroupsCursorByFilter(CSDbFields.KEY_GROUP_ID, mGroupId);
            if (cur.getCount() > 0) {
                cur.moveToNext();
                imageid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_PROFILE_PIC));
                String username = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_NAME));
                String presence = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_DESC));
                filepath = CSDataProvider.getImageFilePath(imageid);
                mGroupNameEdt.setText(username);
                mGroupStatusEdt.setText(presence);

                Bitmap mybitmap = CSDataProvider.getImageBitmap(imageid);
                if (mybitmap != null) {
                    mGroupProfileImage.setImageBitmap(mybitmap);
                }


            }
            cur.close();
            // create group button long click listener
            mCreateGroupButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
            // create group button click listener
            mCreateGroupButton.setOnClickListener(new OnClickListener() {
                @SuppressLint("NewApi")
                @Override
                public void onClick(View v) {
                    mCreateGroupButton.setEnabled(false);
                    // before proceed to create group check if group name available of not
                    // if not show error message to user to add group name to create group
                    if (mGroupNameEdt.getText().toString().isEmpty()) {
                        mGroupNameEdt.setError(getResources().getString(R.string.error_empty_group));
                        return;
                    }
                    String name = mGroupNameEdt.getText().toString();
                    String presence = mGroupStatusEdt.getText().toString();

                    // this API will update the group info after edt
                    CSGroupsObj.updateGroupInfo(name, presence, filepath, mGroupId);

                }
            });
            // profile image update button click listener
            mGroupProfileImage.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // this will show popup message to user to select options for image upload
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

            AlertDialog.Builder successfullyLogin = new AlertDialog.Builder(EditGroupInfoActivity.this);
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
                            //LOG.info("finalaction:"+finalaction);

                            if (finalaction.equals("Camera")) {
                                // if user select Camera option native camera will open
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
                        Toast.makeText(EditGroupInfoActivity.this, "No Image Set", Toast.LENGTH_SHORT).show();
                    } else {
                        if (new File(filepath).length() > 10000000) {
                            filepath = "";
                            Toast.makeText(EditGroupInfoActivity.this, "File Size limit excedded", Toast.LENGTH_SHORT).show();
                        } else {
                            Uri temp = Uri.parse(filepath);
                            mGroupProfileImage.setImageURI(temp);
                        }
                    }
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
     * This method will handle SDK events
     *
     * @param str
     */
    public void updateUI(String str) {

        try {
            if (str.equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                // this block will handle network related event
                mCreateGroupButton.setEnabled(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
                } else if (intent.getAction().equals(CSEvents.CSGROUPS_UPDATEGROUPINFO_RESPONSE)) {
                    // this block will handle group update info response
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                    } else {
                        mCreateGroupButton.setEnabled(true);
                        Toast.makeText(EditGroupInfoActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                } else if (intent.getAction().equals(CSEvents.CSGROUPS_PULLGROUPDETAILS_RESPONSE)) {
                    // this blok will give all the group info details
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        onBackPressed();
                    } else {
                        mCreateGroupButton.setEnabled(true);
                        Toast.makeText(EditGroupInfoActivity.this, "Failed", Toast.LENGTH_SHORT).show();

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
            // Register the receivers for SDK events
            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter1 = new IntentFilter(CSEvents.CSGROUPS_UPDATEGROUPINFO_RESPONSE);
            //IntentFilter filter2 = new IntentFilter(CSEvents.updategrpinfofailure);
            IntentFilter filter3 = new IntentFilter(CSEvents.CSGROUPS_PULLGROUPDETAILS_RESPONSE);
            //IntentFilter filter4 = new IntentFilter(CSEvents.pullGroupDetailsResfailure);


            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);
            //LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj,filter2);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter3);
            //LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj,filter4);

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

}
