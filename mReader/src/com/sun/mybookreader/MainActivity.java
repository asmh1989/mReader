package com.sun.mybookreader;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.sun.mybookreader.html.LinkTagSet;
import com.sun.mybookreader.mt.MtBookCategoryAdapter;
import com.sun.mybookreader.mt.MtParser;

public class MainActivity extends Activity implements OnItemClickListener {
	private final String TAG = "SUN";
	private Button mBtn;
	private ListView mListView;
	private Context mContext = this;
	private ProgressDialog mProgressDialog;
	MtParser mParser;
	private static final int SHOW_BOOK_CATOERY = 1;
	private static final int SHOW_BOOK_LIST = 2;
	private static final int SHOW_PROGRESS = 99;

	private List<LinkTagSet> mBookCategory =  new ArrayList<LinkTagSet>();



	private Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
			case SHOW_BOOK_CATOERY:
				MtBookCategoryAdapter adpater = new MtBookCategoryAdapter(MainActivity.this, mBookCategory);
				mListView.setAdapter(adpater);
				mBtn.setVisibility(View.GONE);
				mProgressDialog.dismiss();
				break;
			case SHOW_BOOK_LIST:

				mProgressDialog.dismiss();
				break;
			case SHOW_PROGRESS:
				mProgressDialog.setMessage(" Loading...");
				mProgressDialog.show();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mBtn = (Button) findViewById(R.id.btn);
		mProgressDialog = new ProgressDialog(this);
		mBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						mHandler.sendEmptyMessage(SHOW_PROGRESS);
						mParser = new MtParser();
						mBookCategory = mParser.getBookCategory();
						Message msg = new Message();
						msg.what = SHOW_BOOK_CATOERY;
						mHandler.sendMessage(msg);
					}
				}).start();
			}
		});

		mListView = (ListView) findViewById(R.id.list);
		mListView.setOnItemClickListener(this);
	} 


	public static byte[] readStream(InputStream inputStream) throws Exception {
		byte[] buffer = new byte[1024];
		int len = -1;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		while ((len = inputStream.read(buffer)) != -1) {
			byteArrayOutputStream.write(buffer, 0, len);
		}

		inputStream.close();
		byteArrayOutputStream.close();
		return byteArrayOutputStream.toByteArray();
	}

	public String testGetHtml(String urlpath) throws Exception {
		URL url = new URL(urlpath);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(6 * 1000);
		conn.setRequestMethod("GET");

		if (conn.getResponseCode() == 200) {
			InputStream inputStream = conn.getInputStream();
			String html = "";

			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "GB2312"));

			String line = "";
			try {
				while ((line = br.readLine()) != null) {
					line=EncodingUtils.getString(line.getBytes(), "utf-8");//然后再对源码转换成想要的编码就行，这个可有可无，平台会按照默认编码读数据。
					html += line;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.d(TAG, e.getMessage());
			}

			MtParser mtParser = new MtParser();
			mtParser.getBookCategory();

			return html;
		}
		return null;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		final int position = arg2;
		new Thread(new Runnable() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(SHOW_PROGRESS);
				mParser.getBookList(mBookCategory.get(position).getLink());
				Message msg = new Message();
				msg.what = SHOW_BOOK_LIST;
				mHandler.sendMessage(msg);
			}
		}).start();
//		Toast.makeText(mContext, mBookCategory.get(arg2).getPlainTextString(), Toast.LENGTH_SHORT).show();
	}

}
