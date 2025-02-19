package com.sp.chatmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class createFolder extends AppCompatActivity {

    EditText name;
    EditText search;
    Button search_btn;
    Button save_btn;
    createFolderAdapter adapter;
    vocabHelper helper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_folder);
        name=findViewById(R.id.folderName);
        search=findViewById(R.id.query);
        search_btn=findViewById(R.id.search);
        save_btn=findViewById(R.id.save_button);
        RecyclerView rv=findViewById(R.id.searchView);
        helper=new vocabHelper(createFolder.this, "japanese");
        adapter=new createFolderAdapter(createFolder.this, helper);
        if (getIntent().hasExtra("folder")){
            String folder=getIntent().getExtras().getString("folder");
            adapter.setFolderCards(helper.getCardsInFolder(folder));
            name.setText(folder);
            name.setEnabled(false);
        }
        rv.setHasFixedSize(true);
        rv.setAdapter(adapter);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.search(search.getText().toString());
            }
        });
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String folderName=name.getText().toString();
                if(!folderName.matches("[A-Za-z0-9]+")||folderName.length()==0){
                    Toast.makeText(createFolder.this, "Name must not be empty and can only contain alphanumeric charaters",Toast.LENGTH_LONG).show();
                    return;
                }
                List<Card> cardl=adapter.getFolderCards();
                helper.deleteFolder(folderName);
                for (int i=0;i<cardl.size();i++){
                    helper.addToFolder(cardl.get(i).id,folderName);
                }
                startActivity(new Intent(createFolder.this,cards.class));
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(this));

    }
}