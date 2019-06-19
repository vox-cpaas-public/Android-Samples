package com.ca.adapaters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;

import com.ca.chatsample.R;
import com.ca.groupmanagementsample.MainActivity;
import com.ca.utils.Constants;
import com.ca.utils.utils;
import com.ca.wrapper.CSDataProvider;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.lang.ref.WeakReference;


/**
 * This class show all group members details to listview
 */

public class ManageGroupAppContactsAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context context;
    Cursor cursor;

    public ManageGroupAppContactsAdapter(Context context, Cursor c, int flags) {

        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.cursor = c;

    }

    public int getCount() {
        return cursor.getCount();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    CheckBox checkBox;
    CircularImageView image;
    TextView title;
    TextView secondary;

    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            final ViewHolder holder;

            //if (convertView == null) {
            ////LOG.info("convertView is null");
            convertView = mInflater.inflate(R.layout.group_contacts_row, null);
            ////LOG.info("Height of convertView:"+convertView.getHeight());
            ////LOG.info("Height of convertView:"+convertView.getMeasuredHeight());
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.text1);
            holder.secondary = (TextView) convertView.findViewById(R.id.text2);
            holder.image = (CircularImageView) convertView.findViewById(R.id.imageview);
            holder.isadmin = (TextView) convertView.findViewById(R.id.isadmin);

            convertView.setTag(holder);
            //} else {
            //////LOG.info("convertView is not null");
            //holder = (ViewHolder) convertView.getTag();
            //}

            //LOG.info("cursor count:" + cursor.getCount());
            holder.isadmin.setVisibility(View.GONE);
            cursor.moveToPosition(position);
            String id = "";
            String picid = "";
            String name = "";
            String number = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_GROUPCONTACTS_CONTACT));
            String role = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_GROUPCONTACTS_ROLE));
            if (role.equals(CSConstants.ADMIN)) {
                holder.isadmin.setVisibility(View.VISIBLE);
            } else {
                holder.isadmin.setVisibility(View.GONE);
            }

            if (number.equals(Constants.phoneNumber)) {
                name = "You";

                Cursor cur1 = CSDataProvider.getSelfProfileCursor();
                if (cur1.getCount() > 0) {
                    cur1.moveToNext();
                    picid = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_SELF_PROFILE_PROFILEPICID));
                }
                cur1.close();

            } else {
                Cursor crr1 = CSDataProvider.getContactsCursorByFilter(CSDbFields.KEY_CONTACT_NUMBER, number);
                if (crr1.getCount() > 0) {
                    crr1.moveToNext();
                    name = crr1.getString(crr1.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
                    id = crr1.getString(crr1.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
                }
                crr1.close();
                if (name.equals("")) {
                    name = "IamLive User";
                }


                Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, number);
                if (cur.getCount() > 0) {
                    cur.moveToNext();
                    picid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                }
                cur.close();

            }


            holder.title.setText(name);
            holder.secondary.setText(number);

            new ImageDownloaderTask(holder.image).execute("app", picid, id);


        } catch (Exception ex) {
            utils.logStacktrace(ex);
        }

        return convertView;
    }


    static class ViewHolder {
        CircularImageView image;
        TextView title;
        TextView secondary;
        TextView isadmin;
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
                if (params[0].equals("app")) {
                    photo = CSDataProvider.getImageBitmap(params[1]);
                    if (photo == null) {
                        //photo = utils.loadContactPhoto(Long.parseLong(params[2]));
                    }
                } else if (params[0].equals("native")) {
                    //photo = utils.loadContactPhoto(Long.parseLong(params[1]));
                }


                if (photo == null) {
                    photo = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.defaultcontact);
                } else {
                    scaleit = true;
                    //ImageView imageView = imageViewReference.get();
                    //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            } catch (Exception e) {
                photo = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.defaultcontact);
                //utils.logStacktrace(e);
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
}
