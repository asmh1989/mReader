package com.sun.mreader.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;

import com.sun.mreader.utils.Log;

public class AutoBreakTextView extends TextView {
	public static int m_iTextHeight; // 文本的高度
	public static int m_iTextWidth;// 文本的宽度
	private TextPaint mPaint = null;
	private String string = "";
	private int LineSpace = 0;// 行间距
	private float scaleX = 1.0F;
	private float scaleY = 1.0F;
	
	
	public AutoBreakTextView(Context context, AttributeSet set) {
		super(context, set);
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		manager.getDefaultDisplay().getMetrics(dm);
	}
	
	public void setMywidth(int w, float x, float y){
		m_iTextWidth = w;
		scaleX = x;
		scaleY = y;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
//		super.onDraw(canvas);
		int m_iFontHeight;
		FontMetrics fm = mPaint.getFontMetrics();
		m_iFontHeight = (int) Math.ceil(fm.descent - fm.ascent);// 计算字体高度（字体高度＋行间距）
		
		String [] lineStr = null;
		if(string.contains("\n")){
			lineStr = string.split("\n");
		} else {
			lineStr = new String [1];
			lineStr[0] = string;
		}
		int length = lineStr.length;
		for (int i = 0, j = 1; i < length; i++, j++) {
			String str = lineStr[i];
			int will = (int)mPaint.measureText(str);
			if(m_iTextWidth - will < (int)mPaint.measureText("我")){
				scaleX = (m_iTextWidth - will)*1.0F /(str.length() - 1);
			} else {
				scaleX = 0;
			}

			if(scaleX > 0){
				for(int s = 0; s < str.length(); s++){
					String c = str.charAt(s)+"";
					float x = scaleX;
					if(s == 0){
						x = 0;
					}
					canvas.drawText(c, s*x+mPaint.measureText(str.substring(0, s)), scaleY*(j-1)+m_iFontHeight * j, mPaint);
				}
			} else {
				canvas.drawText(str, 0, scaleY*(j-1)+m_iFontHeight * j, mPaint);
			}
		}
	}
	
	
	public void setText(String text, TextPaint p) {
		mPaint = p;
		string = text;
		invalidate();
		requestLayout();
	}
}