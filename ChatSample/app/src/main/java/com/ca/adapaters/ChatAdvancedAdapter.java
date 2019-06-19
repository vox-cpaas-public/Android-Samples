package com.ca.adapaters;

/**
 * Created by Dell on 01-02-2019.
 */

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.chatsample.ChatAdvancedActivity;
import com.ca.chatsample.ContactViewActivity;
import com.ca.chatsample.R;
import com.ca.dao.CSChatLocation;
import com.ca.utils.Constants;
import com.ca.utils.utils;
import com.ca.wrapper.CSChat;
import com.ca.wrapper.CSDataProvider;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ChatAdvancedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private enum Actions { DEFAULT,AUDIOPLAY,AUDIOPAUSE,UPLOAD,UPLOADCANCEL,DOWNLOAD,DOWNLOADCANCEL}

    String TAG = ChatAdvancedAdapter.class.getSimpleName();

    CSChat CSChatObj = new CSChat();

    int defaultpreviewidth = 100;
    int defaultprevieheight = 100;
    ArrayList<Integer> selectedpositions = new ArrayList<Integer>();
    public static ArrayList<String> filedownloadinitiatedchatids = new ArrayList<String>();
    public static ArrayList<String> uploadfailedchatids = new ArrayList<String>();
    //public static ArrayList<String> audioplaychatids = new ArrayList<String>();
    public String audioplaychatid = "";
    public int PLAYING_PROGRESS = 0;
    private MediaPlayer mMediaPlayer;
    private Handler mDurationHandler = new Handler();
    String destination = "";
    Context context;
    Cursor cursor;
