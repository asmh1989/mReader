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

public class MainActivity extends Activity{
	private final String TAG = "SUNMainActivity";
	private Button mBtn;
	private Context mContext;
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		mBtn = (Button) findViewById(R.id.btn);
		mBtn.setText("MT");
		mProgressDialog = new ProgressDialog(MainActivity.this);
		mProgressDialog.setCanceledOnTouchOutside(false);

		mBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(mContext, BookCategoryActivity.class);
				i.putExtra("url", MtUtils.MT_URL);
				startActivity(i);
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
}
