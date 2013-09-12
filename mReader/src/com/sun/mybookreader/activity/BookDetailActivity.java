package com.sun.mybookreader.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.mybookreader.R;
import com.sun.mybookreader.database.BookChaptersDBTask;
import com.sun.mybookreader.database.BookDBTask;
import com.sun.mybookreader.database.BookTable;
import com.sun.mybookreader.html.LinkTagSet;
import com.sun.mybookreader.mt.BookChapter;
import com.sun.mybookreader.mt.MtBookCategoryAdapter;
import com.sun.mybookreader.mt.MtBookChapterAdapater;
import com.sun.mybookreader.mt.MtBookDetail;
import com.sun.mybookreader.mt.MtBookUtil;
import com.sun.mybookreader.mt.MtParser;
import com.sun.mybookreader.utils.GlobalContext;
import com.sun.mybookreader.utils.ImageLoader;
import com.sun.mybookreader.utils.Log;

public class BookDetailActivity extends BaseActivity implements OnItemClickListener {
	private static final String TAG = "SUNBookDetailActivity";
	
	private ImageView mImage;
	private Button mBtnAddBook;
	private TextView mTxtBookContent;
	private TextView mTxtBookAbout;
	private ListView mList;
	private Context mContext;
	private String mBookUrl;
	MtBookDetail mbd;
	
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.book_detail);
		mContext = this;
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mImage = (ImageView) findViewById(R.id.image);
		mBtnAddBook = (Button) findViewById(R.id.addbook);
		mTxtBookAbout = (TextView) findViewById(R.id.about);
		mTxtBookContent = (TextView) findViewById(R.id.content);
		mList = (ListView) findViewById(R.id.list);
		

		mBtnAddBook.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mProgressDialog.setMessage(" Adding Book...");
				mProgressDialog.show();
				new AddBookTask().execute("");
			}
		});
		
		
		
		mList.setOnItemClickListener(this);
		
		mBookUrl = getIntent().getStringExtra("url");
		if(mBookUrl != null){
			Log.d(TAG, "open url = "+mBookUrl);
			mProgressDialog.setMessage(" Loading...");
			mProgressDialog.show();
			new GetMtBookDetailTask().execute(mBookUrl);
		} else {
			finish();
		}
		
	}
	
	public void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		((MarginLayoutParams) params).setMargins(10, 10, 10, 10);
		listView.setLayoutParams(params);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	class GetMtBookDetailTask extends AsyncTask<String,Integer,MtBookDetail> {

		@Override  
		protected MtBookDetail doInBackground(String... params) { 
			publishProgress(0); 
			try {
				mbd = GlobalContext.getparser().getBookDetail(params[0]);
				Log.d(TAG, "found chapters num = "+mbd.bookChapters.size());
				return mbd; 
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}  

		protected void onProgressUpdate(Integer... progress) {//在调用publishProgress之后被调用，在ui线程执行  
			mProgressDialog.setProgress(progress[0]);//更新进度条的进度  
		}  

		protected void onPostExecute(MtBookDetail result) {//后台任务执行完之后被调用，在ui线程执行  
			if(result != null) {
				new ImageLoader(mContext).DisplayImage(result.imageUrl, mImage);
				mTxtBookAbout.setText(mbd.bookAbout);
				mTxtBookContent.setText(mbd.bookDetail);
				MtBookChapterAdapater adapter = new MtBookChapterAdapater(mContext, mbd.bookChapters);
				mList.setAdapter(adapter);
//				setListViewHeightBasedOnChildren(mList);
				mProgressDialog.dismiss();
			} else {
				Toast.makeText(mContext, "ERROR!!", Toast.LENGTH_SHORT).show();
			}
		}  

		protected void onPreExecute () {//在 doInBackground(Params...)之前被调用，在ui线程执行  
			mProgressDialog.setProgress(0);//进度条复位  
		}  

		protected void onCancelled () {//在ui线程执行  
			mProgressDialog.setProgress(0);//进度条复位  
		}  
	}  
	

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Log.d(TAG, "click item = " + arg2+" the link = "+mbd.bookChapters.get(arg2).getBookChapterUrl());
		mProgressDialog.setMessage(" Loading...");
		mProgressDialog.show();
		new GetMtBookChapterContentTask().execute(mbd.bookChapters.get(arg2).getBookChapterUrl());
	}
	
	class GetMtBookChapterContentTask extends AsyncTask<String,Integer,String> {

		@Override  
		protected String  doInBackground(String... params) { 
			publishProgress(0); 
			try {
				String s = new MtParser().getBookChapterContent(params[0]);
				Log.d(TAG, "this = "+ s);
				return s; 
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}  

		protected void onProgressUpdate(Integer... progress) {//在调用publishProgress之后被调用，在ui线程执行  
			mProgressDialog.setProgress(progress[0]);//更新进度条的进度  
		}  

		protected void onPostExecute(String result) {//后台任务执行完之后被调用，在ui线程执行  
			if(result != null) {  
				mProgressDialog.dismiss();
			} else {
				Toast.makeText(BookDetailActivity.this, "ERROR!!", Toast.LENGTH_SHORT).show();
			}
		}  

		protected void onPreExecute () {//在 doInBackground(Params...)之前被调用，在ui线程执行  
			mProgressDialog.setProgress(0);//进度条复位  
		}  

		protected void onCancelled () {//在ui线程执行  
			mProgressDialog.setProgress(0);//进度条复位  
		}  
	}  
	
	class AddBookTask extends AsyncTask<String,Integer,String> {

		@Override  
		protected String doInBackground(String... params) { 
			publishProgress(0); 
			try {
				MtBookUtil mt = new MtBookUtil();
				mt.setBookAbout(mbd.bookAbout);
				mt.setBookChapters(mbd.bookChapters.size());
				mt.setBookIsFinish(mbd.isFinish);
				mt.setBookUrl(mBookUrl);
				mt.setImageUrl(mbd.imageUrl);
				mt.setBookName(mbd.bookName);
				mt.setBookUpdateTime(mbd.bookUpdateTime);
				mt.setBookAuthor(mbd.bookAuthor);
				BookDBTask.addOrUpdateBook(mt);
				
				List<BookChapter> bc = new ArrayList<BookChapter>();
				for(BookChapter b : mbd.bookChapters){
					b.setBookID(Integer.parseInt(mt.getBookID()));
					bc.add(b);
				}
				
//				BookChaptersDBTask.addBookChapters(bc);
				
			} catch (Exception e) {
				e.printStackTrace();
				return "failed";
			}
			return "successed";
		}  

		protected void onProgressUpdate(Integer... progress) {//在调用publishProgress之后被调用，在ui线程执行  
			mProgressDialog.setProgress(progress[0]);//更新进度条的进度  
		}  

		protected void onPostExecute(String result) {//后台任务执行完之后被调用，在ui线程执行  
			if(result.equals("successed")) {  
				mProgressDialog.dismiss();
			} else {
				Toast.makeText(BookDetailActivity.this, "ERROR!!", Toast.LENGTH_SHORT).show();
			}
		}  

		protected void onPreExecute () {//在 doInBackground(Params...)之前被调用，在ui线程执行  
			mProgressDialog.setProgress(0);//进度条复位  
		}  

		protected void onCancelled () {//在ui线程执行  
			mProgressDialog.setProgress(0);//进度条复位  
		}  
	}  

}
