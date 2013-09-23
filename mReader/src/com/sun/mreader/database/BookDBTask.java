package com.sun.mreader.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.sun.mreader.mt.MtBookUtil;

public class BookDBTask {
	private BookDBTask() {
	}

	private static SQLiteDatabase getWsd() {

		DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
		return databaseHelper.getWritableDatabase();
	}

	private static SQLiteDatabase getRsd() {
		DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
		return databaseHelper.getReadableDatabase();
	}

	public static void addOrUpdateBook(MtBookUtil book) {
		ContentValues cv = new ContentValues();
		cv.put(BookTable.BOOK_ID, book.getBookID());
		cv.put(BookTable.BOOK_NAME, book.getBookName());
		cv.put(BookTable.BOOK_AUTHOR, book.getBookAuthor());
		cv.put(BookTable.BOOK_URL, book.getBookUrl());
		cv.put(BookTable.BOOK_ABOUT, book.getBookAbout());
		cv.put(BookTable.BOOK_IMAGE_URL, book.getImageUrl());
		cv.put(BookTable.BOOK_CHAPTERS, book.getBookChapters());
		cv.put(BookTable.BOOK_IS_FINISH, book.getBookIsFinish());
		cv.put(BookTable.BOOK_UPDATE_TIME, book.getBookUpdateTime());
		cv.put(BookTable.BOOK_ADD_SHELF_TIME, book.getAddTime());
		cv.put(BookTable.BOOK_LASTREAD, book.getBookLastRead());

		Cursor c = getWsd().query(BookTable.TABLE_NAME, null, BookTable.BOOK_ID + "=?",
				new String[]{book.getBookID()}, null, null, null);

		if (c != null && c.getCount() > 0) {
			String[] args = {book.getBookID()};
			getWsd().update(BookTable.TABLE_NAME, cv, BookTable.BOOK_ID + "=?", args);
		} else {
			getWsd().insert(BookTable.TABLE_NAME,
					BookTable.BOOK_ID, cv);
		}

	}

	public static List<MtBookUtil> getBookList() {
		List<MtBookUtil> bookList = new ArrayList<MtBookUtil>();
		String sql = "select * from " + BookTable.TABLE_NAME;
		Cursor c = getWsd().rawQuery(sql, null);
		while (c.moveToNext()) {
			MtBookUtil book = new MtBookUtil();
			int colid = c.getColumnIndex(BookTable.BOOK_NAME);
			book.setBookName(c.getString(colid));

			colid = c.getColumnIndex(BookTable.BOOK_IMAGE_URL);
			book.setImageUrl(c.getString(colid));

			colid = c.getColumnIndex(BookTable.BOOK_AUTHOR);
			book.setBookAuthor(c.getString(colid));

			colid = c.getColumnIndex(BookTable.BOOK_URL);
			book.setBookUrl(c.getString(colid));
			
			colid = c.getColumnIndex(BookTable.BOOK_CHAPTERS);
			book.setBookChapters(Integer.parseInt(c.getString(colid)));

			bookList.add(book);
		}
		c.close();
		return bookList;
	}

	public static MtBookUtil getBook(String id) {
		String sql = "select * from " + BookTable.TABLE_NAME + " where " + BookTable.BOOK_ID + " = " + id;
		Cursor c = getRsd().rawQuery(sql, null);
		if (c.moveToNext()) {
			MtBookUtil book = new MtBookUtil();
			int colid = c.getColumnIndex(BookTable.BOOK_NAME);
			book.setBookName(c.getString(colid));

			colid = c.getColumnIndex(BookTable.BOOK_IMAGE_URL);
			book.setImageUrl(c.getString(colid));

			colid = c.getColumnIndex(BookTable.BOOK_AUTHOR);
			book.setBookAuthor(c.getString(colid));

			colid = c.getColumnIndex(BookTable.BOOK_URL);
			book.setBookUrl(c.getString(colid));

			colid = c.getColumnIndex(BookTable.BOOK_ADD_SHELF_TIME);
			book.setBookAddTime(c.getString(colid));
			
			colid = c.getColumnIndex(BookTable.BOOK_LASTREAD);
			book.setBookLastRead(c.getString(colid));
			
			colid = c.getColumnIndex(BookTable.BOOK_CHAPTERS);
			book.setBookChapters(Integer.parseInt(c.getString(colid)));
			return book;
		}
		return null;
	}
	
    public static List<MtBookUtil> removeBook(Set<String> checkedItemPosition) {
        String[] args = checkedItemPosition.toArray(new String[0]);
        String asString = Arrays.toString(args);
        asString = asString.replace("[", "(");
        asString = asString.replace("]", ")");

        String sql = "delete from " + BookTable.TABLE_NAME + " where " + BookTable.BOOK_ID + " in " + asString;

        getWsd().execSQL(sql);

        for (String id : args) {
            BookChaptersDBTask.deleteAllOneBookChapters(id);
        }

        return getBookList();
    }
    
    public static Cursor query(String [] projection){
		return  getRsd().query(BookTable.TABLE_NAME, projection, null,
				null, null, null, null);
    }
}
