package com.sp.chatmate;

import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuizQuestionsActivity extends AppCompatActivity {

    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;

    private TextView questionTypeText, questionText;
    private Button option1, option2, option3, nextButton;
    private EditText fillInTheBlank;
    private ImageView micButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_questions);

        // Initialize UI elements
        questionTypeText = findViewById(R.id.question_type);
        questionText = findViewById(R.id.quiz_question);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        fillInTheBlank = findViewById(R.id.fill_in_the_blank);
        micButton = findViewById(R.id.mic_button);
        nextButton = findViewById(R.id.next_button);

        // Fetch AI-generated questions
        generateQuiz();

        // Handle button clicks
        option1.setOnClickListener(v -> checkAnswer(option1.getText().toString()));
        option2.setOnClickListener(v -> checkAnswer(option2.getText().toString()));
        option3.setOnClickListener(v -> checkAnswer(option3.getText().toString()));
        nextButton.setOnClickListener(v -> nextQuestion());
    }

    private void generateQuiz() {
        String prompt = "Generate a language quiz with 15 questions:\n" +
                "1. Form Sentences: Provide a sentence with missing words and give word choices.\n" +
                "2. Read the Word Out: Provide a sentence for the user to read aloud.\n" +
                "3. Meaning of the Word: Show a word and provide 3 multiple-choice options.\n" +
                "Return questions in this format:\n" +
                "Q1: Type=Form Sentence, Question='今日は天気が〇〇です', Choices=['良い', '悪い', '冷たい'], Answer='良い'\n" +
                "Q2: Type=Read Out, Question='私は猫が大好きです', Answer='User must read this'\n" +
                "Q3: Type=Meaning, Question='すばらしい', Choices=['amazing', 'careful', 'hi'], Answer='amazing'";

        GeminiService apiService = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeminiService.class);

        apiService.generateQuiz(new GeminiRequest(prompt)).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    parseQuestions(response.body().getCandidates().get(0).getOutput());
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                Log.e("Quiz AI", "Failed to fetch AI questions", t);
            }
        });
    }

    private void parseQuestions(String aiResponse) {
        // Convert AI response into quiz question objects (parsing logic)
        // Then call showQuestion(0);
    }

    private void showQuestion(int index) {
        if (index >= quizQuestions.size()) return;
        QuizQuestion question = quizQuestions.get(index);
        questionText.setText(question.getQuestion());

        // Show UI based on question type
        if (question.getType().equals("Meaning")) {
            option1.setText(question.getChoices().get(0));
            option2.setText(question.getChoices().get(1));
            option3.setText(question.getChoices().get(2));
            option1.setVisibility(View.VISIBLE);
            option2.setVisibility(View.VISIBLE);
            option3.setVisibility(View.VISIBLE);
        }
    }
}
