package com.sp.chatmate;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
class percentageHelper{
    HashMap<String, Integer> words=new HashMap<>();
    HashMap<Integer, Integer>idtostatus=new HashMap<>();
    public percentageHelper(HashMap<String, Integer> words,HashMap<Integer, Integer>idtostatus){
        this.words=words;
        this.idtostatus=idtostatus;

    }
    public Video getPercentage(Video video){
        String captions=video.captions;
        int LongestWord=5;
        List<Integer> seen=new ArrayList<>();
        List<Integer> numbers=new ArrayList<>();
        for (int i=0;i<4;i++){
            numbers.add(0);

        }
        for (int start=0;start<captions.length()-LongestWord;start++){
            for (int end=LongestWord;end>0;end--){
                if (words.containsKey(captions.substring(start,start+end))){
                    int id=words.get(captions.substring(start,start+end));
                    numbers.set(idtostatus.get(id),numbers.get(idtostatus.get(id))+1);
                    if (idtostatus.get(id)==0){
                        seen.add(id);
                    }
                }
            }
        }
        int total=0;
        for (int i=0;i<4;i++){
            Log.i("total", numbers.get(i).toString());
            total+=numbers.get(i);
        }
        float percentage=(float)(((float)numbers.get(1)*0.1)+(float) numbers.get(2)+(float)numbers.get(3))/(float)total;
        video.percentage=percentage;
        video.willSee=seen;
        return video;


    }


}
public class vocabHelper extends SQLiteOpenHelper {
    private String db;
    private static final int levels = 5;
    private static final int[] intervals = {1, 2, 3, 7, 12};

    private static final int SCHEMA_VERSION = 1;
    Context context;

    public vocabHelper(@Nullable Context context, @Nullable String name) {
        super(context, name, null, SCHEMA_VERSION);
        context=context;
        if((! (context instanceof DownloadManager ))&& getReadableDatabase().rawQuery("SELECT * FROM allWords",null).getCount()==0) {
            Log.i("herre","help");
            context.startActivity(new Intent(context, DownloadManager.class));
        }
        db=name;
    }


