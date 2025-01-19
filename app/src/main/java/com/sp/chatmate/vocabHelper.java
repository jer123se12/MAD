package com.sp.chatmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class vocabHelper extends SQLiteOpenHelper {
    private static final  String DATABASE_NAME="vocab.db";
    private static final int SCHEMA_VERSION = 1;

    public vocabHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE VocabList (_id INTEGER PRIMARY KEY AUTOINCREMENT, term TEXT, definition TEXT, times_seen INTEGER, time_last_accessed INTEGER, last_retention REAL, memory_stability REAL, times_glanced INTEGER, has_seen_flashcard BOOLEAN, has_glanced BOOLEAN)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public Cursor getAll(){
        return (getReadableDatabase().rawQuery("SELECT * FROM VocabList",null));
    }
    public void insert(String term, String definition){
        ContentValues cv=new ContentValues();

        cv.put("term", term);
        cv.put("definition", definition);
        cv.put("times_seen", 0);
        cv.put("time_last_accessed", 0);
        cv.put("last_retention", 0);
        cv.put("memory_stability", 1);
        cv.put("times_glanced", 0);
        cv.put("has_seen_flashcard", false);
        cv.put("has_glanced", false);
        getWritableDatabase().insert("VocabList","term", cv );

    }
    

}
