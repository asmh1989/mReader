package com.sun.mreader.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sun.mreader.mt.BookChapter;
import com.sun.mreader.utils.Log;

public class BookChaptersDBTask {
	private BookChaptersDBTask() {
	}

	private static SQLiteDatabase getWsd() {

		DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
		return databaseHelper.getWritableDatabase();
	}

	private static SQLiteDatabase getRsd() {
		DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
		return databaseHelper.getReadableDatabase();
	}

	public static void addBookChapters(List<BookChapter> chapters) {
		
		for(BookChapter chapter : chapters){
			addBookChapter(chapter);
		}
	}
	
	public static void addBookChapter(BookChapter chapter){
		ContentValues cv = new ContentValues();
		cv.put(BookChaptersTable._ID, chapter.get_ID());
		cv.put(BookChaptersTable.BOOK_ID, chapter.getBookID());
		cv.put(BookChaptersTable.BOOK_CHAPTER, chapter.getBookChapter());
		cv.put(BookChaptersTable.BOOK_CHAPTER_URL, chapter.getBookChapterUrl());
		cv.put(BookChaptersTable.BOOK_CHAPTER_DOWNLOAD, chapter.getIsDownload());

		Cursor c = getWsd().query(BookChaptersTable.TABLE_NAME, null, BookChaptersTable._ID + "=?",
				new String[]{chapter.getBookID()}, null, null, null);

		if (c != null && c.getCount() > 0) {
			String[] args = {chapter.getBookID()};
			getWsd().update(BookChaptersTable.TABLE_NAME, cv, BookChaptersTable.BOOK_ID + "=?", args);
		} else {
			getWsd().insert(BookChaptersTable.TABLE_NAME,
					BookChaptersTable._ID, cv);
		}
	}

	public static List<BookChapter> getBookChapters(String bookid) {
		List<BookChapter> chapters = new ArrayList<BookChapter>();
		String sql = "select * from " + BookChaptersTable.TABLE_NAME+" where " + BookChaptersTable.BOOK_ID + " = " + bookid;;
		Cursor c = getRsd().rawQuery(sql, null);
		while (c.moveToNext()) {
			BookChapter chapter = new BookChapter();
			int colid = c.getColumnIndex(BookChaptersTable.BOOK_CHAPTER);
			chapter.setBookChapter(c.getString(colid));

			colid = c.getColumnIndex(BookChaptersTable.BOOK_CHAPTER_URL);
			chapter.setBookChapterUrl(c.getString(colid));

			colid = c.getColumnIndex(BookChaptersTable.BOOK_CHAPTER_DOWNLOAD);
			chapter.setIsDownload(Boolean.valueOf(c.getString(colid)));

			chapters.add(chapter);
		}
		c.close();
		return chapters;
	}

	public static void deleteAllOneBookChapters(String bookid) {
		String sql = "delete from " + BookChaptersTable.TABLE_NAME + " where " + BookChaptersTable.BOOK_ID+ " in " + "(" + bookid + ")";

		getWsd().execSQL(sql);
	}
}
