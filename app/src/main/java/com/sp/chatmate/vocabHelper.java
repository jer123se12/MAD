package com.sp.chatmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class vocabHelper extends SQLiteOpenHelper {
    private String db;
    private static final int levels = 5;
    private static final int[] intervals = {1, 2, 3, 7, 12};

    private static final int SCHEMA_VERSION = 1;

    public vocabHelper(@Nullable Context context, @Nullable String name) {
        super(context, name, null, SCHEMA_VERSION);
        db=name;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE allWords (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT," + " term TEXT," + " definition TEXT," + " frequency INTEGER," + " status INTEGER);");
        db.execSQL("CREATE TABLE seen (" + "id INTEGER," + " times_seen TEXT);");
        db.execSQL("CREATE TABLE learning (" + "id INTEGER," + " next_review INTEGER," + " box INTEGER);");
        db.execSQL("CREATE TABLE learnt (" + "id INTEGER," + " last_reviewed INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Cursor getAll(String table) {
        return (getReadableDatabase().rawQuery("SELECT * FROM " + table, null));
    }

    public void create(String term, String definition, int frequency) {
        ContentValues cv = new ContentValues();
        cv.put("term", term);
        cv.put("definition", definition);
        cv.put("frequency", frequency);
        cv.put("status", 0);
        getWritableDatabase().insert("allWords", "term", cv);
    }

    // used when seen on video
    public void seen(String term) {
        SQLiteDatabase db = getReadableDatabase();
        // get id
        String query = "SELECT _id, status from allWords WHERE term='" + term + "'";
        Cursor results = db.rawQuery(query, null);

        Log.i("mine", cursorToString(results));
        Log.i("mine", String.valueOf(results.getColumnIndex("_id")));
        if (!results.moveToFirst() || results.getInt(1) > 1) {
            Log.e("mine", "no such word or already started learning");
        }
        ;
        int id = results.getInt(0);
        results.close();
        // check if id exists
        boolean isInside = (db.rawQuery("SELECT id from seen where id=" + String.valueOf(id), null).moveToFirst());
        if (isInside) {
            // just update
            getWritableDatabase().execSQL("UPDATE seen SET times_seen=times_seen+1 where id=" + String.valueOf(id));
        } else {
            // create new
            ContentValues cv = new ContentValues();
            cv.put("id", id);
            cv.put("times_seen", 1);
            getWritableDatabase().insert("seen", null, cv);
            // update status in word list
            updateStatus(id, 1);
        }
    }

    public Card getCard(int id){
        Cursor Card = getReadableDatabase().rawQuery("SELECT term, definition from allWords where id=" + String.valueOf(id), null);
        return new Card(db, Card.getColumnName(0), Card.getColumnName(1), id);
    }


    // called after the flashcards
    public void learn(int id, boolean remembered) {
        Cursor Card = getReadableDatabase().rawQuery("SELECT status from allWords where id=" + String.valueOf(id), null);
        Card.moveToFirst();
        switch (Card.getInt(0)) {
            case 1:
                if (remembered) {
                    // move to learning
                    int box = 3;
                    updateStatus(id, 2);
                    ContentValues cv = new ContentValues();
                    cv.put("id", id);
                    cv.put("next_review", getNextDate(box));
                    cv.put("box", box);
                    getWritableDatabase().insert("learning", null, cv);
                    deleteRow("seen", id);
                }
                break;
            case 2:
                if (remembered) {
                    // check if in box 7
                    Cursor result = getReadableDatabase().rawQuery("SELECT * from learning where id=" + String.valueOf(id), null);
                    result.moveToFirst();
                    if (result.getInt(2) >= (levels - 1)) {
                        //move to learnt
                        ContentValues cv = new ContentValues();
                        cv.put("id", result.getInt(0));
                        cv.put("last_reviewed", System.currentTimeMillis() / 1000L);
                        // add to learnt table and remove from learning table
                        getWritableDatabase().insert("learnt", null, cv);
                        deleteRow("learning", id);
                        updateStatus(id, 3);
                    } else {
                        // update box
                        getWritableDatabase().execSQL("UPDATE learning SET box=box+1 where id=" + String.valueOf(id));
                    }
                    result.close();
                } else {
                    // set back to 1
                    getWritableDatabase().execSQL("UPDATE learning SET box=1 where id=" + String.valueOf(id));
                }
                break;
            case 3:
                if (remembered == false) {
                    updateStatus(id, 2);
                    deleteRow("learnt", id);
                    ContentValues cv = new ContentValues();
                    cv.put("id", id);
                    cv.put("next_review", getNextDate(1));
                    cv.put("box", 1);
                    getWritableDatabase().insert("learning", null, cv);
                }
                break;
        }
    }

    // get todays cards
    public List<Card> getCardsToday(int revisionCards, int newCards) {
        // revision
        Cursor dueCards = getReadableDatabase().rawQuery("SELECT id from learning where next_review<" + String.valueOf(System.currentTimeMillis() / 1000L) + "LIMIT " + String.valueOf(revisionCards), null);
        List<Card> result=cursorToCards(dueCards);

        // new cards
        Cursor nC=getReadableDatabase().rawQuery("SELECT id from allWords where status=0 ORDER BY frequency LIMIT "+String.valueOf(newCards), null);
        result.addAll(cursorToCards(nC));
        return result;
    }

    private List<Card> cursorToCards(Cursor cards){
        List<Card> result=new ArrayList<Card>();
        int num=cards.getCount();
        for (int i=0;i<num;i++){
            cards.move(i);
            result.add(getCard(cards.getInt(0)));
        }
        return result;
    }



    // Download handler
    public void addXMLToDatabase(String filePath){

    }








    // helper functions
    private void updateStatus(int id, int status) {
        getWritableDatabase().execSQL("UPDATE allWords SET status=" + String.valueOf(status) + " where _id=" + String.valueOf(id));
    }

    private int getNextDate(int beforebox) {
        if (beforebox >= 7) {
            Log.e("mine", "no more possible already at last level");
            return -1;
        }
        return (int) ((System.currentTimeMillis() / 1000L)) + (intervals[beforebox] * 24 * 60 * 60);

    }

    private void deleteRow(String table, int id) {
        getWritableDatabase().execSQL("DELETE FROM " + table + " WHERE id=" + String.valueOf(id));
    }


    public String cursorToString(Cursor cursor) {
        String cursorString = "";
        if (cursor.moveToFirst()) {
            String[] columnNames = cursor.getColumnNames();
            for (String name : columnNames)
                cursorString += String.format("%s ][ ", name);
            cursorString += "\n";
            do {
                for (String name : columnNames) {
                    cursorString += String.format("%s ][ ", cursor.getString(cursor.getColumnIndex(name)));
                }
                cursorString += "\n";
            } while (cursor.moveToNext());
        }
        return cursorString;
    }

}
