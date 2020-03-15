package com.ca.adapaters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.ca.Utils.CSDbFields;
import com.ca.chatsample.R;
import com.ca.fragments.Chats;
import com.ca.utils.utils;
import com.ca.wrapper.CSDataProvider;


import java.io.File;
import java.util.Locale;

public class ChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	Context context;
	Cursor cursor;
	String TAG = ChatsAdapter.class.getSimpleName();
	public ChatsAdapter(Context context, Cursor c) {
		this.context = context;
		this.cursor = c;
	}
	public void swapCursorAndNotifyDataSetChanged(Cursor newcursor) {
		try {

			Cursor oldCursor = cursor;
			try {
				if (cursor == newcursor) {
					return;// null;
				}
				this.cursor = newcursor;
				if (this.cursor != null) {
					this.notifyDataSetChanged();
				}
			} catch (Exception ex) {
				utils.logStacktrace(ex);
			}

			if (oldCursor != null) {
				oldCursor.close();
			}


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Override
	public int getItemCount() {
		//Log.i(TAG,"cursor Count:" + cursor.getCount());
		return (cursor == null) ? 0 : cursor.getCount();
	}
	@Override
	public int getItemViewType(int position) {

		//cursor.moveToPosition(position);
		int returnvalue = 0;
		return returnvalue;

	}

	public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnLongClickListener {

		TextView title,secondary,time,unreadcount;
		ImageView image;


		public MyViewHolder(View view) {
			super(view);
			title = (TextView) view.findViewById(R.id.text1);
			secondary = (TextView) view.findViewById(R.id.text2);
			image = (ImageView) view.findViewById(R.id.imageView6);
			 time = (TextView) view.findViewById(R.id.time);
			 unreadcount = (TextView) view.findViewById(R.id.unreadcount);


			view.setOnClickListener(this);
			view.setOnLongClickListener(this);


			//Log.i(TAG,"MyViewHolder0 is called");
		}
		@Override
		public void onClick(View v) {

			int adapterPosition = getAdapterPosition();


				processClick(0,adapterPosition,0);



		}

		@Override
		public boolean onLongClick(View v) {

			int adapterPosition = getAdapterPosition();
			processClick(0,adapterPosition,1);


 /*
            int adapterPosition = getAdapterPosition();
            if (v.getId() == address.getId()){
                processClick(0,adapterPosition,0);
            } else if (v.getId() == pincode.getId()) {
                processClick(0,adapterPosition,0);
            }
            */
			return true;
		}
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		View defaultitemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_row_layout, parent, false);

		switch (viewType) {
			case 0:
				View itemView0 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_row_layout, parent, false);
				return new MyViewHolder(itemView0);

		}


		return new MyViewHolder(defaultitemView);



	}




	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder viewholder, final int position) {
		try {
			viewholder.itemView.setTag(viewholder);
			cursor.moveToPosition(position);
			MyViewHolder myViewHolder = (MyViewHolder) viewholder;
			switch (viewholder.getItemViewType()) {
				case 0:
					myViewHolder = (MyViewHolder) viewholder;
					break;
			}


				//String id = "";//cursor.getString(cursor.getColumnIndex(CSDbFields.KEY_CONTACT_ID));
				int isgroupmessage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_IS_GROUP_MESSAGE));
				String grpid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_GROUPID));

				String name = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_NAME));


				String number = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_LOGINID));

				String lastmessage = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
				int chattype = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MSG_TYPE));
				int issender = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_IS_SENDER));

				Long dateStr = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TIME));
				String currentDate = utils.getDateForChat(dateStr,context);
				myViewHolder.time.setText(currentDate);

				Cursor ccr = null;
				if(isgroupmessage == 0) {
					ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(number);
				} else {
					ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(grpid);
				}

				if (ccr.getCount() > 0) {
					myViewHolder.unreadcount.setVisibility(View.VISIBLE);
					myViewHolder.unreadcount.setText(String.valueOf(ccr.getCount()));
					myViewHolder.time.setTextColor(context.getResources().getColor(R.color.chat_unread_color));
					//secondary.setTypeface(null, Typeface.BOLD);

				} else {
					myViewHolder.unreadcount.setVisibility(View.INVISIBLE);
					myViewHolder.unreadcount.setText(String.valueOf(ccr.getCount()));
					myViewHolder.time.setTextColor(context.getResources().getColor(R.color.black));
					//secondary.setTypeface(null, Typeface.NORMAL);
				}
				ccr.close();
