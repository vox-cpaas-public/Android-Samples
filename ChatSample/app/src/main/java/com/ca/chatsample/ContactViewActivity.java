package com.ca.chatsample;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ca.Utils.CSDbFields;
import com.ca.wrapper.CSDataProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ContactViewActivity extends AppCompatActivity {
    public static Toolbar mToolbar;
    private TextView mConatctNameTv;
    private RecyclerView contactViewRecyclerView;
    private JSONArray numbersList;
    private JSONArray labelsList;
    private TextView mAddNewContactTv, mAddToExistingContactTv;
    private String TAG = "ContactViewActivity";
    private LinearLayout mConatctCreateOptionsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_view);
        mToolbar = findViewById(R.id.toolbar);
        contactViewRecyclerView = findViewById(R.id.contact_view_recycler_view);
        mConatctNameTv = findViewById(R.id.contact_view_name_tv);
        mAddNewContactTv = findViewById(R.id.create_new_contact_tv);
        mAddToExistingContactTv = findViewById(R.id.create_existing_contact_tv);
        mConatctCreateOptionsLayout = findViewById(R.id.contact_create_options_layout);
        mToolbar.setTitle("Contact Details");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        contactViewRecyclerView.setLayoutManager(linearLayoutManager);
        String chatId = getIntent().getStringExtra("chatId");
        Cursor cursor = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_ID, chatId);
        String chatMessage = "";
        if (cursor.moveToNext()) {
            chatMessage = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
            int isSender = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_IS_SENDER));
            if (isSender == 1) {
                mConatctCreateOptionsLayout.setVisibility(View.GONE);
            }
        }

        JSONObject contactJSONObject = null;
        try {
            contactJSONObject = new JSONObject(chatMessage);
            String contactName = contactJSONObject.getString("name");
            numbersList = contactJSONObject.getJSONArray("numbers");
            labelsList = contactJSONObject.getJSONArray("labels");
            mConatctNameTv.setText(contactName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ContactViewAdapter contactViewAdapter = new ContactViewAdapter();
        contactViewRecyclerView.setAdapter(contactViewAdapter);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mAddNewContactTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAsContactConfirmed();
            }
        });
        mAddToExistingContactTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAsExistingContact();
            }
        });

    }

    private void addAsContactConfirmed() {
        try {
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            ArrayList<ContentValues> data = new ArrayList<ContentValues>();

//Filling data with phone numbers
            for (int i = 0; i < numbersList.length(); i++) {
                ContentValues row = new ContentValues();
                row.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                row.put(ContactsContract.CommonDataKinds.Phone.NUMBER, numbersList.get(i).toString());
                row.put(ContactsContract.CommonDataKinds.Phone.TYPE, getContactType(labelsList.get(i).toString()));
                data.add(row);
            }

            intent.putExtra(ContactsContract.Intents.Insert.NAME, mConatctNameTv.getText().toString());
            intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addAsExistingContact() {
        try {
            Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
            intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
            ArrayList<ContentValues> data = new ArrayList<ContentValues>();

//Filling data with phone numbers
            for (int i = 0; i < numbersList.length(); i++) {
                ContentValues row = new ContentValues();
                row.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                row.put(ContactsContract.CommonDataKinds.Phone.NUMBER, numbersList.get(i).toString());
                row.put(ContactsContract.CommonDataKinds.Phone.TYPE, getContactType(labelsList.get(i).toString()));
                data.add(row);
            }

            intent.putExtra(ContactsContract.Intents.Insert.NAME, mConatctNameTv.getText().toString());
            intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ContactViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        private class ContactViewHolder extends RecyclerView.ViewHolder {
            TextView contactTypeTv, conatctNumberTv;

            public ContactViewHolder(View itemView) {
                super(itemView);
                contactTypeTv = itemView.findViewById(R.id.contact_type_tv);
                conatctNumberTv = itemView.findViewById(R.id.contact_number_tv);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.adapter_contact_view, parent, false);

            return new ContactViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ContactViewHolder) {
                final ContactViewHolder contactViewHolder = (ContactViewHolder) holder;
                try {
                    contactViewHolder.conatctNumberTv.setText(numbersList.get(position).toString());
                    contactViewHolder.contactTypeTv.setText(labelsList.get(position).toString());
                    Log.i(TAG, "onBindViewHolder: conatct type " + labelsList.get(position).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getItemCount() {
            return numbersList.length();
        }
    }

    private int getContactType(String conatctType) {
        if (conatctType.toUpperCase().equals("HOME")) {
            return ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
        } else if (conatctType.toUpperCase().equals("MOBILE")) {
            return ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
        } else if (conatctType.toUpperCase().equals("WORK")) {
            return ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
        } else if (conatctType.toUpperCase().equals("OTHER")) {
            return ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
        } else if (conatctType.toUpperCase().equals("FAX")) {
            return ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK;
        }
        return ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
    }
}