String searchstring = "";
boolean insearchmode = false;
    AudioManager am;




    public ChatAdvancedAdapter(Context context, Cursor c, String mydestination) {
        //this.sentimagedirectory = utils.getSentImagesDirectory();
        //this.recvimagedirectory = utils.getReceivedImagesDirectory();
        this.destination = mydestination;
        this.cursor = c;
        this.context = context;
        //setHasStableIds(true);
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void swapCursorAndNotifyDataSetChanged(Cursor newcursor,boolean insearchmode,String searchstring) {
        try {
            this.searchstring = searchstring;
            this.insearchmode = insearchmode;

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





    /*
    @Override
    public int getViewTypeCount() {
        return 6;
    }
    */

    @Override
    public int getItemViewType(int position) {

        cursor.moveToPosition(position);
        int chattype =  cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
        int issender =  cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_IS_SENDER));
        int ismultidevice =  cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE));
        int returnvalue = 0;
        if(chattype == CSConstants.E_TEXTPLAIN || chattype == CSConstants.E_TEXTHTML || chattype == CSConstants.E_CONTACT) {
            if(issender == 1) {
                returnvalue = 1;
            } else {
                returnvalue = 3;
            }
        } else if(chattype == CSConstants.E_LOCATION || chattype == CSConstants.E_IMAGE || chattype == CSConstants.E_VIDEO || chattype == CSConstants.E_DOCUMENT || chattype == CSConstants.E_AUDIO) {
            if(issender == 1) {

                if(ismultidevice == 0) {
                    returnvalue = 2;
                } else {
                    returnvalue = 5;
                }

            } else {
                returnvalue = 4;
            }
        } else {
            returnvalue = 0;
        }
        //Log.i(TAG,"ItemViewType:" + returnvalue);
        return returnvalue;

    }




    public class MyViewHolder0 extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnLongClickListener {

        

        public MyViewHolder0(View view) {
            super(view);

            //Log.i(TAG,"MyViewHolder0 is called");
        }
        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();
            /*
            if (v.getId() == address.getId()){
                processClick(0,adapterPosition,0);
            } else if (v.getId() == pincode.getId()) {
                processClick(0,adapterPosition,0);
            }
            */

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


    public class MyViewHolder1 extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnLongClickListener {

        RelativeLayout sendLayout;
        TextView dateView;
        TextView sendTime;
        TextView sendMessage;
        ImageView sentIcon;
        ImageView deliveredIcon;
        ImageView sentIcon1;
        ImageView deliveredIcon1;
        ImageView failedtext;
        RelativeLayout statusandtime_layout;
        ImageView contactimageview;

        int position;


        public MyViewHolder1(View view) {
            super(view);

            dateView = (TextView) view.findViewById(R.id.dateView);
            sendLayout = (RelativeLayout) view.findViewById(R.id.send_layout);
            sendMessage = (TextView) view.findViewById(R.id.chat_send_text);
            statusandtime_layout = (RelativeLayout) view.findViewById(R.id.statusandtime);
            failedtext = (ImageView) view.findViewById(R.id.failedimage);
            sentIcon = (ImageView) view.findViewById(R.id.sent_icon);
            deliveredIcon = (ImageView) view.findViewById(R.id.delivered_icon);
            sentIcon1 = (ImageView) view.findViewById(R.id.sent_icon1);
            deliveredIcon1 = (ImageView) view.findViewById(R.id.delivered_icon1);
            sendTime = (TextView) view.findViewById(R.id.chat_send_time);
            contactimageview = (ImageView) view.findViewById(R.id.contactplaceholder);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

        }
        @Override
        public void onClick(View v) {
            Log.i(TAG,"MyViewHolder1 item clicked");
            int adapterPosition = getAdapterPosition();
            processClick(0,adapterPosition,1,v, Actions.DEFAULT);



        }

        @Override
        public boolean onLongClick(View v) {

            int adapterPosition = getAdapterPosition();
            processClick(1,adapterPosition,1,v, Actions.DEFAULT);
            /*
            if (v.getId() == address.getId()){
                processClick(0,adapterPosition,0);
            } else if (v.getId() == pincode.getId()) {
                processClick(0,adapterPosition,0);
            }
            */
            return true;
        }
    }

    public class MyViewHolder2 extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnLongClickListener,SeekBar.OnSeekBarChangeListener {

        RelativeLayout sendLayout;
        TextView dateView;
        TextView sendTime,audiolengthview;
        ImageView sentIcon;
        ImageView deliveredIcon;
        ImageView sentIcon1;
        ImageView deliveredIcon1;
        ImageView failedtext;
        RelativeLayout sent_imagelayout,uploaddownloadlayoutview;
        ImageView sent_imageview,sender_document_imgview,video_play_iconview,uploaddownloadimageview,audioplaybuttonview,audiopausebuttonview,uploaddownloadcloseimageview;
        TextView sentimagetext,audioimagetextview,filesizeview;
        RelativeLayout statusandtime_layout,audiolayoutview;
        com.github.lzyzsd.circleprogress.DonutProgress progressBar1;
        SeekBar audioplayseekbar;

        int position;


        public MyViewHolder2(View view) {
            super(view);

            dateView = (TextView) view.findViewById(R.id.dateView);
            sendLayout = (RelativeLayout) view.findViewById(R.id.send_layout);

            sent_imageview = (ImageView) view.findViewById(R.id.sent_imageview);
            sentimagetext = (TextView) view.findViewById(R.id.sentimagetext);
            statusandtime_layout = (RelativeLayout) view.findViewById(R.id.statusandtime);
            failedtext = (ImageView) view.findViewById(R.id.failedimage);
            sentIcon = (ImageView) view.findViewById(R.id.sent_icon);
            deliveredIcon = (ImageView) view.findViewById(R.id.delivered_icon);
            sentIcon1 = (ImageView) view.findViewById(R.id.sent_icon1);
            deliveredIcon1 = (ImageView) view.findViewById(R.id.delivered_icon1);
            sendTime = (TextView) view.findViewById(R.id.chat_send_time);

            progressBar1 = (com.github.lzyzsd.circleprogress.DonutProgress) view.findViewById(R.id.progressBar);
            uploaddownloadimageview = (ImageView) view.findViewById(R.id.uploaddownloadimage);
            uploaddownloadlayoutview = (RelativeLayout) view.findViewById(R.id.uploaddownloadlayout);
            filesizeview = (TextView) view.findViewById(R.id.filesize);



            sender_document_imgview = (ImageView) view.findViewById(R.id.sender_document_img);
            video_play_iconview = (ImageView) view.findViewById(R.id.video_play_icon);

            sent_imagelayout = (RelativeLayout) view.findViewById(R.id.sent_imagelayout);
            audiolayoutview= (RelativeLayout) view.findViewById(R.id.audiolayout);
            audioimagetextview = (TextView) view.findViewById(R.id.audioimagetext);
            audiolengthview = (TextView) view.findViewById(R.id.audiolength);
            audioplaybuttonview = (ImageView) view.findViewById(R.id.audioplaybutton);
            audiopausebuttonview = (ImageView) view.findViewById(R.id.audiopausebutton);
            uploaddownloadcloseimageview = (ImageView) view.findViewById(R.id.uploaddownloadcloseimage);
            audioplayseekbar = (SeekBar) view.findViewById(R.id.audio_seekBar);


            view.setOnClickListener(this);
            view.setOnLongClickListener(this);


            audioplaybuttonview.setOnClickListener(this);
            audioplaybuttonview.setOnLongClickListener(this);
            audiopausebuttonview.setOnClickListener(this);
            audiopausebuttonview.setOnLongClickListener(this);
            uploaddownloadimageview.setOnClickListener(this);
            uploaddownloadimageview.setOnLongClickListener(this);
            uploaddownloadcloseimageview.setOnClickListener(this);
            uploaddownloadcloseimageview.setOnLongClickListener(this);

            audioplayseekbar.setOnSeekBarChangeListener(this);






            //Log.i(TAG,"MyViewHolder2 is called");
        }
        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();


            if (v.getId() == audioplaybuttonview.getId()){
                processClick(0,adapterPosition,2,v, Actions.AUDIOPLAY);
            } else if (v.getId() == audiopausebuttonview.getId()) {
                processClick(0,adapterPosition,2,v, Actions.AUDIOPAUSE);
            } else if (v.getId() == uploaddownloadimageview.getId()) {
                processClick(0,adapterPosition,2,v, Actions.UPLOAD);
            } else if (v.getId() == uploaddownloadcloseimageview.getId()) {
                processClick(0,adapterPosition,2,v, Actions.UPLOADCANCEL);
            } else {
                processClick(0,adapterPosition,2,v, Actions.DEFAULT);
            }

        }

        @Override
        public boolean onLongClick(View v) {
            int adapterPosition = getAdapterPosition();
            processClick(1,adapterPosition,2,v, Actions.DEFAULT);
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

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.i(TAG, "SeekBar changed progress: " + progress + " , position: " + position);


            int adapterPosition = getAdapterPosition();
            PLAYING_PROGRESS = progress;
            if (mMediaPlayer != null && mMediaPlayer.isPlaying() && fromUser) {
                mMediaPlayer.seekTo(progress);
            }
        }





}

    public class MyViewHolder3 extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnLongClickListener {


        RelativeLayout receiveLayout;
        TextView dateView;
        TextView receiveTime;
        TextView receiveMessage;
        ImageView contactimageview;

        int position;


        public MyViewHolder3(View view) {
            super(view);

            dateView = (TextView) view.findViewById(R.id.dateView);
            receiveLayout = (RelativeLayout) view.findViewById(R.id.receive_layout);
            receiveMessage = (TextView) view.findViewById(R.id.chat_receive_text);
            receiveTime = (TextView) view.findViewById(R.id.chat_receive_time);
            contactimageview = (ImageView) view.findViewById(R.id.contactplaceholder);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);



            //Log.i(TAG,"MyViewHolder3 is called");
        }
        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();
            processClick(0,adapterPosition,3,v, Actions.DEFAULT);
            /*
            if (v.getId() == address.getId()){
                processClick(0,adapterPosition,0);
            } else if (v.getId() == pincode.getId()) {
                processClick(0,adapterPosition,0);
            }
*/
        }

        @Override
        public boolean onLongClick(View v) {

            int adapterPosition = getAdapterPosition();
            processClick(1,adapterPosition,3,v, Actions.DEFAULT);
            /*
            if (v.getId() == address.getId()){
                processClick(0,adapterPosition,0);
            } else if (v.getId() == pincode.getId()) {
                processClick(0,adapterPosition,0);
            }
            */
            return true;
        }
    }

    public class MyViewHolder4 extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnLongClickListener,SeekBar.OnSeekBarChangeListener {


        RelativeLayout receiveLayout,audiolayoutview,uploaddownloadlayoutview,audiouploaddownloadlayoutview;
        TextView dateView;
        TextView receiveTime,audiolengthview;
        RelativeLayout receive_image_layout;
        ImageView recv_imageview,sender_document_imgview,video_play_iconview,uploaddownloadimageview,audiouploaddownloadimageview,audiopausebuttonview,uploaddownloadcloseimageview,audiouploaddownloadcloseimageview,audioplaybuttonview;
        TextView recvimagetext,audioimagetextview,audio_chat_receive_timeview,filesizeview,audiofilesizeview;
        com.github.lzyzsd.circleprogress.DonutProgress progressBar1,audioprogressBar1;
        SeekBar audioplayseekbar;
        int position;


        public MyViewHolder4(View view) {
            super(view);

            dateView = (TextView) view.findViewById(R.id.dateView);
            receiveLayout = (RelativeLayout) view.findViewById(R.id.receive_layout);
            receive_image_layout = (RelativeLayout) view.findViewById(R.id.receive_image_layout);
            recv_imageview = (ImageView) view.findViewById(R.id.recv_imageview);
            recvimagetext = (TextView) view.findViewById(R.id.recvimagetext);

            progressBar1 = (com.github.lzyzsd.circleprogress.DonutProgress) view.findViewById(R.id.progressBar);
            uploaddownloadimageview = (ImageView) view.findViewById(R.id.uploaddownloadimage);
            uploaddownloadlayoutview = (RelativeLayout) view.findViewById(R.id.uploaddownloadlayout);
            filesizeview = (TextView) view.findViewById(R.id.filesize);

            audioprogressBar1 = (com.github.lzyzsd.circleprogress.DonutProgress) view.findViewById(R.id.audio_progressBar);
            audiouploaddownloadimageview = (ImageView) view.findViewById(R.id.audio_uploaddownloadimage);
            audiouploaddownloadlayoutview = (RelativeLayout) view.findViewById(R.id.audio_uploaddownloadlayout);
            audiofilesizeview = (TextView) view.findViewById(R.id.audio_filesize);

            receiveTime = (TextView) view.findViewById(R.id.chat_receive_time);
            sender_document_imgview = (ImageView) view.findViewById(R.id.sender_document_img);
            video_play_iconview = (ImageView) view.findViewById(R.id.video_play_icon);

            audiolayoutview= (RelativeLayout) view.findViewById(R.id.audiolayout);
            receive_image_layout = (RelativeLayout) view.findViewById(R.id.receive_image_layout);
            audioimagetextview = (TextView) view.findViewById(R.id.audioimagetext);
            audio_chat_receive_timeview = (TextView) view.findViewById(R.id.audio_chat_receive_time);
            audiolengthview = (TextView) view.findViewById(R.id.audiolength);
            audioplaybuttonview = (ImageView) view.findViewById(R.id.audioplaybutton);
            audiopausebuttonview = (ImageView) view.findViewById(R.id.audiopausebutton);
            uploaddownloadcloseimageview = (ImageView) view.findViewById(R.id.uploaddownloadcloseimage);
            audiouploaddownloadcloseimageview = (ImageView) view.findViewById(R.id.audiouploaddownloadcloseimage);
            audioplayseekbar = (SeekBar) view.findViewById(R.id.audio_seekBar);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);




            audioplaybuttonview.setOnClickListener(this);
            audioplaybuttonview.setOnLongClickListener(this);
            audiopausebuttonview.setOnClickListener(this);
            audiopausebuttonview.setOnLongClickListener(this);
            uploaddownloadimageview.setOnClickListener(this);
            uploaddownloadimageview.setOnLongClickListener(this);
            uploaddownloadcloseimageview.setOnClickListener(this);
            uploaddownloadcloseimageview.setOnLongClickListener(this);

            audiouploaddownloadimageview.setOnClickListener(this);
            audiouploaddownloadimageview.setOnLongClickListener(this);
            audiouploaddownloadcloseimageview.setOnClickListener(this);
            audiouploaddownloadcloseimageview.setOnLongClickListener(this);

            audioplayseekbar.setOnSeekBarChangeListener(this);

            //Log.i(TAG,"MyViewHolder4 is called");
        }
        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();
            if (v.getId() == audioplaybuttonview.getId()){
                processClick(0,adapterPosition,4,v, Actions.AUDIOPLAY);
            } else if (v.getId() == audiopausebuttonview.getId()) {
                processClick(0,adapterPosition,4,v, Actions.AUDIOPAUSE);
            } else if (v.getId() == uploaddownloadimageview.getId()) {
                processClick(0,adapterPosition,4,v, Actions.DOWNLOAD);
            } else if (v.getId() == uploaddownloadcloseimageview.getId()) {
                processClick(0,adapterPosition,4,v, Actions.DOWNLOADCANCEL);
            } else if (v.getId() == audiouploaddownloadimageview.getId()) {
                processClick(0,adapterPosition,4,v, Actions.DOWNLOAD);
            } else if (v.getId() == audiouploaddownloadcloseimageview.getId()) {
                processClick(0,adapterPosition,4,v, Actions.DOWNLOADCANCEL);
            } else {
                processClick(0,adapterPosition,4,v, Actions.DEFAULT);
            }
        }

        @Override
        public boolean onLongClick(View v) {

            int adapterPosition = getAdapterPosition();
            processClick(1,adapterPosition,4,v, Actions.DEFAULT);
            /*
            if (v.getId() == address.getId()){
                processClick(0,adapterPosition,0);
            } else if (v.getId() == pincode.getId()) {
                processClick(0,adapterPosition,0);
            }
            */
            return true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.i(TAG, "SeekBar changed progress: " + progress + " , position: " + position);


            int adapterPosition = getAdapterPosition();
            PLAYING_PROGRESS = progress;
            if (mMediaPlayer != null && mMediaPlayer.isPlaying() && fromUser) {
                mMediaPlayer.seekTo(progress);
            }
        }


    }
    public class MyViewHolder5 extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnLongClickListener,SeekBar.OnSeekBarChangeListener {

        RelativeLayout sendLayout,uploaddownloadlayoutview;
        TextView dateView;
        TextView sendTime,audiolengthview;

        ImageView sentIcon;
        ImageView deliveredIcon;
        ImageView sentIcon1;
        ImageView deliveredIcon1;
        ImageView failedtext;
        RelativeLayout sent_imagelayout,audiolayoutview;
        ImageView sent_imageview,sender_document_imgview,video_play_iconview,uploaddownloadimageview,audiopausebuttonview,uploaddownloadcloseimageview,audioplaybuttonview;
        TextView sentimagetext,audioimagetextview,filesizeview;
        RelativeLayout statusandtime_layout;
        com.github.lzyzsd.circleprogress.DonutProgress progressBar1;
        SeekBar audioplayseekbar;
        int position;


        public MyViewHolder5(View view) {
            super(view);

            dateView = (TextView) view.findViewById(R.id.dateView);
            sendLayout = (RelativeLayout) view.findViewById(R.id.send_layout);

            sent_imageview = (ImageView) view.findViewById(R.id.sent_imageview);
            sentimagetext = (TextView) view.findViewById(R.id.sentimagetext);
            statusandtime_layout = (RelativeLayout) view.findViewById(R.id.statusandtime);
            failedtext = (ImageView) view.findViewById(R.id.failedimage);
            sentIcon = (ImageView) view.findViewById(R.id.sent_icon);
            deliveredIcon = (ImageView) view.findViewById(R.id.delivered_icon);
            sentIcon1 = (ImageView) view.findViewById(R.id.sent_icon1);
            deliveredIcon1 = (ImageView) view.findViewById(R.id.delivered_icon1);
            sendTime = (TextView) view.findViewById(R.id.chat_send_time);

            progressBar1 = (com.github.lzyzsd.circleprogress.DonutProgress) view.findViewById(R.id.progressBar);
            uploaddownloadimageview = (ImageView) view.findViewById(R.id.uploaddownloadimage);
            uploaddownloadlayoutview = (RelativeLayout) view.findViewById(R.id.uploaddownloadlayout);
            filesizeview = (TextView) view.findViewById(R.id.filesize);

            sender_document_imgview = (ImageView) view.findViewById(R.id.sender_document_img);
            video_play_iconview = (ImageView) view.findViewById(R.id.video_play_icon);

            sent_imagelayout = (RelativeLayout) view.findViewById(R.id.sent_imagelayout);
            audiolayoutview= (RelativeLayout) view.findViewById(R.id.audiolayout);
            audioimagetextview = (TextView) view.findViewById(R.id.audioimagetext);
            audiolengthview = (TextView) view.findViewById(R.id.audiolength);
            audioplaybuttonview  = (ImageView) view.findViewById(R.id.audioplaybutton);
            audiopausebuttonview = (ImageView) view.findViewById(R.id.audiopausebutton);
            uploaddownloadcloseimageview = (ImageView) view.findViewById(R.id.uploaddownloadcloseimage);
            audioplayseekbar = (SeekBar) view.findViewById(R.id.audio_seekBar);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            audioplaybuttonview.setOnClickListener(this);
            audioplaybuttonview.setOnLongClickListener(this);
            audiopausebuttonview.setOnClickListener(this);
            audiopausebuttonview.setOnLongClickListener(this);
            uploaddownloadimageview.setOnClickListener(this);
            uploaddownloadimageview.setOnLongClickListener(this);
            uploaddownloadcloseimageview.setOnClickListener(this);
            uploaddownloadcloseimageview.setOnLongClickListener(this);

            audioplayseekbar.setOnSeekBarChangeListener(this);





            //Log.i(TAG,"MyViewHolder5 is called");
        }
        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();
            if (v.getId() == audioplaybuttonview.getId()){
                processClick(0,adapterPosition,5,v, Actions.AUDIOPLAY);
            } else if (v.getId() == audiopausebuttonview.getId()) {
                processClick(0,adapterPosition,5,v, Actions.AUDIOPAUSE);
            } else if (v.getId() == uploaddownloadimageview.getId()) {
                processClick(0,adapterPosition,5,v, Actions.DOWNLOAD);
            } else if (v.getId() == uploaddownloadcloseimageview.getId()) {
                processClick(0,adapterPosition,5,v, Actions.DOWNLOADCANCEL);
            } else {
                processClick(0,adapterPosition,5,v, Actions.DEFAULT);
            }
        }

        @Override
        public boolean onLongClick(View v) {

            int adapterPosition = getAdapterPosition();
            processClick(1,adapterPosition,5,v, Actions.DEFAULT);
            /*
            if (v.getId() == address.getId()){
                processClick(0,adapterPosition,0);
            } else if (v.getId() == pincode.getId()) {
                processClick(0,adapterPosition,0);
            }
            */
            return true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.i(TAG, "SeekBar changed progress: " + progress + " , position: " + position);


            int adapterPosition = getAdapterPosition();
            PLAYING_PROGRESS = progress;
            if (mMediaPlayer != null && mMediaPlayer.isPlaying() && fromUser) {
                mMediaPlayer.seekTo(progress);
            }
        }


    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View defaultitemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_empty, parent, false);
        //Log.i(TAG,"ItemViewType in onCreateViewHolder:" + viewType);
        try {
            switch (viewType) {
                case 0:
                    View itemView0 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_empty, parent, false);
                    return new MyViewHolder0(itemView0);
                case 1:
                    View itemView1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item1, parent, false);
                    return new MyViewHolder1(itemView1);
                case 2:
                    View itemView2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item2, parent, false);
                    return new MyViewHolder2(itemView2);
                case 3:
                    View itemView3 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item3, parent, false);
                    return new MyViewHolder3(itemView3);
                case 4:
                    View itemView4 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item4, parent, false);
                    return new MyViewHolder4(itemView4);
                case 5:
                    View itemView5 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item2_multidevice_chat, parent, false);
                    return new MyViewHolder5(itemView5);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new MyViewHolder0(defaultitemView);

    }
    @Override
    public void onViewRecycled(RecyclerView.ViewHolder viewHolder){
        super.onViewRecycled(viewHolder);

        //Log.i(TAG,"ItemViewType in onViewRecycled:" + viewHolder.getItemViewType());


        switch (viewHolder.getItemViewType()) {
            case 0:
                MyViewHolder0 viewHolder0 = (MyViewHolder0) viewHolder;
                break;
            case 1:
                MyViewHolder1 viewHolder1 = (MyViewHolder1) viewHolder;

                break;
            case 2:
                MyViewHolder2 viewHolder2 = (MyViewHolder2) viewHolder;
                Glide.with(context).clear(viewHolder2.sent_imageview);

                break;
            case 3:
                MyViewHolder3 viewHolder3 = (MyViewHolder3) viewHolder;
                break;
            case 4:
                MyViewHolder4 viewHolder4 = (MyViewHolder4) viewHolder;
                Glide.with(context).clear(viewHolder4.recv_imageview);

                break;
            case 5:
                MyViewHolder5 viewHolder5 = (MyViewHolder5) viewHolder;
                Glide.with(context).clear(viewHolder5.sent_imageview);

                break;

        }




    }
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewholder, final int position) {
        try {
            //Log.i(TAG,"ItemViewType in onBindViewHolder:" + viewholder.getItemViewType());
            //String prevDate = null;

            if(selectedpositions.size() > 0 && selectedpositions.contains(position)) {
                viewholder.itemView.setBackgroundColor(Color.parseColor("#BDBDBD"));
            } else {
                viewholder.itemView.setBackgroundColor(Color.parseColor("#f4f4f4"));
            }

            viewholder.itemView.setTag(viewholder);
            cursor.moveToPosition(position);
            MyViewHolder0 viewHolder0;
            MyViewHolder1 viewHolder1;
            MyViewHolder2 viewHolder2;
            MyViewHolder3 viewHolder3;
            MyViewHolder4 viewHolder4;
            MyViewHolder5 viewHolder5;

            String prevDate = null;

            Long dateStr = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TIME));
            String currentDate = getFormattedDate(dateStr);
            String shortTimeStr = getFormattedTime1(dateStr);
            if (cursor.getPosition() > 0 && cursor.moveToPrevious()) {
                prevDate = getFormattedDate(cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TIME)));
                cursor.moveToNext();
            }

            switch (viewholder.getItemViewType()) {
                case 0:
                    viewHolder0 = (MyViewHolder0)viewholder;
                    break;
                case 1:
                     viewHolder1 = (MyViewHolder1)viewholder;

                    if (prevDate == null || !prevDate.equals(currentDate)) {
                        viewHolder1.dateView.setVisibility(View.VISIBLE);
                        if(DateUtils.isToday(dateStr)) {
                            viewHolder1.dateView.setText("Today");
                        } else if(isYesterday(dateStr)) {
                            viewHolder1.dateView.setText("Yesterday");
                        } else {
                            viewHolder1.dateView.setText(currentDate);
                        }
                    }
                    else {
                        viewHolder1.dateView.setVisibility(View.GONE);
                    }


                    viewHolder1.sendTime.setText(shortTimeStr);
                    viewHolder1.sentIcon.setVisibility(View.INVISIBLE);
                    viewHolder1.deliveredIcon.setVisibility(View.INVISIBLE);
                    viewHolder1.sentIcon1.setVisibility(View.INVISIBLE);
                    viewHolder1.deliveredIcon1.setVisibility(View.INVISIBLE);
                    viewHolder1.failedtext.setVisibility(View.INVISIBLE);

                    if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_NOT_SENT) {
                        viewHolder1.sentIcon.setVisibility(View.INVISIBLE);
                        viewHolder1.deliveredIcon.setVisibility(View.INVISIBLE);
                        viewHolder1.failedtext.setVisibility(View.VISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_SENT) {
                        viewHolder1.sentIcon.setVisibility(View.VISIBLE);
                        viewHolder1.deliveredIcon.setVisibility(View.INVISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED) {
                        viewHolder1.sentIcon.setVisibility(View.VISIBLE);
                        viewHolder1.deliveredIcon.setVisibility(View.VISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_READ) {
                        ////LOG.info("Chat status setting as read:"+cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE)));
                        viewHolder1.sentIcon.setVisibility(View.INVISIBLE);
                        viewHolder1.deliveredIcon.setVisibility(View.INVISIBLE);
                        viewHolder1.sentIcon1.setVisibility(View.VISIBLE);
                        viewHolder1.deliveredIcon1.setVisibility(View.VISIBLE);

                    }













                    if (cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_TEXTPLAIN || cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_TEXTHTML || cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_UNKNOWN_CHAT_TYPE) {
                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        viewHolder1.sendMessage.setText(message);
                        viewHolder1.contactimageview.setVisibility(View.GONE);

                    if(insearchmode) {
                        setSearchTextColor(viewHolder1.sendMessage,message);
                    }
                    } else if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_CONTACT) {

                        //holder.sent_imagelayout.setVisibility(View.GONE);

                        //String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        viewHolder1.contactimageview.setVisibility(View.VISIBLE);

                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        JSONObject jsonObj = new JSONObject(message);
                        JSONArray array = jsonObj.getJSONArray("numbers");
                        JSONArray array1 = jsonObj.getJSONArray("labels");
                        String name = jsonObj.getString("name");

                        /*
                        String finalmessage = "";
                        for (int i = 0; i < array.length(); i++) {
                            ////LOG.info("MEssage number:" + array.get(i).toString());
                            ////LOG.info("labels:" + array.getJSONObject(i).getString("labels"));
                            if(finalmessage.equals("")) {
                                //finalmessage =  name + "\n" + "<u>"+array.get(i).toString() + "</u>"+ "\n" + array1.get(i).toString();
                                finalmessage =  name + "<br>" + array.get(i).toString() + "</br>";   //+ array1.get(i).toString();

                            } else {
                                //finalmessage = "\n\n"+finalmessage + name + "\n" + "<u>"+array.get(i).toString()+ "</u>" + "\n" + array1.get(i).toString();
                                finalmessage = "<br>"+finalmessage + name + "</br>" + array.get(i).toString();  //+ "\n" + array1.get(i).toString();

                            }
                        }*/


                        String number = "";
                        if(array.length()>=1) {
                            number = array.get(0).toString();
                        }

                        if (number.length() < 24) {
                            for (int i = number.length(); i <= 24; i++) {
                                number = number + " ";
                            }
                        }

                        //String finalmessage =  name + "<br>" + number + "</br>";
                        //viewHolder1.sendMessage.setText(Html.fromHtml(finalmessage));

                        String finalmessage =  name + "\n" + number;
                        viewHolder1.sendMessage.setText(finalmessage);

                        if(insearchmode) {
                            setSearchTextColor(viewHolder1.sendMessage,message);
                        }

                    }




                    break;
                case 2:
                     viewHolder2 = (MyViewHolder2)viewholder;

                    if (prevDate == null || !prevDate.equals(currentDate)) {
                        viewHolder2.dateView.setVisibility(View.VISIBLE);
                        if(DateUtils.isToday(dateStr)) {
                            viewHolder2.dateView.setText("Today");
                        } else if(isYesterday(dateStr)) {
                            viewHolder2.dateView.setText("Yesterday");
                        } else {
                            viewHolder2.dateView.setText(currentDate);
                        }
                    }
                    else {
                        viewHolder2.dateView.setVisibility(View.GONE);
                    }
                    viewHolder2.sendTime.setText(shortTimeStr);
                    viewHolder2.sentIcon.setVisibility(View.INVISIBLE);
                    viewHolder2.deliveredIcon.setVisibility(View.INVISIBLE);
                    viewHolder2.sentIcon1.setVisibility(View.INVISIBLE);
                    viewHolder2.deliveredIcon1.setVisibility(View.INVISIBLE);
                    viewHolder2.failedtext.setVisibility(View.INVISIBLE);

                    viewHolder2.sent_imageview.setVisibility(View.VISIBLE);
                    viewHolder2.sender_document_imgview.setVisibility(View.GONE);
                    viewHolder2.video_play_iconview.setVisibility(View.GONE);

                    viewHolder2.sent_imagelayout.setVisibility(View.VISIBLE);
                    viewHolder2.audiolayoutview.setVisibility(View.GONE);

                    viewHolder2.uploaddownloadlayoutview.setVisibility(View.GONE);


                    if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_NOT_SENT) {
                        viewHolder2.sentIcon.setVisibility(View.INVISIBLE);
                        viewHolder2.deliveredIcon.setVisibility(View.INVISIBLE);
                        viewHolder2.failedtext.setVisibility(View.VISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_SENT) {
                        viewHolder2.sentIcon.setVisibility(View.VISIBLE);
                        viewHolder2.deliveredIcon.setVisibility(View.INVISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED) {
                        viewHolder2.sentIcon.setVisibility(View.VISIBLE);
                        viewHolder2.deliveredIcon.setVisibility(View.VISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_READ) {
                        ////LOG.info("Chat status setting as read:"+cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE)));
                        viewHolder2.sentIcon.setVisibility(View.INVISIBLE);
                        viewHolder2.deliveredIcon.setVisibility(View.INVISIBLE);
                        viewHolder2.sentIcon1.setVisibility(View.VISIBLE);
                        viewHolder2.deliveredIcon1.setVisibility(View.VISIBLE);

                    }








                    if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_LOCATION) {
                       //String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        //String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        //int thumbstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));

                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        JSONObject jsonObj = new JSONObject(message);
                        String lat = jsonObj.getString("lat");
                        String lon = jsonObj.getString("lon");
                        try {
                            String address = jsonObj.getString("address");
                            if(address!=null) {
                                viewHolder2.sentimagetext.setText(address);
                                if(insearchmode) {
                                    setSearchTextColor(viewHolder2.sentimagetext,address);
                                }
                            }
                        } catch (Exception ex) {}

                        String locationurl = "https://maps.google.com/maps/api/staticmap?center=" + lat + "," + lon + "&zoom=16&size=200x200&sensor=false&markers=color:blue%" + lat + "," + lon + "&key=AIzaSyBGqiHDk9Qy-akUPC3yrzTDAtGzzadmghs";
                        Log.i(TAG, "onBindViewHolder: location URL " + locationurl);
                        Glide.with(context)
                                .load(locationurl)
                                .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder2.sent_imageview);



/*
                        if (thumbkey != null && !thumbkey.equals("")&&thumbstatus == 1) {
                            //to do new ImageDownloaderTask(viewHolder2.position,holder,viewHolder2.sent_imageview).execute("app",thumbkey,"");
                            String thumbfilepath = CSDataProvider.getImageThumbnailFilePath(thumbkey);
                            if(new File(thumbfilepath).exists()) {
                                Glide.with(context)
                                        .load(Uri.fromFile(new File(thumbfilepath)))
                                        //.apply(new RequestOptions().override(defaultpreviewidth, defaultprevieheight))
                                        .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder2.sent_imageview);
                            }

                        }
*/

                    }
                    else if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_IMAGE) {


                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        //String mainkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        int thumbstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));
                        String uploadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                        int uploadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));

                        if (new File(uploadfilepath).exists()&&uploadpercentage!=100) {
                            viewHolder2.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize/100)*uploadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder2.filesizeview.setText(filesizestr+"/"+filesizestr1);
                            viewHolder2.progressBar1.setProgress(uploadpercentage);

                            int autosend = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILEAUTOSEND));
