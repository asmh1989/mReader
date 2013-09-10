package com.sun.mybookreader.activity;

import java.util.ArrayList;
import java.util.List;

import com.sun.mybookreader.R;
import com.sun.mybookreader.mt.MtBookListAdapter;
import com.sun.mybookreader.mt.MtBookUtil;
import com.sun.mybookreader.mt.MtParser;
import com.sun.mybookreader.utils.GlobalContext;
import com.sun.mybookreader.utils.Log;

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
import android.widget.ListView;
import android.widget.Toast;

public class BookListActivity extends BaseActivity implements OnItemClickListener {
	private final String TAG = "SUNBookListActivity";
	private ListView mListView;
	private Context mContext;
	private ProgressDialog mProgressDialog;
	private List<MtBookUtil> mBookList =  new ArrayList<MtBookUtil>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_activity);
		mContext = this;
		mListView = (ListView) findViewById(R.id.list);
		mListView.setOnItemClickListener(this);
		String open = getIntent().getStringExtra("url");
		if(open != null){
			Log.d(TAG, "open url = "+open);
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setMessage(" Loading...");
			mProgressDialog.show();
			new GetMtBookListTask().execute(open);
		} else {
			finish();
		}
	} 


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent i = new Intent(mContext, BookDetailActivity.class);
		i.putExtra("url", mBookList.get(arg2).getBookUrl());
		startActivity(i);
	}

	class GetMtBookListTask extends AsyncTask<String,Integer,List<MtBookUtil>> {

		@Override  
		protected List<MtBookUtil> doInBackground(String... params) { 
			publishProgress(0); 
			try {
				mBookList = GlobalContext.getparser().getBookList(params[0]);
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
				MtBookListAdapter adpater = new MtBookListAdapter(mContext, result);
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
}
