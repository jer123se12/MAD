package com.sp.chatmate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
enum menuItems{
    FLASHCARDS,
    QUIZ,
    IMMERSION,
    TODAYSFlASHCARD
}

public class navDrawerInit {
    DrawerLayout DL;
    NavigationView NV;
    FirebaseAuth FA;
    DatabaseReference DR;
    Context context;
    ImageView menuBtn;
    menuItems currentPage;
    public navDrawerInit(Context context, NavigationView nv,FirebaseAuth fa,DatabaseReference dr, DrawerLayout dl, ImageView menuBtn, menuItems currentPage){
        this.DL=dl;
        this.NV=nv;
        this.context=context;
        this.menuBtn=menuBtn;
        this.FA=fa;
        this.DR=dr;
        this.currentPage=currentPage;
    }
    public void init(){
        this.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DL.openDrawer(NV);
            }
        });

        NV.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Log.i("id", id + "");

                if (id == R.id.nav_flashcards) {

                    if (!currentPage.equals(menuItems.TODAYSFlASHCARD)) {
                        Log.i("nav", "toFlashcards");
                        context.startActivity(new Intent(context, FlashCards.class));
                    }
                    // Stay on the same screen
                } else if (id == R.id.nav_quiz) {
                    if (!currentPage.equals(menuItems.QUIZ)) {
                        Log.i("nav", "toQuiz");
                        context.startActivity(new Intent(context, QuizStartActivity.class));
                    }
                } else if (id == R.id.nav_immersion) {
                    if (!currentPage.equals(menuItems.IMMERSION)) {
                        Log.i("nav", "toImmerse");
                        context.startActivity(new Intent(context, ImmersionActivity.class));
                    }
                } else if (id==R.id.nav_folders) {
                    if (!currentPage.equals(menuItems.FLASHCARDS)) {
                        Log.i("nav", "toFolders");
                        context.startActivity(new Intent(context, cards.class));
                    }
                }else if(id==R.id.nav_logout){
                    FirebaseAuth.getInstance().signOut();
                    context.startActivity(new Intent(context, MainActivity.class));
                }


                DL.closeDrawers(); // Close drawer after selection
                return true;
            }
        });
        FirebaseUser user = FA.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DR.child(userId).get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                    // Get header view from NavigationView
                    View headerView = NV.getHeaderView(0);
                    ImageView profileImage = headerView.findViewById(R.id.profile_image);
                    TextView tvUsername = headerView.findViewById(R.id.tv_username);

                    tvUsername.setText(username);
                    byte[] decodedString = Base64.decode(profileImageUrl, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    profileImage.setImageBitmap(decodedByte); // Use local default image
                }
            });
        }else{
            Log.e("not found","username is null");
        }

    }
}