if(autosend == 1 || uploadfailedchatids.contains(chatid)) {
    uploadfailedchatids.remove(chatid);
    viewHolder2.uploaddownloadimageview.setVisibility(View.GONE);
    viewHolder2.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);

} else {
    viewHolder2.uploaddownloadcloseimageview.setVisibility(View.GONE);
    viewHolder2.uploaddownloadimageview.setVisibility(View.VISIBLE);
}





                        }
                        /*if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE)) == 0) {
                            myBitmap = decodeFile(new File(uploadfilepath));
                        } else {
                            myBitmap = decodeFile(new File(downloadfilepath));
                        }


                        if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE)) == 1) {
                            if (myBitmap != null) {
                                viewHolder2.sent_uploadimage.setVisibility(View.INVISIBLE);
                            } else {
                                viewHolder2.sent_uploadimage.setVisibility(View.VISIBLE);
                            }
                        }
*/



                        //Bitmap myBitmap = decodeFile(new File(uploadfilepath));
                        //if (myBitmap != null) {
                           // viewHolder2.sent_imageview.setImageBitmap(myBitmap);
                        if (new File(uploadfilepath).exists()) {
                            Glide.with(context)
                                    .load(Uri.fromFile(new File(uploadfilepath)))
                                    //.apply(new RequestOptions().override(defaultpreviewidth, defaultprevieheight))
                                    .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder2.sent_imageview);
                        } else {
                            if (thumbkey != null && !thumbkey.equals("") && thumbstatus == 1) {
                                //to do new ImageDownloaderTask(holder.position, holder, holder.sent_imageview).execute("app", thumbkey, "");
                                //String thumbfilepath = CSDataProvider.getImageThumbnailFilePath(thumbkey);
                                String thumbfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMB_FILE_PATH));

                                if(new File(thumbfilepath).exists()) {
                                    Glide.with(context)
                                            .load(Uri.fromFile(new File(thumbfilepath)))
                                            //.apply(new RequestOptions().override(defaultpreviewidth, defaultprevieheight))
                                            .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder2.sent_imageview);
                                }
                            }
                        }

                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        viewHolder2.sentimagetext.setText(contenttype);
                        if(insearchmode) {
                            setSearchTextColor(viewHolder2.sentimagetext,contenttype);
                        }

                    }
                    else if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_VIDEO) {
                        String filepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));

                        String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        int thumbstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));

                        viewHolder2.video_play_iconview.setVisibility(View.VISIBLE);

                        String uploadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                        int uploadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (new File(uploadfilepath).exists()&&uploadpercentage!=100) {
                            viewHolder2.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize/100)*uploadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder2.filesizeview.setText(filesizestr+"/"+filesizestr1);
                            viewHolder2.progressBar1.setProgress(uploadpercentage);
                            int autosend = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILEAUTOSEND));
                            if(autosend == 1 || uploadfailedchatids.contains(chatid)) {
                                uploadfailedchatids.remove(chatid);
                                viewHolder2.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder2.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);

                            } else {
                                viewHolder2.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder2.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        }

                        //Bitmap myBitmap = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Video.Thumbnails.MICRO_KIND);
