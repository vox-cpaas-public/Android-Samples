package com.ca.adapaters;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.ca.Utils.CSDbFields;
import com.ca.chatsample.R;
import com.ca.groupmanagementsample.MainActivity;
import com.ca.utils.utils;
import com.ca.wrapper.CSDataProvider;

import java.lang.ref.WeakReference;

public class GroupsAdapter extends CursorAdapter {
	private Context context;
	public GroupsAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		this.context = context;
	}
	@Override
	public void bindView(View convertView, final Context context, Cursor cursor) {
		com.mikhaellopez.circularimageview.CircularImageView image = (com.mikhaellopez.circularimageview.CircularImageView) convertView.findViewById(R.id.imageView6);
		TextView title = (TextView) convertView.findViewById(R.id.text1);
		TextView secondary = (TextView) convertView.findViewById(R.id.text2);

		String name = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_NAME));
		String number = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_DESC));

		String picid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_PROFILE_PIC));
		new ImageDownloaderTask(image).execute("group", picid, "");

		title.setText(name);
		secondary.setText(number);
	}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View convertView = inflater.inflate(R.layout.contact_row_layout3, parent, false);
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
					if(photo == null) {
						//photo = utils.loadContactPhoto(Long.parseLong(params[2]));
					}
				} else if(params[0].equals("native")) {
					//photo = utils.loadContactPhoto(Long.parseLong(params[1]));
				}

				else if(params[0].equals("group")&&photo == null) {
					photo = CSDataProvider.getImageBitmap(params[1]);
				}

				if(photo == null) {
					if(!params[0].equals("group")) {
						photo = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.defaultcontact);
					} else {
						photo = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.defaultgroup);
					}
				}
			} catch (Exception e) {
				try {
					if(photo == null) {
						if(!params[0].equals("group")) {
							photo = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.defaultcontact);
						} else {
							photo = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.defaultgroup);
						}
					}
				} catch (Exception ex){}
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
							////LOG.info("Yes scaleit is true");
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


	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
				.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}
}