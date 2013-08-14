package com.sun.mybookreader.html;

public class LinkTagSet {
	private String link;
	private String plaintText;
	
	public LinkTagSet(String l, String p) {
		link = l;
		plaintText = p;
	}
	
	public String getLink(){
		return link;
	}
	
	public String getPlainTextString(){
		return plaintText;
	}
}
