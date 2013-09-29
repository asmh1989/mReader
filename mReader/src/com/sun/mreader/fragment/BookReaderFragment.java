package com.sun.mreader.fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelXorXfermode;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.mreader.R;
import com.sun.mreader.database.BookChaptersDBTask;
import com.sun.mreader.database.BookDBTask;
import com.sun.mreader.database.BookTable;
import com.sun.mreader.fragment.BookReaderFragment.pageView.onePage;
import com.sun.mreader.mt.BookChapter;
import com.sun.mreader.mt.MtBookUtil;
import com.sun.mreader.mt.MtParser;
import com.sun.mreader.ui.AutoBreakTextView;
import com.sun.mreader.utils.BCConvert;
import com.sun.mreader.utils.GlobalContext;
import com.sun.mreader.utils.Log;

@SuppressLint({ "ValidFragment", "NewApi" })
public class BookReaderFragment extends Fragment {

	private static final String TAG = "SUNBookReaderFragment";

	private ViewPager mViewPager;
	private BookPagerAdapter mBookPagerAdapter;
	private pageView mPagesView;
	private List<BookChapter> mBookChapters;

	private String mBookId;
	private MtBookUtil  mBookUtil;
	private String mCurrentRead;

	private String bookChars;

	private ProgressDialog mProgressDialog;

	private boolean mFirstSlectItem = true;

	private page mOnePage = new page();

	private int mCurrentSeletItem = 601;

	class page {
		public int width;
		public int height;
		public int leftmargin = 10;
		public int rightmargin = 10;
		public int topmargin = 8;
		public int bottommargin;
		public int rowmargin;
		public int lines;      //行数
		public int rowWidth;       //文本列的宽度
		public int bottomViewHeight;
		public int topviewHeight;
		public int bodyMargin;
		private TextPaint Charfont;
		private int fontStyle = 0;
		private String fontName = "MONOSPACE";
		private float fontSize;
		private int scaleX = 1;
		private int scaleY = 1;
		private int fontZhWidth;
		private int fontEnWidth;

		public void setScale(int x, int y){
			scaleX = x;
			scaleY = y;
		}

		private chapter [] mChapters = new chapter [3];

		class chapter{
			public String originContent;
			public List<String> onePageString = new ArrayList<String>();
			public int total;
			public int whichChapter = -1;;
			public int location;
			public boolean needFormat = true;
		}


		public page(){
			for (int i = 0; i < mChapters.length ; i++){
				mChapters[i] = new chapter();
			}
		}

		/** 只有在这个函数中才会formatChapter
		 * @param content
		 * @param s
		 * @param chapterNum
		 */
		public void addChapter(String content, STATE s, int chapterNum){
			int loc = s.ordinal();
			if(mChapters[loc].whichChapter != chapterNum){
				mChapters[loc].needFormat = true;
			}
			mChapters[loc].whichChapter = chapterNum;
			mChapters[loc].originContent = content;
			if(lines > 0 && mChapters[loc].needFormat){
				formatChapter(mChapters[loc]);
			}
		}

		public String getOnePageString(int  item){
			String str ="";
			int c = getCurrentChapter(mCurrentRead);
			int whichPage = getCurrentPageLoc(mCurrentRead);
			chapter ch = getChapter(STATE.CURRENT);
			STATE s = getStateFromItem(item);
			if(whichPage == ch.total  && s == STATE.NEXT){
				whichPage = 0;
				c = c + 1;
				ch = getChapter(s);
			} else if( whichPage == 1 && s == STATE.PREV){
				c = c - 1;
				ch = getChapter(s);
				whichPage = whichPage -1;

			}  else if(s != STATE.CURRENT){
				whichPage = (s == STATE.PREV) ? whichPage - 1: whichPage + 1;
			}
			if(checkChapterIsDownload(c, s)){
				if(whichPage  == 0){
					whichPage = ch.total;
				}
				Log.d(TAG, "getOnePageString : has loading chapter = "+c+" s = "+s+" whichPage = "+whichPage);

				str = ch.onePageString.get(whichPage - 1);

				return str;
			} else {
				return null;
			}
		}

