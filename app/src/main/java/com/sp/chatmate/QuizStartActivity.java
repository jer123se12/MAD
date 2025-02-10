package com.sp.chatmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.squareup.picasso.Picasso;

public class QuizStartActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_start);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // Open Navigation Drawer when Menu Icon is Clicked
        findViewById(R.id.menu_icon).setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        // Load user profile details in the navigation header
        loadUserProfile();

        // Handle Navigation Item Clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_flashcards) {
                    startActivity(new Intent(QuizStartActivity.this, FlashCards.class));
                } else if (id == R.id.nav_quiz) {
                    drawerLayout.closeDrawer(navigationView); // Stay on the same page
                } else if (id == R.id.nav_immersion) {
                    startActivity(new Intent(QuizStartActivity.this, ImmersionActivity.class));
                }

                return true;
            }
        });
    }

    // Load Profile Image & Username from Firebase
    private void loadUserProfile() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            databaseReference.child(userId).get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                    // Get header view from NavigationView
                    View headerView = navigationView.getHeaderView(0);
                    ImageView profileImage = headerView.findViewById(R.id.profile_image);
                    TextView tvUsername = headerView.findViewById(R.id.tv_username);

                    tvUsername.setText(username);

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
//                        Picasso.get().load(profileImageUrl).into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.default_profile_background);
                    }
                }
            });
        }
    }
}