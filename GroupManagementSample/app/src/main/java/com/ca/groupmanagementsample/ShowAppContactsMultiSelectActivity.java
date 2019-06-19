package com.ca.groupmanagementsample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import com.ca.adapaters.AppContactsMultiSelectAdapter;
import com.ca.chatsample.R;
import com.ca.utils.Constants;
import com.ca.utils.utils;
import com.ca.views.CustomEditText;
import com.ca.wrapper.CSDataProvider;

import java.util.ArrayList;


public class ShowAppContactsMultiSelectActivity extends AppCompatActivity {

    private ListView mListView;
    private AppContactsMultiSelectAdapter addContactsAdapter;
    private Button submitContacts;
    private EditText editText;
    private Toolbar toolbar;
    public static ArrayList<String> numbers = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sharecontactsinchat);

        mListView = (ListView) findViewById(R.id.contact_list);
        submitContacts = (Button) findViewById(R.id.button_addContacts);
        editText = (CustomEditText) findViewById(R.id.editText);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        editText.setText("");

        toolbar.setTitle("IamLive");

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setSubtitle("Select Contacts");

        // toolbar navigation button click listener
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // on press the navigation button we have close this screen
                onBackPressed();
            }
        });
        // before adding contacts list clear the existing contact list
        numbers.clear();
        if (getIntent().getIntExtra("uiaction", 0) == Constants.UIACTION_FORWARDCHATMESSAGE) {
            submitContacts.setText("Forward");
        } else if (getIntent().getIntExtra("uiaction", 0) == Constants.UIACTION_ADDCONTACTSTOGROUP) {
            submitContacts.setText("Done");
        }

        // below line will get the contact list from Cursor and show it in ListView
        addContactsAdapter = new AppContactsMultiSelectAdapter(ShowAppContactsMultiSelectActivity.this, CSDataProvider.getAppContactsCursor(), 0);
        mListView.setAdapter(addContactsAdapter);

        // list view item click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    // on selecting every item it will add selected contacts to numbers list to add it in group
                    Cursor cursor = null;
                    String searchstring = editText.getText().toString();
                    if (searchstring.equals("")) {
                        cursor = CSDataProvider.getAppContactsCursor();
                    } else {
                        cursor = CSDataProvider.getSearchAppContactsCursor(searchstring);
                    }

                    if (cursor != null && cursor.getCount() > 0) {
                        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                        checkBox.performClick();

                        if (checkBox.isChecked()) {
                            cursor.moveToPosition(position);
                            String number = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NUMBER));
                            if (!numbers.contains(number)) {
                                numbers.add(number);

                            }
                        } else if (!checkBox.isChecked()) {
                            cursor.moveToPosition(position);
                            String number = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NUMBER));
                            numbers.remove(number);
                        }
                        cursor.close();
                    }
                    toolbar.setSubtitle(String.valueOf(numbers.size()) + " Contacts Selected");
                    hideKeyboard();
                } catch (Exception ex) {
                    utils.logStacktrace(ex);
                }


            }


        });

        // submit contacts button click listener
        submitContacts.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // on press this button check the selected contact list
                // atleast one contact should be select to save contact list in group
                if (numbers.size() <= 0) {
                    Toast.makeText(getApplicationContext(), "Select at least one contact", Toast.LENGTH_SHORT).show();
                } else {
                    // this will send selected contact list to group
                    sendAddContactsRequest();
                }
            }
        });

        // search contact listener
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                // this will get the latest contact list by searching
                refreshView();
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });


    }

    /**
     * This method will send request to add selected contact list to given group
     */
    public void sendAddContactsRequest() {

        if (numbers.size() <= 0) {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        } else {
            // this will pass selected contact list through intent results
            submitContacts.setEnabled(false);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("contactnumbers", numbers);
            if (getIntent().getIntExtra("uiaction", 0) == Constants.UIACTION_FORWARDCHATMESSAGE) {
                resultIntent.putExtra("chatid", getIntent().getStringExtra("chatid"));
            }
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        numbers.clear();
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    /**
     * This method will load the contacts list from cursor
     */
    public void refreshView() {

        try {
            // if method called from search contacts it will get contact list from search contacts cursor
            // if method called normally it will get contacts list from all contacts cursor
            if (!editText.getText().toString().equals("")) {

                addContactsAdapter.changeCursor(CSDataProvider.getSearchAppContactsCursor(editText.getText().toString()));
                addContactsAdapter.notifyDataSetChanged();

            } else {

                addContactsAdapter.changeCursor(CSDataProvider.getAppContactsCursor());
                addContactsAdapter.notifyDataSetChanged();

            }

        } catch (Exception ex) {
            utils.logStacktrace(ex);
        }
    }

    /**
     * This method will hide the native soft keypad if ot open state
     */
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

}
