package com.ca.adapaters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.ca.Utils.CSDbFields;
import com.ca.chatsample.MainActivity;
import com.ca.chatsample.R;
import com.ca.chatsample.ShowAppContactsMultiSelectActivity;
import com.ca.utils.utils;
import com.ca.wrapper.CSDataProvider;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class AppContactsMultiSelectAdapter extends CursorAdapter {
	private Context context;
	public  ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();

	public AppContactsMultiSelectAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		this.context = context;
		for (int i = 0; i < this.getCount(); i++) {
			itemChecked.add(i, false); // initializes all items value with false
		}
	}



	@Override
	public void bindView(View convertView, final Context context, Cursor cursor) {
		try {


			TextView title= (TextView)convertView.findViewById(R.id.text1);
			CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
			CircularImageView image = (CircularImageView) convertView.findViewById(R.id.imageview);
			TextView secondary = (TextView) convertView.findViewById(R.id.text2);

			String contactid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
			String name = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
			final String number = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NUMBER));
			title.setText(name);
			//String description = "Hey there! I am using Konverz";
			String description = "";
			String picid = "";
			Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER,number);
			if(cur.getCount()>0) {
				cur.moveToNext();
				picid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
				description = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_DESCRIPTION));
				//if (picid != null && !picid.equals("")) {
					new ImageDownloaderTask(image).execute("app",picid,contactid);
				//}
			}
			cur.close();
			if(description.equals("")) {
				//description = "Hey there! I am using Konverz";
				description = "";
			}
			secondary.setText(description);


			if(ShowAppContactsMultiSelectActivity.numbers.contains(number)) {
				checkBox.setChecked(true);
			} else {
				checkBox.setChecked(false);
			}

			//final int pos = cursor.getPosition();

/*
			checkBox.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					CheckBox cb = (CheckBox) v.findViewById(R.id.checkBox);

					if (cb.isChecked()) {
						itemChecked.set(pos, true);
						// do some operations here
					} else if (!cb.isChecked()) {
						itemChecked.set(pos, false);
						// do some operations here
					}
				}
			});
			checkBox.setChecked(itemChecked.get(pos));
*/
		} catch (Exception ex) {

		}

	}





	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);

		View convertView = inflater.inflate(R.layout.app_contact_row_layout, parent, false);

		return convertView;
	}

	class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		boolean scaleit = false;
		public ImageDownloaderTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap photo = null;
			try {
				if(params[0].equals("app")) {
					photo = CSDataProvider.getImageBitmap(params[1]);
				} else {
					photo = utils.loadContactPhoto(Long.parseLong(params[1]));
				}
				if(params[0].equals("app")&&photo == null) {
					photo = utils.loadContactPhoto(Long.parseLong(params[2]));
				}

				if(photo == null) {
					photo = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.defaultcontact);
				} else {
					scaleit = true;
					//ImageView imageView = imageViewReference.get();
					//imageView.setScaleType(ImageView.ScaleType.FIT_XY);
				}
			} catch (Exception e) {
				photo = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.defaultcontact);
				utils.logStacktrace(e);
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
						/*if(scaleit) {
							//LOG.info("Yes scaleit is true");
							imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
						} else {
							imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
							imageView.setPadding(0,10,0,35);
						}*/
						//imageView.setImageBitmap(getRoundedCornerBitmap(bitmap, 10));
					} else {
						/*TextDrawable drawable2 = TextDrawable.builder()
				                .buildRound("A", Color.RED);*/
						/*Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.placeholder);
						imageView.setImageDrawable(placeholder);*/
					}
				}
			}
		}
	}


}