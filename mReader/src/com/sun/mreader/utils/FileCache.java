package com.sun.mreader.utils;

import java.io.File;
import android.content.Context;
 
public class FileCache {
     
    private File cacheDir;
     
    public FileCache(Context context){
        //找一个用来缓存图片的路径
    	cacheDir = GlobalContext.createPath(GlobalContext.SAVEPATH_IMAGE);
    }
     
    public File getFile(String url){
         
        String filename=String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        return f;
         
    }
     
    public void clear(){
        File[] files=cacheDir.listFiles();
        if(files==null)
            return;
        for(File f:files)
            f.delete();
    }
 
}