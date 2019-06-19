package com.ca.chatsample;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.ca.CustomViews.MyProgressDialog;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.Utils.CSExplicitEvents;
import com.ca.adapaters.ChatAdvancedAdapter;
import com.ca.dao.CSChatContact;
import com.ca.dao.CSChatLocation;
import com.ca.utils.ChatConstants;
import com.ca.utils.ChatMethodHelper;
import com.ca.utils.Constants;
import com.ca.utils.CrossView;
import com.ca.utils.PreferenceProvider;
import com.ca.utils.utils;
import com.ca.wrapper.CSChat;
import com.ca.wrapper.CSDataProvider;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatAdvancedActivity extends AppCompatActivity implements Animation.AnimationListener {
	MyProgressDialog mdialog;
    ProgressDialog mdialog1;
	RecyclerView rv;
	String TAG = ChatAdvancedActivity.class.getSimpleName();
    private PreferenceProvider mPrefereceProvider;
	ChatAdvancedAdapter adp;
    boolean chatfiledelete = false;

	static String destination;
	boolean isGroupChat = false;

    private Toolbar mToolbar;
    private TextView toolbarTitleTextView, toolbarSubTitleTextView;
    private EditText mChatMessageEditText,edittextsearchchatview;
    private RelativeLayout mSendButton, attachments, mAudioCallLayout, mVideoCallLayout, mRecordButton,backview;
    //private RelativeLayout mAttachmentsOverlayLayout;
    private android.support.v7.widget.CardView attachmentslayout;
    private View myView;
    private CrossView mAttachmentsCrossView;
    private RelativeLayout mCameraLayout, mGalleryLayout, mDocumentsLayout, mContactLayout, mAudioLayout, mLocationLayout;
    private static RelativeLayout  chatoptionsview,searchchatview;
    private LinearLayout toolbar_title_layout_view,chatoptionsbacklayoutview;
    private static LinearLayout chat_message_layoutview;
    private TextView mRecordTimerTv, mRecordHelpTextTv;
    private static TextView chat_selected_countview;
    private ImageView userImageView,searchupview,searchdownview;
    private static ImageView chatdeleteview,chatcopyview,chatforwardview,backviewsearch;
    static Toolbar toolbar;

    Runnable RunnableObj;
    final Handler h = new Handler();
    private boolean isRecordButtonLongPressed = false;
    int delay = 1000;
    private int startTime = 0;
    private String AudioSavePathInDevice = "";
    private MediaRecorder mAudioRecorder;

    private static final int IMAGE_CAPTURE_REQUEST_CODE = 1;
    private static final int IMAGE_GALLERY_REQUEST_CODE = 2;
    private static final int VIDEO_GALLERY_REQUEST_CODE = 3;
    private static final int DOCUMENTS_REQUEST_CODE = 4;
    private static final int CONTACT_REQUEST_CODE = 5;
    private static final int AUDIO_REQUEST_CODE = 6;
    private static final int LOCATION_REQUEST_CODE = 7;
    private static final int FORWARD_MESSAGE_INTNET = 8;

    static ArrayList<Integer> selectedpositions = new ArrayList<Integer>();

    private File imageFile;
    private Animation slideDown, slideUp;

    boolean onpaused = false;

     int chatsearchrefcnt = 0;
     String chatsearchrefstring = "";
     boolean chatsearchrefmode = false;

    CSChat CSChatObj = new CSChat();


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.advanced_activity_chat);

        //LOG.info(("onNewIntent onCreate manage chat");
        rv = (RecyclerView) findViewById(R.id.chat_layout);
        mPrefereceProvider = new PreferenceProvider(getApplicationContext());
        //mAttachmentsOverlayLayout = findViewById(R.id.attachment_overlay);
        attachmentslayout = findViewById(R.id.awesome_card);
        myView = findViewById(R.id.awesome_card);
        mCameraLayout = findViewById(R.id.camera_attachment_menu);
        mGalleryLayout = findViewById(R.id.gallery_attachment_menu);
        mDocumentsLayout = findViewById(R.id.documents_attachment_menu);
        mContactLayout = findViewById(R.id.contact_attachment_menu);
        mAudioLayout = findViewById(R.id.audio_attachment_menu);
        mLocationLayout = findViewById(R.id.location_attachment_menu);
        mChatMessageEditText = findViewById(R.id.chat_edittext);
        attachments = findViewById(R.id.chat_attachments);
        mAttachmentsCrossView = findViewById(R.id.chat_attachments_image);
        mSendButton = findViewById(R.id.chat_send);
        mRecordButton = findViewById(R.id.chat_record);
        //mRecyclerView = findViewById(R.id.conversation_recycler_view);
        mRecordTimerTv = findViewById(R.id.recording_timer);
        mRecordHelpTextTv = findViewById(R.id.record_help_text_tv);



        toolbarTitleTextView = findViewById(R.id.toolbar_title);
        toolbarSubTitleTextView = findViewById(R.id.toolbar_sub_title);
        userImageView = findViewById(R.id.user_image_view);
        backview = findViewById(R.id.back_view);
        toolbar_title_layout_view = findViewById(R.id.toolbar_title_layout);

         toolbar =  findViewById(R.id.chat_tool_bar);
         searchchatview =  findViewById(R.id.searchchat);
         backviewsearch = findViewById(R.id.back_arrow_view_search);
         searchupview = findViewById(R.id.search_up);
         searchdownview = findViewById(R.id.search_down);
         edittextsearchchatview = findViewById(R.id.edittextsearchchat);
         chat_message_layoutview = findViewById(R.id.chat_message_layout);

        chatdeleteview = findViewById(R.id.chatdelete);
                chatcopyview = findViewById(R.id.chatcopy);
                chatforwardview = findViewById(R.id.chatforward);
        chat_selected_countview = findViewById(R.id.chat_selected_count);
                chatoptionsbacklayoutview = findViewById(R.id.chatoptionsbacklayout);
        chatoptionsview = findViewById(R.id.chatoptions);


        utils.setFileTrasferPathsHelper();

        setSupportActionBar(toolbar);
         selectedpositions.clear();



        destination = getIntent().getStringExtra("Sender");
		isGroupChat = getIntent().getBooleanExtra("IS_GROUP", false);




        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
		//rv.addItemDecoration(new DividerItemDecoration(ChatAdvancedActivity.this, LinearLayoutManager.VERTICAL));
        //rv.addItemDecoration(new MyDividerItemDecoration(ChatAdvancedActivity.this, DividerItemDecoration.VERTICAL, 36));

        DividerItemDecoration divider = new DividerItemDecoration(ChatAdvancedActivity.this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.custom_divider));
        rv.addItemDecoration(divider);
        adp = new ChatAdvancedAdapter(ChatAdvancedActivity.this, CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination),destination);
		rv.setNestedScrollingEnabled(false);
		rv.setLayoutManager(mLayoutManager);
		rv.setItemAnimator(new DefaultItemAnimator());
		rv.setAdapter(adp);

        slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down_menu);
        slideDown.setAnimationListener(ChatAdvancedActivity.this);
        slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up_menu);

        loadProfilePicture();

        setTitleName();


        backview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onBackPressed();
            }
        });

        backviewsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchchatview.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
                chat_message_layoutview.setVisibility(View.VISIBLE);

                hideKeyboard();
                notifyDataSetChanged(true,false,"",true);
                chatsearchrefmode = false;
                chatsearchrefstring = "";
                chatsearchrefcnt = 0;

                List<String> numbers = new ArrayList<String>();
                numbers.add(destination);
                //LOG.info(("getPresenceReq:" + destination);
                CSChatObj.getPresence(numbers);
                edittextsearchchatview.setText("");
            }
        });

        searchupview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               handleSearch(1);
            }
        });
        searchdownview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSearch(2);
            }
        });

        edittextsearchchatview.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ChatAdvancedActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            } });
        edittextsearchchatview.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Log.i(TAG, "search from keyboard");
                    chatsearchrefstring = edittextsearchchatview.getText().toString();
                    chatsearchrefmode = true;
                    chatsearchrefcnt= 0;
                    notifyDataSetChanged(false,true,edittextsearchchatview.getText().toString(),true);
                    handleSearch(1);
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });
        edittextsearchchatview.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence searchstring, int start, int before, int count) {
                Log.i(TAG, "searchstring:" + searchstring);
                if(searchstring!=null && !searchstring.toString().equals("")) {
                    //notifyDataSetChangedForSearch(searchstring.toString());
                    chatsearchrefstring = searchstring.toString();
                    chatsearchrefmode = true;
                    chatsearchrefcnt= 0;
                    notifyDataSetChanged(false,true,searchstring.toString(),true);
                    //handleSearch(1);
                } else {
                    chatsearchrefmode = false;
                    chatsearchrefstring = "";
                    chatsearchrefcnt= 0;
                    notifyDataSetChanged(false,false,"",true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        toolbar_title_layout_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cur1 = CSDataProvider.getContactsCursorByFilter(CSDbFields.KEY_CONTACT_NUMBER, destination);
                if (cur1.getCount() > 0) {
                    cur1.moveToNext();
                    String id = cur1.getString(cur1.getColumnIndex(CSDbFields.KEY_CONTACT_ID));
                    String name = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
                    String description = "";
                    String picid = "";
                    Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, destination);
                    if (cur.getCount() > 0) {
                        cur.moveToNext();
                        picid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                        description = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_DESCRIPTION));
                    }
                    cur.close();
                    if (description.equals("")) {
                        //description = "Hey there! I am using Konverz";
                        description = "";
                    }

/*
                    Intent intent = new Intent(ChatAdvancedActivity.this, ManageUserActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("name", name);
                    intent.putExtra("number", destination);
                    intent.putExtra("description", description);
                    intent.putExtra("picid", picid);
                    intent.putExtra("nativecontactid", id);
                    startActivity(intent);
*/

                }
                cur1.close();
            }
        });



        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatMessage = mChatMessageEditText.getText().toString().trim();
