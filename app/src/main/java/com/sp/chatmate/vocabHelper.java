package com.sp.chatmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class vocabHelper extends SQLiteOpenHelper {

    private static final int levels=5;
    private static final int[] intervals={1, 2, 3, 7, 12};

    private static final int SCHEMA_VERSION = 1;

    public vocabHelper(@Nullable Context context, @Nullable String name) {
        super(context, name, null, SCHEMA_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE allWords ("+
                "_id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                " term TEXT,"+
                " definition TEXT,"+
                " frequency TEXT,"+
                " status INTEGER);");
        db.execSQL("CREATE TABLE seen ("+
                "id INTEGER,"+
                " times_seen TEXT);");
        db.execSQL("CREATE TABLE learning ("+
                "id INTEGER,"+
                " next_review INTEGER,"+
                " box INTEGER);");
        db.execSQL("CREATE TABLE learnt ("+
                "id INTEGER,"+
                " last_reviewed INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public Cursor getAll(String table){
        return (getReadableDatabase().rawQuery("SELECT * FROM " + table,null));
    }
    public void create(String term, String definition, int frequency){
        ContentValues cv=new ContentValues();
        cv.put("term", term);
        cv.put("definition", definition);
        cv.put("frequency", frequency);
        cv.put("status", 0);
        getWritableDatabase().insert("allWords","term", cv );
    }
    // used when seen on video
    public void seen(String term){
        SQLiteDatabase db = getReadableDatabase();
        // get id
        String query="SELECT _id, status from allWords WHERE term='"+term+"'";
        Cursor results=db.rawQuery(query,null);

        Log.i("mine",cursorToString(results));
        Log.i("mine", String.valueOf(results.getColumnIndex("_id")));
        if(!results.moveToFirst() || results.getInt(1)>1){
            Log.e("mine", "no such word or already started learning");
        };
        int id=results.getInt(0);
        results.close();
        // check if id exists
        boolean isInside= (db.rawQuery("SELECT id from seen where id="+String.valueOf(id) ,null).moveToFirst());
        if (isInside){
            // just update
            getWritableDatabase().execSQL("UPDATE seen SET times_seen=times_seen+1 where id="+String.valueOf(id));
        }else{
            // create new

            ContentValues cv= new ContentValues();
            cv.put("id", id);
            cv.put("times_seen", 1);
            getWritableDatabase().insert("seen", null,cv);
            // update status in word list
            updateStatus(id, 1);
        }
    }

    // remembered
    public void remembered(int id, boolean remembered){
        if(remembered){
            // move to learning
            int box=3;
            updateStatus(id, 2);
            ContentValues cv=new ContentValues();
            cv.put("id", id);
            cv.put("next_review",getNextDate(3));
            cv.put("box",box);
            getWritableDatabase().insert("learning",null,cv);
            deleteRow("seen", id);
        }
    }


    // learn
    public void learn(int id, boolean remembered){
        if (remembered){
            // check if in box 7
            Cursor result=getReadableDatabase().rawQuery("SELECT * from learning where id="+String.valueOf(id),null);
            result.moveToFirst();
            if (result.getInt(2)>=(levels-1)){
                //move to learnt
                ContentValues cv=new ContentValues();
                cv.put("id", result.getInt(0));
                cv.put("last_reviewed", System.currentTimeMillis() / 1000L);
                // add to learnt table and remove from learning table
                getWritableDatabase().insert("learnt", null, cv);
                deleteRow("learning", id);
                updateStatus(id, 3);
            }else {
                // update box
                getWritableDatabase().execSQL("UPDATE learning SET box=box+1 where id=" + String.valueOf(id));
            }
            result.close();
        }else{
            // set back to 1
            getWritableDatabase().execSQL("UPDATE learning SET box=1 where id="+String.valueOf(id));
        }
    }


    // helper functions
    private void updateStatus(int id, int status){
        getWritableDatabase().execSQL("UPDATE allWords SET status="+String.valueOf(status)+" where _id="+String.valueOf(id));
    }
    private int getNextDate(int beforebox){
        if(beforebox>=7){
            Log.e("mine","no more possible already at last level");
            return -1;
        }
        return (int)((System.currentTimeMillis() / 1000L))+(intervals[beforebox]*24*60*60);

    }
    private void deleteRow( String table, int id){
        getWritableDatabase().execSQL("DELETE FROM "+table+" WHERE id="+String.valueOf(id));
    }







    public String cursorToString(Cursor cursor){
        String cursorString = "";
        if (cursor.moveToFirst() ){
            String[] columnNames = cursor.getColumnNames();
            for (String name: columnNames)
                cursorString += String.format("%s ][ ", name);
            cursorString += "\n";
            do {
                for (String name: columnNames) {
                    cursorString += String.format("%s ][ ",
                            cursor.getString(cursor.getColumnIndex(name)));
                }
                cursorString += "\n";
            } while (cursor.moveToNext());
        }
        return cursorString;
    }

}
