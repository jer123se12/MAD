package com.sp.chatmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class cards extends AppCompatActivity {
    vocabHelper helper;
    RecyclerView rv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards);
        new navDrawerInit(
                cards.this,
                findViewById(R.id.nav_view),
                FirebaseAuth.getInstance(),
                FirebaseDatabase.getInstance().getReference("users"),
                findViewById(R.id.cardsLayout),
                findViewById(R.id.menu_icon),
                menuItems.FLASHCARDS
        ).init();
        rv=findViewById(R.id.rvc);
        Log.i("error", String.valueOf(rv.getVisibility()));
        helper=new vocabHelper(cards.this, "japanese");
        List<folderModel> folders=new ArrayList<>();
        List<String> currectFolders=helper.getFolders();
        for (int i=0; i<currectFolders.size();i++){
            folders.add(new folderModel(currectFolders.get(i), helper.getCardsInFolder(currectFolders.get(i))));
        }
        cardsAdapater adapter=new cardsAdapater(cards.this, folders);
        rv.setHasFixedSize(true);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(cards.this, createFolder.class));
            }
        });

    }
}