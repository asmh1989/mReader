package com.sun.mybookreader.activity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.widget.GridView;

import com.actionbarsherlock.view.MenuItem;
import com.sun.mybookreader.R;
import com.sun.mybookreader.adapter.BookShelfCursorAdapter;
import com.sun.mybookreader.database.BookDBTask;
import com.sun.mybookreader.database.BookTable;
import com.sun.mybookreader.mt.MtUtils;
import com.sun.mybookreader.utils.Log;

public class MainActivity extends BaseActivity{
	private final String TAG = "SUNMainActivity";
	private Context mContext;
	private ProgressDialog mProgressDialog;
	private GridView mGridView;
	private BookShelfCursorAdapter mBookShelfAdapter;
	
	private String [] PROJECTION = {BookTable.BOOK_ID, BookTable.BOOK_AUTHOR, BookTable.BOOK_NAME, BookTable.BOOK_IMAGE_URL ,
			BookTable.BOOK_CHAPTERS, BookTable.BOOK_IS_FINISH};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		mProgressDialog = new ProgressDialog(MainActivity.this);
		mProgressDialog.setCanceledOnTouchOutside(false);
//		setEnableGesture(false);
		
		mGridView = (GridView) findViewById(R.id.grid);
		Cursor c = BookDBTask.query(PROJECTION);
		c.registerDataSetObserver(new DataSetObserver() {

			@Override
			public void onChanged() {
				super.onChanged();
				Log.d(TAG, "registerDataSetObserver...");
				mBookShelfAdapter.notifyDataSetChanged();
			}
		});
		
		mBookShelfAdapter = new BookShelfCursorAdapter(this, c, true);
		
		mGridView.setAdapter(mBookShelfAdapter);
	} 

	public String GetHtml(String urlpath) throws Exception {
		Log.d(TAG,"GetHtml  : "+urlpath);
		URL url = new URL(urlpath);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(6 * 1000);
		conn.setRequestMethod("GET");

		if (conn.getResponseCode() == 200) {
			InputStream inputStream = conn.getInputStream();
			String html = "";

			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "GBK"));

			String line = "";
			try {
				while ((line = br.readLine()) != null) {
					line=EncodingUtils.getString(line.getBytes(), "UTF-8");//然后再对源码转换成想要的编码就行，这个可有可无，平台会按照默认编码读数据。
					html += line;
				}
			} catch (Exception e) {
				Log.d(TAG, e.getMessage());
			}

			return html;
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        menu.add(0, R.string.online_book, 0, getString(R.string.online_book))
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.string.online_book){
			Intent i = new Intent(mContext, BookCategoryActivity.class);
			i.putExtra("url", MtUtils.MT_URL);
			startActivity(i);
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
}
