package com.sun.mybookreader.adapter;

import com.sun.mybookreader.R;
import com.sun.mybookreader.database.BookTable;
import com.sun.mybookreader.utils.ImageLoader;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BookShelfCursorAdapter extends CursorAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	
	public BookShelfCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(R.layout.grid_item, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView image = (ImageView) view.findViewById(R.id.image1);
		ImageView image2 = (ImageView) view.findViewById(R.id.image2);
		ImageView image3 = (ImageView) view.findViewById(R.id.image3);
//		TextView txt = (TextView) view.findViewById(R.id.text);
		
		int i = 0;
		while(i == 0 || cursor.moveToNext()){
			String imageurl = cursor.getString(cursor.getColumnIndex(BookTable.BOOK_IMAGE_URL));
			String bookname = cursor.getString(cursor.getColumnIndex(BookTable.BOOK_NAME));
			ImageView v = null;
			switch (i++) {
			case 0:
				v = image;
				break;
			case 1:
				v = image2;
				break;
			case 2:
				v = image3;
				break;
			default:
				v = image;
				break;
			}
			new ImageLoader(mContext).DisplayImage(imageurl, v);
		}
		
		if(i == 1){
			image2.setVisibility(View.INVISIBLE);
			image3.setVisibility(View.INVISIBLE);
		} else {
			image3.setVisibility(View.INVISIBLE);
		}
		
//		txt.setText(bookname);
		

	}
	
}
