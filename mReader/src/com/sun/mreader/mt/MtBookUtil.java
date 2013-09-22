package com.sun.mreader.mt;

public class MtBookUtil { 
	private String imageUrl;
	private String bookName;
	private String bookAuthor;
	private String bookUrl;
	private String bookAbout;
	private int bookChapters;
	private boolean bookIsFinish;
	private String bookUpdateTime;
	private String bookAddTime;
	private String bookLastRead;
	
	public String getBookID(){
		return bookUrl.hashCode() +"";
	}
	
	public String getBookName(){
		return bookName;
	}
	
	public void setBookName(String name){
		bookName = name;
	}
	
	public String getBookAuthor(){
		return bookAuthor;
	}
	
	public void setBookAuthor(String author){
		bookAuthor = author;
	}
	
	public String getBookAbout(){
		return bookAbout;
	}
	
	public void setBookAbout(String about){
		bookAbout = about;
	}
	
	public String getImageUrl(){
		return imageUrl;
	}
	
	public void setImageUrl(String url){
		imageUrl = url;
	}
	
	public String getBookUrl(){
		return bookUrl;
	}
	
	public void setBookUrl(String url){
		bookUrl = url;
	}
	
	public int getBookChapters(){
		return bookChapters;
	}
	
	public void setBookChapters(int num){
		bookChapters = num;
	}
	
	public boolean getBookIsFinish(){
		return bookIsFinish;
	}
	
	public void setBookIsFinish(boolean isfinish){
		bookIsFinish = isfinish;
	}
	
	public String getBookUpdateTime(){
		return bookUpdateTime;
	}
	
	public void setBookUpdateTime(String time){
		bookUpdateTime = time;
	}
	
	public void setBookAddTime(String time){
		bookAddTime = time;
	}
	
	public String getAddTime(){
		return bookAddTime;
	}
	
	public void setBookLastRead(String read){
		bookLastRead = read;
	}
	
	public String getBookLastRead(){
		return bookLastRead;
	}
}