		private chapter getChapter(STATE s){
			int loc = s.ordinal();
			if(mChapters[loc] == null){
				mChapters[loc] = new chapter();
			}

			return mChapters[loc];
		}

		public CharSequence getWhichpageNow(int item) {
			int c = getCurrentChapter(mCurrentRead);
			int whichPage = getCurrentPageLoc(mCurrentRead);
			chapter ch = getChapter(STATE.CURRENT);
			STATE s = getStateFromItem(item);
			if(whichPage == ch.total  && s == STATE.NEXT){
				return "1/"+getChapter(s).total;
			} else if(whichPage == 1 && s == STATE.PREV){
				int w = getChapter(s).total;
				return w +"/"+w;
			}  else if(s != STATE.CURRENT){
				whichPage = (s == STATE.PREV) ? whichPage - 1: whichPage + 1;
				return whichPage+"/"+ch.total;
			}

			return whichPage+"/"+ch.total;
		}

		public void formatChapter(chapter ch){
			int th = 0;
			String str = ch.originContent;
			String [] paragraph = str.split("\n");
			int length = paragraph.length;
			int offset = 0;
			ch.onePageString.clear();
			while(th < length){
				String onePageStr = "";
				String str2 ="";
				for(int i = 0; i < lines && th < length; i++){
					//					String tmpStr = BCConvert.bj2qj(paragraph[th].substring(offset)); //半角转全角
					String tmpStr = paragraph[th].substring(offset);
					int off = getOneLineStringLen(tmpStr);
					if(off == 0){
						th++;
						str2 = tmpStr;
						offset = 0;
					} else {
						str2 = tmpStr.substring(0, off);
						while(getTextPaint().measureText(str2) > getRowsLen()){
							off--;
							str2 = tmpStr.substring(0, off);
						}
						offset += off;

					}

//										Log.d(TAG,  " i = "+i+"offset = "+offset+" th = "+th +" lines = "+ str2);
					//					Log.d(TAG, " length = "+getTextPaint().measureText(str2));

					if(i + 1 == lines){
						onePageStr += str2;
					} else {
						onePageStr += str2+"\n";
					}

				}

				ch.onePageString.add(onePageStr);
			}

			ch.needFormat = false;
			ch.total = ch.onePageString.size();
		}

		public boolean isChinese(char c) {  
			Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);  
			if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS  
					|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS  
					|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A  
					|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION  
					|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION  
					|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {  
				return true;  
			}  
			return false;  
		}  

		private int getOneLineStringLen(String str) {
			int paint = 0;
			int haswidth = getRowsLen();
			String str2 = str;
			int th = 0;

			while(true){
				String tmpStr ;
				if(str2.length() > 128){
					tmpStr = str2.substring(0, 128);
					str2 = str2.substring(128);
				} else {
					tmpStr = str2;
					str2 = "";
				}
				if(tmpStr.length() == 0){
					return 0;
				}
				for(int i = 0; i < tmpStr.length(); i++){
					char c = tmpStr.charAt(i);
					if(th > 20){
						stop();
					}
					if(isChinese(c)){
						paint += GetZNFontInfo();
					} else {
						paint += GetENFontInfo();
					}

					if(paint > haswidth){
						return th;
					}
					paint += scaleX;
					th++;
				}
			}
		}

		private void stop(){

		}

		public int calculateLines(){
			return lines = (height - bottomViewHeight - topviewHeight - bodyMargin) / (getFontHeight()+scaleY);
		}

		public int getRowsLen(){
			if(rowWidth == 0){
				return rowWidth = (width - leftmargin - rightmargin);
			} else {
				return rowWidth;
			}
		}


