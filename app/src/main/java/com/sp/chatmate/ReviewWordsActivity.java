package com.sp.chatmate;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import java.util.List;

public class ReviewWordsActivity extends AppCompatActivity {

    private List<Integer> testedWords;  // List of word IDs passed from QuizStartActivity
    private int currentWordIndex = 0;   // Track the current word for review

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_words);

        // Retrieve the list of word IDs from the Intent
        testedWords = getIntent().getIntegerArrayListExtra("testedWords");

        // If words are available, start the review process
        if (testedWords != null && !testedWords.isEmpty()) {
            showReviewDialog();  // Start showing the words for review
        }
    }

    // Show the dialog for the current word
    private void showReviewDialog() {
        if (currentWordIndex < testedWords.size()) {
            int wordId = testedWords.get(currentWordIndex);

            // Assuming you have a method to get the word using wordId
            String word = getWordById(wordId);  // Get the word from the database

            new AlertDialog.Builder(this)
                    .setMessage("Do you remember the word: " + word + "?")
                    .setCancelable(false)  // Make the dialog non-cancelable
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            handleWordReview(wordId, true);  // If remembered, pass true
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            handleWordReview(wordId, false); // If not remembered, pass false
                        }
                    })
                    .show();
        } else {
            // If all words have been reviewed, show a summary
            showSummary();
        }
    }

    // Handle the user's response (whether they remember the word or not)
    private void handleWordReview(int wordId, boolean remembered) {
        vocabHelper helper = new vocabHelper(this, "japanese");
        helper.learn(wordId, remembered); // Call the learn method to update the database

        // Move to the next word for review
        currentWordIndex++;

        // Show the next word in the list
        showReviewDialog();
    }

    // Show a summary after all words are reviewed
    private void showSummary() {
        Toast.makeText(this, "Review completed! All words have been processed.", Toast.LENGTH_SHORT).show();
        finish(); // Finish this activity and return to the previous one
    }

    // Assuming you have a method to fetch the word by ID
    private String getWordById(int wordId) {
        // Retrieve the word using the word ID from the database or any other source
        Card card = new vocabHelper(this, "japanese").getCard(wordId); // Fetch the card object
        return card != null ? card.getTerm() : "Word not found";  // Return the word term (e.g., "Sample Word")
    }
}

