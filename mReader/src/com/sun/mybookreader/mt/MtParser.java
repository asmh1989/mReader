package com.sun.mybookreader.mt;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeIterator;
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
			Log.e(TAG, "ParserException"+e.toString());
		}
	}

	public Parser parser(String url){
		Parser p = null;
		try {
			p = new Parser(url);
			return p;
		} catch (ParserException e) {	
			e.printStackTrace();
			return p;
		} 
	}

	public List<LinkTagSet> getBookCategory(){
//		Log.d(TAG, "start getBookCategory "+mParser);
		List<LinkTagSet> list  = new ArrayList<LinkTagSet>();
		NodeFilter filter = new HasAttributeFilter("class", "mainnav_list");

//		try {
//			for (NodeIterator i = mParser.elements (); i.hasMoreNodes(); ) {
//				Node node = i.nextNode();
//				Log.d(TAG, "html = "+node.toHtml());
//			}
//		} catch (ParserException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		try {
			NodeList nodes = mParser.extractAllNodesThatMatch(filter);
			if(nodes.size() < 1){
				Log.d(TAG, "getBookCategory = null");
				return null;
			}
			NodeList nodes2 = nodes.elementAt(0).getChildren();
			for (int i = 0; i < nodes2.size(); i++){
				Node n = nodes2.elementAt(i);
				if(n instanceof LinkTag){
					LinkTag link = (LinkTag)n;
					Log.d(TAG, " text : "+ link.toPlainTextString()+"   "+link.getLink());
					if(!MtUtils.MT_URL.equals(link.getLink()) && ! "/".equals(link.getLink()))
						list.add(new LinkTagSet(link.getLink(), link.toPlainTextString()));
				}
			};

		} catch (ParserException e) {
			Log.e(TAG, "getBookCategory ParserException");
			e.printStackTrace();
		}

		return list;
	}

	public List<MtBookUtil> getBookList(String url){
//		Log.d(TAG, "open url = "+url);
		List<MtBookUtil> list = new ArrayList<MtBookUtil>();
		mHtmlUrl = url;
		parser();
		NodeFilter filter = new HasAttributeFilter("class", "tutui");
		try {
			NodeList nodes = mParser.extractAllNodesThatMatch(filter);
			for(int i = 0; i < nodes.size(); i++){
				Node n = nodes.elementAt(i);
				if(n instanceof Div){
					Div d = (Div)n;
					MtBookUtil bl = new MtBookUtil();
					for (int j =0; j < d.getChildren().size(); j++){
						if(d.getChildren().elementAt(j).getText().contains("tutuiImg")){
							Log.d(TAG, "image html = "+d.getChildren().elementAt(j).toHtml());
							NodeList d2 = d.getChildren().elementAt(j).getChildren();
							while(!(d2.elementAt(0) instanceof ImageTag)){
								d2 = d2.elementAt(0).getChildren();
							}

							ImageTag img = (ImageTag)d2.elementAt(0);
							bl.imageUrl = img.getImageURL();
						} else if(d.getChildren().elementAt(j).getText().contains("tutuiTitle")){
							Log.d(TAG, "title html = "+d.getChildren().elementAt(j).toHtml());
							NodeList d2 = d.getChildren().elementAt(j).getChildren();
							for(Node d3 : d2.toNodeArray()){
								if( d3 instanceof HeadingTag){
									HeadingTag d4 = ((HeadingTag) d3);
									String Tag = d4.getTagName();
									if("H1".equals(Tag)){
										LinkTag link = (LinkTag)d4.getChildren().elementAt(0);
										bl.bookUrl = link.getLink();
										bl.bookName = link.toPlainTextString();
									} else if("H2".equals(Tag)){
										bl.bookAuthor = d4.getChildren().elementAt(0).toPlainTextString();
									} else if("H3".equals(Tag)){
										bl.bookAbout = d3.getLastChild().toPlainTextString();
									}
								}
							}
						}
					}
					list.add(bl);
				}
			}

		} catch (ParserException e) {
			Log.e(TAG, "getBookList ParserException");
			e.printStackTrace();
		}

		return list;
	}
}
