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

	private int columnNum;
	private int shelfLayerHeadHeight;;
	private Bitmap rowBackground;
	private Drawable mBackground;


	public BookGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//获得列数
		columnNum = attrs.getAttributeIntValue(NAMESPACE_ANDROID,"numColumns",3);
		//获取自定义属性
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShelfLayer);
		shelfLayerHeadHeight = a.getDimensionPixelSize(R.styleable.ShelfLayer_shelfLayerHeadHeight,50);
		int shelfLayerLeft = a.getResourceId(R.styleable.ShelfLayer_shelfLayerLeft,-1);
		int shelfLayerCenter = a.getResourceId(R.styleable.ShelfLayer_shelfLayerCenter,-1);
		int shelfLayerRight = a.getResourceId(R.styleable.ShelfLayer_shelfLayerRight,-1);
		a.recycle();
		rowBackground = BitmapFactory.decodeResource(getResources(),shelfLayerCenter);
		mBackground = context.getResources().getDrawable(shelfLayerCenter);
	}

	@Override
	protected void dispatchDraw(Canvas canvas){


		int count = getChildCount();
		int width = getWidth();
		int height = getHeight();

		//		for (int y = top; y < height; y += backgroundHeight) {
		//			for (int x = 0; x < width; x += backgroundWidth) {
		//				canvas.drawBitmap(rowBackground, x, y, null);
		//			}
		//		}

		Log.d(TAG, " dispatchDraw ..."+getChildAt(0).getWidth()+" : "+getChildAt(0).getHeight()+
				"columnNum : "+width/getChildAt(0).getWidth()+" height = "+height);


		final Drawable background = mBackground;
		if (background != null) {
			int lines  = width/getChildAt(0).getWidth();
			int rows = count / lines+1;
			for(int i = 0; i < rows; i++){
				final int scrollX = getScrollX();
				final int scrollY = getScrollY();

				background.setBounds(0, getChildAt(0).getTop()+i*getChildAt(0).getHeight(), 
						width, getChildAt(0).getBottom()+i*getChildAt(0).getHeight());

//				if ((scrollX | scrollY) == 0) {
					background.draw(canvas);
//				} else {
//					canvas.translate(scrollX, scrollY);
//					background.draw(canvas);
//					canvas.translate(-scrollX, -scrollY);
//				}
			}
		}

		super.dispatchDraw(canvas);
	}


}
