package com.sun.mybookreader.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.mybookreader.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;


public class ImageLoader {

	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	ExecutorService executorService;
	private Context mContext;

	public ImageLoader(Context context) {
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(10);
		mContext = context;
	}

	final int stub_id = R.drawable.no_image;

	public void DisplayImage(String url, ImageView imageView) {
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null)
			imageView.setImageBitmap(bitmap);
		else {
			File f = fileCache.getFile(url);
			Bitmap b = decodeFile(f);
			
			if (b != null){
				imageView.setImageBitmap(b);
				return;
			}
			
			queuePhoto(url, imageView);
			//			imageView.setImageResource(stub_id);
			imageView.setBackgroundColor(0);
		}
	}

	private void queuePhoto(String url, ImageView imageView) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url) {
		File f = fileCache.getFile(url);

		// 从sd卡
		Bitmap b = decodeFile(f);
		if (b != null)
			return b;

		// 从网络
		try {
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl
					.openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
//			bitmap = decodeFile(f);
			bitmap = safeDecodeStream(Uri.fromFile(f), 200, 250);
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/** 
	 * A safer decodeStream method 
	 * rather than the one of {@link BitmapFactory} 
	 * which will be easy to get OutOfMemory Exception 
	 * while loading a big image file. 
	 *  
	 * @param uri 
	 * @param width 
	 * @param height 
	 * @return 
	 * @throws FileNotFoundException 
	 */  
	protected Bitmap safeDecodeStream(Uri uri, int width, int height)  
			throws FileNotFoundException{  
		int scale = 1;  
		BitmapFactory.Options options = new BitmapFactory.Options();  
		android.content.ContentResolver resolver = mContext.getContentResolver();  

		if(width>0 || height>0){  
			// Decode image size without loading all data into memory  
			options.inJustDecodeBounds = true;  
			BitmapFactory.decodeStream(  
					new BufferedInputStream(resolver.openInputStream(uri), 16*1024),  
					null,  
					options);  

			int w = options.outWidth;  
			int h = options.outHeight;  
//			int len = 100*mContext.getResources().getDisplayMetrics().density;
//			if(w > len){
//				width = len;
//				height = (int)(h * 1.0 * len / w);
//			}
			while (true) {  
				if ((width>0 && w/2 < width)  
						|| (height>0 && h/2 < height)){  
					break;  
				}  
				w /= 2;  
				h /= 2;  
				scale *= 2;  
			}  
		}  

		// Decode with inSampleSize option  
		options.inJustDecodeBounds = false;  
		options.inSampleSize = scale;  
		return BitmapFactory.decodeStream(  
				new BufferedInputStream(resolver.openInputStream(uri), 16*1024),   
				null,   
				options);  
	}
	

	// 解码图像用来减少内存消耗
	private Bitmap decodeFile(File f) {
		try {
			// 解码图像大小
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// 找到正确的刻度值，它应该是2的幂。
			final int REQUIRED_SIZE = 100;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	// 任务队列
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = getBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// 用于显示位图在UI线程
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null)
				photoToLoad.imageView.setImageBitmap(bitmap);
			else {
//				photoToLoad.imageView.setImageResource(stub_id);
				photoToLoad.imageView.setBackgroundColor(0);
			}
		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}
}