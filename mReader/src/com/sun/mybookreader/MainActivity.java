package com.sun.mybookreader;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.sun.mybookreader.html.LinkTagSet;
import com.sun.mybookreader.mt.MtBookCategoryAdapter;
import com.sun.mybookreader.mt.MtBookDetail;
import com.sun.mybookreader.mt.MtBookListAdapter;
import com.sun.mybookreader.mt.MtBookUtil;
import com.sun.mybookreader.mt.MtParser;
import com.sun.mybookreader.mt.MtUtils;
import com.sun.mybookreader.utils.Log;

public class MainActivity extends Activity implements OnItemClickListener {
	private final String TAG = "SUN_MainActivity";
	private Button mBtn;
	private ListView mListView;
	private Context mContext = MainActivity.this;
	private ProgressDialog mProgressDialog;
	MtParser mParser;
	private static final int SHOW_BOOK_CATOERY = 1;
	private static final int SHOW_BOOK_LIST = 2;
	
	private int mShow = SHOW_BOOK_CATOERY;

	private List<LinkTagSet> mBookCategory =  new ArrayList<LinkTagSet>();
	private List<MtBookUtil> mBookList =  new ArrayList<MtBookUtil>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mBtn = (Button) findViewById(R.id.btn);
		mProgressDialog = new ProgressDialog(MainActivity.this);
		mProgressDialog.setCanceledOnTouchOutside(false);

		mBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mProgressDialog.setMessage(" Loading...");
				mProgressDialog.show();
				new GetMtBookCatrgoryTask().execute("");
			}
		});

		mListView = (ListView) findViewById(R.id.list);
		mListView.setOnItemClickListener(this);
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

	class GetMtBookCatrgoryTask extends AsyncTask<String,Integer,List<LinkTagSet>> {

		@Override  
		protected List<LinkTagSet> doInBackground(String... params) { 
			publishProgress(0); 
			try {
				mParser = new MtParser(MtUtils.MT_URL);
				publishProgress(50);  
				mBookCategory = mParser.getBookCategory();
				publishProgress(100);  
				return mBookCategory; 
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, e.getMessage());
				return null;
			}
		}  

		protected void onProgressUpdate(Integer... progress) {//在调用publishProgress之后被调用，在ui线程执行  
			mProgressDialog.setProgress(progress[0]);//更新进度条的进度  
		}  

		protected void onPostExecute(List<LinkTagSet> result) {//后台任务执行完之后被调用，在ui线程执行  
			if(result != null) {  
				MtBookCategoryAdapter adpater = new MtBookCategoryAdapter(MainActivity.this, result);
				mListView.setAdapter(adpater);
				mBtn.setVisibility(View.GONE);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		mProgressDialog.setMessage(" Loading...");
		mProgressDialog.show();
		if(mShow == SHOW_BOOK_CATOERY){
			new GetMtBookListTask().execute(mBookCategory.get(arg2).getLink());
			mShow = SHOW_BOOK_LIST;
		} else if( mShow == SHOW_BOOK_LIST){
			new GetMtBookDetailTask().execute(mBookList.get(arg2).getLink());
		}
		//		Toast.makeText(mContext, mBookCategory.get(arg2).getPlainTextString(), Toast.LENGTH_SHORT).show();
	}

	class GetMtBookListTask extends AsyncTask<String,Integer,List<MtBookUtil>> {

		@Override  
		protected List<MtBookUtil> doInBackground(String... params) { 
			publishProgress(0); 
			try {
				mBookList = mParser.getBookList(params[0]);
				publishProgress(100);  
				return mBookList; 
			} catch (Exception e) {
				e.printStackTrace();
//				Log.d(TAG, e.getMessage());
				return null;
			}
		}  

		protected void onProgressUpdate(Integer... progress) {//在调用publishProgress之后被调用，在ui线程执行  
			mProgressDialog.setProgress(progress[0]);//更新进度条的进度  
		}  

		protected void onPostExecute(List<MtBookUtil> result) {//后台任务执行完之后被调用，在ui线程执行  
			if(result != null) {  
				MtBookListAdapter adpater = new MtBookListAdapter(MainActivity.this, result);
				mListView.setAdapter(adpater);
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
	
	class GetMtBookDetailTask extends AsyncTask<String,Integer,MtBookDetail> {

		@Override  
		protected MtBookDetail doInBackground(String... params) { 
			publishProgress(0); 
			try {
				MtBookDetail mBookList = mParser.getBookDetail(params[0]);
				publishProgress(100);  
				return mBookList; 
			} catch (Exception e) {
				e.printStackTrace();
//				Log.d(TAG, e.getMessage());
				return null;
			}
		}  

		protected void onProgressUpdate(Integer... progress) {//在调用publishProgress之后被调用，在ui线程执行  
			mProgressDialog.setProgress(progress[0]);//更新进度条的进度  
		}  

		protected void onPostExecute(MtBookDetail result) {//后台任务执行完之后被调用，在ui线程执行  
			if(result != null) {  
				mProgressDialog.dismiss();
				Intent i = new Intent(MainActivity.this, BookDetailActivity.class);
				i.putExtra("detail", result);
				startActivity(i);
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
}
