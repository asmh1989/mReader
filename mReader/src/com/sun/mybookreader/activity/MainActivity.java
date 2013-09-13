package com.sun.mybookreader.activity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.util.EncodingUtils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.FrameLayout.LayoutParams;

import com.actionbarsherlock.view.MenuItem;
import com.sun.mreader.service.BookService;
import com.sun.mybookreader.R;
import com.sun.mybookreader.adapter.BookShelfAdapter;
import com.sun.mybookreader.adapter.BookShelfAdapter.OnOpenMenuListener;
import com.sun.mybookreader.adapter.MenuPopAdapter;
import com.sun.mybookreader.database.BookDBTask;
import com.sun.mybookreader.database.BookTable;
import com.sun.mybookreader.mt.MtBookUtil;
import com.sun.mybookreader.mt.MtUtils;
import com.sun.mybookreader.utils.Log;

public class MainActivity extends BaseActivity {
	private final String TAG = "SUNMainActivity";
	private Context mContext;
	private ProgressDialog mProgressDialog;
	private ListView mListView;
	private BookShelfAdapter mBookShelfAdapter;

	PopupWindow menuPop;

	private String [] PROJECTION = {BookTable.BOOK_ID, BookTable.BOOK_AUTHOR, BookTable.BOOK_NAME, BookTable.BOOK_IMAGE_URL ,
			BookTable.BOOK_CHAPTERS, BookTable.BOOK_IS_FINISH};

	private List<MtBookUtil> mBookUtil;
	private MtBookUtil choiceBook;


	@SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		mProgressDialog = new ProgressDialog(MainActivity.this);
		mProgressDialog.setCanceledOnTouchOutside(false);
		//		setEnableGesture(false);

		mListView = (ListView) findViewById(R.id.list);

		//		Cursor c = BookDBTask.query(PROJECTION);
		mBookUtil = BookDBTask.getBookList();

		mBookShelfAdapter = new BookShelfAdapter(this, mBookUtil);

		mBookShelfAdapter.setOnOpenMenuListener(new OnOpenMenuListener() {

			@Override
			public void openMenu(MtBookUtil mt, int X, int Y, View v) {
				if(menuPop.isShowing()){
					menuPop.dismiss();
				}
				choiceBook = mt;
				menuPop.setWidth(v.getWidth());
				menuPop.showAtLocation(mListView, Gravity.LEFT | Gravity.TOP, X, Y);
			}
		});
		mListView.setAdapter(mBookShelfAdapter);

		initPopMenu();
		
		Intent tsintent = new Intent(MainActivity.this,BookService.class);
		startService(tsintent);
	} 

	private void initPopMenu() {
		ListView list = new ListView(this);
		list.setAdapter(new MenuPopAdapter(this));

		menuPop = new PopupWindow(list, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		menuPop.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_buttom_bg));
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int whichItem = MenuPopAdapter.resourceId[position];
				switch (whichItem) {
				case R.string.update:

					break;
				case R.string.download:

					break;
				case R.string.book_web:
					Intent i = new Intent(mContext, BookDetailActivity.class);
					i.putExtra("url", choiceBook.getBookUrl());
					startActivity(i);
					break;
				case  R.string.book_delete:
					Set<String> set = new HashSet<String>();
					set.add(String.valueOf(choiceBook.getBookID()));
					BookDBTask.removeBook(set);
					mBookUtil.remove(choiceBook);
					mBookShelfAdapter.updateData(mBookUtil);
					mBookShelfAdapter.notifyDataSetChanged();
					break;
				}

				menuPop.dismiss();
			}
		});		
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

		menu.add(0, R.string.update_all, 0, getString(R.string.update_all))
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menu.add(0, R.string.download_all, 0, getString(R.string.download_all))
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menu.add(0, R.string.edit_bookshelf, 0, getString(R.string.edit_bookshelf))
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.string.online_book){
			Intent i = new Intent(mContext, BookCategoryActivity.class);
			i.putExtra("url", MtUtils.MT_URL);
			startActivity(i);
		} else if(item.getItemId() == R.string.update_all){

		} else if(item.getItemId() == R.string.download_all){

		} else if(item.getItemId() == R.string.edit_bookshelf){

		}
		return true;
	}

	@Override
	protected void onResume() {
		Log.d("SUNMM", "old size = "+mBookUtil.size());
		mBookUtil = BookDBTask.getBookList();
		Log.d("SUNMM", "new size = "+mBookUtil.size());
		mBookShelfAdapter.updateData(mBookUtil);
		mBookShelfAdapter.notifyDataSetChanged();
		super.onResume();
	}

}
