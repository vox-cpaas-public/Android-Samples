package com.ca.adapaters;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.chatsample.R;
import com.ca.fragments.Contacts;
import com.ca.utils.utils;
import com.ca.wrapper.CSDataProvider;


import java.io.File;

public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    Cursor cursor;
String TAG = ContactsAdapter.class.getSimpleName();
    public ContactsAdapter(Context context, Cursor c) {
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

        TextView title;
        TextView secondary;
        ImageView image;


        public MyViewHolder(View view) {
            super(view);
             title = (TextView) view.findViewById(R.id.text1);
             secondary = (TextView) view.findViewById(R.id.text2);
             image = (ImageView) view.findViewById(R.id.imageView6);

             view.setOnClickListener(this);
             view.setOnLongClickListener(this);
            image.setOnClickListener(this);
            image.setOnLongClickListener(this);


            //Log.i(TAG,"MyViewHolder0 is called");
        }
        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();

            processClick(0,adapterPosition);

        }

        @Override
        public boolean onLongClick(View v) {
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

        View defaultitemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_row_layout3_new, parent, false);

        switch (viewType) {
                case 0:
                    View itemView0 = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_row_layout3, parent, false);
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





            String id = cursor.getString(cursor.getColumnIndex(CSDbFields.KEY_CONTACT_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NUMBER));

            myViewHolder.title.setText(name);
            myViewHolder.secondary.setText(number);

            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));

            Glide.with(context)
                    .load(uri)
                    .apply(new RequestOptions().error(R.drawable.defaultcontact))
                    .apply(RequestOptions.circleCropTransform())
                    //.apply(new RequestOptions().signature(new ObjectKey(String.valueOf(new File(filepath).length()+new File(filepath).lastModified()))))
                    .into(myViewHolder.image);





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

    public void processClick(int clicktype,int cursorposition) {
        try {
            //View view can be null
            Log.i(TAG, "clicktype:" + clicktype);
            Log.i(TAG, "cursorposition:" + cursorposition);
            //Log.i(TAG, "action:" + action);

            Contacts.handleclick(cursorposition);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }





        }