package com.ca.adapaters;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ca.Utils.CSDbFields;
import com.ca.callsample.R;

public class ContactsAdapter extends CursorAdapter {
	private Context context;
	public ContactsAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		this.context = context;
	}
	@Override
	public void bindView(View convertView, final Context context, Cursor cursor) {
		com.mikhaellopez.circularimageview.CircularImageView image = (com.mikhaellopez.circularimageview.CircularImageView) convertView.findViewById(R.id.imageView6);
		TextView title = (TextView) convertView.findViewById(R.id.text1);
		TextView secondary = (TextView) convertView.findViewById(R.id.text2);
		String id = cursor.getString(cursor.getColumnIndex(CSDbFields.KEY_CONTACT_ID));
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
		String number = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NUMBER));
		title.setText(name);
		secondary.setText(number);
	}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View convertView = inflater.inflate(R.layout.contact_row_layout3, parent, false);
		return convertView;
	}
}