//LOG.info("cor name:"+name);
				if (name.equals("")) {
					if(isgroupmessage == 0) {
						name = number;
					} else {

						Cursor cor = CSDataProvider.getGroupsCursorByFilter(CSDbFields.KEY_GROUP_ID,grpid);
						//LOG.info("cor count:"+cor.getCount());
						if(cor.getCount()>0) {
							cor.moveToNext();
							name = cor.getString(cor.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_NAME));
						}
						cor.close();

						if(name.equals("")) {
							name = "Unknown Group";
						}



					}
				}
			myViewHolder.title.setText(name);
			myViewHolder.secondary.setText("");
			String secondarytext = "";

				if(chattype == 0) {
					secondarytext = lastmessage;
					//myViewHolder.secondary.setText(lastmessage);

				} else if(chattype == 1) {
					secondarytext = lastmessage;
					//myViewHolder.secondary.setText(lastmessage);
				} else if(chattype == 2) {
					secondarytext = lastmessage;
					//myViewHolder.secondary.setText(lastmessage);
				} else if(chattype == 3) {
					secondarytext = "Location";
					/*if(issender == 0) {
						myViewHolder.secondary.setText("Location");
					} else {
						secondarytext = lastmessage;
						myViewHolder.secondary.setText("Location");
					}*/
				} else if(chattype == 4) {
					secondarytext = "Photo";

					/*if(issender == 0) {
						myViewHolder.secondary.setText("Photo");
					} else {
						secondarytext = lastmessage;
						myViewHolder.secondary.setText("Photo");
					}*/
				} else if(chattype == 5) {
					secondarytext = "Video";
					/*if (issender == 0) {
						myViewHolder.secondary.setText("Video");
					} else {
						myViewHolder.secondary.setText("Video");
					}
*/
				} else if(chattype == 6) {
					secondarytext = "Contact";
					/*if (issender == 0) {
						myViewHolder.secondary.setText("Contact");
					} else {
						myViewHolder.secondary.setText("Contact");
					}*/
				} else if(chattype == 7) {
					secondarytext = "Document";
					/*if (issender == 0) {
						myViewHolder.secondary.setText("Document");
					} else {
						myViewHolder.secondary.setText("Document");
					}*/
				} else if(chattype == 8) {
					secondarytext = "Audio";
					/*if (issender == 0) {
						myViewHolder.secondary.setText("Audio");
					} else {
						myViewHolder.secondary.setText("Audio");
					}*/
				}

			myViewHolder.secondary.setText(secondarytext);










				if(isgroupmessage == 0) {
					String nativecontactid = "";
					Cursor cur = CSDataProvider.getContactCursorByNumber(number);
					if(cur.getCount()>0) {
						cur.moveToNext();
						nativecontactid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
					}
					cur.close();

					String picid = "";
					Cursor cur1 = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, number);
					//LOG.info("Yes count:"+cur1.getCount());
					if (cur1.getCount() > 0) {
						cur1.moveToNext();
						picid = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
					}
					cur1.close();
					String filepath = CSDataProvider.getImageFilePath(picid);
					//LOG.info("corg filepath:"+filepath);
					//if(new File(filepath).exists()) {
					Glide.with(context)
							.load(Uri.fromFile(new File(filepath)))
							.apply(new RequestOptions().error(R.drawable.defaultcontact))
							.apply(RequestOptions.circleCropTransform())
                            .apply(new RequestOptions().signature(new ObjectKey(String.valueOf(new File(filepath).length()+new File(filepath).lastModified()))))
                            .into(myViewHolder.image);
					//new ChatsAdapter1.ImageDownloaderTask(image).execute("app", picid, nativecontactid);
				} else {
					String picid = "";
					Cursor cur1 = CSDataProvider.getGroupsCursorByFilter(CSDbFields.KEY_GROUP_ID, grpid);
					//LOG.info("Yes count:"+cur1.getCount());
					if (cur1.getCount() > 0) {
						cur1.moveToNext();
						picid = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_PROFILE_PIC));
					}
					cur1.close();
					String filepath = CSDataProvider.getImageFilePath(picid);
					//LOG.info("corg filepath:"+filepath);
					//if(new File(filepath).exists()) {
					Glide.with(context)
							.load(Uri.fromFile(new File(filepath)))
							.apply(new RequestOptions().error(R.drawable.defaultgroup))
							.apply(RequestOptions.circleCropTransform())
                            .apply(new RequestOptions().signature(new ObjectKey(String.valueOf(new File(filepath).length()+new File(filepath).lastModified()))))
                            .into(myViewHolder.image);
					//new ChatsAdapter1.ImageDownloaderTask(image).execute("group", picid);
				}



				if(!Chats.editText.getText().toString().equals("")) {
					setSearchTextColor(myViewHolder.title,Chats.editText.getText().toString(),name);
					setSearchTextColor(myViewHolder.secondary, Chats.editText.getText().toString(),secondarytext);

				}

		}catch (Exception ex) {
			ex.printStackTrace();
		}



	}

	@Override
	public void onViewRecycled(RecyclerView.ViewHolder viewHolder){
		super.onViewRecycled(viewHolder);

		//Log.i(TAG,"ItemViewType in onViewRecycled:" + viewHolder.getItemViewType());


		switch (viewHolder.getItemViewType()) {

			case 0:
				MyViewHolder myviewHolder = (MyViewHolder) viewHolder;
				Glide.with(context).clear(myviewHolder.image);
				break;


		}
	}

	public void processClick(int clicktype,int cursorposition,int action) {
		try {
			//View view can be null
			Log.i(TAG, "clicktype:" + clicktype);
			Log.i(TAG, "cursorposition:" + cursorposition);
			Log.i(TAG, "action:" + action);
if(action == 0) {
	Chats.handleclick(cursorposition);
} else {
	Chats.handlelongclick(cursorposition);
}


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void setSearchTextColor(TextView view,String searchstring,String actualstring) {
		try {
			//if(searchstring != null && !searchstring.equals("") && actualstring != null && !actualstring.equals("")) {
			//int startPos = actualstring.indexOf(searchstring);
			if (actualstring.toLowerCase().contains(searchstring.toLowerCase())) {
				int startPos = actualstring.toLowerCase(Locale.US).indexOf(searchstring.toLowerCase(Locale.US));
				int endPos = startPos + searchstring.length();

				if (startPos != -1) {
					Spannable spanText = Spannable.Factory.getInstance().newSpannable(actualstring);
					spanText.setSpan(new ForegroundColorSpan(Color.RED), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					view.setText(spanText, TextView.BufferType.SPANNABLE);
					//view.setBackgroundColor(Color.parseColor("#BDBDBD"));//getting recycled. see later
				}
			}
			//}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}



}