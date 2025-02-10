package com.sp.chatmate;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class Flashcard extends AppCompatActivity {
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashcard);
        helper=new vocabHelper(this, "japanese");
        cards=helper.getCardsToday(5,5);
        float scale = getApplicationContext().getResources().getDisplayMetrics().density;
         front=findViewById(R.id.front);
         back=findViewById(R.id.back);
        flip=findViewById(R.id.flip_btn);
        hard=findViewById(R.id.hard_btn);
        easy=findViewById(R.id.easy_btn);
        backLL=findViewById(R.id.backLL);
        frontTerm=findViewById(R.id.front_term);
        backTerm=findViewById(R.id.back_term);
        backDef=findViewById(R.id.back_definition);
        counter=findViewById(R.id.counter);

        front.setCameraDistance(8000*scale);
        back.setCameraDistance(8000*scale);
        frontanim= AnimatorInflater.loadAnimator(this,R.animator.front_animator);
        backanim= AnimatorInflater.loadAnimator(this,R.animator.back_animator);
        loadCard(0);
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