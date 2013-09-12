package com.sun.mybookreader.activity;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.tags.TableHeader;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.mybookreader.R;
import com.sun.mybookreader.mt.MtBookListAdapter;
import com.sun.mybookreader.mt.MtBookUtil;
import com.sun.mybookreader.utils.GlobalContext;
import com.sun.mybookreader.utils.Log;

public class BookListActivity extends BaseActivity implements OnItemClickListener {
	private final String TAG = "SUNBookListActivity";
	private ListView mListView;
	private Context mContext;
	private ProgressDialog mProgressDialog;
	private List<MtBookUtil> mBookList =  new ArrayList<MtBookUtil>();
	private ProgressBar mListFooterProgressBar;
	private TextView mListFooterMessage;
	private MtBookListAdapter mMtBookListAdapter;

	private final int LOAD_NEXT_BOOKLIST = 0;
	private final int LOAD_FINISH = 1;
	private boolean mIsLoading = false;

	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_NEXT_BOOKLIST:
				mMtBookListAdapter.updateData(mBookList);
				mMtBookListAdapter.notifyDataSetChanged();
				mIsLoading = false;
				break;
			case LOAD_FINISH:
				mListFooterProgressBar.setVisibility(View.GONE);
				mListFooterMessage.setText(R.string.load_finish);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

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

		initListFooter();

		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// 当不滚动时
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					//判断是否滚动到底部
					if (view.getLastVisiblePosition() == view.getCount() - 1) {
						if(!mIsLoading){
							if(GlobalContext.getparser().hasNextBookUrl()){
								new Thread(
										new Runnable() {
											@Override
											public void run() {
												mIsLoading = true;

												String open = GlobalContext.getparser().getNextBookUrl();
												List<MtBookUtil> booklist = GlobalContext.getparser().getBookList(open);
												mBookList.addAll(booklist);
												Log.d("SUNMM", "found more book size = "+mBookList.size());
												mHandler.sendEmptyMessage(LOAD_NEXT_BOOKLIST);

											}
										}).start();
							} else {
								mHandler.sendEmptyMessage(LOAD_FINISH);
							}
						}
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});
	} 


	private void initListFooter() {
		View v = LayoutInflater.from(this).inflate(R.layout.book_list_footer, null);
		mListFooterProgressBar = (ProgressBar)v.findViewById(R.id.progressbar);
		mListFooterMessage = (TextView) v.findViewById(R.id.message);
		mListView.addFooterView(v);
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
				mMtBookListAdapter = new MtBookListAdapter(mContext, result);
				mListView.setAdapter(mMtBookListAdapter);
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
