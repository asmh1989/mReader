package com.sun.mreader.fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

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
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.sun.mreader.utils.GlobalContext;
import com.sun.mreader.utils.Log;

@SuppressLint("ValidFragment")
public class BookReaderFragment extends Fragment {

	private static final String TAG = "SUNBookReaderFragment";

	private ViewPager mViewPager;
	private BookPagerAdapter mBookPagerAdapter;
	private ViewAdapter mViewAdapter;
	private List<View> mPagesView = new ArrayList<View>();
	private int mPageSize = 3; //前中后
	private List<BookChapter> mBookChapters;

	private String mLastReader;
	private String mBookId;
	private MtBookUtil  mBookUtil;
	private String mCurrentRead;

	private String bookChars;
	private int bookChapter;
	private int nowCharsInChapter;
	private int onepageInScreen = 1;
	private int whichAdatperInFront = 0;

	private ProgressDialog mProgressDialog;

	private page mOnePage = new page();


	public BookReaderFragment() {
	}


	enum STATE{
		PREV, CURRENT,NEXT
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
		private Paint Charfont;
		private int fontStyle = 0;
		private String fontName = "MONOSPACE";
		private float fontSize;

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
		}

		public String getOnePageString(STATE s){
			String str ="";
			int c = getCurrentChapter(mCurrentRead);
			int l = getCurrentPageLoc(mCurrentRead);
			chapter ch;
			if(c == 0 && l == 1){
				ch = mChapters[0];
			} else {
				ch = mChapters[1];
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

		public void formatChapter(STATE s){
			int loc = s.ordinal();
			chapter ch = mChapters[loc];
			if(ch == null) return;
			int length = ch.originContent.length();
			int th = 0;
			while(th < length){
				int prev = th;
				for(int i = 0; i < lines; i++){
					th += getOneLineStringLen(ch.originContent.substring(th), th);
				}
				
				ch.onePageString.add(ch.originContent.substring(prev, th));
			}
			
			ch.needFormat = false;
			ch.readNow = 0;
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

		private int getOneLineStringLen(String str, int th) {
			int paint = 0;
			int haswidth = getRowsLen();
			String str2 = str;
			int th2 = th;

			while(true){
				String tmpStr ;
				if(str.length() > 128){
					tmpStr = str2.substring(th, 128);
					str2 = str2.substring(128);
				}
				else {
					tmpStr = str;
					str2 = "";
				}
				
				if(tmpStr.length() == 0){
					return th2;
				}
				
				for(int i = 0; i < tmpStr.length(); i++){
					char c = tmpStr.charAt(i);
					if(isChinese(c)){
						paint += GetZNFontInfo().width();
					} else {
						if("\t".equals(c)){
							paint += 2* GetZNFontInfo().width();
						} else if("\n".equals(c)){
							return th2++;
						}
					}
					
					if(paint > width){
						return th2;
					}
					
					th2++;
				}
			}
		}

		public int calculateLines(float size){
			//			Log.d(TAG, "fontSize = "+getFontHeight(fontSize)+" bottom height = "+topviewHeight
			//					+" lines = "+(height - bottomViewHeight - topviewHeight) / getFontHeight(fontSize));
			fontSize = size;
			return lines = (height - bottomViewHeight - topviewHeight - bodyMargin) / getFontHeight();
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
				Charfont=getFont();
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
		public Paint getFont(){
			if (Charfont == null){
				Charfont=new Paint();
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
				Charfont.setXfermode(new PixelXorXfermode(7));
				//_VTFont.setTextScaleX(1.0f); //设置文本绽放倍数，默认为1
				return Charfont;
			}
			else
				return Charfont;
		}

		public Rect GetENFontInfo(){
			Rect efontrect = new Rect();
			getFont().getTextBounds("A", 0, 1, efontrect);
			return efontrect;
		}

		private Rect GetZNFontInfo(){
			Rect cfontrect =  new Rect();
			getFont().getTextBounds("辉", 0, 1, cfontrect );
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

		private int getFontWidth(float fontSize) {
			Paint paint = new Paint();  
			paint.setTextSize(fontSize);  
			FontMetrics fm = paint.getFontMetrics();  
			return (int) Math.ceil(fm.descent - fm.ascent);  
		}

		private int getFontHeight(){  
			Paint paint = new Paint();  
			paint.setTextSize(fontSize);  
			FontMetrics fm = paint.getFontMetrics();  
			return (int) Math.ceil(fm.descent - fm.ascent);  
		}

		public int fillScreen() {
			Log.d(TAG, "calculate other height = "+(height - bottomViewHeight - topviewHeight - bodyMargin - lines*getFontHeight()));
			return height - bottomViewHeight - topviewHeight - bodyMargin - lines*getFontHeight();
		}
	}

	private static final int LOAD_NEW_PAGE = 0;

	private Handler mhandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_NEW_PAGE:
				mCurrentRead += onepageInScreen;
				whichAdatperInFront++;
				addPages();
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
		return Integer.parseInt(s);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.book_reader_fragment, container, false);
		mViewPager = (ViewPager) v.findViewById(R.id.viewpager); 
		mBookPagerAdapter = new BookPagerAdapter();
		mViewPager.setAdapter(mBookPagerAdapter);

		checkChapterIsDownload(getCurrentChapter(mCurrentRead));
		addPages();
		addPages();
		mBookPagerAdapter.notifyDataSetChanged();
		mViewPager.setOnPageChangeListener(new myOnPageChangeListener());

		return v;
	}

	private void checkChapterIsDownload(int l) {
		BookChapter b = mBookChapters.get(l);
		String path = GlobalContext.getPath()+"/"+mBookUtil.getBookName().trim()+"/"+b.get_ID();
		Log.d(TAG, "will open : "+path);
		if(b.getIsDownload() && (new File(path).exists()) ){
			bookChars = getChapterContentFromFiles(b.get_ID(), mBookUtil.getBookName().trim());
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

	private void addPages() {
		GridView gd = new GridView(getActivity());
		ViewAdapter Adapter;
		Adapter = new ViewAdapter();
		if(whichAdatperInFront == mPagesView.size()){
			mViewAdapter = Adapter;
		}
		gd.setAdapter(mViewAdapter);
		gd.setNumColumns(1);
		gd.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		gd.setBackgroundColor(Color.TRANSPARENT);
		gd.setCacheColorHint(Color.TRANSPARENT);
		gd.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gd.setPadding(mOnePage.leftmargin, mOnePage.topmargin, mOnePage.leftmargin, mOnePage.bottommargin);
		mPagesView.add(gd);

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
			((ViewPager) arg0).removeView(mPagesView.get(arg1));
		}

		@Override
		public int getCount() {
			//			Log.d(TAG, "BookPagerAdapter getCount() = "+mPagesView.size());
			return mPagesView.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			//			Log.d(TAG, "instantiateItem : mPagesView.get(arg1) = "+mPagesView.get(arg1));
			((ViewPager) arg0).addView(mPagesView.get(arg1), 0);
			return mPagesView.get(arg1);
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

		public ViewAdapter(){
			mInflater = LayoutInflater.from(getActivity());
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
			TextView chapterBoby = (TextView)convertView.findViewById(R.id.tv_body);
			TextView battery = (TextView) convertView.findViewById(R.id.tv_battery);
			TextView time = (TextView) convertView.findViewById(R.id.tv_time);
			TextView pageNumber = (TextView) convertView.findViewById(R.id.tv_page_number);

			chapterTitle.setText(mBookChapters.get(getCurrentChapter(mCurrentRead)).getBookChapter());
			chapterBoby.setLines(mOnePage.calculateLines(chapterBoby.getTextSize()));
			chapterBoby.setText(bookChars);
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

		public void onPageScrollStateChanged(int arg0) {
		}

		// 第一个View滑动前调用
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		// 新View被加载后调用
		public void onPageSelected(int arg0) {
			// 判断当前View是否为倒数第一个View
			if (mPagesView.size() == arg0 + 1&&arg0+1>1) {
				Message ms = mhandler.obtainMessage();
				ms.what = LOAD_NEW_PAGE;
				ms.arg1 = arg0+1;
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

				mViewAdapter.notifyDataSetChanged();
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
}
