package com.sun.mreader.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.GridView;

import com.sun.mreader.R;
import com.sun.mreader.utils.Log;

public class BookGridView extends GridView{
	private static String NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";
	private static final String TAG="SUN-BookGridView"; 

	private Drawable mBackgroundCenter;
	private Drawable mBackgroundLeft;
	private Drawable mBackgroundRight;
	private int shelfLayerHeadHeight;

	private final static int LEFT_AND_RIGHT = 24;


	public BookGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//获取自定义属性
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShelfLayer);
		shelfLayerHeadHeight = a.getDimensionPixelSize(R.styleable.ShelfLayer_shelfLayerHeadHeight,50);
		int shelfLayerLeft = a.getResourceId(R.styleable.ShelfLayer_shelfLayerLeft,-1);
		int shelfLayerCenter = a.getResourceId(R.styleable.ShelfLayer_shelfLayerCenter,-1);
		int shelfLayerRight = a.getResourceId(R.styleable.ShelfLayer_shelfLayerRight,-1);
		a.recycle();
		mBackgroundCenter = context.getResources().getDrawable(shelfLayerCenter);
		mBackgroundLeft = context.getResources().getDrawable(shelfLayerLeft);
		mBackgroundRight = context.getResources().getDrawable(shelfLayerRight);
	}

	@Override
	protected void dispatchDraw(Canvas canvas){

		int count = getChildCount();
		if(count > 0){
			int width = getWidth();
			int height = getHeight();
			int childHeight = getChildAt(0).getHeight();


//			Log.d(TAG, " dispatchDraw ..."+getChildAt(0).getWidth()+" : "+getChildAt(0).getHeight()+
//					"columnNum : "+width/getChildAt(0).getWidth()+" height = "+height);


			if (mBackgroundCenter != null && mBackgroundLeft != null &&  mBackgroundRight != null) {
				int rows = height / childHeight +1;
				for(int i = 0; i < rows+1; i++){
					final int scrollX = getScrollX();
					final int scrollY = getScrollY();
					mBackgroundLeft.setBounds(0, getChildAt(0).getTop()+i*getChildAt(0).getHeight(), 
							LEFT_AND_RIGHT, getChildAt(0).getBottom()+i*getChildAt(0).getHeight());

					mBackgroundCenter.setBounds(LEFT_AND_RIGHT, getChildAt(0).getTop()+i*getChildAt(0).getHeight(), 
							width-LEFT_AND_RIGHT, getChildAt(0).getBottom()+i*getChildAt(0).getHeight());

					mBackgroundRight.setBounds(width-LEFT_AND_RIGHT, getChildAt(0).getTop()+i*getChildAt(0).getHeight(), 
							width, getChildAt(0).getBottom()+i*getChildAt(0).getHeight());

					if ((scrollX | scrollY) == 0) {
						mBackgroundLeft.draw(canvas);
						mBackgroundCenter.draw(canvas);
						mBackgroundRight.draw(canvas);
					} else {
						canvas.translate(scrollX, scrollY);
						mBackgroundLeft.draw(canvas);
						mBackgroundCenter.draw(canvas);
						mBackgroundRight.draw(canvas);
						canvas.translate(-scrollX, -scrollY);
					}
				}
			}
		}
		
		super.dispatchDraw(canvas);
	}


}