		public  float [] setLineSpace(){
			if (Charfont == null){
				Charfont=getTextPaint();
			}
			FontMetrics fm = Charfont.getFontMetrics();  

			float fFontHeight = (float)Math.ceil(fm.descent - fm.ascent);  
			float fLineHeight = (float)(height - bottomViewHeight - topviewHeight - bodyMargin) / lines;
			float fMulValue;
			float fAddValue;
			if(fFontHeight > fLineHeight){  
				fMulValue = fLineHeight / fFontHeight;  
				fAddValue = -1;  
			} else{  
				fMulValue = 1;  
				fAddValue = fLineHeight - fFontHeight;  
			}  
			return new float []{fAddValue, fMulValue};  
		}
		public TextPaint getTextPaint(){
			if (Charfont == null){
				Charfont=new TextPaint();
				if (fontName.equals("MONOSPACE"))
					Charfont.setTypeface(Typeface.create(Typeface.MONOSPACE,getVTFontStyle()));
				else if (fontName.equals("SANS_SERIF"))
					Charfont.setTypeface(Typeface.create(Typeface.SANS_SERIF, getVTFontStyle()));
				else if (fontName.equals("SERIF"))
					Charfont.setTypeface(Typeface.create(Typeface.SERIF, getVTFontStyle()));
				else
					Charfont.setTypeface(Typeface.create(fontName, getVTFontStyle()));
				Charfont.setTextSize(fontSize);
				Charfont.setAntiAlias(true);
				//				Charfont.setXfermode(new PixelXorXfermode(7));
				//_VTFont.setTextScaleX(1.0f); //设置文本绽放倍数，默认为1
				return Charfont;
			}
			else
				return Charfont;
		}

		public int GetENFontInfo(){
			if(fontEnWidth == 0){
				Rect cfontrect =  new Rect();
				getTextPaint().getTextBounds("A", 0, 1, cfontrect);
				fontEnWidth = cfontrect.width();
			}
			return fontEnWidth;
		}

		private int GetZNFontInfo(){
			if(fontZhWidth == 0){
				Rect cfontrect =  new Rect();
				getTextPaint().getTextBounds("辉", 0, 1, cfontrect);
				fontZhWidth = cfontrect.width();
			} 
			return fontZhWidth;
		}

		private int getVTFontStyle(){
			if (fontStyle == 1)
				return Typeface.BOLD;
			else if (fontStyle == 2)
				return Typeface.ITALIC;
			else
				return Typeface.NORMAL;
		}

		private int getFontHeight(){  
			TextPaint paint = getTextPaint();
			FontMetrics fm = paint.getFontMetrics();  
			return (int) Math.ceil(fm.descent - fm.ascent);  
		}

		public int fillScreen() {
			Log.d(TAG, "calculate other height = "+(height - bottomViewHeight - topviewHeight - bodyMargin - lines*getFontHeight()));
			return height - bottomViewHeight - topviewHeight - bodyMargin - lines*getFontHeight();
		}

		/** 章节的页数从1开始， 0代表到头了 
		 * @param s
		 * @param lastread
		 * @return
		 */
		public String getNextLoctionPage(STATE s, String lastread) {
			int c = getCurrentChapter(lastread);
			int loc = getCurrentPageLoc(lastread);
			//			int loc = mChapters[1].location +1;
			switch (s) {
			case PREV:
				if(loc == 1){
					if(c == 0){			//已经是第一章了
						return "0";
					} else{
						if(mChapters[0].total == 0){
							return (c-1)+":"+"0";
						}
						return (c-1)+":"+(mChapters[0].total);
					}
				} else 
					return c+":"+(loc-1);
			case NEXT:
				if(loc == mChapters[1].total){
					if(c == mBookUtil.getBookChapters() - 1){
						return (mBookUtil.getBookChapters())+"";
					} else {
						return (c+1)+":"+"1";
					}
				} else {
					return c+":"+(loc+1);
				}
			}
			return null;
		}

