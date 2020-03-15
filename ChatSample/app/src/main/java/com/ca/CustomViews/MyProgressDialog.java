package com.ca.CustomViews;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.widget.ProgressBar;

import com.ca.chatsample.R;


/**
 * Created by Dell on 01-02-2019.
 */

public class MyProgressDialog extends AlertDialog {
    Context mycontext;
    public MyProgressDialog(Context context) {
        super(context);
        mycontext = context;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myprogressdialog);
        ProgressBar mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
           //not working  //mProgressBar.setProgressTintList(ColorStateList.valueOf(mycontext.getResources().getColor(R.color.colorPrimary)));
        //} else {
            mProgressBar.getIndeterminateDrawable().mutate().setColorFilter(mycontext.getResources().getColor(R.color.theme_color), PorterDuff.Mode.SRC_IN);
        //}
        }

}
