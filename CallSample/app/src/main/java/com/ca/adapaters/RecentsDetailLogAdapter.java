package com.ca.adapaters;
import android.content.Context;
import android.database.Cursor;
import androidx.cursoradapter.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ca.Utils.CSDbFields;
import com.ca.callsample.R;
import com.ca.utils.utils;
import com.ca.wrapper.CSDataProvider;
import java.text.SimpleDateFormat;
import java.util.Calendar;
public class RecentsDetailLogAdapter extends CursorAdapter {

	private Context context;

	public RecentsDetailLogAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		this.context = context;
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		try {

			TextView title = (TextView) convertView.findViewById(R.id.text1);
			TextView secondary = (TextView) convertView.findViewById(R.id.text2);
			TextView dateView = (TextView) convertView.findViewById(R.id.dateView);
			ImageView callTypeImg = convertView.findViewById(R.id.call_type_img);
			TextView durationTv = convertView.findViewById(R.id.call_duration_tv);
			LinearLayout dateLayout = convertView.findViewById(R.id.date_layout);

			String number = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DIR));

			title.setText(number.toLowerCase());

			String prevDate = "";

			Long dateStr = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_TIME));
			String currentDate = getFormattedDate(dateStr);
			if (cursor.getPosition() > 0 && cursor.moveToPrevious()) {
				prevDate = getFormattedDate(cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_TIME)));
				cursor.moveToNext();
			}
			if (number.toLowerCase().contains("incoming")) {
				callTypeImg.setImageResource((R.drawable.incomingorange));
			} else if (number.toLowerCase().contains("outgoing")) {
				callTypeImg.setImageResource((R.drawable.outgoingcall));
			} else if (number.toLowerCase().contains("missed")) {
				callTypeImg.setImageResource((R.drawable.missedcall));
			}
			if (prevDate == null || !prevDate.equals(currentDate)) {
				dateView.setVisibility(View.VISIBLE);
				dateLayout.setVisibility(View.VISIBLE);
				if (DateUtils.isToday(dateStr)) {
					dateView.setText("Today");
				} else if (isYesterday(dateStr)) {
					dateView.setText("Yesterday");
				} else {
					dateView.setText(currentDate);
				}
			} else {
				dateView.setVisibility(View.GONE);
				dateLayout.setVisibility(View.GONE);
			}


			String date = "";
			String time = "";
			String Duration = "";
			String cost = "";

			String formattedDate = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_TIME));
			long timme = Long.valueOf(formattedDate);

			date = new SimpleDateFormat("dd/MM/yyyy").format(timme);
			time = new SimpleDateFormat("hh:mm a").format(timme);


			String ds1 = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DURATION));
			int du1 = Integer.parseInt(ds1);

			int mins = (du1) / 60;
			int sec = (du1) % 60;

			if (mins == 0 && sec == 0) {
				Duration = "";
			} else if (mins == 0 && sec > 0) {
				if (sec == 1) {
					Duration = sec + " second";
				} else {
					Duration = sec + " seconds";
				}
			} else if (mins > 0 && sec == 0) {
				if (mins == 1) {
					Duration = mins + " minute";
				} else {
					Duration = mins + " minutes";
				}
			} else if (mins > 0 && sec > 0) {
				if(mins>1&&sec==1){
					Duration=mins + " minutes "+sec+" second";
				}else if(mins==1&&sec>1){
					Duration=mins + " minute "+sec+" seconds";
				}else {
					Duration=mins + " minutes "+sec+" seconds";
				}
			}
			String mysecondary = time;


			secondary.setText(mysecondary);
			durationTv.setText(Duration);

			try {
				int isalreadynotified = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_MISSEDCALL_NOTIFIED));
				if (isalreadynotified != 1) {
					String callid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_CALLID));
					CSDataProvider.UpdateCallLogbyCallidAndFilter(callid, CSDbFields.KEY_CALLLOG_MISSEDCALL_NOTIFIED, 1);
				}
			} catch (Exception ex) {
				ex.printStackTrace();

			}


		} catch (Exception ex) {
			utils.logStacktrace(ex);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);

		View convertView = inflater.inflate(R.layout.contact_row_layout1, parent, false);

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

	private String getFormattedDate(long dateStr) {
		try {
			return new SimpleDateFormat("dd-MM-yyyy").format(dateStr);
		} catch (Exception e) {
			utils.logStacktrace(e);
		}
		return "";
	}


}