if(!chatMessage.equals("")) {
    CSChatObj.sendMessage(destination, chatMessage, isGroupChat);
    mChatMessageEditText.setText("");
    notifyDataSetChanged(true,false,"",true);
}
            }
        });
        mGalleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAttachmentMenu();
                showGalleryOptions();
            }
        });
        mLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAttachmentMenu();

                LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
                boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(ChatAdvancedActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 102);
                }
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (!enabled) {
                        showactivatelocationalert("Please enable GPS");
                    } else {
                        Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
                        startActivityForResult(intent, LOCATION_REQUEST_CODE);
                    }
                }
                /*if(PermissionUtils.checkLocationPermission(ChatAdvancedActivity.this)) {
                    Intent intent = new Intent(mContext, LocationActivity.class);
                    startActivityForResult(intent, LOCATION_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(ChatAdvancedActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                }*/
            }
        });

        mContactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAttachmentMenu();
                Intent contactPickerIntent = new Intent(ChatAdvancedActivity.this,ShareContactsInChatActivity.class);
                startActivityForResult(contactPickerIntent, CONTACT_REQUEST_CODE);
                //Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                //startActivityForResult(intent, CONTACT_REQUEST_CODE);
            }
        });
        mAudioLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAttachmentMenu();

                if (Build.VERSION.SDK_INT < 19) {
                    Intent intent = new Intent();
                    intent.setType("audio/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(
                            Intent.createChooser(intent, getResources().getString(R.string.select_file)),
                            AUDIO_REQUEST_CODE);
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("audio/*");
                    startActivityForResult(
                            Intent.createChooser(intent, getResources().getString(R.string.select_file)),
                            AUDIO_REQUEST_CODE);
                }

            }
        });

        mDocumentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAttachmentMenu();


                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");      //all files
                //intent.setType("text/xml");   //XML file only
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(Intent.createChooser(intent, "Choose a File"), DOCUMENTS_REQUEST_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(ChatAdvancedActivity.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                }


            }

        });

        // Chat File sharing listeners
        mCameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAttachmentMenu();

                String CHAT_IMAGES_DIRECTORY = Environment.getExternalStorageDirectory().toString() + "/ChatSample/Images";
                String directoryPath = ChatConstants.CHAT_IMAGES_DIRECTORY;
                String fileName = "IMG_"
                        + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
                        + ".jpg";
                imageFile = ChatMethodHelper.createFile(directoryPath, fileName);

                Log.i(TAG, "Image Path: " + imageFile);


                if(ContextCompat.checkSelfPermission(ChatAdvancedActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", imageFile));
                    } else {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                    }
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(intent, IMAGE_CAPTURE_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(ChatAdvancedActivity.this, new String[]{Manifest.permission.CAMERA}, 101);
                }
            }
        });

        attachments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!attachmentslayout.isShown()) {
                    /*if (isUpdateChatSelectionCalled) {
                        disableMessageSelection();
                    }*/
                    mAttachmentsCrossView.toggle();

                    //mAttachmentsOverlayLayout.setVisibility(View.VISIBLE);
                    attachmentslayout.setVisibility(View.VISIBLE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                        int width = getWindowManager().getDefaultDisplay().getWidth();

                        // get the center for the clipping circle
                        int cx = (myView.getLeft() + myView.getRight()) / 2;
                        int cy = (myView.getTop() + myView.getBottom()) / 2;

                        // get the final radius for the clipping circle
                        int dx = Math.max(cx, myView.getWidth() - cx);
                        int dy = Math.max(cy, myView.getHeight() - cy);
                        float finalRadius = (float) Math.hypot(width, getResources().getDimension(R.dimen._200dp));

                        // Android native animator
                        Animator animator =
                                ViewAnimationUtils.createCircularReveal(myView, 0, (int) getResources().getDimension(R.dimen._200dp), 0, finalRadius);
                        animator.setInterpolator(new AccelerateDecelerateInterpolator());
                        animator.setDuration(300);
                        animator.start();

                    /*AnimationSet animSet = new AnimationSet(true);
                    animSet.setInterpolator(new DecelerateInterpolator());
                    animSet.setFillAfter(true);
                    animSet.setFillEnabled(true);

                    final RotateAnimation animRotate = new RotateAnimation(0.0f, 45.0f,
                            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                            RotateAnimation.RELATIVE_TO_SELF, 0.5f);

                    animRotate.setDuration(200);
                    animRotate.setFillAfter(true);
                    animSet.addAnimation(animRotate);

                    mChatAttachmentImageView.startAnimation(animSet);*/
                    } else {
                        attachmentslayout.startAnimation(slideUp);
                    }


                } else {
                    hideAttachmentMenu();
                }
            }
        });

        RunnableObj = new Runnable() {
            public void run() {
                h.postDelayed(this, delay);
                //Log.i(TAG,"printing at 1 sec");
                ChatAdvancedActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String minutes = "00";
                        String seconds = "00";
                        int x = startTime++;
                        int mins = (x) / 60;
                        int sec = (x) % 60;
                        if (mins < 10) {
                            minutes = "0" + String.valueOf(mins);
                        } else {
                            minutes = String.valueOf(mins);
                        }
                        if (sec < 10) {
                            seconds = "0" + String.valueOf(sec);
                        } else {
                            seconds = String.valueOf(sec);
                        }
                        mRecordTimerTv.setText(minutes + ":" + seconds);
                    }
                });
            }
        };

        mRecordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (isRecordButtonLongPressed) {
                        try {
                            h.removeCallbacks(RunnableObj);
                            if (mAudioRecorder != null)
                                mAudioRecorder.stop();
                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(AudioSavePathInDevice);
                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            mmr.release();
                            Log.i(TAG, "File size " + duration);
                            if (Integer.parseInt(duration) > 1000) {
                                if(new File(AudioSavePathInDevice).exists()) {
                                    showConfirmationDialog();
                                } else {
                                    Toast.makeText(ChatAdvancedActivity.this, getString(R.string.media_not_available), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                File file = new File(AudioSavePathInDevice);
                                file.delete();
                            }


                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                        mChatMessageEditText.setVisibility(View.VISIBLE);
                        mRecordHelpTextTv.setVisibility(View.GONE);
                        mRecordTimerTv.setVisibility(View.GONE);
                        isRecordButtonLongPressed = false;
                    }
                }
                return false;
            }
        });


        mRecordButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                hideAttachmentMenu();
                isRecordButtonLongPressed = true;
                getTheFileNameWithPath();
                MediaRecorderReady();
                try {
                    mChatMessageEditText.setVisibility(View.GONE);
                    mRecordHelpTextTv.setVisibility(View.VISIBLE);
                    mRecordTimerTv.setVisibility(View.VISIBLE);
                    mRecordTimerTv.setText("00" + ":" + "00");
                    startTime = 0;
                    h.postDelayed(RunnableObj,0);

                    mAudioRecorder.prepare();
                    mAudioRecorder.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(ChatAdvancedActivity.this, getString(R.string.chat_screen_audio_start_message),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideAttachmentMenu();


                Toast.makeText(getApplicationContext(), getString(R.string.record_help_text), Toast.LENGTH_SHORT).show();
            }
        });

        mChatMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (mChatMessageEditText.getText().toString().length() == 1) {
                    hideAttachmentMenu();
                }
                if (mChatMessageEditText.getText().toString().length() == 1 || mChatMessageEditText.getText().toString().length() % 3 == 1) {
                    Log.i(TAG, "Sending typing request");
                    CSChatObj.sendIsTyping(destination, false, true);
                }
                if (s.length() > 0) {
                    mSendButton.setVisibility(View.VISIBLE);
                    mRecordButton.setVisibility(View.GONE);
                } else {
                    mSendButton.setVisibility(View.GONE);
                    mRecordButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        chatdeleteview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showdeletealert();
            }

        });
        chatcopyview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