		public void setPaint(TextPaint paint) {
			Charfont = paint;
		}

		public int getChapterOfAdater(int item){
			int c = getCurrentChapter(mCurrentRead);
			int loc = getCurrentPageLoc(mCurrentRead);
			STATE s = getStateFromItem(item);

			Log.d("SUNMM", "getChapterOfAdater ： item = "+item+" pageState = ");

			if(loc == 1 && s == STATE.PREV){
				c = c - 1;
			} else if(s == STATE.NEXT){
				if(mChapters[STATE.CURRENT.ordinal()] != null && mChapters[STATE.CURRENT.ordinal()].total == loc)
					c = c + 1; 
			}
			return c;
		}

		public boolean checkContent(int item) {
			int c = getChapterOfAdater(item);
			STATE s = getStateFromItem(item);

			return  checkChapterIsDownload(c, s);
		}

		public void checkChapters(final STATE s, String last) {
			int c = getCurrentChapter(last);
			final int c2 = getChapterOfAdater(mCurrentSeletItem);
			if(c2 != c){
				Log.d(TAG, "need load chapters = "+last+ " s = "+s);
				checkChapterIsDownload(c2, s);
			}
		}

		public boolean hasThisChapter(int l, STATE s) {
			for(int i = 0; i < 3; i++){
				if(mChapters[i] != null && mChapters[i].whichChapter == l){
					return true;
				}
			}
			return false;
		}

		public void switchChapters(STATE s) {
			if(s == STATE.PREV){
				mChapters[2] = mChapters[1];
				mChapters[1] = mChapters[0];
				if (mChapters[0] != null){
					mChapters[0].whichChapter = -1;
				}
			} else if(s == STATE.NEXT){
				mChapters[0] = mChapters[1];
				mChapters[1] = mChapters[2];
				if (mChapters[2] != null){
					mChapters[2].whichChapter = -1;
				}
			}
		}
	}

	public STATE getStateFromItem(int item){
        STATE s = STATE.CURRENT;
        if(item == mCurrentSeletItem - 1){
            s = STATE.PREV;
        } else if(item == mCurrentSeletItem + 1){
            s = STATE.NEXT;
        }
        return s;
	}
	private static final int LOAD_NEW_PAGE = 0;

	private Handler mhandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_NEW_PAGE:
				STATE s = (STATE) msg.obj;
				if(mFirstSlectItem){
					mFirstSlectItem = false;
				} else {
					String tmp =  mOnePage.getNextLoctionPage(s, mCurrentRead);
					int prev = getCurrentChapter(mCurrentRead);
					int now = getCurrentChapter(tmp);
					if(prev != now){
						mOnePage.switchChapters(s);
					}
					mCurrentRead = tmp;
				}
				Log.d(TAG, "LOAD_NEW_PAGE mCUrrent = "+mCurrentRead+" s ="+s);
				mOnePage.checkChapters(s, mCurrentRead);
