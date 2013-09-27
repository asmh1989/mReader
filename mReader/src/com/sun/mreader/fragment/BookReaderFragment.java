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
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.mreader.R;
import com.sun.mreader.database.BookChaptersDBTask;
import com.sun.mreader.database.BookDBTask;
import com.sun.mreader.database.BookTable;
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
	private Queue<pageView> vieww;
	private int mPageSize = 3; //前中后
	private List<BookChapter> mBookChapters;

	private String mBookId;
	private MtBookUtil  mBookUtil;
	private String mCurrentRead;

	private String bookChars;

	private ProgressDialog mProgressDialog;

	private boolean mFirstSlectItem = true;

	private page mOnePage = new page();


	public BookReaderFragment() {
	}



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
		private int scaleX;
		private int scaleY;

		public void setScale(int x, int y){
			scaleX = x;
			scaleY = y;
		}

		private chapter [] mChapters = new chapter [3];

		class chapter{
			public String originContent;
			public List<String> onePageString = new ArrayList<String>();
			public int total;
			public int readNow;
			public int location;
			public boolean needFormat = true;
		}

		public void addChapter(String content, STATE s){
			int loc = s.ordinal();
			if(mChapters[loc] == null){
				mChapters[loc] = new chapter();
			}
			mChapters[loc].originContent = content;
			if(lines > 0){
				formatChapter(mChapters[loc]);
			}
		}

		public String getOnePageString(STATE s){
			String str ="";
			int c = getCurrentChapter(mCurrentRead);
			int l = getCurrentPageLoc(mCurrentRead);
			chapter ch;
//			if(c == 0 && l == 0){
//				ch = mChapters[0];
//			} else {
				ch = mChapters[1];
//			}

			if(ch == null){
				return "";
			}

			if(ch.needFormat){
				formatChapter(s);
			}
			switch (s){
			case NEXT:

				break;
			case PREV:

				break;
			case CURRENT:
				str = ch.onePageString.get(l);
				break;
			}

			return str;
		}

		public void formatChapter(chapter ch){
			int th = 0;
			String str = ch.originContent;
			String [] paragraph = str.split("\n");
			int length = paragraph.length;
			int offset = 0;
			while(th < length){
				String onePageStr = "";
				String str2 ="";
				for(int i = 0; i < lines && th < length; i++){
					String tmpStr = BCConvert.bj2qj(paragraph[th].substring(offset));
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

					Log.d(TAG,  " i = "+i+"offset = "+offset+" th = "+th +" lines = "+ str2);
					Log.d(TAG, " length = "+getTextPaint().measureText(str2));
					if(i + 1 == lines){
						onePageStr += str2;
					} else {
						onePageStr += str2+"\n";
					}

				}

				ch.onePageString.add(onePageStr);
			}

			ch.needFormat = false;
			ch.readNow = 0;
			ch.total = ch.onePageString.size();
		}

		public void formatChapter(STATE s){
			int loc = s.ordinal();
			chapter ch = mChapters[loc];
			if(ch == null) return;
			formatChapter(ch);
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
						paint += GetZNFontInfo(c).width();
					} else {
						paint += GetENFontInfo(c).width();
					}

					if(paint > haswidth){
						return th;
					}

					th++;
				}
			}
		}

		private void stop(){

		}

		public int calculateLines(float size){
			//			Log.d(TAG, "fontSize = "+getFontHeight(fontSize)+" bottom height = "+topviewHeight
			//					+" lines = "+(height - bottomViewHeight - topviewHeight) / getFontHeight(fontSize));
			fontSize = size;
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

		public Rect GetENFontInfo(char c){
			Rect efontrect = new Rect();
			getTextPaint().getTextBounds(c+"", 0, 1, efontrect);
			return efontrect;
		}

		private Rect GetZNFontInfo(char c){
			Rect cfontrect =  new Rect();
			getTextPaint().getTextBounds("辉", 0, 1, cfontrect );
//			Log.d("SUNMM", " char : "+c+" length = "+cfontrect.width());
			return cfontrect;
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

		public String getNextLoctionPage(STATE s, String lastread) {
			int c = getCurrentChapter(lastread);
			int loc = getCurrentPageLoc(lastread);
			//			int loc = mChapters[1].location +1;
			switch (s) {
			case PREV:
				if(loc == 1){
					if(c == 0){			//已经是第一章了
						return "0";
					} else
						return (c-1)+":"+(mChapters[0].total-1);
				}
				break;
			case NEXT:
				if(loc == mChapters[1].total){
					if(c == mBookUtil.getBookChapters() - 1){
						return (mBookUtil.getBookChapters())+"";
					} else {
						return (c+1)+":"+"0";
					}
				}
				break;
			}
			return null;
		}

		public void setPaint(TextPaint paint) {
			Charfont = paint;
		}
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
					mCurrentRead = mOnePage.getNextLoctionPage(s, mCurrentRead);
				}
				//				addPages(s);
				mBookPagerAdapter.notifyDataSetChanged();
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
		mCurrentRead = "2:0";
		mProgressDialog = new ProgressDialog(getActivity());
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		mOnePage.width = display.getWidth();
		mOnePage.height = display.getHeight();
		final float density = getActivity().getResources().getDisplayMetrics().density;
		int h = (int)(getActivity().getResources().getDimension(R.dimen.page_small_text_height));
		Log.d(TAG, "screen width = "+mOnePage.width+" hegiht = "+mOnePage.height+" density = "+density+" h = "+h);
		int left = (int)(getActivity().getResources().getDimension(R.dimen.page_left_right));
		int top = (int)(getActivity().getResources().getDimension(R.dimen.page_top_bottom));
		mOnePage.bodyMargin = (int)(getActivity().getResources().getDimension(R.dimen.page_body_margin));
		mOnePage.bottomViewHeight = mOnePage.topviewHeight  =h+top;
		mOnePage.leftmargin = mOnePage.rightmargin = left;
		mOnePage.topmargin = mOnePage.bottommargin = top;
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

		checkChapterIsDownload(getCurrentChapter(mCurrentRead));

		mViewPager.setOnPageChangeListener(new myOnPageChangeListener());
		mViewPager.setCurrentItem(601);

		mPagesView = new pageView(601);

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

	private void checkChapterIsDownload(int l) {
		BookChapter b = mBookChapters.get(l);
		String path = GlobalContext.getPath()+"/"+mBookUtil.getBookName().trim()+"/"+b.get_ID();
		Log.d(TAG, "will open : "+path);
		if(b.getIsDownload() && (new File(path).exists()) ){
			bookChars = getChapterContentFromFiles(b.get_ID(), mBookUtil.getBookName().trim());
			mOnePage.addChapter(bookChars, STATE.CURRENT);
		} else {
			mProgressDialog.setMessage(getActivity().getResources().getString(R.string.loading));
			mProgressDialog.show();

			new GetMtBookChapterContentTask(b).execute();
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
			STATE s = STATE.values()[arg1 % 3];
			View v =mPagesView.getPageView(s);
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
		private STATE pageState;
		private String pageReadState;

		public ViewAdapter(STATE s){
			mInflater = LayoutInflater.from(getActivity());
			pageState= s;
		}

		public void setAdapterState(STATE s, String r){
			pageState = s;
			pageReadState = r;
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

			chapterTitle.setText(mBookChapters.get(getCurrentChapter(mCurrentRead)).getBookChapter());
//			mOnePage.setPaint(chapterBoby.getPaint());
			chapterBoby.setLines(mOnePage.calculateLines(chapterBoby.getTextSize()));
			mOnePage.setScale((int)chapterBoby.getScaleX(),(int)chapterBoby.getScaleY());
			chapterBoby.setMywidth(mOnePage.getRowsLen(), chapterBoby.getScaleX(), chapterBoby.getScaleY());
			chapterBoby.setText(mOnePage.getOnePageString(pageState), mOnePage.getTextPaint());
			//						chapterBoby.setText(bookChars);

			float [] f = mOnePage.setLineSpace();
			chapterBoby.setLineSpacing(f[0], f[1]);
			battery.setText("99");
			time.setText("23:12");
			pageNumber.setText("1/10");

			battery.setVisibility(View.VISIBLE);
			time.setVisibility(View.VISIBLE);
			pageNumber.setVisibility(View.VISIBLE);

			if(bookChars != null){
				//				Log.d(TAG, " bookChars = "+bookChars.substring(0, 20));
			}
			//			Log.d(TAG, " chapters name = "+mBookChapters.get(mCurrentRead / 100).getBookChapter());
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
			Log.d(TAG, " onPageSelected : "+ arg0 +" changed = "+changed +" scrolled = "+scrolled);
			if (arg0+1>1) {
				Message ms = mhandler.obtainMessage();
				ms.what = LOAD_NEW_PAGE;
				ms.obj = (arg0 > scrolled) ? STATE.NEXT : STATE.PREV;
				ms.sendToTarget();
			}
		}
	}

	class GetMtBookChapterContentTask extends AsyncTask<String,Integer,String> {
		BookChapter chapter;
		public GetMtBookChapterContentTask(BookChapter b) {
			chapter = b;
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
				mOnePage.addChapter(bookChars, STATE.CURRENT);
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
			//			initPage();
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

		//		private void initPage(){
		//			prevPage = addPages(STATE.PREV);
		//			currentPage = addPages(STATE.CURRENT);
		//			nextPage = addPages(STATE.NEXT);
		//		}

		public View getPageView(STATE s){
			int which = s.ordinal();
			View v = null;

			onePage p = addPages(s);
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

		private onePage addPages(STATE s) {
			GridView gd = new GridView(getActivity());
			ViewAdapter Adapter = new ViewAdapter(s);
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
			//			switch (s) {
			//			case PREV:
			//				if(mPagesView[0].v != null){
			//					mPagesView[2] = mPagesView[1];
			//					mPagesView[1] = mPagesView[0];
			//				}
			//				mPagesView[0].v = gd;
			//				mPagesView[0].adpater = Adapter;
			//				break;
			//			case CURRENT:
			//				mPagesView[1].v = gd;
			//				mPagesView[1].adpater = Adapter;
			//				break;
			//			case NEXT:
			//				if(mPagesView[2].v != null){
			//					mPagesView[0] = mPagesView[1];
			//					mPagesView[1] = mPagesView[2];
			//				}
			//				mPagesView[2].v = gd;
			//				mPagesView[2].adpater = Adapter;
			//				break;
			//			}
			//			Log.d(TAG, "add page = "+s+" v="+gd);
		}
	}

	public static enum STATE{
		PREV, CURRENT,NEXT
	}
}
