package com.sun.mreader.adapter;

import java.util.List;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.sun.mreader.mt.MtBookUtil;
import com.sun.mreader.utils.ImageLoader;
import com.sun.mreader.R;

public class BookShelfAdapter extends BaseAdapter implements OnTouchListener {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<MtBookUtil> mBookUtil;
	private int positionX;
	private int positionY;


	private OnOpenMenuListener mOnOpenMenuListener;
	private ImageLoader mImageLoader;

	public interface OnOpenMenuListener{
		void openMenu(MtBookUtil mt, int x, int y, View v);
	}

	public void setOnOpenMenuListener(OnOpenMenuListener l){
		mOnOpenMenuListener = l;
	}

	public BookShelfAdapter(Context context, List<MtBookUtil>  c) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mBookUtil = c;
		mImageLoader = new ImageLoader(mContext);
	}

	@Override
	public int getCount() {
		int length = mBookUtil.size();
		if(length < 15){
			length = 15;
		}
		return length;
	}

	public void updateData(List<MtBookUtil>  c){
		mBookUtil = c;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.grid_item, parent, false);
		}
		ImageView image = (ImageView) convertView.findViewById(R.id.image);
		if( mBookUtil.size() > position){
			mImageLoader.DisplayImage(mBookUtil.get(position).getImageUrl(), image);
		}

		return convertView;
	}

	class MyClickListener implements android.view.View.OnClickListener{
		MtBookUtil bookUtil;

		public MyClickListener(MtBookUtil m){
			bookUtil = m;
		}

		@Override
		public void onClick(View v) {
			Log.d("SUNMM", "click open book name = "+ bookUtil.getBookName().trim());
		}
	}

	class MyLongClickListener implements android.view.View.OnLongClickListener{
		MtBookUtil bookUtil;
		int pos;
		public MyLongClickListener(MtBookUtil mt, int position) {
			bookUtil = mt;
			pos = position;
		}

		@Override
		public boolean onLongClick(View v) {
			//			vibrate(200);

			int [] loc = new int[2];
			v.getLocationInWindow(loc);
			positionX += loc[0];
			positionY += loc[1];
			if(mOnOpenMenuListener != null){
				mOnOpenMenuListener.openMenu(bookUtil, positionX, positionY, v);
			}
			return false;
		}

	}

	private void vibrate(long duration) {
		Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = {
				0, duration
		};
		vibrator.vibrate(pattern, -1);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		positionX = (int)event.getX();
		positionY = (int)event.getY();
		return false;
	}
}
