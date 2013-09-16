package com.sun.mreader.utils;

public class Log {
	private static final Boolean DEBUG = true;
	static public void d(String tag, String msg){
		if(DEBUG){
			android.util.Log.d(tag, msg);
		}
	}

	static public void v(String tag, String msg){
		if(DEBUG){
			android.util.Log.v(tag, msg);
		}
	}

	static public void e(String tag, String msg){
		if(DEBUG){
			android.util.Log.e(tag, msg);
		}
	}

	static public void w(String tag, String msg){
		if(DEBUG){
			android.util.Log.w(tag, msg);
		}
	}

	public static void w(String tag, String msg, RuntimeException re) {
		if(DEBUG){
			android.util.Log.w(tag, msg, re);
		}		
	}

	public static void w(String tag, String msg, IllegalAccessException e) {
		if(DEBUG){
			android.util.Log.w(tag, msg, e);
		}			
	}

	public static void w(String tag, String msg, Throwable cause) {
		if(DEBUG){
			android.util.Log.w(tag, msg, cause);
		}		
	}
}
