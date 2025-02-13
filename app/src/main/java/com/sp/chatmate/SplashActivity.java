package com.sp.chatmate;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Lottie Animation View
        LottieAnimationView animationView = findViewById(R.id.lottieAnimationView);
        SharedPreferences prefs=getSharedPreferences("USER", Context.MODE_PRIVATE);
        MediaPlayer.create(this,R.raw.sound).start();

        // Delay for 3 seconds and move to MainActivity
        new Handler().postDelayed(() -> {
            if(prefs.getString("firstTime",null)==null){
                prefs.edit().putString("firstTime","yes").apply();
            startActivity(new Intent(SplashActivity.this, onboarding.class));
            finish();
            }else{
                startActivity(new Intent(SplashActivity.this, FlashCards.class));
            }
        }, 3000);
    }
}