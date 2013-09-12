package com.sun.mybookreader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sun.mybookreader.R;

public class MenuPopAdapter extends BaseAdapter {
	
	public static final int [] resourceId = new int[]{R.string.update, R.string.download, R.string.book_web, R.string.book_delete};
	private Context mContext;
	private LayoutInflater mInflater;
	
	public MenuPopAdapter(Context c){
		mContext = c;
		mInflater = LayoutInflater.from(c);
	}
	
	@Override
	public int getCount() {
		return resourceId.length;
	}

	@Override
	public Object getItem(int position) {
		return mContext.getResources().getString(resourceId[position]);
	}

	@Override
	public long getItemId(int position) {
		return resourceId[position];
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.menu_pop_list, parent, false);
		}
		
		TextView v = (TextView) convertView.findViewById(R.id.text1);
		v.setText(resourceId[position]);
		return convertView;
	}

}
