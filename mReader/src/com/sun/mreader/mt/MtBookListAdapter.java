package com.sun.mreader.mt;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sun.mreader.utils.ImageLoader;
import com.sun.mreader.utils.Log;
import com.sun.mreader.R;

public class MtBookListAdapter extends BaseAdapter {
	private static final String TAG = "MtBookListAdapter";
	
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private List<MtBookUtil> mList;
	public ImageLoader mImageLoader; // 用来下载图片的类，后面有介绍
	
	public MtBookListAdapter(Context c, List<MtBookUtil> m){
		mContext = c;
		mLayoutInflater = LayoutInflater.from(mContext);
		mList = m;
		mImageLoader = new ImageLoader(mContext);
	}
	
	public void updateData(List<MtBookUtil> m){
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
			convertView = mLayoutInflater.inflate(R.layout.book_list_item, null);
		}
		ImageView image = (ImageView)convertView.findViewById(R.id.list_image);
		TextView name = (TextView)convertView.findViewById(R.id.content);
		MtBookUtil mt = mList.get(position);
		
		
		name.setText(mt.getBookName()+"\n"+mt.getBookAuthor()+"\n"+mt.getBookAbout());
		
		Log.d(TAG, "getimageurl = "+mt.getImageUrl()+" bookname = "+mt.getBookName());
		mImageLoader.DisplayImage(mt.getImageUrl(),image);
		
		return convertView;
	}
	
	

}
