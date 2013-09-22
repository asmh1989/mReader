package com.sun.mreader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.view.MenuItem;
import com.sun.mreader.R;
import com.sun.mreader.fragment.BookShelfFragment;
import com.sun.mreader.mt.MtUtils;

public class MainActivity extends BaseActivity {
	private final String TAG = "SUNMainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
		
        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, new BookShelfFragment(), TAG);
            ft.commit();
        }

//		Intent tsintent = new Intent(MainActivity.this,BookService.class);
//		startService(tsintent);
	}
	
}