/*
                        if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE)) == 0) {
                            myBitmap = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Video.Thumbnails.MICRO_KIND);
                        } else {
                            filepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                            myBitmap = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Video.Thumbnails.MICRO_KIND);
                        }


                        if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE)) == 1) {
                            if (myBitmap != null) {
                                viewHolder2.sent_uploadimage.setVisibility(View.INVISIBLE);
                            } else {
                                viewHolder2.sent_uploadimage.setVisibility(View.VISIBLE);
                            }
                        }
                        */
                        //Bitmap myBitmap = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Video.Thumbnails.MICRO_KIND);
                        //if(myBitmap!=null) {
                          //  viewHolder2.sent_imageview.setImageBitmap(myBitmap);
if(new File(filepath).exists()) {
                            Glide.with(context)
                                    .load(Uri.fromFile(new File(filepath)))
                                    //.apply(new RequestOptions().override(defaultpreviewidth, defaultprevieheight))
                                    .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder2.sent_imageview);
                        } else {
                            if (thumbkey != null && !thumbkey.equals("") && thumbstatus == 1) {
                                //to do new ImageDownloaderTask(holder.position, holder, holder.sent_imageview).execute("app", thumbkey, "");
                                //String thumbfilepath = CSDataProvider.getImageThumbnailFilePath(thumbkey);
                                String thumbfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMB_FILE_PATH));

                                if(new File(thumbfilepath).exists()) {
                                    Glide.with(context)
                                            .load(Uri.fromFile(new File(thumbfilepath)))
                                            //.apply(new RequestOptions().override(defaultpreviewidth, defaultprevieheight))
                                            .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder2.sent_imageview);
                                }
                            }
                        }


                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        viewHolder2.sentimagetext.setText(contenttype);
                        if(insearchmode) {
                            setSearchTextColor(viewHolder2.sentimagetext,contenttype);
                        }

                    }

                    else if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_DOCUMENT) {
                        //viewHolder2.sent_imageview.setImageResource(R.drawable.defaultocicon);
                        viewHolder2.sender_document_imgview.setVisibility(View.VISIBLE);
                        viewHolder2.sent_imageview.setVisibility(View.GONE);
                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String uploadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                        int uploadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (new File(uploadfilepath).exists()&&uploadpercentage!=100) {
                            viewHolder2.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize/100)*uploadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder2.filesizeview.setText(filesizestr+"/"+filesizestr1);
                            viewHolder2.progressBar1.setProgress(uploadpercentage);
                            int autosend = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILEAUTOSEND));
                            if(autosend == 1 || uploadfailedchatids.contains(chatid)) {
                                uploadfailedchatids.remove(chatid);
                                viewHolder2.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder2.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);

                            } else {
                                viewHolder2.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder2.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        }