try {

    String final_mesage = "";


    Cursor cur =  CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination);

    for(int cursorposition:selectedpositions) {
        cur.moveToPosition(cursorposition);
        int chattype =  cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
        if (chattype == CSConstants.E_TEXTPLAIN || chattype == CSConstants.E_CONTACT || chattype == CSConstants.E_LOCATION) {
            final_mesage = final_mesage+cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE))+"\n";
        }
    }
    final_mesage = final_mesage.trim();
    cur.close();





    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("label", final_mesage);
    clipboard.setPrimaryClip(clip);
    Toast.makeText(ChatAdvancedActivity.this, "Copied to clipboard", Toast.LENGTH_LONG).show();
    chatoptionsbacklayoutview.performClick();

} catch (Exception ex) {
    ex.printStackTrace();
}
            }

        });
        chatforwardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    Intent contactPickerIntent = new Intent(getApplicationContext(), ShowAppContactsMultiSelectActivity.class);
                    contactPickerIntent.putExtra("uiaction", Constants.UIACTION_FORWARDCHATMESSAGE);
                    contactPickerIntent.putExtra("chatid", "chatid");
                    startActivityForResult(contactPickerIntent, FORWARD_MESSAGE_INTNET);



                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            }

        });

        chatoptionsbacklayoutview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chatoptionsview.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
                chat_message_layoutview.setVisibility(View.VISIBLE);
                selectedpositions.clear();
                adp.clearselecteditems();
                notifyDataSetChanged(false,false,"",true);
            }

        });


        if (mPrefereceProvider.getPrefBoolean(PreferenceProvider.IS_FILE_SHARE_AVAILABLE)) {
            mPrefereceProvider.setPrefboolean(PreferenceProvider.IS_FILE_SHARE_AVAILABLE, false);
            boolean isFileAvailable = getIntent().getBooleanExtra("isShareFileAvailable", false);
            if (isFileAvailable) {
                String receivedFileType = getIntent().getStringExtra("receivedFileType");
                String receivedFilePath = getIntent().getStringExtra("receivedFilePath");
                if (receivedFileType.equals("text")) {
                    CSChatObj.sendMessage(destination, receivedFilePath, false);
                } else if (receivedFileType.equals("audio")) {
                    //String audioPath = utils.getPath(getApplicationContext(), Uri.parse(receivedFilePath));
                    //CSChatObj.sendAudio(destination, audioPath, false);

                    new SendFile(Uri.parse(receivedFilePath),"audio").execute();

                } else if (receivedFileType.equals("image")) {
                    String filepath = utils.getRealPathFromURI(getApplicationContext(), Uri.parse(receivedFilePath));
                    //LOG.info(("filepath from share:"+filepath);
                    if (filepath.equals("")) {
                        Log.i(TAG, "Showing toast:" + filepath);
                        Toast.makeText(getApplicationContext(), "No Video Selected", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i(TAG, "not showing toast:" + filepath);
                        CSChatObj.sendPhoto(destination, filepath, false);

                    }
                } else if (receivedFileType.equals("video")) {
                    String filepath = utils.getRealPathFromURI(getApplicationContext(), Uri.parse(receivedFilePath));

                    if (filepath.equals("")) {
                        Log.i(TAG, "Showing toast:" + filepath);
                        Toast.makeText(getApplicationContext(), "No Video Selected", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i(TAG, "not showing toast:" + filepath);
                        CSChatObj.sendVideo(destination, filepath, false);

                    }
                }
            }
        }
    }
    private void hideAttachmentMenu() {

        if (attachmentslayout.isShown()) {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                int width = getWindowManager().getDefaultDisplay().getWidth();

                float finalRadius = (float) Math.hypot(width, getResources().getDimension(R.dimen._200dp));

                Animator animator = ViewAnimationUtils.createCircularReveal(myView, myView.getLeft(), (int) getResources().getDimension(R.dimen._200dp), finalRadius, 0);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(300);
                animator.start();


                mAttachmentsCrossView.toggle();

                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        attachmentslayout.setVisibility(View.GONE);
                        //mAttachmentsOverlayLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            } else {
                mAttachmentsCrossView.toggle();

                attachmentslayout.startAnimation(slideDown);
            }
        }

    }
    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (attachmentslayout.isShown()) {
            attachmentslayout.setVisibility(View.GONE);
            //mAttachmentsOverlayLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
	@SuppressLint("ResourceAsColor")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.clear();
		getMenuInflater().inflate(R.menu.customersmenu, menu);



		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
/*
			case R.id.audiocall:
				utils.donewvoicecall(destination, ChatAdvancedActivity.this);
				return true;

			case R.id.videocall:
				utils.donewVideocall(destination, ChatAdvancedActivity.this);
				return true;
*/
			case R.id.menu_search:

                searchchatview.setVisibility(View.VISIBLE);
                searchchatview.requestFocus();


                toolbar.setVisibility(View.GONE);
                chat_message_layoutview.setVisibility(View.GONE);

                if(chatoptionsview.getVisibility() == View.VISIBLE) {
                    chatoptionsbacklayoutview.performClick();
                }
                return true;
            case R.id.menu_chat_clear:
                showclearchatoptions();
                return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}




    public class ChatEventReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            try {
                //LOG.info(("Yes Something receieved in RecentReceiver:"+intent.getAction().toString());
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    //LOG.info(("NetworkError receieved");
                    //to do toolbarSubTitleTextView.setText("");

                } else if (intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    if(intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        List<String> numbers = new ArrayList<String>();
                        numbers.add(destination);
                        //LOG.info(("getPresenceReq:" + destination);
                        CSChatObj.getPresence(numbers);
                    }
                } else if(intent.getAction().equals(CSEvents.CSCHAT_CHATUPDATED)) {
                    if(destination.equals(intent.getStringExtra("destinationnumber"))) {
                        notifyDataSetChanged(true,false,"",true);
                    }
                } else if(intent.getAction().equals(CSEvents.CSCHAT_ISTYPING)) {
                    //LOG.info(("compare isTypingReq:"+destination);
                    //LOG.info(("compare destinationnumber:"+intent.getStringExtra("destinationnumber"));
                    boolean istyping = intent.getBooleanExtra("istyping",false);

                    if(destination.equals(intent.getStringExtra("destinationnumber"))&&istyping&&intent.getStringExtra("destinationgroupid").equals("")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //LOG.info(("runOnUiThread:");
                                final String subtitle = "online";
                                toolbarSubTitleTextView.setText("typing..");
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //LOG.info(("runOnUiThread1:");

                                        toolbarSubTitleTextView.setText(subtitle);
                                    }
                                }, 1000);

                            }
                        });

                    }

                }
                else if(intent.getAction().equals(CSExplicitEvents.CSChatReceiver)) {
                    if(destination.equals(intent.getStringExtra("destinationnumber"))) {
                        //LOG.info(("Chat Receieved in ChatActivity");
                       notifyDataSetChanged(false,false,"",true);
                    }
                }
                else if(intent.getAction().equals(CSEvents.CSCHAT_GETPRESENCE_RESPONSE)) {
                    try {

                        if(intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {

                            if (destination.equals(intent.getStringExtra("presencenumber"))) {
                                String presencestatus = intent.getStringExtra("presencestatus");
                                long lastseentime = intent.getLongExtra("lastseentime", 0);
                                if (presencestatus.equals("ONLINE")) {
                                    toolbarSubTitleTextView.setText("online");
                                } else {
                                    if (DateUtils.isToday(lastseentime)) {
                                        toolbarSubTitleTextView.setText("active today at " + new SimpleDateFormat("hh:mm a").format(lastseentime));
                                        applyAnimationToSubTitle();
                                    } else if (utils.isYesterday(lastseentime)) {
                                        toolbarSubTitleTextView.setText("active yesterday at " + new SimpleDateFormat("hh:mm a").format(lastseentime));
                                        applyAnimationToSubTitle();
                                    } else {
                                        toolbarSubTitleTextView.setText("active " + new SimpleDateFormat("dd-MM-yyyy hh:mm a").format(lastseentime));
                                        applyAnimationToSubTitle();
                                    }
                                }
                            }
                        }

                    } catch(Exception ex){}
                }
                else if (intent.getAction().equals(CSEvents.CSCHAT_UPLOADFILEDONE)) {
                    //notifyDataSetChanged(true,false,"");
                }
                else if (intent.getAction().equals(CSEvents.CSCHAT_UPLOADFILEFAILED)) {
                    adp.uploadfailedchatids.add(intent.getStringExtra("chatid"));

                    notifyDataSetChanged(false,false,"",false);
                    /*
                    int position = getpositionfromchatid(intent.getStringExtra("chatid"));
                    if(position>=0) {
                        adp.notifyItemChanged(position);
                    }*/

                    if(adp.uploadfailedchatids.contains(intent.getStringExtra("chatid"))) {
                        adp.uploadfailedchatids.remove(intent.getStringExtra("chatid"));
                    }
              //notifyDataSetChanged(true,false,"");
                }

                else if (intent.getAction().equals(CSEvents.CSCHAT_UPLOADPROGRESS)) {
                    int percentage = intent.getIntExtra("transferpercentage",0);

                    //LOG.info(("uploadprogress percentage:"+percentage);
                    //LOG.info(("chatid from activity upload:" +  intent.getStringExtra("chatid"));

                    notifyDataSetChanged(false,false,"",false);
                    /*
                    int position = getpositionfromchatid(intent.getStringExtra("chatid"));
                    if(position>=0) {
                        adp.notifyItemChanged(position);
                    }
                    */
                    //to do updateView(intent.getStringExtra("chatid"),CSEvents.CSCHAT_UPLOADPROGRESS,percentage);
                }

                else if (intent.getAction().equals(CSEvents.CSCHAT_DOWNLOADPROGRESS)) {
                    int percentage = intent.getIntExtra("transferpercentage",0);
                    //LOG.info(("Download percentage:"+percentage);
                    //LOG.info(("chatid from activity download:" +  intent.getStringExtra("chatid"));

                    //to do updateView(intent.getStringExtra("chatid"),CSEvents.CSCHAT_DOWNLOADPROGRESS,percentage);
                    notifyDataSetChanged(false,false,"",false);
                    /*
                    int position = getpositionfromchatid(intent.getStringExtra("chatid"));
                    if(position>=0) {
                        adp.notifyItemChanged(position);
                    }
                    */

                }
                else if (intent.getAction().equals(CSEvents.CSCHAT_DOWNLOADFILEDONE)) {
                    //to do
                     /*int position = mChatList.getFirstVisiblePosition();
                    chatAdapter.changeCursor(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination));
                    chatAdapter.notifyDataSetChanged();
                    mChatList.setSelection(position);
*/
                    //updateView(intent.getStringExtra("chatid"),"downloadfiledone",0);
                }

                else if (intent.getAction().equals(CSEvents.CSCHAT_DOWNLOADFILEFAILED)) {
                    //to do
                    /* chatAdapter.changeCursor(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination));
                    chatAdapter.notifyDataSetChanged();
                    */

//					updateView(intent.getStringExtra("chatid"),"downloadfilefailed",0);


                    if(adp.filedownloadinitiatedchatids.contains(intent.getStringExtra("chatid"))) {
                        adp.filedownloadinitiatedchatids.remove(intent.getStringExtra("chatid"));
                    }
                    notifyDataSetChanged(false,false,"",false);
                    /*
                    int position = getpositionfromchatid(intent.getStringExtra("chatid"));
                    if(position>=0) {
                        adp.notifyItemChanged(position);
                    }
                    */

                }
            } catch(Exception ex){}
        }
    }


    ChatEventReceiver chatEventReceiverObj = new ChatEventReceiver();


    @Override
	public void onResume() {
		super.onResume();

		try {
            //LOG.info(("onNewIntent onResume manage chat");
            ////LOG.info(("TEST DELAY 24:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));

            onpaused = false;

            Cursor ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(destination);
            int focus = ccr.getCount();
            //LOG.info(("Read req test: unread count:"+focus);
            while(ccr.moveToNext()) {
                if(ccr.getInt(ccr.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS))== CSConstants.MESSAGE_RECEIVED   || ccr.getInt(ccr.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS))== CSConstants.MESSAGE_DELIVERED_ACK || ccr.getInt(ccr.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED) {
                    //LOG.info(("Read req test: sending read request from app");
                    CSChatObj.sendReadReceipt(ccr.getString(ccr.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID)));
                }
            }
            ccr.close();
            //LOG.info(("Focus count:"+focus);

            notifyDataSetChanged(true,false,"",true);

            try {
                h.removeCallbacks(RunnableObj);
                if (mAudioRecorder != null) {
                    mAudioRecorder.stop();
                    mChatMessageEditText.setVisibility(View.VISIBLE);
                    mRecordHelpTextTv.setVisibility(View.GONE);
                    mRecordTimerTv.setVisibility(View.GONE);
                    isRecordButtonLongPressed = false;
                }
            } catch (Exception e) {

                e.printStackTrace();
            }




            Constants.inchatactivitydestination = destination;
             chatEventReceiverObj = new ChatEventReceiver();


            IntentFilter filter = new IntentFilter();
            filter.addAction(CSEvents.CSCLIENT_NETWORKERROR);
            filter.addAction(CSEvents.CSCHAT_CHATUPDATED);
            filter.addAction(CSExplicitEvents.CSChatReceiver);
            filter.addAction(CSEvents.CSCHAT_GETPRESENCE_RESPONSE);
            filter.addAction(CSEvents.CSCHAT_UPLOADFILEDONE);
            filter.addAction(CSEvents.CSCHAT_UPLOADFILEFAILED);
            filter.addAction(CSEvents.CSCHAT_DOWNLOADFILEDONE);
            filter.addAction(CSEvents.CSCHAT_DOWNLOADFILEFAILED);
            filter.addAction(CSEvents.CSCLIENT_LOGIN_RESPONSE);
            filter.addAction(CSEvents.CSCHAT_DOWNLOADPROGRESS);
            filter.addAction(CSEvents.CSCHAT_UPLOADPROGRESS);
            filter.addAction(CSEvents.CSCHAT_ISTYPING);

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(chatEventReceiverObj,filter);





            if(CSDataProvider.getLoginstatus()) {
                List<String> numbers = new ArrayList<String>();
                numbers.add(destination);
                //LOG.info(("getPresenceReq:"+destination);
                CSChatObj.getPresence(numbers);
            }


            ////LOG.info(("TEST DELAY 25:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));



        } catch (Exception ex) {
			ex.printStackTrace();
		}




	}
    @Override
    public void onBackPressed() {
        if(chatoptionsview.getVisibility() == View.GONE && searchchatview.getVisibility() == View.GONE) {
            super.onBackPressed();
        } else {
            if (chatoptionsview.getVisibility() == View.VISIBLE) {
                chatoptionsbacklayoutview.performClick();
                return;
            }
            if (searchchatview.getVisibility() == View.VISIBLE) {
                backviewsearch.performClick();
                return;
            }
        }
    }



    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        //LOG.info(("onNewIntent is called manage chat");
