package com.sun.mybookreader;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.mybookreader.html.LinkTagSet;
import com.sun.mybookreader.mt.MtBookCategoryAdapter;
import com.sun.mybookreader.mt.MtBookDetail;
import com.sun.mybookreader.mt.MtParser;
import com.sun.mybookreader.utils.ImageLoader;
import com.sun.mybookreader.utils.Log;

public class BookDetailActivity extends Activity implements OnItemClickListener {
	
	private static final String TAG = "BookDetailActivity";
	
	private ImageView mImage;
	private Button mBtnAddBook;
	private TextView mTxtBookContent;
	private TextView mTxtBookAbout;
	private ListView mList;
	MtBookDetail mbd;
	
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mbd = (MtBookDetail)getIntent().getSerializableExtra("detail");
		if(mbd == null){
			finish();
		}
		
		setContentView(R.layout.book_detail);
		mProgressDialog = new ProgressDialog(this);
		
		mImage = (ImageView) findViewById(R.id.image);
		mBtnAddBook = (Button) findViewById(R.id.addbook);
		mTxtBookAbout = (TextView) findViewById(R.id.about);
		mTxtBookContent = (TextView) findViewById(R.id.content);
		mList = (ListView) findViewById(R.id.list);
		
		new ImageLoader(this).DisplayImage(mbd.imageUrl, mImage);
		mBtnAddBook.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});
		
		mTxtBookAbout.setText(mbd.bookAbout);
		mTxtBookContent.setText(mbd.bookDetail);
		List<LinkTagSet> list =  new ArrayList<LinkTagSet>();
		for(int i = 0; i < mbd.bookChapters.size(); i++){
			list.add(mbd.bookChapters.get(i+""));
		}
		MtBookCategoryAdapter adpater = new MtBookCategoryAdapter(this, list);
		mList.setAdapter(adpater);
		setListViewHeightBasedOnChildren(mList);
		mList.setOnItemClickListener(this);
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
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Log.d(TAG, "click item = " + arg2+" the link = "+mbd.bookChapters.get(arg2+"").getLink());
		mProgressDialog.setMessage(" Loading...");
		mProgressDialog.show();
		new GetMtBookChapterContentTask().execute( mbd.bookChapters.get(arg2+"").getLink());
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
//				Log.d(TAG, e.getMessage());
				return null;
			}
		}  

		protected void onProgressUpdate(Integer... progress) {//在调用publishProgress之后被调用，在ui线程执行  
			mProgressDialog.setProgress(progress[0]);//更新进度条的进度  
		}  

		protected void onPostExecute(String result) {//后台任务执行完之后被调用，在ui线程执行  
			if(result != null) {  
				mProgressDialog.dismiss();
//				Intent i = new Intent(MainActivity.this, BookDetailActivity.class);
//				i.putExtra("detail", result);
//				startActivity(i);
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
