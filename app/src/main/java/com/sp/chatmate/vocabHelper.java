package com.sp.chatmate;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class vocabHelper extends SQLiteOpenHelper {

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE VocabList (_id INTEGER PRIMARY KEY AUTOINCREMENT, term TEXT, definition TEXT, times_seen INTEGER, time_last_accessed INTEGER, last_retention REAL, memory_stability REAL, times_glanced INTEGER, has_seen_flashcard BOOLEAN)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    
}
