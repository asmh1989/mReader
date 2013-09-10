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
		ImageView image = (ImageView) view.findViewById(R.id.image);
		TextView txt = (TextView) view.findViewById(R.id.text);
		
		String imageurl = cursor.getString(cursor.getColumnIndex(BookTable.BOOK_IMAGE_URL));
		String bookname = cursor.getString(cursor.getColumnIndex(BookTable.BOOK_NAME));
		
		txt.setText(bookname);
		
		new ImageLoader(mContext).DisplayImage(imageurl, image);
	}
	
}
