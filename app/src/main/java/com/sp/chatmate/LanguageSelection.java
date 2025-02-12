package com.sp.chatmate;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageSelection extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LanguageAdapter adapter;
    private List<String> languages;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.language_selection);

        // Initialize the list of languages
        languages = new ArrayList<>();
        languages.add("English");
        languages.add("Chinese");
        languages.add("Malay");

        // Set up the RecyclerView
        recyclerView = findViewById(R.id.recycler_view_languages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LanguageAdapter(languages, this::changeAppLanguage);
        recyclerView.setAdapter(adapter);
        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to another activity after language selection
                Intent intent = new Intent(LanguageSelection.this, LanguageLearn.class);
                startActivity(intent);
            }
        });
    }

    // Change app language based on selection
    private void changeAppLanguage(String language) {
        Locale locale;
        switch (language) {
            case "Chinese":
                locale = Locale.SIMPLIFIED_CHINESE;
                break;
            default:
                locale = Locale.ENGLISH;
        }

        // Update app configuration
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Show a confirmation message
        Toast.makeText(this, "Language changed to " + language, Toast.LENGTH_SHORT).show();

        // Restart activity to apply the change
        recreate();
    }
}