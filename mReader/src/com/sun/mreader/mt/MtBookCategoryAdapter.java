package com.sun.mreader.mt;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sun.mreader.html.LinkTagSet;
import com.sun.mreader.utils.Log;
import com.sun.mreader.R;

public class MtBookCategoryAdapter extends BaseAdapter {
	private static final String TAG = "MtBookCategoryAdapter";
	
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private List<LinkTagSet> mList;

	public MtBookCategoryAdapter(Context c, List<LinkTagSet> m){
		mContext = c;
		mLayoutInflater = LayoutInflater.from(mContext);
		mList = m;
	}
	
	@Override
	public int getCount() {
		if(mList == null){
			return 0;
		}
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		if(mList == null){
			return null;
		}
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		if(convertView == null){    
			convertView = mLayoutInflater.inflate(R.layout.bookcategory_list_item, null);
		}
		TextView tv1 = (TextView)convertView.findViewById(R.id.text1);
		tv1.setText(mList.get(position).getPlainTextString());
		return convertView;
	}

}