//				mBookPagerAdapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBookId = getArguments().getString(BookTable.BOOK_ID);
		mBookChapters = BookChaptersDBTask.getBookChapters(mBookId);
		mBookUtil = BookDBTask.getBook(mBookId);
		mCurrentRead = mBookUtil.getBookLastRead();
		Log.d(TAG, "mCurrentRead = "+mCurrentRead+" chapters = "+mBookUtil.getBookChapters());
		mCurrentRead = "2:1";

		mProgressDialog = new ProgressDialog(getActivity());
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		mOnePage.width = display.getWidth();
		mOnePage.height = display.getHeight();
		final float density = getActivity().getResources().getDisplayMetrics().density;
		int h = (int)(getActivity().getResources().getDimension(R.dimen.page_small_text_height));
		int left = (int)(getActivity().getResources().getDimension(R.dimen.page_left_right));
		int top = (int)(getActivity().getResources().getDimension(R.dimen.page_top_bottom));
		mOnePage.bodyMargin = (int)(getActivity().getResources().getDimension(R.dimen.page_body_margin));
		mOnePage.bottomViewHeight = mOnePage.topviewHeight  =h+top;
		mOnePage.leftmargin = mOnePage.rightmargin = left;
		mOnePage.topmargin = mOnePage.bottommargin = top;

		mOnePage.fontSize = getResources().getDimension(R.dimen.body_text_size);
		int lines  = mOnePage.calculateLines();
		Log.d(TAG, "width = "+mOnePage.width+" hegiht = "+mOnePage.height+" density = "+density+" h = "+h+" fontsize = "
				+mOnePage.fontSize+" lines = "+lines);


		checkChapterIsDownload(getCurrentChapter(mCurrentRead), STATE.CURRENT);
	}

	private int getCurrentChapter(String s){
		if(s.contains(":")){
			return Integer.parseInt(s.split(":")[0]);
		} 
		return Integer.parseInt(s);
	}

	private int getCurrentPageLoc(String s){
		if(s.contains(":")){
			return Integer.parseInt(s.split(":")[1]);
		} 
		return 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.book_reader_fragment, container, false);
		mViewPager = (ViewPager) v.findViewById(R.id.viewpager); 
		mBookPagerAdapter = new BookPagerAdapter();
		mViewPager.setAdapter(mBookPagerAdapter);

		mViewPager.setOnPageChangeListener(new myOnPageChangeListener());
		mViewPager.setCurrentItem(mCurrentSeletItem);

		mPagesView = new pageView(mCurrentSeletItem);

		mBookPagerAdapter.notifyDataSetChanged();
		return v;
	}

	private boolean hasNextPage() {
		//		int c = getCurrentChapter(mCurrentRead);
		//		int w = getCurrentPageLoc(mCurrentRead);
		//		if(c == mBookUtil.getBookChapters()){
		//			return false;
		//		}
		return true;
	}

	private boolean hasPrevPage() {

		//		int c = getCurrentChapter(mCurrentRead);
		//		int w = getCurrentPageLoc(mCurrentRead);
		//		if(c == 0 && w == 0){
		//			return false;
		//		}
		return true;
	}

	private boolean checkChapterIsDownload(int l, STATE s) {
		BookChapter b = mBookChapters.get(l);
		String path = GlobalContext.getPath()+"/"+mBookUtil.getBookName().trim()+"/"+b.get_ID();
		if(mOnePage.hasThisChapter(l, s)){
			return true;
		}
		//		Log.d(TAG, "will open : "+path);
		if(b.getIsDownload() && (new File(path).exists()) ){
			bookChars = getChapterContentFromFiles(b.get_ID(), mBookUtil.getBookName().trim());
			mOnePage.addChapter(bookChars, s, l);
			Log.d(TAG, "checkChapterIsDownload ... has load chapter = "+ l+" s = "+s);
			return true;
		} else {
			mProgressDialog.setMessage(getActivity().getResources().getString(R.string.loading));
			if(s == STATE.CURRENT){
				mProgressDialog.show();
			}

			new GetMtBookChapterContentTask(b,s,l).execute();
			Log.d(TAG, "checkChapterIsDownload ... start  download chapter = "+ l+" s = "+s);
			return false;
		}
	}

	public String getChapterContentFromFiles(String name, String bookname){
		String content = null;
		FileInputStream inStream;
		try {
			//			Log.d(TAG, "file = "+GlobalContext.getPath()+"/"+bookname+"/"+name);
			inStream = new FileInputStream(GlobalContext.getPath()+"/"+bookname+"/"+name);
			ByteArrayOutputStream stream=new ByteArrayOutputStream();
			byte[] buffer=new byte[1024];
			int length=-1;
			while((length=inStream.read(buffer))!=-1)   {
				stream.write(buffer,0,length);
			}
			stream.close();
			inStream.close();
			content = stream.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	class BookPagerAdapter extends PagerAdapter{

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			//			Log.d(TAG, "destroyItem arg2 = "+ (View)arg2);
			((ViewPager) arg0).removeView((View)arg2);
		}

		@Override
		public int getCount() {
			//			Log.d(TAG, "BookPagerAdapter getCount() = "+mPagesView.size());
			return Integer.MAX_VALUE;
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			STATE s = getStateFromItem(arg1);
			View v =mPagesView.getPageView(s, arg1);

			((ViewPager) arg0).addView(v, 0);
			//			Log.d(TAG, "instantiateItem : s = "+s+" v = "+v +" count = "+((ViewPager) arg0).getChildCount());


			return v;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void finishUpdate(View arg0) {

		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}

	}

	class ViewAdapter extends BaseAdapter{
		private LayoutInflater mInflater;
		private int item;

		public ViewAdapter(int i){
			mInflater = LayoutInflater.from(getActivity());
			item = i;
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.reader_page, parent, false);
			}

			TextView chapterTitle = (TextView)convertView.findViewById(R.id.tv_title);
			AutoBreakTextView chapterBoby = (AutoBreakTextView)convertView.findViewById(R.id.tv_body);
			TextView battery = (TextView) convertView.findViewById(R.id.tv_battery);
			TextView time = (TextView) convertView.findViewById(R.id.tv_time);
			TextView pageNumber = (TextView) convertView.findViewById(R.id.tv_page_number);
			LinearLayout loading = (LinearLayout) convertView.findViewById(R.id.loading);

			battery.setText("99");
			time.setText("23:12");
			pageNumber.setText("1/10");

			chapterBoby.setTextSize(TypedValue.COMPLEX_UNIT_PX, mOnePage.fontSize);

			if(mOnePage.checkContent(item)){
				chapterBoby.setVisibility(View.VISIBLE);
				loading.setVisibility(View.GONE);
				chapterTitle.setText(mBookChapters.get(mOnePage.getChapterOfAdater(item)).getBookChapter());
				chapterBoby.setLines(mOnePage.calculateLines());
				mOnePage.setScale((int)chapterBoby.getScaleX(),(int)chapterBoby.getScaleY());
				chapterBoby.setMywidth(mOnePage.getRowsLen(), chapterBoby.getScaleX(), chapterBoby.getScaleY());
				String out = mOnePage.getOnePageString(item);
				if(out != null){
					chapterBoby.setText(out, mOnePage.getTextPaint());
				} else {
					chapterBoby.setText("...", mOnePage.getTextPaint());
				}
				pageNumber.setText(mOnePage.getWhichpageNow(item));
			} else {
				chapterBoby.setVisibility(View.INVISIBLE);
				loading.setVisibility(View.VISIBLE);
			}

            float [] f = mOnePage.setLineSpace();
            chapterBoby.setLineSpacing(f[0], f[1]);
            
			battery.setVisibility(View.VISIBLE);
			time.setVisibility(View.VISIBLE);
			pageNumber.setVisibility(View.VISIBLE);

			return convertView;
		}

	}

	private class myOnPageChangeListener implements OnPageChangeListener {

		private static final boolean DEBUG = true;
		private int changed;
		private int scrolled;
		public void onPageScrollStateChanged(int arg0) {
			//			Log.d(TAG, "onPageScrollStateChanged  arg0 = "+arg0);
			changed = arg0;
		}

		// 第一个View滑动前调用
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			scrolled = arg0;
			//			Log.d(TAG, "onPageScrolled  arg0 = "+arg0+" arg1 = "+arg1+ " arg2 = "+arg2);
		}

		// 新View被加载后调用
		public void onPageSelected(int arg0) {
			// 判断当前View是否为倒数第一个View
//			Log.d(TAG, " onPageSelected : "+ arg0 +" changed = "+changed +" scrolled = "+scrolled);
			if (arg0+1>1) {
				Message ms = mhandler.obtainMessage();
				ms.what = LOAD_NEW_PAGE;
				ms.obj = (arg0 > scrolled) ? STATE.NEXT : STATE.PREV;
				mCurrentSeletItem = arg0;
				ms.sendToTarget();
			}
		}
	}

	class GetMtBookChapterContentTask extends AsyncTask<String,Integer,String> {
		BookChapter chapter;
		STATE state;
		int ChapterNum;
		public GetMtBookChapterContentTask(BookChapter b, STATE s, int l) {
			chapter = b;
			state = s;
			ChapterNum = l;
		}

		@Override  
		protected String  doInBackground(String... params) { 
			publishProgress(0); 
			try {
				String s = new MtParser().getBookChapterContent(chapter.getBookChapterUrl());
				return s; 
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}  

		protected void onProgressUpdate(Integer... progress) { 
			mProgressDialog.setProgress(progress[0]); 
		}  

		protected void onPostExecute(String result) { 
			if(result != null) {  
				bookChars = result;
				mProgressDialog.dismiss();
				chapter.setIsDownload(true);
				GlobalContext.saveContent(result, chapter.get_ID(), mBookUtil.getBookName());
				BookChaptersDBTask.updateBookChapterForDownload(chapter);
				mOnePage.addChapter(bookChars, state, ChapterNum);
				mPagesView.getAdapter(STATE.CURRENT).notifyDataSetChanged();
			} else {
				Toast.makeText(getActivity(), "ERROR!!", Toast.LENGTH_SHORT).show();
			}
		}  

		protected void onPreExecute () {//在 doInBackground(Params...)之前被调用，在ui线程执行  
			mProgressDialog.setProgress(0);//进度条复位  
		}  

		protected void onCancelled () {//在ui线程执行  
			mProgressDialog.setProgress(0);//进度条复位  
		}  
	}  

	class pageView{
		class onePage{
			public View v;
			public ViewAdapter adpater;
		}

		public int currentItem;
		private onePage prevPage;
		private onePage currentPage;
		private onePage nextPage;

		public pageView(int cur){
			currentItem = cur % 3;
		}

		public ViewAdapter getAdapter(STATE s) {
			switch (s) {
			case PREV:
				return prevPage.adpater;
			case CURRENT:
				return currentPage.adpater;
			case NEXT:
				return nextPage.adpater;
			}
			return null;
		}

		public View getPageView(STATE s, int item){
			int which = s.ordinal();
			View v = null;

			onePage p = addPages(item);
			if(which == currentItem){
				return (currentPage = p).v;
			}
			switch (currentItem) {
			case 0:
				v = (which == 1) ? (nextPage = p).v : (prevPage = p).v;
				break;
			case 1:
				v = (which == 2) ? (nextPage = p).v : (prevPage = p).v;
				break;
			case 2:
				v = (which == 0) ? (nextPage = p).v : (prevPage = p).v;
				break;
			}

			return v;
		}

		private onePage addPages(int item) {
			GridView gd = new GridView(getActivity());
			ViewAdapter Adapter = new ViewAdapter(item);
			gd.setAdapter(Adapter);
			gd.setNumColumns(1);
			gd.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			gd.setBackgroundColor(Color.TRANSPARENT);
			gd.setCacheColorHint(Color.TRANSPARENT);
			gd.setSelector(new ColorDrawable(Color.TRANSPARENT));
			gd.setPadding(mOnePage.leftmargin, mOnePage.topmargin, mOnePage.leftmargin, mOnePage.bottommargin);

			gd.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Toast.makeText(getActivity(), mViewPager.getCurrentItem()+"", Toast.LENGTH_SHORT).show();
				}
			});

			onePage p = new onePage();
			p.v = gd;
			p.adpater = Adapter;
			return p;
		}
	}

	public static enum STATE{
		PREV, CURRENT,NEXT
	}
}
