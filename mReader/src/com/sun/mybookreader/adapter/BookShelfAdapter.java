package com.sun.mybookreader.adapter;

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

import com.sun.mybookreader.R;
import com.sun.mybookreader.mt.MtBookUtil;
import com.sun.mybookreader.utils.ImageLoader;

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
		int length = mBookUtil.size()/3 + 1;
		if(length < 5){
			length = 5;
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
		ImageView image = (ImageView) convertView.findViewById(R.id.image1);
		ImageView image2 = (ImageView) convertView.findViewById(R.id.image2);
		ImageView image3 = (ImageView) convertView.findViewById(R.id.image3);
		//		TextView txt = (TextView) view.findViewById(R.id.text);

		int i = 0;
		int which =  position * 3;
		int length = mBookUtil.size();

		switch ((length - which) % 3) {
		case 0:
			image.setVisibility(View.INVISIBLE);
			image2.setVisibility(View.INVISIBLE);
			image3.setVisibility(View.INVISIBLE);
			break;
		case 1:
			image2.setVisibility(View.INVISIBLE);
			image3.setVisibility(View.INVISIBLE);
			break;
		case 2:
			image3.setVisibility(View.INVISIBLE);
			break;
		}
		
		while( i < 3 && which < length){
			MtBookUtil mt = mBookUtil.get(which++);
			String imageurl = mt.getImageUrl();
			//			String bookname = cursor.getString(cursor.getColumnIndex(BookTable.BOOK_NAME));
			ImageView v = null;
			switch (i) {
			case 0:	v = image;	break;
			case 1:	v = image2;	break;
			case 2:	v = image3;	break;
			}
			v.setOnClickListener(new MyClickListener(mt));
			v.setOnLongClickListener(new MyLongClickListener(mt, which));
			v.setOnTouchListener(this);
			mImageLoader.DisplayImage(imageurl, v);
			v.setVisibility(View.VISIBLE);
			i++;
		}

		//		Log.d("SUNMM", "i = "+i +" which = "+which+" length = "+length);
		//		
		//		if(i == 1){
		//
		//		} else if(i == 2) {
		//
		//		} else if(i == 0){
		//
		//		}

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
