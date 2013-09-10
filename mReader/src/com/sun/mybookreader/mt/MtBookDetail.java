package com.sun.mybookreader.mt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sun.mybookreader.html.LinkTagSet;

public class MtBookDetail{
	public String imageUrl;
	public String bookDetail="";
	public String bookAbout;
	public String bookName ="";
	public String bookUpdateTime;
	public String bookAuthor;
	public boolean isFinish = false;
	public List<BookChapter> bookChapters = new ArrayList<BookChapter>();
}
