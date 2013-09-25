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
import com.sun.mreader.utils.GlobalContext;
import com.sun.mreader.utils.Log;

@SuppressLint("ValidFragment")
public class BookReaderFragment extends Fragment {

	private static final String TAG = "SUNBookReaderFragment";

	private ViewPager mViewPager;
	private BookPagerAdapter mBookPagerAdapter;
	private pageView[] mPagesView = new pageView [3];
	private Queue<pageView> vieww;
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

	class pageView{
		public pageView(GridView gd, ViewAdapter a) {
			v = gd;
			adpater = a;
		}
		public pageView() {
			// TODO Auto-generated constructor stub
		}
		public GridView v;
		public ViewAdapter adpater;
	}
	public static enum STATE{
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
				for(int i = 0; i < lines && th < length; i++){
					th += getOneLineStringLen(ch.originContent.substring(th), th);
				}
				if(th < length){
					ch.onePageString.add(ch.originContent.substring(prev, th));
				} else {
					ch.onePageString.add(ch.originContent.substring(prev));
				}
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
					tmpStr = str2.substring(0, 128);
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

		public String getNextLoctionPage(STATE s, String lastread) {
			int c = getCurrentChapter(lastread);
			//			int loc = getCurrentPageLoc(lastread);
			int loc = mChapters[1].location +1;
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
	}

	private static final int LOAD_NEW_PAGE = 0;

	private Handler mhandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_NEW_PAGE:
				STATE s = (STATE) msg.obj;
				//				mCurrentRead = mOnePage.getNextLoctionPage(s, mCurrentRead);
				addPages(s);
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

		for(int i = 0; i < mPagesView.length; i++){
			mPagesView[i] = new pageView();
		}
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
		if(hasPrevPage()){
			addPages(STATE.PREV);
		}
		addPages(STATE.CURRENT);
		if(hasNextPage()){
			addPages(STATE.NEXT);
		}

		mViewPager.setOnPageChangeListener(new myOnPageChangeListener());
		mViewPager.setCurrentItem(601);
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

	private void addPages(STATE s) {
		GridView gd = new GridView(getActivity());
		ViewAdapter Adapter = new ViewAdapter();
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
		switch (s) {
		case PREV:
			if(mPagesView[0].v != null){
				mPagesView[2] = mPagesView[1];
				mPagesView[1] = mPagesView[0];
			}
			mPagesView[0].v = gd;
			mPagesView[0].adpater = Adapter;
			break;
		case CURRENT:
			mPagesView[1].v = gd;
			mPagesView[1].adpater = Adapter;
			break;
		case NEXT:
			if(mPagesView[2].v != null){
				mPagesView[0] = mPagesView[1];
				mPagesView[1] = mPagesView[2];
			}
			mPagesView[2].v = gd;
			mPagesView[2].adpater = Adapter;
			break;
		}
		Log.d(TAG, "add page = "+s+" v="+gd);
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
			((ViewPager) arg0).removeView((View)arg2);
		}

		@Override
		public int getCount() {
			//			Log.d(TAG, "BookPagerAdapter getCount() = "+mPagesView.size());
			return mPagesView.length*1000000;
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			View v = null;
			if(hasPrevPage()){
				v = mPagesView[arg1%3].v;
			} else{
				v = mPagesView[(arg1+1)%3].v;
			}

			if(v == null){
				v = mPagesView[1].v;
			}
			((ViewPager) arg0).addView(v, 0);
			Log.d(TAG, "instantiateItem : mPagesView.get(arg1) = "+arg1+" v = "+v +" count = "+((ViewPager) arg0).getChildCount());

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

		public ViewAdapter(){
			mInflater = LayoutInflater.from(getActivity());
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

				mPagesView[STATE.CURRENT.ordinal()].adpater.notifyDataSetChanged();
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
