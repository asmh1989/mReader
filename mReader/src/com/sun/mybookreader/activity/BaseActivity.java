package com.sun.mybookreader.activity;

import android.os.Bundle;

import com.sun.mybookreader.R;
import com.sun.swipebacklayout.lib.SwipeBackLayout;
import com.sun.swipebacklayout.lib.app.SwipeBackActivity;

public class BaseActivity extends SwipeBackActivity {

	private SwipeBackLayout mSwipeBackLayout;
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
	}

	public void setEdgeTrackingEnabled(int i){
		mSwipeBackLayout.setEdgeTrackingEnabled(i);
	}
	
    public void setEnableGesture(boolean enable) {
    	mSwipeBackLayout.setEnableGesture(enable);
    }
    
	@Override
	protected void onResume() {
		super.onResume();
	}

}