/*
                        if(cursor.getInt(cursor.getColumnInedex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE)) == 1) {
                            String downloadpath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                            if (!downloadpath.equals("")) {
                                viewHolder2.sent_uploadimage.setVisibility(View.INVISIBLE);
                            } else {
                                viewHolder2.sent_uploadimage.setVisibility(View.VISIBLE);
                            }
                        }
*/
                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        viewHolder2.sentimagetext.setText(contenttype);
                        if(insearchmode) {
                            setSearchTextColor(viewHolder2.sentimagetext,contenttype);
                        }
                        //String filename = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        //LOG.info("filename from sent app document:"+filename);


                    }
                    else if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_AUDIO) {

                        //viewHolder2.sent_imageview.setImageResource(R.drawable.defaultauioicon);

                        viewHolder2.sent_imagelayout.setVisibility(View.GONE);
                        viewHolder2.audiolayoutview.setVisibility(View.VISIBLE);

                       /* if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE)) == 1) {
                            String downloadpath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                            if (!downloadpath.equals("")) {
                                viewHolder2.sent_uploadimage.setVisibility(View.INVISIBLE);
                            } else {
                                viewHolder2.sent_uploadimage.setVisibility(View.VISIBLE);
                            }
                        }
*/
                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String uploadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                        int uploadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (new File(uploadfilepath).exists()&&uploadpercentage!=100) {
                            viewHolder2.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize/100)*uploadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder2.filesizeview.setText(filesizestr+"/"+filesizestr1);
                            viewHolder2.progressBar1.setProgress(uploadpercentage);
                            int autosend = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILEAUTOSEND));
                            if(autosend == 1 || uploadfailedchatids.contains(chatid)) {
                                uploadfailedchatids.remove(chatid);
                                viewHolder2.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder2.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);

                            } else {
                                viewHolder2.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder2.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        }


                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                         if(new File(uploadfilepath).exists()) {
                            viewHolder2.audiolengthview.setText(getDuration(uploadfilepath));

                            int max = getDurationInMS(uploadfilepath);
                            viewHolder2.audioplayseekbar.setMax(max);

                            if(audioplaychatid.equals(chatid)) {
                                viewHolder2.audioplaybuttonview.setVisibility(View.INVISIBLE);
                                viewHolder2.audiopausebuttonview.setVisibility(View.VISIBLE);
                                if(mMediaPlayer!=null) {
                                    viewHolder2.audioplayseekbar.setProgress(mMediaPlayer.getCurrentPosition());
                                    viewHolder2.audiolengthview.setText(formateMilliSeccond(mMediaPlayer.getCurrentPosition())+"/"+getDuration(uploadfilepath));

                                }
                            } else {
                                viewHolder2.audiopausebuttonview.setVisibility(View.INVISIBLE);
                                viewHolder2.audioplaybuttonview.setVisibility(View.VISIBLE);
                                viewHolder2.audioplayseekbar.setProgress(0);
                            }

                        } else {
                            viewHolder2.audiolengthview.setText("00:00");
                             viewHolder2.audioplayseekbar.setMax(0);
                        }

                        //viewHolder2.sentimagetext.setText(contenttype);
                        viewHolder2.audioimagetextview.setText(contenttype);
                        if(insearchmode) {
                            setSearchTextColor(viewHolder2.sentimagetext,contenttype);
                        }
                    }



                    viewHolder2.sendTime.setText(shortTimeStr);
                    viewHolder2.sentIcon.setVisibility(View.INVISIBLE);
                    viewHolder2.deliveredIcon.setVisibility(View.INVISIBLE);
                    viewHolder2.sentIcon1.setVisibility(View.INVISIBLE);
                    viewHolder2.deliveredIcon1.setVisibility(View.INVISIBLE);
                    viewHolder2.failedtext.setVisibility(View.INVISIBLE);

                    if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_NOT_SENT) {
                        viewHolder2.sentIcon.setVisibility(View.INVISIBLE);
                        viewHolder2.deliveredIcon.setVisibility(View.INVISIBLE);
                        viewHolder2.failedtext.setVisibility(View.VISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_SENT) {
                        viewHolder2.sentIcon.setVisibility(View.VISIBLE);
                        viewHolder2.deliveredIcon.setVisibility(View.INVISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED) {
                        viewHolder2.sentIcon.setVisibility(View.VISIBLE);
                        viewHolder2.deliveredIcon.setVisibility(View.VISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_READ) {
                        ////LOG.info("Chat status setting as read:"+cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE)));
                        viewHolder2.sentIcon.setVisibility(View.INVISIBLE);
                        viewHolder2.deliveredIcon.setVisibility(View.INVISIBLE);
                        viewHolder2.sentIcon1.setVisibility(View.VISIBLE);
                        viewHolder2.deliveredIcon1.setVisibility(View.VISIBLE);

                    }

/*
                    if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE)) == 1) {

                        if (viewHolder2.sent_uploadimage != null) {
                            viewHolder2.sent_uploadimage.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    try {


                                        //LOG.info(" viewHolder2.position:" + viewHolder2.position);
                                        //LOG.info(" cursor.position:" + cursor.getPosition());

                                        Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);
                                        cur.moveToPosition(viewHolder2.position);
                                        String mainkey = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                                        String chatid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                                        //LOG.info(" chatid from adaptor1:" + chatid);
                                        cur.close();

                                        //viewHolder2.progressBar.setVisibility(View.VISIBLE);
                                        viewHolder2.sent_uploadimage.setVisibility(View.INVISIBLE);
                                        CSChatObj.downloadFile(chatid, utils.getSentImagesDirectory() + "/" + mainkey);
                                    } catch (Exception ex) {
                                        utils.logStacktrace(ex);
                                    }
                                }
                            });
                        }
                    }
*/


                    break;
                case 3:
                     viewHolder3 = (MyViewHolder3)viewholder;

                    if (prevDate == null || !prevDate.equals(currentDate)) {
                        viewHolder3.dateView.setVisibility(View.VISIBLE);
                        if(DateUtils.isToday(dateStr)) {
                            viewHolder3.dateView.setText("Today");
                        } else if(isYesterday(dateStr)) {
                            viewHolder3.dateView.setText("Yesterday");
                        } else {
                            viewHolder3.dateView.setText(currentDate);
                        }
                    }
                    else {
                        viewHolder3.dateView.setVisibility(View.GONE);
                    }

                    viewHolder3.receiveTime.setText(shortTimeStr);
                    if (cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_TEXTPLAIN || cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_TEXTHTML || cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_UNKNOWN_CHAT_TYPE) {
                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        viewHolder3.receiveMessage.setText(message);
                        viewHolder3.contactimageview.setVisibility(View.GONE);
                        if(insearchmode) {
                            setSearchTextColor(viewHolder3.receiveMessage,message);
                        }
                    }
                    else if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_CONTACT) {

                        viewHolder3.contactimageview.setVisibility(View.VISIBLE);

                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        JSONObject jsonObj = new JSONObject(message);
                        JSONArray array = jsonObj.getJSONArray("numbers");
                        JSONArray array1 = jsonObj.getJSONArray("labels");
                        String name = jsonObj.getString("name");

                        /*
                        String finalmessage = "";
                        for (int i = 0; i < array.length(); i++) {
                            ////LOG.info("MEssage number:" + array.get(i).toString());
                            ////LOG.info("labels:" + array.getJSONObject(i).getString("labels"));
                            if(finalmessage.equals("")) {
                                //finalmessage =  name + "\n" + "<u>"+array.get(i).toString() + "</u>"+ "\n" + array1.get(i).toString();
                                finalmessage =  name + "<br>" + array.get(i).toString() + "</br>";   //+ array1.get(i).toString();

                            } else {
                                //finalmessage = "\n\n"+finalmessage + name + "\n" + "<u>"+array.get(i).toString()+ "</u>" + "\n" + array1.get(i).toString();
                                finalmessage = "<br>"+finalmessage + name + "</br>" + array.get(i).toString();  //+ "\n" + array1.get(i).toString();


                            }
                        }
*/


                        String number = "";
                        if(array.length()>=1) {
                            number = array.get(0).toString();
                        }

                        if (number.length() < 24) {
                            for (int i = number.length(); i <= 24; i++) {
                                number = number + " ";
                            }
                        }

                        //String finalmessage =  name + "<br>" + number + "</br>";
                        //viewHolder3.receiveMessage.setText(Html.fromHtml(finalmessage));

                        String finalmessage =  name + "\n" + number;
                        viewHolder3.receiveMessage.setText(finalmessage);


                        if(insearchmode) {
                            setSearchTextColor(viewHolder3.receiveMessage,message);
                        }

                    }

                    if(cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS))== CSConstants.MESSAGE_RECEIVED   || cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS))== CSConstants.MESSAGE_DELIVERED_ACK || cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED) {
                        CSChatObj.sendReadReceipt(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID)));
                        //to do  this.changeCursor(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination));
                        //notifyDataSetChanged(); //doesnt work like this. use swapCursorAndNotifyDataSetChanged
                        return;
                    }
                    break;
                case 4:
                     viewHolder4 = (MyViewHolder4)viewholder;

                    if (prevDate == null || !prevDate.equals(currentDate)) {
                        viewHolder4.dateView.setVisibility(View.VISIBLE);
                        if(DateUtils.isToday(dateStr)) {
                            viewHolder4.dateView.setText("Today");
                        } else if(isYesterday(dateStr)) {
                            viewHolder4.dateView.setText("Yesterday");
                        } else {
                            viewHolder4.dateView.setText(currentDate);
                        }
                    }
                    else {
                        viewHolder4.dateView.setVisibility(View.GONE);
                    }
                    viewHolder4.receiveTime.setText(shortTimeStr);

                    viewHolder4.recv_imageview.setVisibility(View.VISIBLE);
                    viewHolder4.sender_document_imgview.setVisibility(View.GONE);
                    viewHolder4.video_play_iconview.setVisibility(View.GONE);
                    viewHolder4.receive_image_layout.setVisibility(View.VISIBLE);
                    viewHolder4.audiolayoutview.setVisibility(View.GONE);

                    viewHolder4.uploaddownloadlayoutview.setVisibility(View.GONE);
                    viewHolder4.audiouploaddownloadlayoutview.setVisibility(View.GONE);

                    if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_LOCATION) {
                        //viewHolder4.receiveMessage.setVisibility(View.GONE);
                        //viewHolder4.receive_image_layout.setVisibility(View.VISIBLE);


                        //String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));

                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        JSONObject jsonObj = new JSONObject(message);
                        String lat = jsonObj.getString("lat");
                        String lon = jsonObj.getString("lon");
                        try {
                            String address = jsonObj.getString("address");
                            if(address!=null) {
                                viewHolder4.recvimagetext.setText(address);
                                if(insearchmode) {
                                    setSearchTextColor(viewHolder4.recvimagetext,address);
                                }
                            }
                        } catch (Exception ex) {}


                        String locationurl = "https://maps.google.com/maps/api/staticmap?center=" + lat + "," + lon + "&zoom=16&size=200x200&sensor=false&markers=color:blue%" + lat + "," + lon + "&key=AIzaSyBGqiHDk9Qy-akUPC3yrzTDAtGzzadmghs";
                        //Log.i(TAG, "onBindViewHolder: location URL " + locationurl);
                        Glide.with(context)
                                .load(locationurl)
                                .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder4.recv_imageview);


                        /*
                        if (thumbkey != null && !thumbkey.equals("")) {
                            //to do new ImageDownloaderTask(viewHolder4.position,holder,viewHolder4.recv_imageview).execute("app",thumbkey,"");
                            String thumbfilepath = CSDataProvider.getImageThumbnailFilePath(thumbkey);
                            if(new File(thumbfilepath).exists()) {
                                Glide.with(context)
                                        .load(Uri.fromFile(new File(thumbfilepath)))
                                        //.apply(new RequestOptions().override(defaultpreviewidth, defaultprevieheight))
                                        .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder4.recv_imageview);
                            }
                        }
                        */
                        //viewHolder4.sent_imageview.

                    }
                    else if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_IMAGE) {


                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        //String mainkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        int thumstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));

                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (!new File(downloadfilepath).exists() || downloadpercentage!=100) {
                            viewHolder4.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize/100)*downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder4.filesizeview.setText(filesizestr+"/"+filesizestr1);
                            viewHolder4.progressBar1.setProgress(downloadpercentage);


                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                    viewHolder4.uploaddownloadimageview.setVisibility(View.GONE);
                                    viewHolder4.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                                } else {
                                    viewHolder4.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                    viewHolder4.uploaddownloadimageview.setVisibility(View.VISIBLE);
                                }



                        } else {
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);
                            }
                        }

                        //String downloadedfilepath = utils.getReceivedImagesDirectory()+"/"+mainkey;
                        if(new File(downloadfilepath).exists()&&downloadpercentage==100) {

                            //LOG.info("TEST THUMBAINAL");
                            //Bitmap myBitmap = BitmapFactory.decodeFile(downloadedfilepath);

                            //Bitmap myBitmap = decodeFile(new File(downloadedfilepath));
                            //viewHolder4.recv_imageview.setImageBitmap(myBitmap);

                            Glide.with(context)
                                    .load(Uri.fromFile(new File(downloadfilepath)))
                                   // .apply(new RequestOptions().override(defaultpreviewidth, defaultprevieheight))
                                    .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder4.recv_imageview);


                        }

                        else if(thumstatus == 1) {
                            //LOG.info("TEST THUMBAINAL2");
                            if (thumbkey != null && !thumbkey.equals("")) {
                                //LOG.info("TEST THUMBAINAL3");
                                viewHolder4.recv_imageview.setVisibility(View.VISIBLE);
                                //to do new ImageDownloaderTask(viewHolder4.position,holder,viewHolder4.recv_imageview).execute("app",thumbkey,"");
                                //String thumbfilepath = CSDataProvider.getImageThumbnailFilePath(thumbkey);
                                String thumbfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMB_FILE_PATH));

                                if(new File(thumbfilepath).exists()) {
                                    Glide.with(context)
                                            .load(Uri.fromFile(new File(thumbfilepath)))
                                            //.apply(new RequestOptions().override(defaultpreviewidth, defaultprevieheight))
                                            .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder4.recv_imageview);
                                }

                            }

                        }


                        ////LOG.info("Yes i am here yes");

                        //viewHolder4.recvimagetext.setVisibility(View.VISIBLE);
                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        viewHolder4.recvimagetext.setText(contenttype);

                        if(insearchmode) {
                            setSearchTextColor(viewHolder4.recvimagetext,contenttype);
                        }

                        //LOG.info("TEST THUMBAINAL4");
                    }
                    else if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_VIDEO) {
                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        int thumstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));

                        viewHolder4.video_play_iconview.setVisibility(View.VISIBLE);

                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (!new File(downloadfilepath).exists() || downloadpercentage!=100) {
                            viewHolder4.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize/100)*downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder4.filesizeview.setText(filesizestr+"/"+filesizestr1);
                            viewHolder4.progressBar1.setProgress(downloadpercentage);
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder4.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder4.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder4.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder4.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);
                            }
                        }

                        if(new File(downloadfilepath).exists()&&downloadpercentage==100) {

                                Glide.with(context)
                                    .load(Uri.fromFile(new File(downloadfilepath)))
                                    // .apply(new RequestOptions().override(defaultpreviewidth, defaultprevieheight))
                                    .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                    .into(viewHolder4.recv_imageview);
                        }

                        else if(thumstatus == 1) {

                            if (thumbkey != null && !thumbkey.equals("")) {
                                //to do new ImageDownloaderTask(viewHolder4.position,holder,viewHolder4.recv_imageview).execute("app",thumbkey,"");
                                //String thumbfilepath = CSDataProvider.getImageThumbnailFilePath(thumbkey);
                                String thumbfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMB_FILE_PATH));

                                if(new File(thumbfilepath).exists()) {
                                    Glide.with(context)
                                            .load(Uri.fromFile(new File(thumbfilepath)))
                                            //.apply(new RequestOptions().override(defaultpreviewidth, defaultprevieheight))
                                            .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder4.recv_imageview);
                                }

                            } else {
                                //viewHolder4.recv_imageview.setVisibility(View.INVISIBLE);
                            }

                        } else {
                            //viewHolder4.recv_imageview.setVisibility(View.INVISIBLE);
                        }


                        ////LOG.info("Yes i am here yes");

                        //viewHolder4.recvimagetext.setVisibility(View.VISIBLE);
                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        viewHolder4.recvimagetext.setText(contenttype);

                        if(insearchmode) {
                            setSearchTextColor(viewHolder4.recvimagetext,contenttype);
                        }





                    }

                    else if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_DOCUMENT) {


                        viewHolder4.sender_document_imgview.setVisibility(View.VISIBLE);
                        viewHolder4.recv_imageview.setVisibility(View.GONE);

                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));


