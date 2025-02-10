package com.sp.chatmate;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class cards extends AppCompatActivity {
    vocabHelper helper;
    RecyclerView rv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards);
        rv=findViewById(R.id.rvc);
        Log.i("error", String.valueOf(rv.getVisibility()));
        helper=new vocabHelper(cards.this, "japanese");
        List<folderModel> folders=new ArrayList<>();
        folders.add(new folderModel("Seen",helper.getCards("seen")));
        folders.add(new folderModel("Learning",helper.getCards("Learning")));
        folders.add(new folderModel("all words",helper.getCards("Learning")));
        cardsAdapater adapter=new cardsAdapater(cards.this, folders);
        rv.setHasFixedSize(true);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));

    }
}