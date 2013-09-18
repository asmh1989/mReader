package com.sun.mreader.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.sun.mreader.BuildConfig;
import com.sun.mreader.R;
import com.sun.mreader.activity.BookCategoryActivity;
import com.sun.mreader.activity.BookDetailActivity;
import com.sun.mreader.adapter.MenuPopAdapter;
import com.sun.mreader.database.BookChaptersDBTask;
import com.sun.mreader.database.BookDBTask;
import com.sun.mreader.mt.BookChapter;
import com.sun.mreader.mt.MtBookChapterAdapater;
import com.sun.mreader.mt.MtBookDetail;
import com.sun.mreader.mt.MtBookUtil;
import com.sun.mreader.mt.MtUtils;
import com.sun.mreader.ui.RecyclingImageView;
import com.sun.mreader.util.ImageCache.ImageCacheParams;
import com.sun.mreader.utils.GlobalContext;
import com.sun.mreader.utils.ImageLoader;
import com.sun.mreader.util.ImageFetcher;

public class BookShelfFragment extends SherlockFragment implements OnItemClickListener {
	private static final String TAG = "BookShelfFragment";
	private static final String IMAGE_CACHE_DIR = "thumbs";

	private Context mContext;
	private int mBookThumbSize;
	private int mBookThumbSpacing;
	private BookAdapter mAdapter;
	private ImageFetcher mImageFetcher;
	private List<MtBookUtil> mBookUtil;
	private PopupWindow menuPop;
	private int mChoiceBook;
	private GridView mBookGridView;

	private int positionX;
	private int positionY;

	private static final int UPDATE_BOOKS = 0;
	private static final int UPDATE_BOOKS_FINISH = 1;
	private static final int DOWNLOAD_BOOKS = 2;
	private static final int DOWNLOAD_BOOKS_finish = 3;

	private Handler mHander = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_BOOKS:
				mAdapter.setBarStatus(msg.arg1, msg.arg2);
				mAdapter.notifyDataSetChanged();
				if(msg.arg2 == 100){
					Message n = new Message();
					n.what = UPDATE_BOOKS_FINISH;
					n.arg1 = msg.arg1;
					mHander.sendMessageDelayed(n, 400);
				}
				break;
			case UPDATE_BOOKS_FINISH:
				mAdapter.resetOneBar(msg.arg1);
				mAdapter.notifyDataSetChanged();
				break;
			case DOWNLOAD_BOOKS:

				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	public BookShelfFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mBookThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mBookThumbSpacing = 0;//getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

		mBookUtil = BookDBTask.getBookList();
		mContext = getActivity();

		mAdapter = new BookAdapter(mContext);

		ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

		// The ImageFetcher takes care of loading images into our ImageView children asynchronously
		mImageFetcher = new ImageFetcher(mContext, mBookThumbSize);
		//		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);

