package com.ca.utils;

import android.os.Environment;

public class ChatConstants {

    // Below is the Folder structure for Chat
    public static final String extStorageDirectory = Environment
            .getExternalStorageDirectory().toString();
    public static final String CHAT_IMAGES_DIRECTORY = extStorageDirectory
            + "/ChatSample/Images";
    public static final String CHAT_VIDEOS_DIRECTORY = extStorageDirectory
            + "/ChatSample/Videos";
    public static final String CHAT_AUDIO_DIRECTORY = extStorageDirectory
            + "/ChatSample/Audios";
    public static final String CHAT_AUDIO_DIRECTORY_SENT = extStorageDirectory
            + "/ChatSample/Audios/Sent";
    public static final String CHAT_DOCUMENTS_DIRECTORY = extStorageDirectory
            + "/ChatSample/Documents";


    public static final String INTENT_DESTINATION_NUMBER = "destinationnumber";
    public static final String INTENT_CHAT_ID = "chatid";
    public static final String INTENT_LOCATION_LATITUDE = "latitude";
    public static final String INTENT_LOCATION_LONGITUDE = "longitude";
    public static final String INTENT_LOCATION_ADDRESS = "address";

    public static final String TIME_FORMAT = "hh:mm a";

}
