package com.ca.adapaters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.ca.chatsample.R;

import java.util.ArrayList;

public class SimpleImageTextAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<String> navDrawerItems;

	public SimpleImageTextAdapter(Context context, ArrayList<String> navDrawerItems){
		this.context = context;
		this.navDrawerItems = navDrawerItems;
	}

	@Override
	public int getCount() {
		return navDrawerItems.size();
	}

	@Override
	public Object getItem(int position) {		
		return navDrawerItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.simple_list_item_3, null);
        }

        TextView txtTitle = (TextView) convertView.findViewById(R.id.text1);
		ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView4);

        txtTitle.setText(navDrawerItems.get(position).toString());

		if(position == 0) {
			imageView.setBackgroundResource(R.drawable.ic_camera_profile_big1);
		} else if(position == 1) {
			imageView.setBackgroundResource(R.drawable.dashboard);

		} else {

		}

        return convertView;
	}

}
