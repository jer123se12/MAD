package com.sp.chatmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuizStartActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

//    private TextView questionStatusText;
    private int questionsCompleted = 0;
    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;  // Track the current question
    private String correctAnswer;

    private TextView questionTypeText, questionText;
    private Button nextButton;
    private ImageView micButton;
    private int correctAnswers = 0;

    private GeminiService geminiApiService;
    private vocabHelper vocabHelper;
    private List<Integer> testedWords = new ArrayList<>();

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_start); // Start screen layout with "Start Quiz" button

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize Views for navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // Initialize vocabHelper to fetch data
        vocabHelper = new vocabHelper(this, "japanese");

        // Open Navigation Drawer when Menu Icon is Clicked
        findViewById(R.id.menu_icon).setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        // Load user profile details in the navigation header
        loadUserProfile();

        // Handle Navigation Item Clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_flashcards) {
                startActivity(new Intent(QuizStartActivity.this, FlashCards.class));
            } else if (id == R.id.nav_quiz) {
                drawerLayout.closeDrawer(navigationView); // Stay on the same page
            } else if (id == R.id.nav_immersion) {
                startActivity(new Intent(QuizStartActivity.this, ImmersionActivity.class));
            }

            return true;
        });

        // Start Quiz Button - User clicks to start the quiz
        Button startQuizButton = findViewById(R.id.start_button);
        startQuizButton.setOnClickListener(v -> startQuiz());

        // Initialize Gemini API Service
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/") // Set the appropriate API base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        geminiApiService = retrofit.create(GeminiService.class);
    }

    // Start the quiz
    private void startQuiz() {
        fetchWordsFromDatabase();
    }

    // Fetch words from the database and send them to Gemini API
    private void fetchWordsFromDatabase() {
        Log.d("QuizStart", "Fetching words from the database...");

        // Get the first 1000 words (or adjust based on your needs)
        List<Card> cards = vocabHelper.getCardsToday(5, 5);  // Fetching words from database
        if (cards.isEmpty()) {
            Log.e("QuizStart", "No words found in the database.");
            return;
        }

        // Create a prompt by joining all terms from the database
        StringBuilder promptBuilder = new StringBuilder();
        for (Card card : cards) {
            promptBuilder.append(card.term).append(", ");
        }

        // Remove the last comma and space
        String prompt = promptBuilder.length() > 0 ? promptBuilder.substring(0, promptBuilder.length() - 2) : "";

        // Now send the prompt to Gemini to generate quiz questions
        generateQuiz(prompt);
    }

    // Generate quiz questions dynamically from AI (Gemini API)
    private void generateQuiz(String prompt) {
        Log.d("QuizStart", "Fetching quiz questions...");
        String apiKey = "AIzaSyBESUV5OocfjvwIY8BeOWyeHmPMbao2ORY"; // Your API key here

        JSONObject bodyjson = new JSONObject();
        try {
            bodyjson = new JSONObject("{ \"contents\": [{\"parts\":[{\"text\": \"" +
                    "Generate 10 quiz questions with these words 5 for each question with the question type whats the meaning of the word the word is in japanese and the second question will be what is this english word in japanese seperate those 2 make sure to not use the same word and provide the answer for each of the question at the end dont use markdown : " + prompt
                    + "\"}]}]}");
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyjson.toString());
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=AIzaSyBESUV5OocfjvwIY8BeOWyeHmPMbao2ORY")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("QuizStart", "API call failed", e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                Log.d("QuizStart", "API response code: " + response.code());
                String responseBody = response.body().string(); // Read the response body once
                Log.i("QuizStart", "API Response Body: " + responseBody); // Log the full response

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject responseJson = new JSONObject(responseBody);
                        JSONArray candidates = responseJson.getJSONArray("candidates");

                        for (int i = 0; i < candidates.length(); i++) {
                            JSONObject candidate = candidates.getJSONObject(i);

                            // Access the content -> parts -> text field
                            JSONObject content = candidate.getJSONObject("content");
                            JSONArray parts = content.getJSONArray("parts");
                            String questionText = parts.getJSONObject(0).getString("text");

                            Log.i("QuizStart", "Question: " + questionText); // Log the question retrieved

                            quizQuestions.add(new QuizQuestion(
                                    "Forming Sentences",  // Example question type
                                    questionText,
                                    "Option 1", "Option 2", "Option 3", "Correct Option" // Replace with actual options
                            ));
                        }

                        // Shuffle the questions and update UI
                        Collections.shuffle(quizQuestions);

                        // Run UI update on the main thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displayQuestion(); // Ensure displayQuestion() is run on the main thread
                            }
                        });

                    } catch (Exception e) {
                        Log.e("QuizStart", "Error parsing response: " + e.toString());
                    }

                } else {
                    Log.e("QuizStart", "API response failed: " + response.code());
                }
            }
        });
    }

    // Display the current question
    private void displayQuestion() {
        if (currentQuestionIndex < quizQuestions.size()) {
            QuizQuestion question = quizQuestions.get(currentQuestionIndex);

            // Assuming you have a method to get the word ID, e.g., question.getWordId()
            int wordId = question.getWordId();  // Adjust this method to fetch word ID
            testedWords.add(wordId);  // Add the word ID to the list

            // Inflate the appropriate layout based on question type
            inflateLayoutBasedOnQuestionType(question.getType());

            // Set the data for the question in the UI
            questionTypeText.setText(question.getType());
            questionText.setText(question.getQuestion());

            correctAnswer = question.getCorrectAnswer();

            // Set onClickListeners for options (you may not need this if it's removed from layout)
            nextButton.setOnClickListener(v -> {
                currentQuestionIndex++;
                questionsCompleted++;
//                updateQuestionStatus(); // Update questions completed status
                if (currentQuestionIndex < quizQuestions.size()) {
                    displayQuestion();
                } else {
                    showResults();
                }
            });
        }
    }


    // Inflate the layout dynamically based on the question type
    private void inflateLayoutBasedOnQuestionType(String questionType) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.activity_forming_sentence, null); // Always inflate activity_forming_sentence.xml

        // Set the content view dynamically
        setContentView(layout);

        // Now assign the UI elements after inflating
        questionTypeText = layout.findViewById(R.id.question_type);
        questionText = layout.findViewById(R.id.quiz_question);
        nextButton = layout.findViewById(R.id.next_button);
    }

    // Update the status of questions completed
//    private void updateQuestionStatus() {
//        questionStatusText.setText(questionsCompleted + "/15"); // Assuming 15 questions in total
//    }

    // Check if the selected answer is correct
    private void checkAnswer(String correctAnswer, String selectedAnswer) {
        if (correctAnswer.equals(selectedAnswer)) {
            correctAnswers++;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Incorrect! Try Again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Show the results at the end of the quiz
    // In your QuizStartActivity:
    private void showResults() {
        Toast.makeText(this, "Quiz Completed! You got " + correctAnswers + " out of " + quizQuestions.size(), Toast.LENGTH_LONG).show();

        // Pass the list of tested word IDs to the next activity (ReviewWordsActivity)
        Intent intent = new Intent(QuizStartActivity.this, ReviewWordsActivity.class);
        intent.putIntegerArrayListExtra("testedWords", new ArrayList<>(testedWords));  // testedWords is the list of word IDs
        startActivity(intent);
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