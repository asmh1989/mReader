package com.sun.mreader.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Application;

import com.sun.mreader.mt.MtParser;

public class GlobalContext extends Application {
	private static final String TAG = "GlobalContext";
	//singleton
	private static GlobalContext globalContext = null;
	private static MtParser mParser;

	public static final String SAVEPATH = "mReader";
	public static final String SAVEPATH_IMAGE = "images";
	public static final String SAVEPATH_BOOKS = "books";

	public static final String ACTION_BOOK_READER = "com.sun.mreader.view";


	@Override
	public void onCreate() {
		super.onCreate();
		globalContext = this;
		mParser = new MtParser();
	}

	public static GlobalContext getInstance() {
		return globalContext;
	}

	public static MtParser getparser(){
		return mParser;
	}

	public static File createPath(String name){
		File cacheDir = null;
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
			File f1=new File(android.os.Environment.getExternalStorageDirectory(),SAVEPATH);
			if(!f1.exists()){
				f1.mkdir();
			}
			cacheDir = new File(f1, name);
		}else{
			File f1=globalContext.getCacheDir();
			cacheDir = new File(f1, name);
		}
		if(!cacheDir.exists())
			cacheDir.mkdirs();

		return cacheDir;
	}
	
	public static String getPath(){
		return createPath(SAVEPATH_BOOKS).getAbsolutePath();
	}

	public static File createFile(String Path){
		File root = createPath(SAVEPATH_BOOKS);
		String [] p = Path.split("/");
		int len = p.length;
		for(int i = 0; i < len; i++){
			root = new File(root, p[i]);
			if(!root.exists()){
				try {
					if(i == len - 1){
						root.createNewFile();
					} else {
						root.mkdir();
					}
				} catch (IOException e) {
					Log.e(TAG, e.toString());
					return null;
				}
			}
		}
		return root;
	}

	public static void saveContent(String content, String name, String bookname) {
		FileOutputStream outStream;
		try {
			outStream = new FileOutputStream(createFile(bookname+"/"+name));
			outStream.write(content.getBytes());
			outStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
