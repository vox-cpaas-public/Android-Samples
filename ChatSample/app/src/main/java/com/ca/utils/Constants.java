package com.ca.utils;

public class Constants {
    public static String server = null;
    public static int port = 0;
    public static String password = "password";
    public static String phoneNumber = "+9177940xxxxx";
    public static boolean isAlreadySignedUp = false;


    // SignUp screen Constants
    public static String INTENT_MOBILE_NUMBER = "registeredMobileNumber";
    public static String INTENT_REGION = "regionSelectedByUser";
    public static int ACTIVATION_SCREEN_INTENT_CODE = 998;

    public static final String MyPREFERENCES = "ConnectPrefs";
    public static int tab_selected = 1;
    public static int incallcount = 0;
    public static int answeredcallcount = 0;
    public static String lastcallid = "";
    public static String inchatactivitydestination = "";

    // Chat Constants
    public static final String INTENT_CHAT_CONTACT_NUMBER = "contactNumber";
    public static final String INTENT_CHAT_CONTACT_NAME = "contactName";

    public static int UIACTION_NEWCHAT = 0;
    public static int UIACTION_FORWARDCHATMESSAGE = 1;
    public static int UIACTION_ADDCONTACTSTOGROUP = 2;
    public static boolean iSHoldCalled = false;
    public static boolean isintentionalhold = false;


    public static String chatappname = "";
    //image
    public static String imagedirectory = chatappname+"/Images";
    public static String imagedirectorysent = chatappname+"/Images/Sent";//used for location and doc
    public static String imagedirectoryreceived = chatappname+"/Images/Received"; //used for location and thumbainal internally

    //video
    public static String videodirectory = chatappname+"/Videos";
    public static String videodirectorysent = chatappname+"/Videos/Sent";//used for location and doc
    public static String videodirectoryreceived = chatappname+"/Videos/Received"; //used for location and thumbainal internally

    //audio
    public static String audiodirectory = chatappname+"/Audios";
    public static String audiodirectorysent = chatappname+"/Audios/Sent";//used for location and doc
    public static String audiodirectoryreceived = chatappname+"/Audios/Received"; //used for location and thumbainal internally

    //Documents
    public static String docsdirectory = chatappname+"/Documents";
    public static String docsdirectorysent = chatappname+"/Documents/Sent";//used for location and doc
    public static String docsdirectoryreceived = chatappname+"/Documents/Received"; //used for location and thumbainal internally

    //profiles
    public static String profilesdirectory = chatappname+"/Profile Photos";
    //public static String profilesdirectorysent = chatappname+"/Profile Photos/Sent";//used for location and doc
    //public static String profilesdirectoryreceived = chatappname+"/Profile Photos/Received"; //used for location and thumbainal internally

    public static String thumbnailsdirectory = chatappname+"/Thumbnails";
    public static String recordingsdirectory = chatappname+"/Recordings";

    public static final String ONLY_DATE_FORMAT = "dd MMM yy";
    public static final String TIME_FORMAT = "hh:mm a";
    public static final String TIME_FORMAT_24HR = "HH:mm";



}
