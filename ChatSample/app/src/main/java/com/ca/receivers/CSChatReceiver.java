package com.ca.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;

import com.ca.Utils.CSDbFields;
import com.ca.chatsample.MainActivity;
import com.ca.utils.Constants;
import com.ca.utils.utils;
import com.ca.wrapper.CSDataProvider;


import java.util.ArrayList;



public class CSChatReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    MainActivity.context = context.getApplicationContext();
                    LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(intent);

                    //LOG.info("Chat Receieved in CSChatReceiver");


                    String chatid = intent.getStringExtra("chatid");
                    String destination = intent.getStringExtra("destinationnumber");
                    String destinationname = intent.getStringExtra("destinationname");
                    int isgroupmessage = intent.getIntExtra("isgroupmessage",0);

                    String message = "";
                    String finalmessage = "";
                    int chattype = 0;
                    int issender = 1;
                    String grpmessagesender = "";
                    int lastissender = 1; //hot fix for notification when forward



                    if(isgroupmessage == 0) {
                        if(destinationname.equals("")) {
                            destinationname = destination;
                        }
                    } else {

                        if (destinationname.equals("")) {
                            Cursor cor = CSDataProvider.getGroupsCursorByFilter(CSDbFields.KEY_GROUP_ID, destination);
                            //LOG.info("cor count:"+cor.getCount());
                            if (cor.getCount() > 0) {
                                cor.moveToNext();
                                destinationname = cor.getString(cor.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_NAME));
                            }
                            cor.close();
                        }
                    }


                    ArrayList<String> messageList = new ArrayList<>();
                    Cursor cur = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(destination);
                    while(cur.moveToNext()) {
                         issender = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_IS_SENDER));
                        String chatidd = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        if(chatidd.equals(chatid)&&lastissender == 1) {
                            lastissender = issender;
                        }
                         if(issender == 0) {
                            message = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                            chattype = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
                            grpmessagesender = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_GROUP_MESSAGE_SENDER));

                            //LOG.info("message:" + message);
                            if (chattype == 0) {
                                finalmessage = message;
                            } else if (chattype == 1) {
                                finalmessage = message;
                            } else if (chattype == 2) {
                                finalmessage = message;
                            } else if (chattype == 3) {
                                finalmessage = "Location Received";
                            } else if (chattype == 4) {
                                finalmessage = "Image Received";
                            } else if (chattype == 5) {
                                finalmessage = "Video Received";
                            } else if (chattype == 6) {
                                finalmessage = "Contact Received";
                            } else if (chattype == 7) {
                                finalmessage = "Document Received";
                            } else if (chattype == 8) {
                                finalmessage = "Audio Received";
                            }






                            if(isgroupmessage == 1) {
                                Cursor cur1 = CSDataProvider.getContactCursorByNumber(grpmessagesender);
                                if (cur1.getCount() > 0) {
                                    cur1.moveToNext();
                                    grpmessagesender = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
                                }
                                cur1.close();

                                finalmessage = grpmessagesender + ":" + finalmessage;
                            }

                            messageList.add(finalmessage);
                            //LOG.info("finalmessage:" + finalmessage);

                        }

                    }
                    cur.close();

                    if (!destination.equals(Constants.inchatactivitydestination)&&lastissender == 0) {
                        utils.notifyUserChat(context, destination, messageList.toArray(new String[messageList.size()]), isgroupmessage,destinationname,finalmessage);
                    }
                } catch (Exception ex) {
                    utils.logStacktrace(ex);
                }
            }
        }).start();
    }
}