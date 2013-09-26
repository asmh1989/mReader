package com.sun.mreader.ui;

import java.util.Vector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;

import com.sun.mreader.utils.Log;

public class AutoBreakTextView extends TextView {
	public static int m_iTextHeight; // 文本的高度
	public static int m_iTextWidth;// 文本的宽度
	private Paint mPaint = null;
	private String string = "";
	private int LineSpace = 0;// 行间距
	
	public AutoBreakTextView(Context context, AttributeSet set) {
		super(context, set);
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		manager.getDefaultDisplay().getMetrics(dm);
		float textSize = this.getTextSize();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(textSize);
		mPaint.setColor(Color.GRAY);
	}
	
	public void setMywidth(int x){
		m_iTextWidth = x;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		char ch;
		int w = 0;
		int istart = 0;
		int m_iFontHeight;
		int m_iRealLine = 0;
		Vector m_String = new Vector();
		FontMetrics fm = mPaint.getFontMetrics();
		m_iFontHeight = (int) Math.ceil(fm.descent - fm.top) + (int) LineSpace;// 计算字体高度（字体高度＋行间距）
		for (int i = 0; i < string.length(); i++) {
			ch = string.charAt(i);
			float[] widths = new float[1];
			String srt = String.valueOf(ch);
			mPaint.getTextWidths(srt, widths);
			if (ch == '\n') {
				m_iRealLine++;
				m_String.addElement(string.substring(istart, i));
				istart = i + 1;
				w = 0;
			} else {
				w += (int) (Math.ceil(widths[0]));
				if (w > m_iTextWidth) {
					m_iRealLine++;
					m_String.addElement(string.substring(istart, i));
					istart = i;
					i--;
					w = 0;
				} else {
					if (i == (string.length() - 1)) {
						m_iRealLine++;
						m_String.addElement(string.substring(istart,
								string.length()));
					}
				}
			}
		}
//		canvas.s(m_iTextWidth, m_iTextHeight);
		for (int i = 0, j = 1; i < m_iRealLine; i++, j++) {
			canvas.drawText((String) (m_String.elementAt(i)), 0, 0
					+ m_iFontHeight * j, mPaint);
		}
	}
	
	public void setText(String text) {
		string = text;
		invalidate();
		requestLayout();
	}
}