package com.ca.views;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.ca.utils.utils;

public class CustomEditText extends EditText {

	public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public CustomEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CustomEditText(Context context) {
		super(context);
		init();
	}

	private void init() {
		if (!isInEditMode()) {
			try {
			//Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Michroma.ttf");
			//setTypeface(tf);
			} catch (Exception e) {
				utils.logStacktrace(e);
			}
		}
	}

}