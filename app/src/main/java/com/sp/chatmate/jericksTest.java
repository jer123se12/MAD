package com.sp.chatmate;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class jericksTest extends AppCompatActivity {
    vocabHelper helper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        helper=new vocabHelper(this, "japanese");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_jericks_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.create(((EditText)findViewById(R.id.terme)).getText().toString(),((EditText)findViewById(R.id.definition)).getText().toString(), 10);
                Log.i("mine",  cursorToString(helper.getAll("allWords")));
            }
        });
        findViewById(R.id.seen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.seen("aaaa");
            }
        });
        findViewById(R.id.rmb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.remembered(2, true);
            }
        });
        findViewById(R.id.learn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.learn(2, true);
            }
        });









        findViewById(R.id.showall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("mine",  cursorToString(helper.getAll("allWords")));
            }
        });
        findViewById(R.id.glanced).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("mine",  cursorToString(helper.getAll("seen")));
            }
        });
        findViewById(R.id.learning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("mine",  cursorToString(helper.getAll("learning")));
            }
        });
        findViewById(R.id.learnt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("mine",  cursorToString(helper.getAll("learnt")));
            }
        });
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