    public percentageHelper getPercentage() {
        int learning=0;
        HashMap<String, Integer> words=new HashMap<>();
        HashMap<Integer, Integer>idtostatus=new HashMap<>();
        Cursor allWords=getReadableDatabase().rawQuery("SELECT * FROM allWords ORDER BY length(term)", null);
        allWords.moveToFirst();
        for (int i=0;i<allWords.getCount();i++){
            words.put(allWords.getString(1),allWords.getInt(0));
            idtostatus.put(allWords.getInt(0), allWords.getInt(4));
            allWords.move(1);
        }
        return  new percentageHelper(words,idtostatus);



    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE allWords (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT," + " term TEXT," + " definition TEXT," + " frequency INTEGER," + " status INTEGER, "+"folder TEXT);");
        db.execSQL("CREATE TABLE seen (" + "id INTEGER," + " times_seen TEXT);");
        db.execSQL("CREATE TABLE learning (" + "id INTEGER," + " next_review INTEGER," + " box INTEGER);");
        db.execSQL("CREATE TABLE learnt (" + "id INTEGER," + " last_reviewed INTEGER);");
        db.execSQL("CREATE TABLE folders(" + "folder TEXT);" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void deleteALl(){
        getWritableDatabase().execSQL("DELETE FROM allWords");
        getWritableDatabase().execSQL("DELETE FROM seen");
        getWritableDatabase().execSQL("DELETE FROM learning");
        getWritableDatabase().execSQL("DELETE FROM learnt");
        getWritableDatabase().execSQL("DELETE FROM folders");
    }
    public Cursor getAll(String table) {
        return (getReadableDatabase().rawQuery("SELECT * FROM " + table+" LIMIT 1000", null));
    }
    public List<Card> getCards(String table){

        Cursor cards= getReadableDatabase().rawQuery("SELECT id from "+table+" LIMIT 1000;", null);
        return cursorToCards(cards);
    }
    public List<Card> getAllCards(){

        Cursor cards= getReadableDatabase().rawQuery("SELECT _id from allWords LIMIT 1000;", null);
        return cursorToCards(cards);
    }


    public void newFolder(String folder){
        getWritableDatabase().execSQL("INSERT INTO folders VALUES('"+folder+"')");
    }
    public void addToFolder(int id, String folder){
        if (!getFolders().contains(folder)){
            newFolder(folder);
        }
        Cursor cursor=getReadableDatabase().rawQuery("SELECT folder from allWords where _id="+String.valueOf(id), null);
        cursor.moveToFirst();
        String folders=cursor.getString(0);
        List<String> newFolder= new ArrayList<>();
        if (folders.length()>0) {
            newFolder = Arrays.asList(folders.split(","));
        }
        newFolder.add(folder);

        Log.i("value","UPDATE allWords SET folder='"+String.join(",",newFolder)+"' WHERE _id="+String.valueOf(id));
        getWritableDatabase().execSQL("UPDATE allWords SET folder='"+String.join(",",newFolder)+"' WHERE _id="+String.valueOf(id));
    }
    public List<String> getFolders(){
        Cursor folders=getReadableDatabase().rawQuery("SELECT * FROM folders", null);
        List<String> result=new ArrayList<String>();
        folders.moveToFirst();
        for (int i=0;i<folders.getCount();i++){
            result.add(folders.getString(0));
            folders.move(1);
        }
        return  result;
    }
    public List<Card> getCardsInFolder(String folder){
        Cursor cardsInFolder=getReadableDatabase().rawQuery("SELECT * FROM allWords WHERE (',' || folder || ',') LIKE '%,"+folder+",%'", null);
        List<Card> result=new ArrayList<>();
        cardsInFolder.moveToFirst();
        for (int i=0;i<cardsInFolder.getCount();i++){
            result.add(new Card(db, cardsInFolder.getString(1), cardsInFolder.getString(2), cardsInFolder.getInt(0)));
            cardsInFolder.move(1);
        }
        return  result;
    }
    public void deleteFolder(String folder){
        List<Card> cards=getCardsInFolder(folder);
        for(int i=0;i<cards.size();i++){
            removeFromFolder(cards.get(i).id,folder);
        }
    }

    public void create(String term, String definition, int frequency) {
        ContentValues cv = new ContentValues();
        cv.put("term", term);
        cv.put("definition", definition);
        cv.put("frequency", frequency);
        cv.put("status", 0);
        cv.put("folder", "");
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
            return;
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
    public void seen(int id) {
        SQLiteDatabase db = getReadableDatabase();
        // get id
        String query = "SELECT _id, status from allWords WHERE _id='" + String.valueOf(id) + "'";
        Cursor results = db.rawQuery(query, null);

        Log.i("mine", cursorToString(results));
        Log.i("mine", String.valueOf(results.getColumnIndex("_id")));
        if (!results.moveToFirst() || results.getInt(1) > 1) {
            return;
        }
        ;
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


    public List<Card> search(String term) {
        Cursor cards=getReadableDatabase().rawQuery("SELECT * from allWords where term like '%"+term+"%' limit 100", null);
        return cursorToCards(cards);
    }
    public Card getCard(int id){
        Cursor card = getReadableDatabase().rawQuery("SELECT term, definition from allWords where _id=" + String.valueOf(id), null);
        card.moveToFirst();
        Card c=new Card(db, card.getString(0), card.getString(1), id);
        card.close();
        return c;
    }


    // called after the flashcards
    public void learn(int id, boolean remembered) {
        Cursor Card = getReadableDatabase().rawQuery("SELECT status from allWords where _id=" + String.valueOf(id), null);
        Card.moveToFirst();
        switch (Card.getInt(0)) {
            case 0:{
                Log.i("here","help");
                updateStatus(id,2);
                int rbox=(remembered)?1:0;
                ContentValues cv = new ContentValues();
                cv.put("id", id);
                cv.put("next_review", getNextDate(rbox));
                cv.put("box", rbox);
                getWritableDatabase().insert("learning", null, cv);
                break;}
            case 1:
                if (remembered) {
                    Log.i("learn", "added to learning");
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
        Cursor dueCards = getReadableDatabase().rawQuery("SELECT id from learning where (`next_review`+0)<" + String.valueOf(System.currentTimeMillis() / 1000L) + " LIMIT " + String.valueOf(revisionCards), null);
        List<Card> result=cursorToCards(dueCards);

        // new cards
        Cursor nC=getReadableDatabase().rawQuery("SELECT _id from allWords where status=0 ORDER BY `frequency`+0 DESC LIMIT "+String.valueOf(newCards), null);
        result.addAll(cursorToCards(nC));
        return result;
    }
    public List<Card> getCardsFolder(int revisionCards, int newCards,String folder){
        Cursor dueCards = getReadableDatabase().rawQuery("SELECT id from learning LEFT JOIN allWords ON learning.id=allWords._id where (',' || folder || ',') LIKE '%,"+folder+",%' AND (`next_review`+0)<" + String.valueOf(System.currentTimeMillis() / 1000L) +
                " LIMIT " + String.valueOf(revisionCards), null);
        List<Card> result=cursorToCards(dueCards);

        // new cards
        Cursor nC=getReadableDatabase().rawQuery("SELECT _id from allWords where status=0 and (',' || folder || ',') LIKE '%,"+folder+",%' ORDER BY `frequency`+0 DESC LIMIT "+String.valueOf(newCards), null);
        result.addAll(cursorToCards(nC));
        return result;
    };

    private List<Card> cursorToCards(Cursor cards){
        List<Card> result=new ArrayList<Card>();
        int num=cards.getCount();
        Log.i("length", String.valueOf(cards.getCount()));
        cards.moveToFirst();
        for (int i=0;i<num;i++){
            Log.i("index", String.valueOf(i));
            result.add(getCard(cards.getInt(0)));
            cards.move(1);
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

    public void removeFromFolder(int id,String folder) {
        Cursor cursor=getReadableDatabase().rawQuery("SELECT folder from allWords where _id="+String.valueOf(id), null);
        cursor.moveToFirst();
        String[] folders=cursor.getString(0).split(",");
        List<String> newFolders=new ArrayList<>();
        for (int i=0;i<folders.length;i++) {
            if (!folders[i].equals(folder)){
                newFolders.add(folders[i]);
            }
        }
        getWritableDatabase().execSQL("UPDATE allWords SET folder='"+String.join(",",newFolders)+"' where _id="+String.valueOf(id));
    }
}
