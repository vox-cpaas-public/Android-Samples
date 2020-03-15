package com.ca.adapaters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import androidx.cursoradapter.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ca.Utils.CSDbFields;
import com.ca.callsample.MainActivity;
import com.ca.callsample.R;
import com.ca.callsample.ShowUserLogActivity;
import com.ca.utils.utils;
import com.ca.wrapper.CSDataProvider;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Recent list adapter to display all the call logs
 */
public class RecentsAdapter extends CursorAdapter {

    private Context context;

    public RecentsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.context = context;
    }

    @Override
    public void bindView(View convertView, Context context, Cursor cursor) {
        try {
            CircularImageView image = convertView.findViewById(R.id.imageview);
            image.setVisibility(View.GONE);
            TextView title = convertView.findViewById(R.id.text1);
            TextView secondary = convertView.findViewById(R.id.text2);
            ImageView dir = (ImageView) convertView.findViewById(R.id.imageview1);
            TextView text4 = (TextView) convertView.findViewById(R.id.text4);

            ImageView infoimageView = (ImageView) convertView.findViewById(R.id.infoimageView);
            final String name = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_NAME));
            String number = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_NUMBER));
            String directionn = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DIR));
            String groupingdate = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_GROUPING_IDENTIFIER_DATE));

            Cursor cs = CSDataProvider.getCallLogCursorByThreeFilters(CSDbFields.KEY_CALLLOG_NUMBER, number, CSDbFields.KEY_CALLLOG_DIR, directionn, CSDbFields.KEY_CALLLOG_GROUPING_IDENTIFIER_DATE, groupingdate);
            String count = String.valueOf(cs.getCount());
            cs.close();
            final String number1 = number;

            number = number.replaceAll("[^0-9+]", "");
            String id = "";

            Cursor ccr = CSDataProvider.getContactCursorByNumber(number);
            if (ccr.getCount() > 0) {
                ccr.moveToNext();
                id = ccr.getString(ccr.getColumnIndex(CSDbFields.KEY_CONTACT_ID));
            }
            ccr.close();
            final String id1 = id;

            String temp_str = "";
            if (name.equals("")) {
                temp_str = number;
            } else {
                temp_str = name;
            }

            if (count.equals("0") || count.equals("1")) {
                title.setText(temp_str);
            } else {
                title.setText(temp_str);
            }


            String date = "";
            String time = "";


            String formattedDate = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_TIME));
            long timme = Long.valueOf(formattedDate);

            date = new SimpleDateFormat("dd/MM/yyyy").format(timme);
            //time = new SimpleDateFormat("hh:mm:ss a").format(timme);
            time = new SimpleDateFormat("hh:mm a").format(timme);

            if (DateUtils.isToday(timme)) {
                date = "";
            } else if (isYesterday(timme)) {
                date = "Yesterday";
            }


            String mysecondary = date + " " + time;// + " " + Duration;// + "|" + callcost;
            secondary.setText(mysecondary);
            String direction = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DIR));
            if (Integer.parseInt(count) > 1) {
                text4.setText(direction.toLowerCase() + "(" + count + ")");
            } else {
                text4.setText(direction.toLowerCase());
            }
            if (direction.contains("OUTGOING")) {
                dir.setImageDrawable(MainActivity.context.getResources().getDrawable(R.drawable.outgoingcall));
            } else if (direction.contains("INCOMING")) {
                dir.setImageDrawable(MainActivity.context.getResources().getDrawable(R.drawable.incomingorange));
            } else if (direction.contains("MISSED")) {
                dir.setImageDrawable(MainActivity.context.getResources().getDrawable(R.drawable.missedcall));
            } else {
                //dir.setImageDrawable(MainActivity.context.getResources().getDrawable(R.drawable.outgoingcall));
            }


            final String mydirection = direction;
            infoimageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {

                        Intent intentt = new Intent(MainActivity.context, ShowUserLogActivity.class);
                        intentt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intentt.putExtra("number", number1);
                        intentt.putExtra("name", name);
                        intentt.putExtra("id", id1);
                        intentt.putExtra("direction", mydirection);
                        MainActivity.context.startActivity(intentt);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });


        } catch (Exception ex) {
            utils.logStacktrace(ex);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View convertView = inflater.inflate(R.layout.recents_row_layout, parent, false);

        return convertView;
    }

    public static boolean isYesterday(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);
        now.add(Calendar.DATE, -1);
        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }
}