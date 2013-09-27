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
//		for (int i = 0; i < string.length(); i++) {
//			ch = string.charAt(i);
//			float[] widths = new float[1];
//			String srt = String.valueOf(ch);
//			mPaint.getTextWidths(srt, widths);
//			if (ch == '\n') {
//				m_iRealLine++;
//				m_String.addElement(string.substring(istart, i));
//				istart = i + 1;
//				w = 0;
//			} else {
//				w += (int) (Math.ceil(widths[0]));
//				if (w > m_iTextWidth) {
//					m_iRealLine++;
//					m_String.addElement(string.substring(istart, i));
//					istart = i;
//					i--;
//					w = 0;
//				} else {
//					if (i == (string.length() - 1)) {
//						m_iRealLine++;
//						m_String.addElement(string.substring(istart,
//								string.length()));
//					}
//				}
//			}
//		}
//		canvas.s(m_iTextWidth, m_iTextHeight);
		String [] lineStr = null;
		if(string.contains("\n")){
			lineStr = string.split("\n");
		} else {
			lineStr = new String [1];
			lineStr[0] = string;
		}
		Log.d("SUNMM", "found lines = "+ lineStr.length);
		int length = lineStr.length;
		float height = 0.0F;
		for (int i = 0, j = 1; i < length; i++, j++) {
			int will = (int)mPaint.measureText(lineStr[i]);
			if(m_iTextWidth - will < (int)mPaint.measureText("我")){
				scaleX = m_iTextWidth - will;
			} else {
				scaleX = 0;
			}
			String str = lineStr[i];
//			canvas.drawText(mSpannableFactory.newSpannable(str).toString(), 0, scaleY*(j-1)
//					+ m_iFontHeight * j, mPaint);
			
			canvas.drawText(str, 0, str.length(), 0, scaleY*(j-1)+m_iFontHeight * j, mPaint);
			
//			canvas.translate(0, height);
//			StaticLayout layout = new StaticLayout(string,mPaint,m_iTextWidth,Alignment.ALIGN_NORMAL,scaleY,0.0F,true); 
//			height += layout.getHeight();
//			layout.draw(canvas);
		}
	}
	
	
	public void setText(String text, TextPaint p) {
		mPaint = p;
		string = text;
		invalidate();
		requestLayout();
	}
}