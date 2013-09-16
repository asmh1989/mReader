package com.sun.mreader.html;

import java.io.Serializable;

public class LinkTagSet implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5347372036903222029L;
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
