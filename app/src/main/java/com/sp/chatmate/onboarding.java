package com.sp.chatmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class onboarding extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity); // Use your correct layout file name

        // Find the Next button
        Button nextButton = findViewById(R.id.nextButton);

        // Set a click listener on the Next button
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to LanguageSelection activity
                Intent intent = new Intent(onboarding.this, LanguageSelection.class);
                startActivity(intent);
                // Optionally finish the onboarding activity so the user cannot go back
                finish();
            }
        });
    }
}