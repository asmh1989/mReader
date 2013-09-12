package com.sun.mybookreader.mt;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.sun.mybookreader.html.LinkTagSet;
import com.sun.mybookreader.utils.Log;

public class MtParser {
	private static final String TAG = "SUNMtParser";

	private String mHtmlUrl;
	private Parser mParser;
	private static String mNextBookListUrl;
	private static boolean mhasNextBookListUrl;

	public MtParser() {

	}

	public MtParser(String url){
		parser(url);
	}

	private void parser(){
		try {
			mParser =  null;
			mParser = new Parser(mHtmlUrl);
		} catch (ParserException e) {	
			e.printStackTrace();
			Log.e(TAG, "ParserException"+e.toString());
		}
	}

	public void parser(String url){
		mHtmlUrl = url;
		parser();
	}

	public List<LinkTagSet> getBookCategory(String url){
		//		Log.d(TAG, "start getBookCategory "+mParser);
		List<LinkTagSet> list  = new ArrayList<LinkTagSet>();
		parser(url);
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

	public String getNextBookUrl(){
		return mNextBookListUrl;
	}
	
	public boolean hasNextBookUrl(){
		return mhasNextBookListUrl;
	}
	
	public List<MtBookUtil> getBookList(String url){
		//		Log.d(TAG, "open url = "+url);
		List<MtBookUtil> list = new ArrayList<MtBookUtil>();
		parser(url);
		
		NodeFilter filter = new HasAttributeFilter("class", "list_page");
		try {
			NodeList nodes = mParser.extractAllNodesThatMatch(filter);
			nodes = nodes.elementAt(0).getChildren();
			for(int i = 0; i < nodes.size(); i++){
				Node n = nodes.elementAt(i);
				if(n instanceof LinkTag){
					LinkTag l = (LinkTag)n;
					Log.d(TAG, "print = "+n.toPlainTextString()+" link = "+l.getLink());
					if(l.toPlainTextString().contains("下一页")){
						mNextBookListUrl = l.getLink();
					} else if(l.toPlainTextString().contains("尾页")){
						if(url.contains(mNextBookListUrl.trim())){
							mhasNextBookListUrl = false;
						} else {
							mhasNextBookListUrl = true;
						}
					}
				} else {
					if(n.toPlainTextString().contains("下一页")){
						mhasNextBookListUrl = false;
					}
				}
			}
			
			
		} catch (ParserException e) {
			Log.e(TAG, "getBookList ParserException");
			e.printStackTrace();
		}
		
		mParser.reset();
		
		filter = new HasAttributeFilter("class", "tutui");
		try {
			NodeList nodes = mParser.extractAllNodesThatMatch(filter);
			for(int i = 0; i < nodes.size(); i++){
				Node n = nodes.elementAt(i);
				if(n instanceof Div){
					Div d = (Div)n;
					MtBookUtil bl = new MtBookUtil();
					for (int j =0; j < d.getChildren().size(); j++){
						if(d.getChildren().elementAt(j).getText().contains("tutuiImg")){
							//							Log.d(TAG, "image html = "+d.getChildren().elementAt(j).toHtml());
							NodeList d2 = d.getChildren().elementAt(j).getChildren();
							while(!(d2.elementAt(0) instanceof ImageTag)){
								d2 = d2.elementAt(0).getChildren();
							}

							ImageTag img = (ImageTag)d2.elementAt(0);
							bl.setImageUrl(img.getImageURL());
						} else if(d.getChildren().elementAt(j).getText().contains("tutuiTitle")){
							//							Log.d(TAG, "title html = "+d.getChildren().elementAt(j).toHtml());
							NodeList d2 = d.getChildren().elementAt(j).getChildren();
							for(Node d3 : d2.toNodeArray()){
								if( d3 instanceof HeadingTag){
									HeadingTag d4 = ((HeadingTag) d3);
									String Tag = d4.getTagName();
									if("H1".equals(Tag)){
										LinkTag link = (LinkTag)d4.getChildren().elementAt(0);
										bl.setBookUrl(link.getLink());
										bl.setBookName(link.toPlainTextString());
									} else if("H2".equals(Tag)){
										bl.setBookAuthor(d4.getChildren().elementAt(0).toPlainTextString());
									} else if("H3".equals(Tag)){
										bl.setBookAbout(d3.getLastChild().toPlainTextString());
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

	public MtBookDetail getBookDetail(String url){
		Log.d(TAG, ".......getBookDetail.... url = "+url);
		parser(url);
		MtBookDetail mbd = new MtBookDetail();

		NodeFilter filter = new HasAttributeFilter("class", "navigation");
		try {
			NodeList nodes = mParser.extractAllNodesThatMatch(filter);
			String str = nodes.elementAt(0).toPlainTextString();
			mbd.bookName = str.split(";")[str.split(";").length - 1];
			Log.d(TAG, "get book name = "+mbd.bookName);
		} catch (ParserException e) {
			Log.e(TAG, "getBookDetail ParserException");
			e.printStackTrace();
		}
		mParser.reset();
		filter = new HasAttributeFilter("class", "content");
		try {
			NodeList nodes = mParser.extractAllNodesThatMatch(filter);
			if(nodes.size() == 0){
				return null;
			}
			NodeList n = nodes.elementAt(0).getChildren();
			for(Node d : n.toNodeArray()){
				if(d.getChildren() == null){
					continue;
				}
				for (int j =0; j < d.getChildren().size(); j++){
					String divClass = d.getChildren().elementAt(j).getText();
					if(divClass.contains("movieInfo")){
						NodeList d2 = d.getChildren().elementAt(j).getChildren();
						for(Node d3 : d2.toNodeArray()){
							if(d3.getText().contains("moviePic")){
								ImageTag img = (ImageTag)d3.getFirstChild();
								mbd.imageUrl = img.getImageURL();
								Log.d(TAG, "mbd.imageUrl = "+mbd.imageUrl);
							} else if(d3.getText().contains("movieDetail")){
								int size = d3.getChildren().size();
								for(int num = 0; num <size; num++){
									if(d3.getChildren().elementAt(num) instanceof ParagraphTag){
										String s = d3.getChildren().elementAt(num).toPlainTextString();
										mbd.bookDetail += s;
										Log.d(TAG, "s = "+s);
										if(s.contains("作者:")){
											mbd.bookAuthor = s.split(":")[1];
											Log.d(TAG, "mbd.bookAuthor  = "+mbd.bookAuthor);
										} else if(s.contains("状态:")){
											if(!s.contains("连载")){
												mbd.isFinish = true;
											}
										} else if(s.contains("更新时间:")){
											mbd.bookUpdateTime = s.substring(s.indexOf(":")+1);
											Log.d(TAG, "mbd.bookUpdateTime = "+mbd.bookUpdateTime);
										}

										if(num != size - 1){
											mbd.bookDetail += "\n";
										}
									}
								}

								Log.d(TAG, "mbd.bookDetail = "+mbd.bookDetail);
							}
						}
					} else if(divClass.contains("movieIntro")){
						NodeList d2 = d.getChildren().elementAt(j).getChildren();
						for(Node d3 : d2.toNodeArray()){
							if(d3 instanceof ParagraphTag){
								mbd.bookAbout = d3.toPlainTextString();
								Log.d(TAG, "mbd.bookAbout = "+mbd.bookAbout);
							}
						}
					} else if(divClass.contains("book_listtext")){
						NodeList d2 = d.getChildren().elementAt(j).getChildren();
						for(Node d3 : d2.toNodeArray()){
							if(d3.getChildren() == null){
								continue;
							}
							if(d3.getFirstChild() instanceof LinkTag){
								LinkTag d4 = (LinkTag) d3.getFirstChild();

								BookChapter ch = new BookChapter();
								String name = d4.toPlainTextString();
								name = name.contains("/") ? name.substring(0, name.indexOf("/")) : name;
//								Log.d(TAG,  "plaint text = "+name +" ## Bullet link = "+d4.getLink());
								ch.setBookChapter(d4.toPlainTextString());
								ch.setBookChapterUrl(d4.getLink());
								ch.setIsDownload(false);
								mbd.bookChapters.add(ch);
							}
						}
					}
				}
			}

		} catch (ParserException e) {
			Log.e(TAG, "getBookList ParserException");
			e.printStackTrace();
		}

		return mbd;
	}

	public String getBookChapterContent(String url) {
		String str = "";
		parser(url);
		NodeFilter filter = new HasAttributeFilter("id", "booktext");
		try {
			NodeList nodes = mParser.extractAllNodesThatMatch(filter);
			if(nodes.size() == 0){
				return null;
			}
			NodeList n = nodes.elementAt(0).getChildren();
			for(Node d : n.toNodeArray()){
				if(d.toPlainTextString().length() > 0){
					str += d.toPlainTextString()+'\n';
				}
			}

			//			str = nodes.elementAt(0).toPlainTextString();
		} catch (ParserException e) {
			//			Log.e(TAG, "getBookChapterContent ："+e.toString());
			e.printStackTrace();
		}
		Log.e(TAG, "getBookChapterContent ：\n"+str);
		return str;
	}
}
