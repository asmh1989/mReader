package com.sun.mybookreader.utils;

import com.sun.mybookreader.mt.MtParser;

import android.app.Application;

public class GlobalContext extends Application {
	
    //singleton
    private static GlobalContext globalContext = null;
    private static MtParser mParser;
    
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
}
