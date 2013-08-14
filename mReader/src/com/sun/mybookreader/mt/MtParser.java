package com.sun.mybookreader.mt;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.sun.mybookreader.utils.Log;

import com.sun.mybookreader.html.LinkTagSet;

public class MtParser {
	private static final String TAG = "MtParser";

	private String mHtmlUrl;
	private Parser mParser;

	public MtParser() {
		mHtmlUrl = MtUtils.MT_URL;
		parser();
	}

	public MtParser(String url){
		mHtmlUrl = url;
		parser();
	}

	public void parser(){
		try {
			mParser = new Parser(mHtmlUrl);
		} catch (ParserException e) {	
			e.printStackTrace();
		}
	}

	public List<LinkTagSet> getBookCategory(){
		List<LinkTagSet> list  = new ArrayList<LinkTagSet>();
		NodeFilter filter = new HasAttributeFilter("class", "mainnav_list");
		try {
			NodeList nodes = mParser.extractAllNodesThatMatch(filter);
			if(nodes.size() < 1){
				return null;
			}
			NodeList nodes2 = nodes.elementAt(0).getChildren();
			for (int i = 0; i < nodes2.size(); i++){
				Node n = nodes2.elementAt(i);
				if(n instanceof LinkTag){
					LinkTag link = (LinkTag)n;
					Log.d(TAG, " text : "+ link.toPlainTextString()+"   "+link.getLink());
					if(!MtUtils.MT_URL.equals(link.getLink()))
						list.add(new LinkTagSet(link.getLink(), link.toPlainTextString()));
				}
			};

		} catch (ParserException e) {
			Log.e(TAG, "getBookCategory ParserException");
			e.printStackTrace();
		}
		return list;
	}

	public List<MtBookList> getBookList(String url){
		Log.d(TAG, "open url = "+url);
		List<MtBookList> list = new ArrayList<MtBookList>();
		mHtmlUrl = url;
		parser();
		NodeFilter filter = new HasAttributeFilter("class", "tutui");
		try {
			NodeList nodes = mParser.extractAllNodesThatMatch(filter);
			for(int i = 0; i < nodes.size(); i++){
				Node n = nodes.elementAt(i);
				if(n instanceof Div){
					Div d = (Div)n;
					for (int j =0; j < d.getChildren().size(); j++){
						Log.d(TAG, j+ " text = "+d.getChildren().elementAt(j).getText() + " painet"+
								d.getChildren().elementAt(j).toPlainTextString());
					}
				}
			}

		} catch (ParserException e) {
			Log.e(TAG, "getBookList ParserException");
			e.printStackTrace();
		}

		return list;
	}
}
