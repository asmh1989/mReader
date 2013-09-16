package com.sun.mreader.database;

import com.sun.mreader.utils.GlobalContext;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper singleton = null;

    private static final String DATABASE_NAME = "mreader.db";
    private static final int DATABASE_VERSION = 1;

    static final String CREATE_BOOK_TABLE_SQL = "create table " + BookTable.TABLE_NAME
            + "("
            + BookTable.BOOK_ID + " integer primary key autoincrement,"
            + BookTable.BOOK_NAME + " text,"
            + BookTable.BOOK_AUTHOR + " text,"
            + BookTable.BOOK_URL + " text,"
            + BookTable.BOOK_IMAGE_URL + " text,"
            + BookTable.BOOK_ABOUT + " integer,"
            + BookTable.BOOK_CHAPTERS + " integer,"
            + BookTable.BOOK_UPDATE_TIME +" text,"
            + BookTable.BOOK_IS_FINISH +" boolean"
            + ");";
    
    static final String CREATE_BOOKCHAPTERS_TABLE_SQL = "create table " + BookChaptersTable.TABLE_NAME 
    		+ "("
    		+ BookChaptersTable._ID + " integer primary key autoincrement,"
    		+ BookChaptersTable.BOOK_ID + " integer,"
    		+ BookChaptersTable.BOOK_CHAPTER + " text,"
    		+ BookChaptersTable.BOOK_CHAPTER_URL +" text,"
    		+ BookChaptersTable.BOOK_CHAPTER_DOWNLOAD + " boolean"
    		+");";


    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_BOOK_TABLE_SQL);
        db.execSQL(CREATE_BOOKCHAPTERS_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 19:
            case 26:
            case 30:
                deleteAllTable(db);
                onCreate(db);
            default:
                deleteAllTableExceptBook(db);
        }


    }

    public static synchronized DatabaseHelper getInstance() {
        if (singleton == null) {
            singleton = new DatabaseHelper(GlobalContext.getInstance());
        }
        return singleton;
    }

    private void deleteAllTableExceptBook(SQLiteDatabase db) {

        db.execSQL("DROP TABLE IF EXISTS " + BookTable.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + BookChaptersTable.TABLE_NAME);

    }

    private void deleteAllTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + BookTable.TABLE_NAME);

        deleteAllTableExceptBook(db);
    }
}
