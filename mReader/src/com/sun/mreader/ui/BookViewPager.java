package com.sun.mreader.ui;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.sun.mreader.utils.Log;

public class BookViewPager extends ViewPager {

	private static final String TAG = "BookViewPager";
	private boolean canScrollLeft = true;
	private boolean canScrollRight = true;


	public void setScrollLeft(boolean b){
		canScrollLeft = b;
	}

	public void setScrollRight(boolean b){
		canScrollRight = b;
	}

	float mLastMotionX;
	float mLastMotionY;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {

		return super.onInterceptTouchEvent(arg0);
	}
	public BookViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
}