
package com.sp.chatmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class LanguageLearn extends AppCompatActivity {

    private Button btnNext;
    private List<RadioButton> radioButtons;
    private String selectedLanguage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.language_learn);

        // Initialize Views
        btnNext = findViewById(R.id.btn_next);
        btnNext.setEnabled(false);

        // Initialize list of RadioButtons
        radioButtons = new ArrayList<>();
        radioButtons.add(findViewById(R.id.radio_chinese));
        radioButtons.add(findViewById(R.id.radio_english));
        radioButtons.add(findViewById(R.id.radio_spanish));
        radioButtons.add(findViewById(R.id.radio_thai));
        radioButtons.add(findViewById(R.id.radio_japanese));
        radioButtons.add(findViewById(R.id.radio_korean));
        radioButtons.add(findViewById(R.id.radio_french));
        radioButtons.add(findViewById(R.id.radio_german));

        // Set click listener for each radio button
        for (RadioButton radioButton : radioButtons) {
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Uncheck all RadioButtons
                    for (RadioButton rb : radioButtons) {
                        rb.setChecked(false);
                    }
                    // Check the clicked RadioButton
                    ((RadioButton) v).setChecked(true);
                    selectedLanguage = ((RadioButton) v).getText().toString();
                    btnNext.setEnabled(true);
                }
            });
        }

        // Next Button Click Listener
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLanguage != null) {
                    // Show confirmation message
                    Toast.makeText(LanguageLearn.this, "Selected Language: " + selectedLanguage, Toast.LENGTH_SHORT).show();

                    // Navigate to NextActivity and pass selected language
                    Intent intent = new Intent(LanguageLearn.this, StartScratch.class);
                    intent.putExtra("selected_language", selectedLanguage);
                    startActivity(intent);
                }
            }
        });
    }
}