/*
                        String filename = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String mainkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        int thumstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));
*/

                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (!new File(downloadfilepath).exists() || downloadpercentage!=100) {
                            viewHolder4.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize/100)*downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder4.filesizeview.setText(filesizestr+"/"+filesizestr1);
                            viewHolder4.progressBar1.setProgress(downloadpercentage);
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder4.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder4.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder4.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder4.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);
                            }
                        }


                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                       /* if(contenttype.length()<21) {  //temp change. remove later
                            contenttype = contenttype+"\n";
                        }*/
                        viewHolder4.recvimagetext.setText(contenttype);

                        if(insearchmode) {
                            setSearchTextColor(viewHolder4.recvimagetext,contenttype);
                        }

                    }

                    else if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_AUDIO) {


                        viewHolder4.receive_image_layout.setVisibility(View.GONE);
                        viewHolder4.audiolayoutview.setVisibility(View.VISIBLE);
                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));


/*
                        String filename = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String mainkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        int thumstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));
*/

                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (!new File(downloadfilepath).exists() || downloadpercentage!=100) {
                            viewHolder4.audiouploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize/100)*downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder4.audiofilesizeview.setText(filesizestr+"/"+filesizestr1);
                            viewHolder4.audioprogressBar1.setProgress(downloadpercentage);
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder4.audiouploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder4.audiouploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder4.audiouploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder4.audiouploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);
                            }
                        }

                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));

                         if(new File(downloadfilepath).exists()&&downloadpercentage == 100) {
                            viewHolder4.audiolengthview.setText(getDuration(downloadfilepath));
                             int max = getDurationInMS(downloadfilepath);
                             viewHolder4.audioplayseekbar.setMax(max);
                             if(audioplaychatid.equals(chatid)) {
                                 viewHolder4.audioplaybuttonview.setVisibility(View.INVISIBLE);
                                 viewHolder4.audiopausebuttonview.setVisibility(View.VISIBLE);
                                 if(mMediaPlayer!=null) {
                                     viewHolder4.audioplayseekbar.setProgress(mMediaPlayer.getCurrentPosition());
                                     viewHolder4.audiolengthview.setText(formateMilliSeccond(mMediaPlayer.getCurrentPosition())+"/"+getDuration(downloadfilepath));

                                 }
                             } else {
                                 viewHolder4.audiopausebuttonview.setVisibility(View.INVISIBLE);
                                 viewHolder4.audioplaybuttonview.setVisibility(View.VISIBLE);
                                 viewHolder4.audioplayseekbar.setProgress(0);
                             }
                         } else {
                            viewHolder4.audiolengthview.setText("00:00");
                            viewHolder4.audioplayseekbar.setMax(0);
                        }
                        //viewHolder4.recvimagetext.setText(contenttype);

                        viewHolder4.audioimagetextview.setText(contenttype);
                        viewHolder4.audio_chat_receive_timeview.setText(shortTimeStr);
                        if(insearchmode) {
                            setSearchTextColor(viewHolder4.recvimagetext,contenttype);
                        }

                    }

                    /*if(viewHolder4.recv_downloadimage!=null) {
                        viewHolder4.recv_downloadimage.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                try {


                                    //LOG.info(" viewHolder4.position:" + viewHolder4.position);
                                    //LOG.info(" cursor.position:" + cursor.getPosition());

                                    Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);
                                    cur.moveToPosition(viewHolder4.position);
                                    String mainkey = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                                    String chatid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                                    //LOG.info(" chatid from adaptor1:" + chatid);
                                    cur.close();

                                    //viewHolder4.progressBar.setVisibility(View.VISIBLE);
                                    viewHolder4.recv_downloadimage.setVisibility(View.INVISIBLE);
                                    CSChatObj.downloadFile(chatid,  utils.getReceivedImagesDirectory() + "/" + mainkey);
                                } catch (Exception ex) {
                                    utils.logStacktrace(ex);
                                }
                            }
                        });
                    }
                    */
                    //if(cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS))== CSConstants.MESSAGE_RECEIVED   || cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS))== CSConstants.MESSAGE_DELIVERED_ACK) {
                    if(cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS))== CSConstants.MESSAGE_RECEIVED   || cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS))== CSConstants.MESSAGE_DELIVERED_ACK || cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED) {
                        CSChatObj.sendReadReceipt(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID)));
                        //to do  this.changeCursor(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination));
                        //notifyDataSetChanged();  //doesnt work like this. use swapCursorAndNotifyDataSetChanged
                        return;
                    }



                    break;
                case 5:
                     viewHolder5 = (MyViewHolder5)viewholder;

                    if (prevDate == null || !prevDate.equals(currentDate)) {
                        viewHolder5.dateView.setVisibility(View.VISIBLE);
                        if(DateUtils.isToday(dateStr)) {
                            viewHolder5.dateView.setText("Today");
                        } else if(isYesterday(dateStr)) {
                            viewHolder5.dateView.setText("Yesterday");
                        } else {
                            viewHolder5.dateView.setText(currentDate);
                        }
                    }
                    else {
                        viewHolder5.dateView.setVisibility(View.GONE);
                    }

                    viewHolder5.sendTime.setText(shortTimeStr);
                    viewHolder5.sentIcon.setVisibility(View.INVISIBLE);
                    viewHolder5.deliveredIcon.setVisibility(View.INVISIBLE);
                    viewHolder5.sentIcon1.setVisibility(View.INVISIBLE);
                    viewHolder5.deliveredIcon1.setVisibility(View.INVISIBLE);
                    viewHolder5.failedtext.setVisibility(View.INVISIBLE);

                    viewHolder5.uploaddownloadlayoutview.setVisibility(View.GONE);

                    if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_NOT_SENT) {
                        viewHolder5.sentIcon.setVisibility(View.INVISIBLE);
                        viewHolder5.deliveredIcon.setVisibility(View.INVISIBLE);
                        viewHolder5.failedtext.setVisibility(View.VISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_SENT) {
                        viewHolder5.sentIcon.setVisibility(View.VISIBLE);
                        viewHolder5.deliveredIcon.setVisibility(View.INVISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED) {
                        viewHolder5.sentIcon.setVisibility(View.VISIBLE);
                        viewHolder5.deliveredIcon.setVisibility(View.VISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_READ) {
                        ////LOG.info("Chat status setting as read:"+cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE)));
                        viewHolder5.sentIcon.setVisibility(View.INVISIBLE);
                        viewHolder5.deliveredIcon.setVisibility(View.INVISIBLE);
                        viewHolder5.sentIcon1.setVisibility(View.VISIBLE);
                        viewHolder5.deliveredIcon1.setVisibility(View.VISIBLE);

                    }

                    viewHolder5.sent_imageview.setVisibility(View.VISIBLE);
                    viewHolder5.sender_document_imgview.setVisibility(View.GONE);
                    viewHolder5.video_play_iconview.setVisibility(View.GONE);
                    viewHolder5.sent_imagelayout.setVisibility(View.VISIBLE);
                    viewHolder5.audiolayoutview.setVisibility(View.GONE);


/*
                    if (viewHolder5.sent_uploadimage != null) {
                        viewHolder5.sent_uploadimage.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                try {


                                    //LOG.info(" viewHolder5.position:" + viewHolder5.position);
                                    //LOG.info(" cursor.position:" + cursor.getPosition());

                                    Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);
                                    cur.moveToPosition(viewHolder5.position);
                                    String mainkey = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                                    String chatid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                                    //LOG.info(" chatid from adaptor1:" + chatid);
                                    cur.close();

                                    //viewHolder5.progressBar.setVisibility(View.VISIBLE);
                                    viewHolder5.sent_uploadimage.setVisibility(View.INVISIBLE);
                                    CSChatObj.downloadFile(chatid, utils.getSentImagesDirectory() + "/" + mainkey);
                                } catch (Exception ex) {
                                    utils.logStacktrace(ex);
                                }
                            }
                        });
                    }

*/








                    if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_LOCATION) {
                        //viewHolder5.sendMessage.setVisibility(View.GONE);

                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        //String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        //int thumbstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));

                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        JSONObject jsonObj = new JSONObject(message);
                        String lat = jsonObj.getString("lat");
                        String lon = jsonObj.getString("lon");
                        try {
                        String address = jsonObj.getString("address");
                        if(address!=null) {
                            viewHolder5.sentimagetext.setText(address);
                            if(insearchmode) {
                                setSearchTextColor(viewHolder5.sentimagetext,address);
                            }
                        }
                        } catch (Exception ex) {}


                        String locationurl = "https://maps.google.com/maps/api/staticmap?center=" + lat + "," + lon + "&zoom=16&size=200x200&sensor=false&markers=color:blue%" + lat + "," + lon + "&key=AIzaSyBGqiHDk9Qy-akUPC3yrzTDAtGzzadmghs";
                        //Log.i(TAG, "onBindViewHolder: location URL " + locationurl);
                        Glide.with(context)
                                .load(locationurl)
                                .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder5.sent_imageview);


/*
                        if (thumbkey != null && !thumbkey.equals("")&&thumbstatus == 1) {
                            //to do new ImageDownloaderTask(viewHolder5.position,holder,viewHolder5.sent_imageview).execute("app",thumbkey,"");
                            String thumbfilepath = CSDataProvider.getImageThumbnailFilePath(thumbkey);
                            if(new File(thumbfilepath).exists()) {
                                Glide.with(context)
                                        .load(Uri.fromFile(new File(thumbfilepath)))
                                        //.apply(new RequestOptions().override(defaultpreviewidth, defaultprevieheight))
                                        .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder5.sent_imageview);
                            }
                        }
*/

                    }
                    else if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_IMAGE) {

                        //int filename = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_RETRY_COUNT));
                        //LOG.info("filename retry count from app image:"+filename);
                        //viewHolder5.sendMessage.setVisibility(View.GONE);

                        //String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        //String mainkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        int thumbstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));
                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));


                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (!new File(downloadfilepath).exists() || downloadpercentage!=100) {
                            viewHolder5.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize/100)*downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder5.filesizeview.setText(filesizestr+"/"+filesizestr1);
                            viewHolder5.progressBar1.setProgress(downloadpercentage);
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder5.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);
                            }
                        }
                        

                    
