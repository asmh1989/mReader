package com.sun.mybookreader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EncodingUtils;

import com.sun.mybookreader.html.LinkTagSet;
import com.sun.mybookreader.mt.MtBookCategoryAdapter;
import com.sun.mybookreader.mt.MtBookDetail;
import com.sun.mybookreader.mt.MtBookListAdapter;
import com.sun.mybookreader.mt.MtBookUtil;
import com.sun.mybookreader.mt.MtParser;
import com.sun.mybookreader.mt.MtUtils;
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

public class BookCategoryActivity extends Activity implements OnItemClickListener {
	private final String TAG = "SUNBookCategoryActivity";
	private ListView mListView;
	private Context mContext;
	private ProgressDialog mProgressDialog;

	private List<LinkTagSet> mBookCategory =  new ArrayList<LinkTagSet>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_activity);
		mContext = this;
		mProgressDialog = new ProgressDialog(this);
		mListView = (ListView) findViewById(R.id.list);
		mListView.setOnItemClickListener(this);

		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setMessage(" Loading...");
		String open = getIntent().getStringExtra("url");
		if(open != null){
			mProgressDialog.show();
			new GetMtBookCatrgoryTask().execute(MtUtils.MT_URL);
		} else {
			finish();
		}
	} 

	class GetMtBookCatrgoryTask extends AsyncTask<String,Integer,List<LinkTagSet>> {

		@Override  
		protected List<LinkTagSet> doInBackground(String... params) { 
			publishProgress(0); 
			try {
				mBookCategory = GlobalContext.getparser().getBookCategory(params[0]);
				return mBookCategory; 
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}  

		protected void onProgressUpdate(Integer... progress) {//在调用publishProgress之后被调用，在ui线程执行  
			mProgressDialog.setProgress(progress[0]);//更新进度条的进度  
		}  

		protected void onPostExecute(List<LinkTagSet> result) {//后台任务执行完之后被调用，在ui线程执行  
			if(result != null) {  
				MtBookCategoryAdapter adpater = new MtBookCategoryAdapter(mContext, result);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent i = new Intent(mContext, BookListActivity.class);
		i.putExtra("url", mBookCategory.get(arg2).getLink());
		startActivity(i);
		//		Toast.makeText(mContext, mBookCategory.get(arg2).getPlainTextString(), Toast.LENGTH_SHORT).show();
	}
	
}
