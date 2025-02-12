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

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class QuizStartActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private TextView questionStatusText;
    private int questionsCompleted = 0;
    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private String correctAnswer;

    private TextView questionTypeText, questionText;
    private Button option1, option2, option3, nextButton;
    private ImageView micButton;
    private int correctAnswers = 0;

    private GeminiService geminiApiService;
    private vocabHelper vocabHelper;
    OkHttpClient client=new OkHttpClient();

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
        // Fetch words from the database
        fetchWordsFromDatabase();
    }

    // Fetch words from the database and send them to Gemini API
    private void fetchWordsFromDatabase() {
        Log.d("QuizStart", "Fetching words from the database...");

        // Get the first 1000 words (or adjust based on your needs)
        List<Card> cards = vocabHelper.getCardsToday(5,5);  // Fetching words from database
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

        JSONObject bodyjson=new JSONObject();
        try {
            bodyjson = new JSONObject("{ \"contents\": [{\"parts\":[{\"text\": \""+
                    "Generate 15 quiz questions with these words: " + prompt
                    +"\"}]}]}");
        }catch (Exception e){
            Log.e("error",e.toString());
        }
        RequestBody body= RequestBody.create(MediaType.parse("application/json; charset=utf-8"),bodyjson.toString());
        Request request=new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=AIzaSyBESUV5OocfjvwIY8BeOWyeHmPMbao2ORY")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                Log.d("QuizStart", "API response code: " + response.code());
                Log.i("help",response.body().string());
//                if (response.isSuccessful() && response.body() != null) {
//                    Log.d("QuizStart", "API call successful!");
//                    List<GeminiResponse.Candidate> candidates = response.body().getCandidates();
//                    // Log the number of candidates received
//                    Log.d("QuizStart", "Received " + candidates.size() + " candidates.");
//                    for (GeminiResponse.Candidate candidate : candidates) {
//                        Log.d("QuizStart", "Candidate: " + candidate.getOutput()); // Log candidate text
//                        quizQuestions.add(new QuizQuestion(
//                                "Forming Sentences",
//                                candidate.getOutput(),
//                                "Option 1", "Option 2", "Option 3", "Correct Option"
//                        ));
//                    }
//                    Collections.shuffle(quizQuestions);
//                    displayQuestion();
//                } else {
//                    Log.e("QuizStart", "API response not successful: " + response.code() + " - " + response.body());
//                    Log.e("QuizStart", "Error details: " + response.errorBody());
//                    Toast.makeText(QuizStartActivity.this, "Failed to fetch quiz questions: " + response.message(), Toast.LENGTH_SHORT).show();
//                }
            }
        });



    }

    // Display the current question
    private void displayQuestion() {
        if (currentQuestionIndex < quizQuestions.size()) {
            QuizQuestion question = quizQuestions.get(currentQuestionIndex);

            // Inflate the appropriate layout based on question type
            inflateLayoutBasedOnQuestionType(question.getType());

            // Set the data for the question in the UI
            questionTypeText.setText(question.getType());
            questionText.setText(question.getQuestion());
            option1.setText(question.getOption1());
            option2.setText(question.getOption2());
            option3.setText(question.getOption3());

            correctAnswer = question.getCorrectAnswer();

            // Set onClickListeners for options
            option1.setOnClickListener(v -> checkAnswer(correctAnswer, option1.getText().toString()));
            option2.setOnClickListener(v -> checkAnswer(correctAnswer, option2.getText().toString()));
            option3.setOnClickListener(v -> checkAnswer(correctAnswer, option3.getText().toString()));

            // Next button logic
            nextButton.setOnClickListener(v -> {
                currentQuestionIndex++;
                questionsCompleted++;
                updateQuestionStatus(); // Update questions completed status
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
        View layout = null;

        if (questionType.equals("Forming Sentences")) {
            layout = inflater.inflate(R.layout.activity_forming_sentence, null);
        } else if (questionType.equals("Meaning of the Word")) {
            layout = inflater.inflate(R.layout.activity_meaning_of_word, null);
        } else if (questionType.equals("Read the Word Out")) {
            layout = inflater.inflate(R.layout.activity_read_word_out, null);
        }

        // Set the content view dynamically
        setContentView(layout);

        // Now assign the UI elements after inflating
        questionTypeText = layout.findViewById(R.id.question_type);
        questionText = layout.findViewById(R.id.quiz_question);
        option1 = layout.findViewById(R.id.option1);
        option2 = layout.findViewById(R.id.option2);
        option3 = layout.findViewById(R.id.option3);
        nextButton = layout.findViewById(R.id.next_button);
        micButton = layout.findViewById(R.id.mic_button);
    }

    // Update the status of questions completed
    private void updateQuestionStatus() {
        questionStatusText.setText(questionsCompleted + "/15"); // Assuming 15 questions in total
    }

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
    private void showResults() {
        Toast.makeText(this, "Quiz Completed! You got " + correctAnswers + " out of " + quizQuestions.size(), Toast.LENGTH_LONG).show();
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