if(new File(downloadfilepath).exists()&&downloadpercentage==100) {
                            Glide.with(context)
                                    .load(Uri.fromFile(new File(downloadfilepath)))
                                    //.apply(new RequestOptions().override(defaultpreviewidth, defaultprevieheight))
                                    .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder5.sent_imageview);
                        } else {
                            if (thumbkey != null && !thumbkey.equals("") && thumbstatus == 1) {
                                //to do new ImageDownloaderTask(viewHolder5.position, holder, viewHolder5.sent_imageview).execute("app", thumbkey, "");
                                //String thumbfilepath = CSDataProvider.getImageThumbnailFilePath(thumbkey);
                                String thumbfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMB_FILE_PATH));

                                if(new File(thumbfilepath).exists()) {
                                    Glide.with(context)
                                            .load(Uri.fromFile(new File(thumbfilepath)))
                                            //.apply(new RequestOptions().override(defaultpreviewidth, defaultprevieheight))
                                            .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder5.sent_imageview);
                                }
                            }
                        }

                        //viewHolder5.sentimagetext.setVisibility(View.VISIBLE);
                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        viewHolder5.sentimagetext.setText(contenttype);

                        if(insearchmode) {
                            setSearchTextColor(viewHolder5.sentimagetext,contenttype);
                        }

                    }
                    else if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_VIDEO) {
                        viewHolder5.video_play_iconview.setVisibility(View.VISIBLE);
                        //viewHolder5.sendMessage.setVisibility(View.GONE);
                        //String filepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));

                        String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        int thumbstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));

                        //filepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        /*
                        Bitmap myBitmap;
                        if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE)) == 0) {
                            myBitmap = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Video.Thumbnails.MICRO_KIND);
                        } else {
                            filepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                            myBitmap = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Video.Thumbnails.MICRO_KIND);
                        }
*/
                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (!new File(downloadfilepath).exists() || downloadpercentage!=100) {
                            viewHolder5.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize/100)*downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder5.filesizeview.setText(filesizestr+"/"+filesizestr1);
                            viewHolder5.progressBar1.setProgress(downloadpercentage);
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder5.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);
                            }
                        }


                        if(new File(downloadfilepath).exists()&&downloadpercentage==100) {
                            Glide.with(context)
                                    .load(Uri.fromFile(new File(downloadfilepath)))
                                    //.apply(new RequestOptions().override(defaultpreviewidth, defaultprevieheight))
                                    .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder5.sent_imageview);

                        } else {
                            if (thumbkey != null && !thumbkey.equals("") && thumbstatus == 1) {
                                //to do new ImageDownloaderTask(viewHolder5.position, holder, viewHolder5.sent_imageview).execute("app", thumbkey, "");
                                //String thumbfilepath = CSDataProvider.getImageThumbnailFilePath(thumbkey);
                                String thumbfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMB_FILE_PATH));

                                if(new File(thumbfilepath).exists()) {
                                    Glide.with(context)
                                            .load(Uri.fromFile(new File(thumbfilepath)))
                                            .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                                .into(viewHolder5.sent_imageview);
                                }

                            }
                        }


                        //viewHolder5.sentimagetext.setVisibility(View.VISIBLE);
                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        viewHolder5.sentimagetext.setText(contenttype);
                        if(insearchmode) {
                            setSearchTextColor(viewHolder5.sentimagetext,contenttype);
                        }

                    }

                    else if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_DOCUMENT) {
                        //viewHolder5.sendMessage.setVisibility(View.GONE);
                        //viewHolder5.sent_imageview.setImageResource(R.drawable.defaultocicon);
                        viewHolder5.sender_document_imgview.setVisibility(View.VISIBLE);
                        viewHolder5.sent_imageview.setVisibility(View.GONE);

                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));

                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (!new File(downloadfilepath).exists() || downloadpercentage!=100) {
                            viewHolder5.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize/100)*downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder5.filesizeview.setText(filesizestr+"/"+filesizestr1);
                            viewHolder5.progressBar1.setProgress(downloadpercentage);
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder5.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);
                            }
                        }



                        //viewHolder5.sentimagetext.setVisibility(View.VISIBLE);
                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        viewHolder5.sentimagetext.setText(contenttype);

                        //String filename = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        //LOG.info("filename from sent app document:"+filename);
                        if(insearchmode) {
                            setSearchTextColor(viewHolder5.sentimagetext,contenttype);
                        }

                    }
                    else if(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_AUDIO) {
                        //viewHolder5.sendMessage.setVisibility(View.GONE);

                        //viewHolder5.sent_imageview.setImageResource(R.drawable.defaultauioicon);
                        viewHolder5.sent_imagelayout.setVisibility(View.GONE);
                        viewHolder5.audiolayoutview.setVisibility(View.VISIBLE);
                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (!new File(downloadfilepath).exists() || downloadpercentage!=100) {
                            viewHolder5.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize/100)*downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder5.filesizeview.setText(filesizestr+"/"+filesizestr1);
                            viewHolder5.progressBar1.setProgress(downloadpercentage);
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder5.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if(filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);
                            }
                        }





                        //viewHolder5.sentimagetext.setVisibility(View.VISIBLE);
                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        //String uploadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                        if(new File(downloadfilepath).exists()&&downloadpercentage==100) {
                            viewHolder5.audiolengthview.setText(getDuration(downloadfilepath));
                            int max = getDurationInMS(downloadfilepath);
                            viewHolder5.audioplayseekbar.setMax(max);
                            if(audioplaychatid.equals(chatid)) {
                                viewHolder5.audioplaybuttonview.setVisibility(View.INVISIBLE);
                                viewHolder5.audiopausebuttonview.setVisibility(View.VISIBLE);
                                if(mMediaPlayer!=null) {
                                    viewHolder5.audioplayseekbar.setProgress(mMediaPlayer.getCurrentPosition());
                                    viewHolder5.audiolengthview.setText(formateMilliSeccond(mMediaPlayer.getCurrentPosition())+"/"+getDuration(downloadfilepath));
                                }
                            } else {
                                viewHolder5.audiopausebuttonview.setVisibility(View.INVISIBLE);
                                viewHolder5.audioplaybuttonview.setVisibility(View.VISIBLE);
                                viewHolder5.audioplayseekbar.setProgress(0);
                            }
                        } else {
                            viewHolder5.audiolengthview.setText("00:00");
                            viewHolder5.audioplayseekbar.setMax(0);
                        }

                        //viewHolder5.sentimagetext.setText(contenttype);
                        viewHolder5.audioimagetextview.setText(contenttype);
                        if(insearchmode) {
                            setSearchTextColor(viewHolder5.sentimagetext,contenttype);
                        }
                    }



                    viewHolder5.sendTime.setText(shortTimeStr);
                    viewHolder5.sentIcon.setVisibility(View.INVISIBLE);
                    viewHolder5.deliveredIcon.setVisibility(View.INVISIBLE);
                    viewHolder5.sentIcon1.setVisibility(View.INVISIBLE);
                    viewHolder5.deliveredIcon1.setVisibility(View.INVISIBLE);
                    viewHolder5.failedtext.setVisibility(View.INVISIBLE);

                    if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_NOT_SENT) {
                        viewHolder5.sentIcon.setVisibility(View.INVISIBLE);
                        viewHolder5.deliveredIcon.setVisibility(View.INVISIBLE);
                        viewHolder5.failedtext.setVisibility(View.VISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_SENT) {
                        viewHolder5.sentIcon.setVisibility(View.VISIBLE);
                        viewHolder5.deliveredIcon.setVisibility(View.INVISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED) {
                        viewHolder5.sentIcon.setVisibility(View.VISIBLE);
                        viewHolder5.deliveredIcon.setVisibility(View.VISIBLE);
                    } else if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_READ) {
                        ////LOG.info("Chat status setting as read:"+cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE)));
                        viewHolder5.sentIcon.setVisibility(View.INVISIBLE);
                        viewHolder5.deliveredIcon.setVisibility(View.INVISIBLE);
                        viewHolder5.sentIcon1.setVisibility(View.VISIBLE);
                        viewHolder5.deliveredIcon1.setVisibility(View.VISIBLE);

                    }
                    break;
            }

            //processandsetdefaultdata();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void processClick(int clicktype,int cursorposition,int viewtype,View view,Actions action) {
        try {
            //View view can be null
            Log.i(TAG,"clicktype:" + clicktype);
            Log.i(TAG,"cursorposition:" + cursorposition);
            Log.i(TAG,"action:" + action);
            Log.i(TAG,"viewtype:" + viewtype);
            Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,this.destination); //this can be sensitive and cause issues on notify date set changed when on long click. take care(delete is fine since after delete we are exiting from onlong click slection )
            cur.moveToPosition(cursorposition);
            int chattype =  cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
            String chatid = cur.getString(cur.getColumnIndex(CSDbFields.KEY_CHAT_ID));
            String contenttype = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_CONTENT_TYPE));
            String uploadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
            String downloadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
            int downloadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
            String mainkey = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));

            String finalfilepath = "";
            Uri finalfilepathuri = null;
            boolean downloadcompleted = false;

            if(viewtype == 4 || viewtype == 5) {
                finalfilepath = downloadfilepath;
                if(downloadpercentage>=100) {
                    downloadcompleted = true;
                }
            } else {
                finalfilepath = uploadfilepath;
                downloadcompleted = true;
            }


            if(!finalfilepath.equals("")) {
                if (new File(finalfilepath).exists()&&downloadcompleted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        finalfilepathuri = FileProvider.getUriForFile(
                                context.getApplicationContext(),
                                context.getApplicationContext()
                                        .getPackageName() + ".provider", new File(finalfilepath));
                    } else {
                        finalfilepathuri = Uri.fromFile(new File(finalfilepath));
                    }
                }
            }



            Log.i(TAG,"chattype:" + chattype);

            if(selectedpositions.size() >= 1) {
                clicktype = 1;
            }


                    if(selectedpositions.contains(cursorposition)) {
                        selectedpositions.remove((Object)cursorposition);
                        view.setBackgroundColor(Color.parseColor("#f4f4f4"));
                    } else {
                        if(clicktype == 1 && action == Actions.DEFAULT) {
                            selectedpositions.add(cursorposition);
                            view.setBackgroundColor(Color.parseColor("#BDBDBD"));
                        }
                    }
            Log.i(TAG,"selectedpositions size:" + selectedpositions.size());
            ChatAdvancedActivity.handlelongclick(selectedpositions);








                    if(clicktype == 0 &&(viewtype == 1 || viewtype == 3)) {
                        if (chattype == CSConstants.E_TEXTPLAIN) {
                        } else if (chattype == CSConstants.E_CONTACT) {
                                Intent conatctViewIntent = new Intent(this.context, ContactViewActivity.class);
                                conatctViewIntent.putExtra("chatId", chatid);
                                this.context.startActivity(conatctViewIntent);
                        }
                    }


                    else if(clicktype == 0 && (viewtype == 2 || viewtype == 4 || viewtype == 5)) {
                        if (chattype == CSConstants.E_LOCATION) {
if(action == Actions.DEFAULT) {
    CSChatLocation location = CSChatObj.getLocationFromChatID(chatid);
    Double lat = location.getLat();
    Double lng = location.getLng();
    String geoUri = "https://maps.google.com/maps?q=loc:" + lat + "," + lng+"&zoom=14&markers=color:blue%7C"+lat+","+lng;
    //LOG.info("geoUri to show on map:"+geoUri);
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
}
                        } else if (chattype == CSConstants.E_IMAGE) {

                                if (action == Actions.DEFAULT) {
openFile(finalfilepathuri,contenttype);
                            } else {
                                    uploaddownloadhelper(action,chatid, Constants.imagedirectoryreceived+File.separator+mainkey);
                                }
                        } else if (chattype == CSConstants.E_VIDEO) {

                                if(action == Actions.DEFAULT) {
                                    openFile(finalfilepathuri,contenttype);
                                } else {
                                    uploaddownloadhelper(action,chatid,Constants.videodirectoryreceived+File.separator+mainkey);
                                }

                        } else if (chattype == CSConstants.E_DOCUMENT) {

                                if(action == Actions.DEFAULT) {
                                    openFile(finalfilepathuri,contenttype);
                                } else {
                                    uploaddownloadhelper(action,chatid,Constants.docsdirectoryreceived+File.separator+mainkey);
                                }

                        } else if (chattype == CSConstants.E_AUDIO) {

                                if(action == Actions.DEFAULT) {
                                    //openFile(finalfilepathuri,contenttype);
                                } else if(action == Actions.AUDIOPLAY) {
                                    if (finalfilepathuri != null) {
                                        releaseMediaPlayer();
                                        audioplaychatid = chatid;
                                        startPlaying(finalfilepathuri);
                                        swapCursorAndNotifyDataSetChanged(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination), false, "");
                                    } else {
                                        Toast.makeText(context, "Media doesn't exist on internal storage!", Toast.LENGTH_SHORT).show();
                                    }
                                } else if(action == Actions.AUDIOPAUSE) {
                                    releaseMediaPlayer();
                                } else {
                                    uploaddownloadhelper(action,chatid,Constants.audiodirectoryreceived+File.separator+mainkey);
                                }
                        }
                    }

            cur.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }





    ////start of helper methods
    private String getFormattedDate1(String dateStr) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        Date date = null;
        try {
            date = sdf1.parse(dateStr);
        } catch (java.text.ParseException e) {
            // TODO Auto-generated catch block
            utils.logStacktrace(e);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String shortDate = sdf.format(date);
        return shortDate;
    }

    private String getFormattedTime(long dateStr) {
        try {
            return new SimpleDateFormat("hh:mm:ss a").format(dateStr);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            utils.logStacktrace(e);
        }
        return "";
    }
    private String getFormattedDate(long dateStr) {
        try {
            return new SimpleDateFormat("dd-MM-yyyy").format(dateStr);
        } catch (Exception e) {
            utils.logStacktrace(e);
        }
        return "";
    }
    private String getFormattedTime1(long dateStr) {

        try {
            return new SimpleDateFormat("hh:mm a").format(dateStr);
        } catch (Exception e) {
            utils.logStacktrace(e);
        }
        return "";
    }

    public static boolean isYesterday(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);
        now.add(Calendar.DATE,-1);
        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }


