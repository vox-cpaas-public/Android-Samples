package com.ca.chatsample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ca.Utils.CSDbFields;
import com.ca.adapaters.ShareContactsInChatAdapter;
import com.ca.wrapper.CSDataProvider;

import java.util.ArrayList;
import java.util.List;


public class ShareContactsInChatActivity extends AppCompatActivity {

    private ListView mListView;
    private ShareContactsInChatAdapter addContactsAdapter;
    private Button submitContacts;
    private EditText editText;
    Toolbar toolbar;
    //int selected_contact_count = 0;

    public static ArrayList<String> rawnumbers = new ArrayList<String>();
    ArrayList<String> labels = new ArrayList<String>();
    ArrayList<String> contactnames = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sharecontactsinchat);

        mListView = (ListView) findViewById(R.id.contact_list);
        submitContacts = (Button) findViewById(R.id.button_addContacts);
        editText = (EditText) findViewById(R.id.editText);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        editText.setText("");

        toolbar.setTitle("Konverz");

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setSubtitle("Share Contacts");


        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        rawnumbers.clear();
        labels.clear();
        contactnames.clear();


        addContactsAdapter = new ShareContactsInChatAdapter(ShareContactsInChatActivity.this, CSDataProvider.getContactsCursor(), 0);
        mListView.setAdapter(addContactsAdapter);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Cursor cursor = null;
                    String searchstring = editText.getText().toString();
                    if (searchstring.equals("")) {
                        cursor = CSDataProvider.getContactsCursor();
                    } else {
                        cursor = CSDataProvider.getSearchContactsCursor(searchstring);
                    }

                    if (cursor != null && cursor.getCount() > 0) {


                        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                        checkBox.performClick();

                        if (checkBox.isChecked()) {
                            cursor.moveToPosition(position);
                            String rawnumber = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_RAW_NUMBER));
                            String contactname = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
                            if (!rawnumbers.contains(rawnumber)) {
                                rawnumbers.add(rawnumber);
                                contactnames.add(contactname);
                            }
                        } else if (!checkBox.isChecked()) {
                            cursor.moveToPosition(position);
                            String rawnumber = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_RAW_NUMBER));
                            String contactname = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));

                            rawnumbers.remove(rawnumber);
                            contactnames.remove(contactname);

                        }


                        cursor.close();
                    }


                    toolbar.setSubtitle(String.valueOf(rawnumbers.size()) + " Contacts Selected");
                    hideKeyboard();
                } catch (Exception ex) {
                    //utils.logStacktrace(ex);
                    ex.printStackTrace();
                }


            }


        });


        submitContacts.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (rawnumbers.size() <= 0) {
                    Toast.makeText(getApplicationContext(), "Select at least one contact", Toast.LENGTH_SHORT).show();
                } else {
                    sendAddContactsRequest();
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
                refreshview();
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });
    }

    public void sendAddContactsRequest() {
        if (rawnumbers.size() <= 0) {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        } else {


            submitContacts.setEnabled(false);

            labels = getContactLabelDetails(rawnumbers);


            Intent resultIntent = new Intent();
            resultIntent.putExtra("contactnumbers", rawnumbers);
            resultIntent.putExtra("contactslabels", labels);
            resultIntent.putExtra("contactsnames", contactnames);
            setResult(Activity.RESULT_OK, resultIntent);

            //rawnumbers.clear();
            //labels.clear();
            //contactnames.clear();
            finish();

        }


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        rawnumbers.clear();
        labels.clear();
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    public void refreshview() {

        try {
            if (!editText.getText().toString().equals("")) {

                addContactsAdapter.changeCursor(CSDataProvider.getSearchContactsCursor(editText.getText().toString()));
                addContactsAdapter.notifyDataSetChanged();

            } else {

                addContactsAdapter.changeCursor(CSDataProvider.getContactsCursor());
                addContactsAdapter.notifyDataSetChanged();

            }

        } catch (Exception ex) {
            //utils.logStacktrace(ex);
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

        }
    }

    private ArrayList<String> getContactLabelDetails(List<String> rawnumbers) {
        ArrayList<String> labels = new ArrayList<String>();
        try {


            for (String rawnumber : rawnumbers) {
                //LOG.info("Raw number:" + rawnumber);
                Cursor cursor = getApplicationContext().getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                        new String[]{rawnumber}, null);

                if (cursor.getCount() > 0) {
                    cursor.moveToNext();

                    String label = "";

                    int labelType = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                    if (labelType == ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT) {
                        label = "Assistant";
                    } else if (labelType == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM) {
                        label = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
                    } else {
                        CharSequence seq = ContactsContract.CommonDataKinds.Phone.getTypeLabel(getApplicationContext().getResources(), labelType, "Mobile");
                        label = seq.toString();
                    }


                    //LOG.info("Raw number label:" + label);
                    if (label == null) {
                        label = "";
                    }
                    labels.add(label);
                }

                cursor.close();

            }

        } catch (Exception ex) {
        }


        return (labels);
    }
}
