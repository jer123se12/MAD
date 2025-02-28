package com.sp.chatmate;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;
//import com.squareup.picasso.Picasso;

public class FlashCards extends AppCompatActivity {
    private Animator frontanim;
    private Animator backanim;
    boolean isFront=true;
    List<Card> cards;
    vocabHelper helper;
    TextView frontTerm;
    TextView backTerm;
    TextView counter;
    TextView backDef;
    FrameLayout front;
    FrameLayout back;
    Button flip;
    Button hard;
    Button easy;
    LinearLayout backLL;
    int currentCard=0;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flashcards);
        // initialise nav button and drawer
        new navDrawerInit(
                FlashCards.this,
                findViewById(R.id.nav_view),
                FirebaseAuth.getInstance(),
                FirebaseDatabase.getInstance("https://langify-a017b-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users"),
                findViewById(R.id.drawer_layout),
                findViewById(R.id.menu_icon),
                menuItems.TODAYSFlASHCARD
        ).init();
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    tts.setLanguage(Locale.JAPANESE);
                }
            }
        });
        helper=new vocabHelper(this, "japanese");
        Bundle bundle=getIntent().getExtras();

        float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        front=findViewById(R.id.front);
        back=findViewById(R.id.back);
        flip=findViewById(R.id.flip_btn);
        hard=findViewById(R.id.hard_btn);
        easy=findViewById(R.id.easy_btn);
        Button sound=findViewById(R.id.psound);
        backLL=findViewById(R.id.backLL);
        frontTerm=findViewById(R.id.front_term);
        backTerm=findViewById(R.id.back_term);
        backDef=findViewById(R.id.back_definition);
        counter=findViewById(R.id.counter);

        front.setCameraDistance(8000*scale);
        back.setCameraDistance(8000*scale);
        frontanim= AnimatorInflater.loadAnimator(this,R.animator.front_animator);
        backanim= AnimatorInflater.loadAnimator(this,R.animator.back_animator);
        flip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!frontanim.isRunning()&& !backanim.isRunning()){
                    if(isFront){
                        back();
                        goToBack();
                    }else{
                        front();
                        goToFront();

                    }
                }
            }
        });
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts.speak(cards.get(currentCard).term,TextToSpeech.QUEUE_FLUSH,null);
            }
        });

        hard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.learn(cards.get(currentCard).id,false);
                if (currentCard<cards.size()-1) {
                    loadCard(currentCard + 1);
                }
            }
        });
        easy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.learn(cards.get(currentCard).id,true);
                if (currentCard<cards.size()-1) {
                    loadCard(currentCard + 1);
                }
            }
        });
        if (getIntent().hasExtra("folder")) {
            cards=helper.getCardsInFolder(bundle.getString("folder"));
            if (cards.size()>0) {
                loadCard(0);
            }
        }else{
            cards = helper.getCardsToday(5, 5);
            if (cards.size()>0) {
                loadCard(0);
            }
        }
    }
    void front(){
        flip.setVisibility(View.VISIBLE);
        backLL.setVisibility(View.INVISIBLE);

    }
    void back(){
        backLL.setVisibility(View.VISIBLE);
        flip.setVisibility(View.INVISIBLE);

    }
    void goToBack(){
        if (isFront) {
            frontanim.setTarget(front);
            backanim.setTarget(back);
            frontanim.start();
            backanim.start();
            isFront = false;
        }
    }
    void goToFront(){
        if (!isFront) {
            frontanim.setTarget(back);
            backanim.setTarget(front);
            frontanim.start();
            backanim.start();
            isFront = true;
        }
    }
    void loadCard(int i){
        currentCard=i;
        goToFront();
        frontanim.end();
        backanim.end();
        front();
        Card card=cards.get(i);
        Log.i("term", card.term);
        frontTerm.setText(card.term);
        backTerm.setText(card.term);
        backDef.setText(card.definition);
        counter.setText(String.valueOf(currentCard+1)+"/"+String.valueOf(cards.size()));
    }

}