/*
    private Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE=70;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (Exception e) {}
        return null;
    }
*/


private void setSearchTextColor(TextView view,String actualstring) {
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


    private String getDuration(String filepath) {
    try {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(filepath);
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return formateMilliSeccond(Long.parseLong(durationStr));
    } catch (Exception ex) {
        ex.printStackTrace();
    }
      return "00:00";
    }
    private int getDurationInMS(String filepath) {
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(filepath);
            String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return Integer.parseInt(durationStr);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }
    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    public static String formateMilliSeccond(long milliseconds) {
        String finalTimerString = "";
        try {
            String secondsString = "";
            String minutesString = "";
            // Convert total duration into time
            int hours = (int) (milliseconds / (1000 * 60 * 60));
            int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
            int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);




            // Add hours if there
            if (hours > 0) {
                finalTimerString = hours + ":";
            }

            // Prepending 0 to seconds if it is one digit
            if (seconds < 10) {
                secondsString = "0" + seconds;
            } else {
                secondsString = "" + seconds;
            }
            if (minutes < 10) {
                minutesString = "0" + minutes;
            } else {
                minutesString = "" + minutes;
            }

            finalTimerString = finalTimerString + minutesString + ":" + secondsString;

            //      return  String.format("%02d Min, %02d Sec",
            //                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
            //                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
            //                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));

            // return timer string
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return finalTimerString;
    }

    public void clearselecteditems() {
        try {
          selectedpositions.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openFile(Uri finalfilepathuri,String contenttype) {
        try {
            if (finalfilepathuri != null) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(finalfilepathuri, contenttype);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Media doesn't exist on internal storage!", Toast.LENGTH_SHORT).show();
            }
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
            Toast.makeText(context, "No Application found to open the file<"+contenttype+">.", Toast.LENGTH_SHORT).show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void uploaddownloadhelper(Actions action,String chatid,String path) {
        try {
            if(action == Actions.UPLOAD) {
                CSChatObj.resumeTransfer(chatid);
            } else if(action == Actions.UPLOADCANCEL) {
                CSChatObj.cancelTransfer(chatid);

            } else if(action == Actions.DOWNLOAD) {
                if (!utils.isinternetavailable(context)) {
                    Toast.makeText(context, "No interent", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(context, "Write external storage permission needed", Toast.LENGTH_LONG).show();

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        ArrayList<String> allpermissions = new ArrayList<String>();
                        allpermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            ActivityCompat.requestPermissions((Activity)context, allpermissions.toArray(new String[allpermissions.size()]), 101);
                    }
                    return;
                }
                    filedownloadinitiatedchatids.add(chatid);
                CSChatObj.downloadFile(chatid,path);

            } else if(action == Actions.DOWNLOADCANCEL) {
                CSChatObj.cancelTransfer(chatid);
                if(filedownloadinitiatedchatids.contains(chatid)) {
                    filedownloadinitiatedchatids.remove(chatid);
                }
            }

            swapCursorAndNotifyDataSetChanged(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination),false,"");

            /*int position = ChatAdvancedActivity.getpositionfromchatid(chatid);
            if(position>=0) {
                notifyItemChanged(position);
            }
            */

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public String getFileSizeasString(long size) {
try {
    DecimalFormat df = new DecimalFormat("0.00");

    float sizeKb = 1024.0f;
    float sizeMb = sizeKb * sizeKb;
    float sizeGb = sizeMb * sizeKb;
    float sizeTerra = sizeGb * sizeKb;


    if (size < sizeMb)
        return df.format(size / sizeKb) + " Kb";
    else if (size < sizeGb)
        return df.format(size / sizeMb) + " Mb";
    else if (size < sizeTerra)
        return df.format(size / sizeGb) + " Gb";

    return "";
} catch (Exception ex) {
    ex.printStackTrace();
    return "";
}
    }






    private void startPlaying(Uri myUri) {
        //Uri myUri = Uri.parse(path);

        try {

            am.requestAudioFocus(mOnAudioFocusChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);



            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(context, myUri);
            mMediaPlayer.prepare(); // might take long! (for buffering, etc)
            mMediaPlayer.start();


            mDurationHandler.postDelayed(mUpdateSeekBarTime, 1000);

           /*
            mTimeElapsed = mMediaPlayer.getCurrentPosition();
            mSeekBar.setProgress((int) mTimeProgressUpdate);
            mDurationHandler.postDelayed(mUpdateSeekBarTime, 1000);
*/

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                     @Override
                                                     public void onCompletion(MediaPlayer mp) {
                                                         Log.i(TAG, "Audio completed listener");
                                                         releaseMediaPlayer();

                                                         //mSeekBar.setProgress(0);
                                                         //mAudioPlayingMessageID = "";

                                                     }
                                                 }
            );


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show();

        }

    }

    private void pauseOrresumePlaying() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            } else {
                mMediaPlayer.start();
            }
        }
    }


    public void releaseMediaPlayer() {
        try {


            swapCursorAndNotifyDataSetChanged(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination),false,"");

            audioplaychatid = "";
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;

                // abandon Audio focus
                //abandonAudioFocus();
                am.abandonAudioFocus(mOnAudioFocusChangeListener);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.i(TAG, "AUDIOFOCUS_GAIN");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.i(TAG, "AUDIOFOCUS_LOSS");
                    releaseMediaPlayer();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.i(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                    releaseMediaPlayer();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.i(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    break;
                case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                    Log.i(TAG, "AUDIOFOCUS_REQUEST_FAILED");
                    break;
                default:
                    //
            }
        }
    };

    private Runnable mUpdateSeekBarTime = new Runnable() {
        public void run() {
            try {
                if (mMediaPlayer != null) {
                    //get current position

                    swapCursorAndNotifyDataSetChanged(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination),false,"");
                    mDurationHandler.postDelayed(this, 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

}