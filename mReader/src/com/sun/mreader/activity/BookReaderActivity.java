package com.sun.mreader.activity;

import com.sun.mreader.database.BookTable;
import com.sun.mreader.fragment.BookReaderFragment;
import com.sun.mreader.utils.GlobalContext;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.view.WindowManager;

public class BookReaderActivity extends FragmentActivity {
	private static final String TAG="SUNBookReaderActivity";

	private String mBookId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
        //设置全屏  
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		super.onCreate(savedInstanceState);
		if(GlobalContext.ACTION_BOOK_READER.equals(getIntent().getAction())){
			mBookId = getIntent().getStringExtra(BookTable.BOOK_ID);
	        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
	            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	            BookReaderFragment fragment = new BookReaderFragment();
	            Bundle b = new Bundle();
	            b.putString(BookTable.BOOK_ID, mBookId);
	            fragment.setArguments(b);
	            ft.add(android.R.id.content, fragment, TAG);
	            ft.commit();
	        }
		}
	}
	
}