		initPopMenu();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(0, R.string.online_book, 0, getString(R.string.online_book))
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add(0, R.string.update_all, 0, getString(R.string.update_all))
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menu.add(0, R.string.download_all, 0, getString(R.string.download_all))
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menu.add(0, R.string.edit_bookshelf, 0, getString(R.string.edit_bookshelf))
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.string.online_book){
			Intent i = new Intent(mContext, BookCategoryActivity.class);
			i.putExtra("url", MtUtils.MT_URL);
			startActivity(i);
		} else if(item.getItemId() == R.string.update_all){
			List<Integer> l = new ArrayList<Integer>();
			int len = mBookUtil.size();
			for(int i = 0; i < len; i++){
				l.add(i);
			}
			mAdapter.updateBooks(l);

		} else if(item.getItemId() == R.string.download_all){

		} else if(item.getItemId() == R.string.edit_bookshelf){
			((SherlockFragmentActivity)getActivity()).startActionMode(new AnActionModeOfEpicProportions());
			mAdapter.setEditing(true);
		}
		return super.onOptionsItemSelected(item);
	}


	private void initPopMenu() {
		ListView list = new ListView(mContext);
		list.setAdapter(new MenuPopAdapter(mContext));

		menuPop = new PopupWindow(list, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		menuPop.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_buttom_bg));
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int whichItem = MenuPopAdapter.resourceId[position];
				switch (whichItem) {
				case R.string.update:
					List<Integer> l = new ArrayList<Integer>();
					l.add(mChoiceBook);
					mAdapter.updateBooks(l);
					break;
				case R.string.download:
					mAdapter.downloadBooks();
					break;
				case R.string.book_web:
					Intent i = new Intent(mContext, BookDetailActivity.class);
					i.putExtra("url", mBookUtil.get(mChoiceBook).getBookUrl());
					startActivity(i);
					break;
				case  R.string.book_delete:
					Set<String> set = new HashSet<String>();
					set.add(String.valueOf(mBookUtil.get(mChoiceBook).getBookID()));
					BookDBTask.removeBook(set);
					mBookUtil.remove(mChoiceBook);
					mAdapter.notifyDataSetChanged();
					break;
				}

				menuPop.dismiss();
			}

		});		
	}


	public void openMenu(int mt) {
		if(menuPop.isShowing()){
			menuPop.dismiss();
		}
		mChoiceBook = mt;
		menuPop.setWidth(mBookThumbSize);
		menuPop.showAtLocation(mBookGridView, Gravity.LEFT | Gravity.TOP, positionX, positionY);
	}

	@Override
	public View onCreateView(
			LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.book_grid_fragment, container, false);
		mBookGridView = (GridView) v.findViewById(R.id.gridView);
		mBookGridView.setAdapter(mAdapter);
		mBookGridView.setOnItemClickListener(this);
		mBookGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					mImageFetcher.setPauseWork(true);
				} else {
					mImageFetcher.setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});

		// This listener is used to get the final width of the GridView and then calculate the
		// number of columns and the width of each column. The width of each column is variable
		// as the GridView has stretchMode=columnWidth. The column width is used to set the height
		// of each view so we get nice square thumbnails.
		mBookGridView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (mAdapter.getNumColumns() == 0) {
							final int numColumns = (int) Math.floor(
									mBookGridView.getWidth() / (mBookThumbSize + mBookThumbSpacing));
							if (numColumns > 0) {
								final int columnWidth =
										(mBookGridView.getWidth() / numColumns) - mBookThumbSpacing;
								//								mAdapter.setNumColumns(numColumns);
								mAdapter.setItemHeight(columnWidth);
								if (BuildConfig.DEBUG) {
									Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
								}
							}
						}
					}
				});

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
		//		Log.d("SUNMM", "old size = "+mBookUtil.size());
		mBookUtil = BookDBTask.getBookList();
		//		Log.d("SUNMM", "new size = "+mBookUtil.size());
		
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

	}


	/**
	 * The main adapter that backs the GridView. This is fairly standard except the number of
	 * columns in the GridView is used to create a fake top row of empty views as we use a
	 * transparent ActionBar and don't want the real top row of images to start off covered by it.
	 */
	private class BookAdapter extends BaseAdapter implements OnTouchListener {
		private final Context mContext;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private int mActionBarHeight = 0;
		private GridView.LayoutParams mBookLayoutParams;
		private LayoutInflater mInflater;
		private boolean mIsEdited = false;
		private ProgressBar mUpdateBar;
		private ImageView mChoiceImg;
		private boolean[] mChoiceNum;
		private List<BarStatus> mShowbar = new ArrayList<BookShelfFragment.BookAdapter.BarStatus>();

		class BarStatus{
			public boolean isShow = false;
			public int barStatus = 0;
			public void reset() {
				isShow = false;
				barStatus = 0;
			}
		}

		@Override
		public void notifyDataSetChanged() {
			int len = mBookUtil.size();
			mChoiceNum = new boolean [len];
			for(int i = 0; i < len; i++){
				BarStatus b = new BarStatus();
				mShowbar.add(b);
			}
			super.notifyDataSetChanged();
		}

		public BookAdapter(Context context) {
			super();
			mContext = context;
			mBookLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			// Calculate ActionBar height

			mInflater = LayoutInflater.from(context);

			TypedValue tv = new TypedValue();
			if (context.getTheme().resolveAttribute(
					android.R.attr.actionBarSize, tv, true)) {
				mActionBarHeight = TypedValue.complexToDimensionPixelSize(
						tv.data, context.getResources().getDisplayMetrics());
			}
		}

		@Override
		public int getCount() {
			return mBookUtil.size() + mNumColumns;
		}

		@Override
		public Object getItem(int position) {
			return position < mNumColumns ?
					null : mBookUtil.get(position - mNumColumns);
		}

		@Override
		public long getItemId(int position) {
			return position < mNumColumns ? 0 : position - mNumColumns;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			return (position < mNumColumns) ? 1 : 0;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		public void setEditing(boolean b){
			mIsEdited = b;
			for(int i = 0; i < mChoiceNum.length; i++){
				mChoiceNum[i] = false;
			}
			notifyDataSetChanged();
		}

		public void setAllChoice(){
			boolean b = false;
			for(int i = 0; i < mChoiceNum.length; i++){
				if(!mChoiceNum[i]){
					b = true;
					break;
				}	
			}

			for(int i = 0; i < mChoiceNum.length; i++){
				mChoiceNum[i] = b;
			}
			notifyDataSetChanged();
		}

		public void deleteBooks() {
			Set<String> set = new HashSet<String>();
			for(int i = 0; i < mChoiceNum.length; i++){
				if(mChoiceNum[i]){
					set.add(String.valueOf(mBookUtil.get(i).getBookID()));
					BookDBTask.removeBook(set);
					mBookUtil.remove(i);
				}
			}
			notifyDataSetChanged();
		}

		/** longclick or all_update: b != null; ActionMode_menu : b == null
		 * @param b
		 */
		public void updateBooks(List<Integer> b) {
			if(b != null){
				new ShowBarThead(b, UPDATE_BOOKS).start();
			} else {
				new ShowBarThead(getIntArrayFromBoolean(mChoiceNum), UPDATE_BOOKS).start();
			}

		}

		private List<Integer> getIntArrayFromBoolean(boolean[] mChoiceNum2) {
			List<Integer> list= new ArrayList<Integer>();
			int len = mChoiceNum2.length;
			for(int i = 0; i < len; i++){
				if(mChoiceNum2[i]){
					list.add(i);
				}
			}
			return list;
		}

		public void resetOneBar(int w){
			mShowbar.get(w).reset();
		}

		public void setBarStatus(int w, int s){
			mShowbar.get(w).barStatus = s;
		}

		public void downloadBooks() {

		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			// First check if this is the top row
			if (position < mNumColumns) {
				if (convertView == null) {
					convertView = new View(mContext);
				}
				// Set empty view with height of ActionBar
				convertView.setLayoutParams(new AbsListView.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight));
				return convertView;
			}

			// Now handle the main ImageView thumbnails
			if (convertView == null) { // if it's not recycled, instantiate and initialize
				convertView = mInflater.inflate(R.layout.grid_item, container, false);
			}

			ImageView imageView =(RecyclingImageView) convertView.findViewById(R.id.image);
			mUpdateBar = (ProgressBar) convertView.findViewById(R.id.show_bar);
			mChoiceImg = (ImageView)convertView.findViewById(R.id.choice_edit);

			//			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			convertView.setLayoutParams(mBookLayoutParams);

			// Check the height matches our calculated column width
			if (convertView.getLayoutParams().height != mItemHeight) {
				convertView.setLayoutParams(mBookLayoutParams);
			}

			if(mIsEdited){
				mChoiceImg.setVisibility(View.VISIBLE);
				if(mChoiceNum[position]){
					mChoiceImg.setBackgroundResource(R.drawable.books_management_focus);
				} else {
					mChoiceImg.setBackgroundResource(R.drawable.books_management);
				}
			} else {
				mChoiceImg.setVisibility(View.GONE);
			}

			if(mShowbar.size() > position && mShowbar.get(position).isShow){
				Log.d("SUNMM", "  willbe = "+mShowbar.get(position).barStatus+" pre = "+mUpdateBar.getProgress()+" position = "+position);
				mUpdateBar.setProgress(mShowbar.get(position).barStatus);
				mUpdateBar.setVisibility(View.VISIBLE);
			} else {
				mUpdateBar.setProgress(0);
				mUpdateBar.setVisibility(View.GONE);
			}

			// Finally load the image asynchronously into the ImageView, this also takes care of
			// setting a placeholder image while the background thread runs
			MtBookUtil mt = mBookUtil.get(position - mNumColumns);
			mImageFetcher.loadImage(mt.getImageUrl(), imageView);
			imageView.setOnClickListener(new MyClickListener(mt, position));
			imageView.setOnLongClickListener(new MyLongClickListener(position));
			imageView.setOnTouchListener(this);

			return convertView;
		}

		class ShowBarThead extends Thread{
			List<Integer> bars;
			int why;

			public ShowBarThead(List<Integer> b, int w){
				bars = b;
				why = w;
			}

			void  handMessage(int what, int arg1, int arg2){
				Message msg = new Message();
				msg.what = what;
				msg.arg1 = arg1;
				msg.arg2 = arg2;
				int time = 400;
				mHander.sendMessageDelayed(msg, time);
			}

			private void showUpdatesBar(){
				for(int which : bars){
					mShowbar.get(which).isShow = true;
				}

				for(int which : bars){
					handMessage(UPDATE_BOOKS, which, 10);
					List<BookChapter> mbd = GlobalContext.getparser().getBookChapters(mBookUtil.get(which).getBookUrl());
					handMessage(UPDATE_BOOKS, which, 40);
					int oldLen = mBookUtil.get(which).getBookChapters();
					int newLen = mbd.size();
					Log.d("SUNMM", "oldLen = "+ oldLen+" newLen = "+newLen+" mChoice = "+mChoiceBook);
					if(newLen > oldLen){
						int len = newLen - oldLen;
						int oncePer = len * 100 / 50;
						for(int i = oldLen; i < newLen; i++){
							BookChapter b = mbd.get(i);
							BookChaptersDBTask.addBookChapter(b);
							handMessage(UPDATE_BOOKS, which, 50+ (i+1-oldLen) * oncePer);
						}

						mBookUtil.get(which).setBookChapters(newLen);
						BookDBTask.addOrUpdateBook(mBookUtil.get(which));
						mBookUtil = BookDBTask.getBookList();
					}
					handMessage(UPDATE_BOOKS, which, 100);
				}
			}

			@Override
			public void run() {
				if(why == UPDATE_BOOKS){
					showUpdatesBar();
				} else if(why == DOWNLOAD_BOOKS){
					showDownloadsBar();
				}
			}

			private void showDownloadsBar() {

			}
		}


		class MyClickListener implements android.view.View.OnClickListener{
			MtBookUtil bookUtil;
			private int pos;

			public MyClickListener(MtBookUtil m, int p){
				bookUtil = m;
				pos = p;
			}

			@Override
			public void onClick(View v) {
				Log.d("SUNMM", "click open book name = "+ bookUtil.getBookName().trim()+" mIsEdited = "+mIsEdited);
				if(mIsEdited){
					mChoiceNum[pos] = !mChoiceNum[pos];
					notifyDataSetChanged();
				}
			}
		}

		class MyLongClickListener implements android.view.View.OnLongClickListener{
			int pos;
			public MyLongClickListener(int m){
				pos = m;
			}
			@Override
			public boolean onLongClick(View v) {
				//			vibrate(200);
				int [] loc = new int[2];
				v.getLocationInWindow(loc);
				positionX += loc[0];
				positionY += loc[1];
				openMenu(pos);
				return false;
			}

		}

		private void vibrate(long duration) {
			Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
			long[] pattern = {
					0, duration
			};
			vibrator.vibrate(pattern, -1);
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			positionX = (int)event.getX();
			positionY = (int)event.getY();
			return false;
		}


		/**
		 * Sets the item height. Useful for when we know the column width so the height can be set
		 * to match.
		 *
		 * @param height
		 */
		public void setItemHeight(int height) {
			if (height == mItemHeight) {
				return;
			}
			mItemHeight = height;
			mBookLayoutParams =
					new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
			mImageFetcher.setImageSize(height);
			notifyDataSetChanged();
		}

		public void setNumColumns(int numColumns) {
			mNumColumns = numColumns;
		}

		public int getNumColumns() {
			return mNumColumns;
		}
	}

	private final class AnActionModeOfEpicProportions implements ActionMode.Callback {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			//Used to put dark icons on light action bar

			menu.add(0, R.string.all_choice, 0, getString(R.string.all_choice))
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			menu.add(0, R.string.book_delete, 0, getString(R.string.book_delete))
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			menu.add(0, R.string.update, 0, getString(R.string.update))
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			menu.add(0, R.string.download, 0, getString(R.string.download))
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.string.all_choice:
				mAdapter.setAllChoice();
				break;
			case R.string.book_delete:
				mAdapter.deleteBooks();
				mode.finish();
				break;
			case R.string.update:
				mAdapter.updateBooks(null);
				mode.finish();
				break;
			case R.string.download:
				mAdapter.downloadBooks();
				mode.finish();
				break;
			}

			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mAdapter.setEditing(false);
		}
	}
}
