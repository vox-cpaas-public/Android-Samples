package com.ca.callsample;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ca.Utils.CSDbFields;
import com.ca.adapaters.RecentsDetailLogAdapter;
import com.ca.utils.PrefereceProvider;
import com.ca.utils.utils;
import com.ca.wrapper.CSDataProvider;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.lang.ref.WeakReference;

public class ShowUserLogActivity extends AppCompatActivity {
    private String mContactNumber = "";
    ListView mListView;
    private CircularImageView mCallLogsImage;
    private TextView mContactNameTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlog);
        try {
            TextView totalRecordsTv = (TextView) findViewById(R.id.total_records_tv);
            mContactNameTv=findViewById(R.id.call_logs_name_tv);
            mCallLogsImage=findViewById(R.id.user_profle_image);

            mContactNumber = getIntent().getStringExtra("number");
            String name = getIntent().getStringExtra("name");
            PrefereceProvider prefereceProvider=new PrefereceProvider(getApplicationContext());
            prefereceProvider.setPrefString(mContactNumber+"MissedData","");
            // if contact not saved in device below if condition show the name as "Unknown"
            if (name.equals("")) {
                name = "Unknown";
            }

            if(name.equals("Unknown")){
                mContactNameTv.setText(mContactNumber);
            }else {
                mContactNameTv.setText(name);
            }

            // this method will get the contact image
            getProfilePicture();
            getSupportActionBar().setTitle("Call Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            mListView = findViewById(R.id.call_logs_listView);

            // this will create adapter with call logs info list from cursor
            RecentsDetailLogAdapter appContactsAdapter = new RecentsDetailLogAdapter(MainActivity.context, CSDataProvider.getCallLogCursorByFilter(CSDbFields.KEY_CALLLOG_NUMBER, mContactNumber), 0);
            mListView.setAdapter(appContactsAdapter);
            // below API will get total no of records for particular call
            Cursor cur = CSDataProvider.getCallLogCursorByFilter(CSDbFields.KEY_CALLLOG_NUMBER, mContactNumber);
            totalRecordsTv.setText(String.valueOf(cur.getCount()) + " record(s) in total");
            cur.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            finish();
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
     * This method will get the profile details to get contact image
     */
    private void getProfilePicture() {
        String nativecontactid = "";
        Cursor cur = CSDataProvider.getContactCursorByNumber(mContactNumber);
        if (cur.getCount() > 0) {
            cur.moveToNext();
            nativecontactid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
        }
        cur.close();

        String picid = "";
        Cursor cur1 = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, mContactNumber);
        if (cur1.getCount() > 0) {
            cur1.moveToNext();
            picid = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
        }
        cur1.close();

        new ImageDownloaderTask(mCallLogsImage).execute("app", picid, nativecontactid);
    }

    /**
     * This AsyncTask will get the contact profile image
     */
    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap photo = null;
            try {
                if (params[0].equals("app")) {
                    photo = CSDataProvider.getImageBitmap(params[1]);
                } else {
                    photo = utils.loadContactPhoto(Long.parseLong(params[1]));
                }
                if (params[0].equals("app") && photo == null) {
                    photo = utils.loadContactPhoto(Long.parseLong(params[2]));
                }

                if (photo == null) {
                    photo = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.defaultcontact);
                }
            } catch (Exception e) {
                photo = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.defaultcontact);
            }
            return photo;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }
    }
}
