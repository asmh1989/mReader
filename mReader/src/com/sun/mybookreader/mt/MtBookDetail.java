package com.sun.mybookreader.mt;

import java.io.Serializable;
import java.util.HashMap;

import com.sun.mybookreader.html.LinkTagSet;

public class MtBookDetail implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String imageUrl;
	public String bookDetail="";
	public String bookAbout;
	public HashMap<String, LinkTagSet> bookChapters = new HashMap<String, LinkTagSet>();
}
