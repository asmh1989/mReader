package com.sun.mybookreader.mt;

public class BookChapter {
	private int _ID;
	private int book_id;
	private String book_chapter;
	private String book_chapter_url;
	private boolean is_download;
	
	public String get_ID() {
		return (book_id+book_chapter).hashCode()+"";
	}
	
	public String getBookID(){
		return book_id+"";
	}
	
	public void setBookID(int id){
		book_id = id;
	}
	
	public String getBookChapter(){
		return book_chapter;
	}
	
	public void setBookChapter(String c){
		book_chapter = c;
	}
	
	public String getBookChapterUrl(){
		return book_chapter_url;
	}
	
	public void setBookChapterUrl(String url){
		book_chapter_url = url;
	}
	
	public boolean getIsDownload(){
		return is_download;
	}
	
	public void setIsDownload(boolean b){
		is_download = b;
	}

}