try {
    //setIntent(intent);

    finish();
    startActivity(intent);

    /*
       Intent intent1 = new Intent(MainActivity.context, ChatAdvancedActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.putExtra("Sender", intent.getStringExtra("Sender"));
        intent1.putExtra("IS_GROUP", intent.getBooleanExtra("IS_GROUP",false));
   // Intent i = new Intent();
   // i.setAction("do_nothing");
    //setIntent(i);
       startActivity(intent1);
    ////LOG.info(("onNewIntent is called manage chat 1");
   finish();
*/
    ////LOG.info(("onNewIntent is called manage chat 2");
    } catch (Exception ex) {
        ex.printStackTrace();
    }
        /*if(intent.getStringExtra("methodName").equals("myMethod"))
        {

        }
        */
    }




	@Override
	public void onPause() {
		super.onPause();

        //LOG.info(("onNewIntent onPause manage chat");

        onpaused = true;
        Constants.inchatactivitydestination = "";
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(chatEventReceiverObj);

        try {
            h.removeCallbacks(RunnableObj);
            if (mAudioRecorder != null) {
                mAudioRecorder.stop();
                mChatMessageEditText.setVisibility(View.VISIBLE);
                mRecordHelpTextTv.setVisibility(View.GONE);
                mRecordTimerTv.setVisibility(View.GONE);
                isRecordButtonLongPressed = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(adp!=null) {
            adp.releaseMediaPlayer();
        }
	}



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        Log.i(TAG, "onActivityResult, RequestCode: " + requestCode);
        switch (requestCode) {

            case IMAGE_CAPTURE_REQUEST_CODE:



                if (imageFile == null || imageFile.getAbsolutePath().equals("")) {
                    Toast.makeText(ChatAdvancedActivity.this, "No Image Seected", Toast.LENGTH_SHORT).show();
                } else {
                    String filepath = new String(imageFile.getAbsolutePath());
                    new SendFile(Uri.fromFile(new File(filepath)),"cameraimage").execute();
                    /*
            CSChatObj.sendPhoto(destination,filepath,false);
*/
                }
                break;

            case IMAGE_GALLERY_REQUEST_CODE:
                //LOG.info("IMAGE_GALLERY_REQUEST_CODE received");
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    //LOG.info("IMAGE_GALLERY_REQUEST_CODE imageUri"+imageUri);
                    new SendFile(data.getData(),"gallaryimage").execute();
                    /*
                    String picturePath = ChatMethodHelper.getImagePath(ChatAdvancedActivity.this, imageUri);
                    if (picturePath==null || picturePath.equals("")) {
                        Toast.makeText(ChatAdvancedActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                    } else {
                        CSChatObj.sendPhoto(destination,picturePath,false);
                    }*/
                }

                break;
            case VIDEO_GALLERY_REQUEST_CODE:

                if (data != null && data.getData() != null) {
                    Uri videoUri = data.getData();
                    //LOG.info("IMAGE_GALLERY_REQUEST_CODE imageUri"+videoUri);
                    new SendFile(data.getData(),"video").execute();
                    /*
                    String videoPath = ChatMethodHelper.getImagePath(ChatAdvancedActivity.this, videoUri);
                    if (videoPath==null || videoPath.equals("")) {
                        Toast.makeText(ChatAdvancedActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                    } else {
                        CSChatObj.sendVideo(destination, videoPath, false);
                    }
                    */
                }

                break;
            case DOCUMENTS_REQUEST_CODE:

                if (data != null && data.getData() != null) {
                    Log.i(TAG, "Sending document, FilePath: " + data.getData());
                    try {
                        //CSChatObj.sendDocument(destination, data.getData(), false);
                        new SendFile(data.getData(),"doc").execute();
                    } catch (Exception e) {
                        Toast.makeText(ChatAdvancedActivity.this, getResources().getString(R.string.wrong_document), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChatAdvancedActivity.this, getResources().getString(R.string.wrong_document), Toast.LENGTH_SHORT).show();
                }

                break;

            case LOCATION_REQUEST_CODE:

                if (data != null) {
                    CSChatObj.sendLocation(destination, new CSChatLocation(data.getDoubleExtra(ChatConstants.INTENT_LOCATION_LATITUDE,0.0),data.getDoubleExtra(ChatConstants.INTENT_LOCATION_LONGITUDE,0.0),data.getStringExtra(ChatConstants.INTENT_LOCATION_ADDRESS)), false);
                }

                break;

            case AUDIO_REQUEST_CODE:

                if (data != null && data.getData() != null) {
                    Uri selectedAudio = data.getData();
                    Log.i(TAG, "Selected Audio URI: " + selectedAudio);

                    //String audioPath = ChatMethodHelper.getPath(getApplicationContext(), selectedAudio);
                    //CSChatObj.sendAudio(destination, audioPath, false);
                    new SendFile(selectedAudio,"audio").execute();
                }

                break;
            case FORWARD_MESSAGE_INTNET:
                try {
                    showProgressBar();
                    if (data != null && resultCode == Activity.RESULT_OK) {
                        //LOG.info(("Got to be forwarded data");
                        String chatid1 = data.getStringExtra("chatid"); //dummy data
                        List<String> numbers = data.getStringArrayListExtra("contactnumbers");
                        if (chatid1 == null || chatid1.equals("") || numbers.isEmpty()) {
                            Toast.makeText(ChatAdvancedActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            Toast.makeText(ChatAdvancedActivity.this, "Forwarding..", Toast.LENGTH_SHORT).show();
                        }



                        Cursor cur =  CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination);

                        for(int cursorposition:selectedpositions) {
                            boolean showforward = true;
                            cur.moveToPosition(cursorposition);
                            int chattype =  cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
                            String chatid = cur.getString(cur.getColumnIndex(CSDbFields.KEY_CHAT_ID));
                            String message = cur.getString(cur.getColumnIndex(CSDbFields.KEY_CHAT_MESSAGE));
                            String uploadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                            String downloadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                            int issender =  cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_IS_SENDER));
                            int ismultidevice =  cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE));
                            int downloadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                            //int uploadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));

                            if (chattype == CSConstants.E_TEXTPLAIN || chattype == CSConstants.E_CONTACT || chattype == CSConstants.E_LOCATION) {
                            } else {

                                if(issender == 1) {

                                    if(ismultidevice == 0) {
                                        if(!new File(uploadfilepath).exists()) {
                                            showforward = false;
                                        }
                                    } else {
                                        if(!new File(downloadfilepath).exists() || downloadpercentage != 100) {
                                            showforward = false;
                                        }
                                    }

                                } else {
                                    if(!new File(downloadfilepath).exists() || downloadpercentage != 100) {
                                        showforward = false;
                                    }
                                }
                            }

                            if(showforward) {










                        for (String number : numbers) {

                            boolean isgroupmessage = false;
                           /* try {
                                JSONObject jsonObj = new JSONObject(number);
                                String id = jsonObj.getString("id");
                                String type = jsonObj.getString("type");
                                if (type.equals(com.ca.Utils.CSConstants.GROUP)) {
                                    isgroupmessage = true;
                                }
                                number = id;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Toast.makeText(ChatAdvancedActivity.this, "skipped to a recipient", Toast.LENGTH_SHORT).show();
                                continue;
                            }
                            */
                            //LOG.info(("Forwarding to:" + number);
                            //LOG.info(("Forwarding to isgroupmessage:" + isgroupmessage);

                            switch (chattype) {
                                case CSConstants.E_TEXTPLAIN:
                                    CSChatObj.sendMessage(number, message, isgroupmessage);
                                    break;
                                case CSConstants.E_TEXTHTML:
                                    CSChatObj.sendMessage(number, message, isgroupmessage);
                                    break;
                                case CSConstants.E_LOCATION:
                                    CSChatLocation cschatlocation = CSChatObj.getLocationFromChatID(chatid);
                                    CSChatObj.sendLocation(number, cschatlocation, isgroupmessage);
                                    break;
                                case CSConstants.E_IMAGE:
                                    if (issender == 1) {
                                        if (ismultidevice == 0) {
                                            CSChatObj.sendPhoto(number, uploadfilepath, isgroupmessage);
                                        } else {
                                            CSChatObj.sendPhoto(number, downloadfilepath, isgroupmessage);
                                        }
                                    } else {
                                        CSChatObj.sendPhoto(number, downloadfilepath, isgroupmessage);
                                    }
                                    break;
                                case CSConstants.E_VIDEO:
                                    //LOG.info(("uploadfilepath:" + uploadfilepath);

                                    if (issender == 1) {
                                        if (ismultidevice == 0) {
                                            CSChatObj.sendVideo(number, uploadfilepath, isgroupmessage);
                                        } else {
                                            CSChatObj.sendVideo(number, downloadfilepath, isgroupmessage);
                                        }
                                    } else {
                                        CSChatObj.sendVideo(number, downloadfilepath, isgroupmessage);
                                    }
                                    break;
                                case CSConstants.E_CONTACT:
                                    CSChatContact cschatContact = CSChatObj.getContactFromChatID(chatid);
                                    CSChatObj.sendContact(number, cschatContact, isgroupmessage);
                                    break;
                                case CSConstants.E_DOCUMENT:
                                    if (issender == 1) {
                                        if (ismultidevice == 0) {
                                            CSChatObj.sendDocument(number, uploadfilepath, isgroupmessage);
                                        } else {
                                            CSChatObj.sendDocument(number, downloadfilepath, isgroupmessage);
                                        }
                                    } else {
                                        CSChatObj.sendDocument(number, downloadfilepath, isgroupmessage);
                                    }
                                    break;
                                case CSConstants.E_AUDIO:
                                    if (issender == 1) {
                                        if (ismultidevice == 0) {
                                            CSChatObj.sendAudio(number, uploadfilepath, isgroupmessage);
                                        } else {
                                            CSChatObj.sendAudio(number, downloadfilepath, isgroupmessage);
                                        }
                                    } else {
                                        CSChatObj.sendAudio(number, downloadfilepath, isgroupmessage);
                                    }
                                    break;
                            }


                        }


                            }

                        }

                        cur.close();

                        if (numbers.size() == 1) {

                            Intent intent = new Intent(getApplicationContext(), ChatAdvancedActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            intent.putExtra("Sender", numbers.get(0));
                            intent.putExtra("IS_GROUP", false);
                            MainActivity.context.startActivity(intent);

                            finish();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                chatoptionsbacklayoutview.performClick();
                cancelProgressBar();
                break;

            case CONTACT_REQUEST_CODE:
                if(resultCode == Activity.RESULT_OK && data != null) {
                    //LOG.info(("Got Contact data");

                    ArrayList<String> contacts = data.getStringArrayListExtra("contactnumbers");
                    ArrayList<String> labels = data.getStringArrayListExtra("contactslabels");
                    ArrayList<String> contactnames = data.getStringArrayListExtra("contactsnames");

                    //LOG.info(("Got Contact data size" + contacts.size());


                    for (int i = 0; i < contacts.size(); i++) {

                        List<String> list1 = new ArrayList<String>();
                        List<String> list2 = new ArrayList<String>();

                        String name = contactnames.get(i);
                        list1.add(contacts.get(i));
                        list2.add(labels.get(i));


                        CSChatContact CSChatContactObj = new CSChatContact(name, list1, list2);
                        boolean result = CSChatObj.sendContact(destination, CSChatContactObj, false);
                        //LOG.info(("MEssage Result:" + result);
                    }
                }
                    break;
        }
    }

















    private void showGalleryOptions() {
        final Dialog chatGalleryOptionDialog = new Dialog(ChatAdvancedActivity.this);
        chatGalleryOptionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        chatGalleryOptionDialog.setContentView(R.layout.dialog_chat_options);
        chatGalleryOptionDialog.setCancelable(true);
        chatGalleryOptionDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        // chatGalleryOptionDialog.getWindow().getAttributes().windowAnimations = R.style.Theme_Dialog_Translucent;
        LinearLayout imageGalleryTV = chatGalleryOptionDialog.findViewById(R.id.chat_option_image);
        LinearLayout videoGalleryTV = chatGalleryOptionDialog.findViewById(R.id.chat_option_video);

        imageGalleryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatGalleryOptionDialog!=null) {
                    chatGalleryOptionDialog.dismiss();
                }
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                startActivityForResult(i, IMAGE_GALLERY_REQUEST_CODE);
            }
        });

        videoGalleryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatGalleryOptionDialog!=null) {
                    chatGalleryOptionDialog.dismiss();
                }
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("video/*");
                startActivityForResult(i, VIDEO_GALLERY_REQUEST_CODE);
            }
        });

        chatGalleryOptionDialog.show();
    }
	public void showProgressBar() {
		try {
if(mdialog!=null&&mdialog.isShowing()){
    return;
}
		    Log.i(TAG,"showProgressBar");
			mdialog = new MyProgressDialog(ChatAdvancedActivity.this);
			//mdialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
			mdialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
			//mdialog.setMessage("Loading media...");
			mdialog.setCancelable(false);
			mdialog.getWindow().setDimAmount(0.0f);
             mdialog.show();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void cancelProgressBar() {
		try {
            Log.i(TAG,"cancelProgressBar");
            //if(!chatsearchrefstring.equals("hi he")) {
                if (mdialog != null) {
                    mdialog.dismiss();
                }
            //}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
    public void showProgressBar1() {
        try {
            if(mdialog1!=null&&mdialog1.isShowing()){
                return;
            }
            Log.i(TAG,"showProgressBar1");
            mdialog1 = new ProgressDialog(ChatAdvancedActivity.this);
            mdialog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //mdialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            //mdialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            mdialog1.setMessage("Loading media...");
            mdialog1.setCancelable(false);
            //mdialog1.getWindow().setDimAmount(0.0f);
            mdialog1.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void cancelProgressBar1() {
        try {
            Log.i(TAG,"cancelProgressBar1");
            //if(!chatsearchrefstring.equals("hi he")) {
            if (mdialog1 != null) {
                mdialog1.dismiss();
            }
            //}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public boolean showactivatelocationalert(String message) {
        try {
            AlertDialog.Builder successfullyLogin = new AlertDialog.Builder(ChatAdvancedActivity.this);
            successfullyLogin.setMessage(message);
            successfullyLogin.setPositiveButton("Settings",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
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
        } catch(Exception ex) {
            return false;
        }

    }


    private void notifyDataSetChanged(boolean focustounreadorbottom,boolean insearchmode,String searchstring,boolean isfocusneeded) {
        try {
            Log.i(TAG,"adp.getItemCount():"+adp.getItemCount());
            adp.swapCursorAndNotifyDataSetChanged(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination),insearchmode,searchstring);


            if(isfocusneeded) {
                Cursor ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(destination);
                int focus = ccr.getCount();
                if (focustounreadorbottom) {
                    rv.getLayoutManager().scrollToPosition(adp.getItemCount() - (1 + focus));
                } else {
                    rv.getLayoutManager().scrollToPosition(adp.getItemCount() - (1));
                }
                ccr.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

/*
    private void notifyDataSetChangedForSearch(String searchstring) {
        try {

            adp.swapCursorAndNotifyDataSetChanged(CSDataProvider.getSearchInSingleChatCursor(destination, searchstring),true,searchstring);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
*/






    public int checkOrientation(String imagePath) {
        int orientation = 0;
        try {

            ExifInterface exif = new ExifInterface(imagePath);

            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = 270;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = 180;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = 90;

                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                    orientation = 0;

                    break;
                default:
                    //LOG.info(("orientation is invalid");
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return orientation;
    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        //matrix.preRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }



    /**
     * Handle media recorder
     */
    public void MediaRecorderReady() {
        mAudioRecorder = new MediaRecorder();
        mAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mAudioRecorder.setOutputFile(AudioSavePathInDevice);
    }

    private void getTheFileNameWithPath() {
        try {
            File wallpaperDirectory = new File(ChatConstants.CHAT_AUDIO_DIRECTORY_SENT);
            if (!wallpaperDirectory.exists()) {
                if (wallpaperDirectory.mkdirs()) {
                    Log.d(TAG, "Successfully created the parent dir:" + wallpaperDirectory.getName());
                } else {
                    Log.d(TAG, "Failed to create the parent dir:" + wallpaperDirectory.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        AudioSavePathInDevice = ChatConstants.CHAT_AUDIO_DIRECTORY_SENT + "/" + "Rec_"+new SimpleDateFormat("dd-MM-yyyy hh mm a").format(new Date().getTime())  + "mp3";

    }

    /**
     * This method opens the dialog to get confirmation from user to share the recorded file
     */
    private void showConfirmationDialog() {
        try {
            try {
                h.removeCallbacks(RunnableObj);
                if (mAudioRecorder != null)
                    mAudioRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(ChatAdvancedActivity.this);
            dialog.setTitle("Confirmation");
            dialog.setMessage("Are you sure want to send this audio?");
            dialog.setPositiveButton(("Send"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    CSChatObj.sendAudio(destination, AudioSavePathInDevice, false);
                }
            });

            dialog.setNegativeButton(("Cancel"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        File file = new File(AudioSavePathInDevice);
                        file.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
private void setTitleName() {
        try {
            String name = "";
            Cursor ccfr = CSDataProvider.getContactCursorByNumber(destination);
            ////LOG.info(("ccfr.getCount():"+ccfr.getCount());
            if(ccfr.getCount()>0) {
                ccfr.moveToNext();
                name = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
            }
            ccfr.close();
            if(name.equals("")) {
                toolbarTitleTextView.setText(destination);
            } else {
                toolbarTitleTextView.setText(name);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
}

    private void loadProfilePicture() {
try {

    String picid = "";
    Cursor cur1 = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, destination);
    if (cur1.getCount() > 0) {
        cur1.moveToNext();
        picid = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
    }
    cur1.close();
    Log.i(TAG,"picid:"+picid);
    String filepath = CSDataProvider.getImageThumbnailFilePath(picid);
    String filepath1 = CSDataProvider.getImageFilePath(picid);
    Log.i(TAG,"filepath:"+filepath);
    Log.i(TAG,"filepath:"+filepath1);

    if (filepath != null && new File(filepath).exists()) {
        Glide.with(ChatAdvancedActivity.this)
                .load(Uri.fromFile(new File(filepath)))
                .apply(new RequestOptions().error(R.drawable.defaultcontact))
                .apply(RequestOptions.circleCropTransform())
                .apply(new RequestOptions().signature(new ObjectKey(String.valueOf(new File(filepath).length()+new File(filepath).lastModified()))))
                .into(userImageView);
    }
} catch (Exception ex) {
    ex.printStackTrace();
}

    }
    private void applyAnimationToSubTitle() {
        try {



            toolbarSubTitleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            toolbarSubTitleTextView.setMarqueeRepeatLimit(1);
            toolbarSubTitleTextView.setSelected(true);



                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if(!onpaused) { //change later
                                        toolbarSubTitleTextView.setText(toolbarSubTitleTextView.getText().toString().replace("active ", ""));
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }, 2950);



        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
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



    private void handleSearch(int direction) {


        showProgressBar();
        if(mdialog!=null) {
            Log.i(TAG,"mdialog:"+mdialog.isShowing());
        } else {
            Log.i(TAG,"mdialog null:");
        }
        try {
            Thread.sleep(500);//remove this later
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
        try {


            if (chatsearchrefmode && chatsearchrefstring !=null && !chatsearchrefstring.equals("")) {
                //showProgressBar();
                if(direction == 1) {
                    chatsearchrefcnt++;
                } else {
                    chatsearchrefcnt--;
                }

                Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);

                int i = 0;
                int temp_chatsearchrefcnt = 0;
                boolean searchstringfound = false;
int totalcount = cur.getCount();

/*
                Log.i(TAG,"i:"+i);
                Log.i(TAG,"temp_chatsearchrefcnt:"+temp_chatsearchrefcnt);
                Log.i(TAG,"searchstringfound:"+searchstringfound);
                Log.i(TAG,"chatsearchrefcnt:"+chatsearchrefcnt);
*/
                //cur.moveToLast();
for(int k = (totalcount-1);k>=0;k--) {
    cur.moveToPosition(k);
                //while (cur.moveToPrevious()) {
                    String message = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                    String filename = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                    if(!chatsearchrefstring.equals("") && (message.toLowerCase().contains(chatsearchrefstring.toLowerCase()) || filename.toLowerCase().contains(chatsearchrefstring.toLowerCase()))) {
                        temp_chatsearchrefcnt++;
                    }
                    i++;

                    if(temp_chatsearchrefcnt == chatsearchrefcnt) {
                        searchstringfound = true;
                        /*Log.i(TAG,"matching:"+chatsearchrefstring);
                        Log.i(TAG,"matching message:"+message);
                        Log.i(TAG,"matching filename:"+filename);*/
                        break;
                    }
                }
                cur.close();

                if(searchstringfound) {

final int final_i = i;
                    ChatAdvancedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            rv.getLayoutManager().scrollToPosition(totalcount-final_i);
                        }
                    });

} else {

    if(direction == 1) {
        chatsearchrefcnt--;
    } else {
        chatsearchrefcnt++;
    }
                    ChatAdvancedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Log.i(TAG,"search String not found");
                            Toast.makeText(ChatAdvancedActivity.this, "Not found", Toast.LENGTH_SHORT).show();
                        }
                    });


}


            }
            cancelProgressBar();
        } catch (Exception ex) {
            ex.printStackTrace();
            cancelProgressBar();
        }
            }}).start();
    }


    public static void handlelongclick(ArrayList<Integer> selpositions) {
        try {
            selectedpositions = new ArrayList<>(selpositions);
            if(selectedpositions.size()>0) {
                chatoptionsview.setVisibility(View.VISIBLE);

                toolbar.setVisibility(View.GONE);
                if(searchchatview.getVisibility() == View.VISIBLE) {
                    backviewsearch.performClick();
                }


                chat_selected_countview.setText(String.valueOf(selectedpositions.size()));

boolean showdelete = true; //dont assign true any where
boolean showcopy = true;
boolean showforward = true;

              Cursor cur =  CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination);

              for(int cursorposition:selpositions) {
                  cur.moveToPosition(cursorposition);
                  int chattype =  cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
                  //String chatid = cur.getString(cur.getColumnIndex(CSDbFields.KEY_CHAT_ID));
                  String uploadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                  String downloadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                  int issender =  cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_IS_SENDER));
                  int ismultidevice =  cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE));
                  int downloadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                  int uploadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));

                  if (chattype == CSConstants.E_TEXTPLAIN || chattype == CSConstants.E_CONTACT || chattype == CSConstants.E_LOCATION) {

                      //showdelete //always true for this types but don't assign here
                      //showcopy //always true for this types but don't assign here
                      //showforward //always true for this types but don't assign here

                  } else {
                      showcopy = false; //always false for this types but don't assign here

                      if(issender == 1) {

                          if(ismultidevice == 0) {
                              if(!new File(uploadfilepath).exists()) {
                                  showforward = false;
                              }
                          } else {
                              if(!new File(downloadfilepath).exists() || downloadpercentage != 100) {
                                  showforward = false;
                              }
                          }

                      } else {
                          if(!new File(downloadfilepath).exists() || downloadpercentage != 100) {
                              showforward = false;
                          }
                      }
                  }



              }

              cur.close();

              if(showdelete) {
                  chatdeleteview.setVisibility(View.VISIBLE);
              } else {
                  chatdeleteview.setVisibility(View.GONE);
              }
                if(showcopy) {
                    chatcopyview.setVisibility(View.VISIBLE);
                } else {
                    chatcopyview.setVisibility(View.GONE);
                }

                if(showforward) {
                    chatforwardview.setVisibility(View.VISIBLE);
                } else {
                    chatforwardview.setVisibility(View.GONE);
                }

            } else {
                chatoptionsview.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
                chat_message_layoutview.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
ex.printStackTrace();
        }
    }



    public boolean showdeletealert() {
        try {
            //android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(ChatAdvancedActivity.this);

            chatfiledelete = false;
            boolean containsfiles = false;


            final Dialog successfullyLogin = new Dialog(ChatAdvancedActivity.this);

            successfullyLogin.setCancelable(true);
            String deletetitle = "Delete " + selectedpositions.size()+" message/s ?";
            successfullyLogin.setTitle(deletetitle);
            successfullyLogin.setContentView(R.layout.deletechatconfirmation);

            TextView title = (TextView) successfullyLogin.findViewById(R.id.title);
            CheckBox checkbox = (CheckBox) successfullyLogin.findViewById(R.id.checkbox);
            TextView cancel = (TextView) successfullyLogin.findViewById(R.id.cancel);
            TextView delete = (TextView) successfullyLogin.findViewById(R.id.delete);
            title.setText(deletetitle);


            Cursor cur =  CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination);

            for(int cursorposition:selectedpositions) {
                cur.moveToPosition(cursorposition);
                int chattype =  cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
                //String chatid = cur.getString(cur.getColumnIndex(CSDbFields.KEY_CHAT_ID));
                String uploadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                String downloadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                int issender =  cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_IS_SENDER));
                int ismultidevice =  cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE));
                int downloadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                int uploadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));

                if (chattype == CSConstants.E_TEXTPLAIN || chattype == CSConstants.E_CONTACT) {

                } else {

                    if(issender == 1) {

                        if(ismultidevice == 0) {
                            containsfiles = false;
                        } else {
                            if(new File(downloadfilepath).exists() && downloadpercentage == 100) {
                                containsfiles = true;
                            }
                        }

                    } else {
                        if(new File(downloadfilepath).exists() && downloadpercentage == 100) {
                            containsfiles = true;
                        }
                    }
                }



            }

            cur.close();
















            if(containsfiles) {
                checkbox.setVisibility(View.VISIBLE);
            } else {
                checkbox.setVisibility(View.GONE);
            }

            checkbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        if (checkbox.isChecked()) {
                            chatfiledelete =  true;
                        } else {
                            chatfiledelete = false;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

adp.releaseMediaPlayer();

                    Cursor cur =  CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination);

                    for(int cursorposition:selectedpositions) {
                        cur.moveToPosition(cursorposition);
                        int chattype =  cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
                        String chatid = cur.getString(cur.getColumnIndex(CSDbFields.KEY_CHAT_ID));
                        String uploadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                        String downloadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int issender =  cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_IS_SENDER));
                        int ismultidevice =  cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE));
                        int downloadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        int uploadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));

                        CSChatObj.deleteChatMessagebyfilter(CSDbFields.KEY_CHAT_ID, chatid);

                        if(chatfiledelete) {
                            if (chattype == CSConstants.E_TEXTPLAIN || chattype == CSConstants.E_CONTACT) {

                            } else {

                                if (issender == 1) {

                                    if (ismultidevice == 0) {

                                    } else {
                                        if (new File(downloadfilepath).exists() && downloadpercentage == 100) {
                                            //containsfiles = true;
                                            new File(downloadfilepath).delete();
                                        }
                                    }

                                } else {
                                    if (new File(downloadfilepath).exists() && downloadpercentage == 100) {
                                        //containsfiles = true;
                                        new File(downloadfilepath).delete();
                                    }
                                }
                            }
                        }


                    }

                    cur.close();
                    successfullyLogin.dismiss();

                    chatoptionsbacklayoutview.performClick();

                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    successfullyLogin.dismiss();
                }
            });

        /*
            successfullyLogin.setPositiveButton("Delete",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {



                        }
                    });

            successfullyLogin.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {

                        }
                    });
            */

            successfullyLogin.show();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    /*use //doesnt work like this. use swapCursorAndNotifyDataSetChanged
    public static int getpositionfromchatid(String chatid) {
        try {
            int position = -1;
            Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination);
            if(cur.getCount()>0) {
                for(int i = 0;i<cur.getCount();i++) {
                    cur.moveToNext();
                    String cursorchatid = cur.getString(cur.getColumnIndex(CSDbFields.KEY_CHAT_ID));
if(cursorchatid.equals(chatid)) {
    position = i;
    break;
}
                }
            }
            cur.close();
            return position;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }

    }
*/



    public  boolean showclearchatoptions() {
        try {

            AlertDialog.Builder successfullyLogin = new AlertDialog.Builder(ChatAdvancedActivity.this);
            String name = "";
            Cursor ccfr = CSDataProvider.getContactCursorByNumber(destination);
            ////LOG.info(("ccfr.getCount():"+ccfr.getCount());
            if(ccfr.getCount()>0) {
                ccfr.moveToNext();
                name = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
            }
            ccfr.close();
            if(name.equals("")) {
                successfullyLogin.setMessage("Clear all Chat with " + destination + "?");
            } else {
                successfullyLogin.setMessage("Clear all Chat with " + name + "?");
            }


            successfullyLogin.setCancelable(true);


            successfullyLogin.setPositiveButton("Delete",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {

                            CSChatObj.deleteChatMessagebyfilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);
                            notifyDataSetChanged(false,false,"",true);
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







    public String getfilepathAfterCopy(Uri fileUri,String targetpath) {

        String attachmentpath = "";
        try {

System.out.println("fileUri:"+fileUri);
            String extension = "";
            String filename = "";


            try {
                Cursor cursor = MainActivity.context.getContentResolver().query(fileUri, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } catch (Exception ex) {
                   ex.printStackTrace();
                } finally {
                    cursor.close();
                }
            } catch (Exception ex) {
               ex.printStackTrace();
            }



            if (filename.equals("")) {
                try {
                    int i = fileUri.toString().lastIndexOf('/');
                    if (i > 0) {
                        filename = fileUri.toString().substring(i);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


            if (filename.equals("")) {
                try {

                    ContentResolver cR = MainActivity.context.getContentResolver();
                    if (fileUri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                        //If scheme is a content
                        final MimeTypeMap mime = MimeTypeMap.getSingleton();
                        extension = mime.getExtensionFromMimeType(MainActivity.context.getContentResolver().getType(fileUri));
                    } else {
                        //If scheme is a File
                        //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
                        //extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(imageUri.getPath())).toString());
                        extension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());

                    }
                    //LOG.info(("extension from mime:"+extension);

                    if (extension == null) {
                        int i = fileUri.toString().lastIndexOf('.');
                        if (i > 0) {
                            extension = fileUri.toString().substring(i + 1);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (extension == null) {
                    extension = "";
                }


                if (filename.equals("")) {
                    filename = "Doc_" + String.valueOf(new Date().getTime()) + "." + extension;
                }
            }






            createDirIfNotExists(targetpath);


            InputStream input = MainActivity.context.getContentResolver().openInputStream(fileUri);
            try {
                if (new File(targetpath,filename).exists()) {
                    //LOG.info(("File name to copy:"+filename);
                    try {
                        String[] filenames = filename.split("\\.");
                        String t_name = filenames[0];
                        String t_extention = filenames[1];
                        filename = t_name + "_" + String.valueOf(new Date().getTime()) + "." + t_extention;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        filename = filename + "_" + String.valueOf(new Date().getTime());

                    }
                }
            } catch (Exception ex) {
               ex.printStackTrace();
            }
            File file = new File(targetpath,filename);
            try {

                OutputStream output = new FileOutputStream(file);
                try {
                    try {
                        byte[] buffer = new byte[4 * 1024]; // or other buffer size
                        int read;

                        if (input != null) {
                            while ((read = input.read(buffer)) != -1) {
                                output.write(buffer, 0, read);
                            }
                        }
                        output.flush();
                    } finally {
                        output.close();
                        attachmentpath = file.getAbsolutePath();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (Exception e) {
                   e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return attachmentpath;
    }



    public static boolean createDirIfNotExists(String path) {
        try {

            if (ContextCompat.checkSelfPermission(MainActivity.context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {


                Boolean isSDPresent = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
                //Constants.appname = IAmLiveCore.getApplicationName();

                if (isSDPresent) {
                    ////LOG.info(("yes SD-card is present:"+Environment.getExternalStorageDirectory().getAbsolutePath());

                    File file = new File(Environment.getExternalStorageDirectory() + File.separator + path);
                    if (!file.exists()) {
                        if (!file.mkdirs()) {
                            ////LOG.info(("Problem creating a folder");
                            return false;
                        }
                    }


                    return true;

                } else {

                    ////LOG.info(("NO SD-card is present\n");
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;

    }

    class SendFile extends AsyncTask<String, Integer, String>
    {
        String TAG = getClass().getSimpleName();


        private Uri fileUri;
        private String filetype = "doc";


        public SendFile(Uri uri, String type) {
            fileUri = uri;
            filetype = type;
        }

        protected void onPreExecute (){
            super.onPreExecute();
            Log.d(TAG + " PreExceute","On pre Exceute......");
            showProgressBar1();
        }

        protected String doInBackground(String... params) {
            Log.d(TAG + " DoINBackGround","On doInBackground...");

            String targetpath = Constants.docsdirectorysent;
            if(filetype.equals("audio")) {
                targetpath = Constants.audiodirectorysent;
            } else if(filetype.equals("video")) {
                targetpath = Constants.videodirectorysent;
            } else if(filetype.equals("gallaryimage")) {
                targetpath = Constants.imagedirectorysent;
            } else if(filetype.equals("cameraimage")) {
                targetpath = Constants.imagedirectorysent;
            } else {
                targetpath = Constants.docsdirectorysent;
            }
            createDirIfNotExists(targetpath);
            return getfilepathAfterCopy(fileUri,targetpath);

        }

        /*
        protected void onProgressUpdate(Integer...a){
            super.onProgressUpdate(a);
            Log.d(TAG + " onProgressUpdate", "You are in progress update ... " + a[0]);
        }
        */

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG + " onPostExecute", "" + result);

            if(result!=null && !result.equals("")) {
                if(filetype.equals("audio")) {
                    CSChatObj.sendAudio(destination, result, false);
                } else if(filetype.equals("video")) {
                    CSChatObj.sendVideo(destination, result, false);
                } else if(filetype.equals("gallaryimage")) {
                    CSChatObj.sendPhoto(destination, result, false);
                } else if(filetype.equals("cameraimage")) {
                    CSChatObj.sendPhoto(destination, result, false);
                    if(imageFile.exists()) {
                        imageFile.delete();
                    }
                } else {
                    CSChatObj.sendDocument(destination, result, false);
                }
            }
            cancelProgressBar1();
        }
    }


}
