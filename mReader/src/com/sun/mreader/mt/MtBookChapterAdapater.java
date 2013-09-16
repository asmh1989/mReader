package com.sun.mreader.mt;

import java.util.List;

import com.sun.mreader.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class MtBookChapterAdapater extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private List<BookChapter> mList;

	public MtBookChapterAdapater(Context c, List<BookChapter> m){
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
			convertView = mLayoutInflater.inflate(R.layout.book_chapaters_item, null);
		}
		TextView tv1 = (TextView)convertView.findViewById(R.id.text2);
		tv1.setText(mList.get(position).getBookChapter());
		return convertView;
	}

}
