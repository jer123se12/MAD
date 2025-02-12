package com.sp.chatmate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizQuestionsActivity extends AppCompatActivity {

    private TextView questionTypeText, questionText;
    private Button option1, option2, option3, nextButton;
    private ImageButton micButton;
    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private String correctAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set initial layout (this will be updated later based on the question type)
        //setContentView(R.layout.activity_quiz_questions);

        // Initialize UI elements
        questionTypeText = findViewById(R.id.question_type);
        questionText = findViewById(R.id.quiz_question);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        nextButton = findViewById(R.id.next_button);
        micButton = findViewById(R.id.mic_button);

        // Fetch AI-generated quiz questions
        generateQuiz();
    }

    private void generateQuiz() {
        // Example of AI-generated questions with three different types
        quizQuestions.add(new QuizQuestion("Forming Sentences", "She ___ a student", "is", "are", "am", "is"));
        quizQuestions.add(new QuizQuestion("Meaning of the Word", "What is the meaning of 'apple'?", "Fruit", "Animal", "Building", "Fruit"));
        quizQuestions.add(new QuizQuestion("Read the Word Out", "What is the word?", "example", "test", "question", "example"));

        // Shuffle questions to randomize order
        Collections.shuffle(quizQuestions);

        // Display first question
        displayQuestion();
    }

    private void displayQuestion() {
        if (currentQuestionIndex < quizQuestions.size()) {
            QuizQuestion question = quizQuestions.get(currentQuestionIndex);
            questionTypeText.setText(question.getType());
            questionText.setText(question.getQuestion());
            option1.setText(question.getOption1());
            option2.setText(question.getOption2());
            option3.setText(question.getOption3());

            correctAnswer = question.getCorrectAnswer();

            // Dynamically inflate the appropriate layout based on the question type
            if (question.getType().equals("Forming Sentences")) {
                setContentView(R.layout.activity_forming_sentence);
            } else if (question.getType().equals("Meaning of the Word")) {
                setContentView(R.layout.activity_meaning_of_word);
            } else if (question.getType().equals("Read the Word Out")) {
                setContentView(R.layout.activity_read_word_out);
            }

            option1.setOnClickListener(v -> checkAnswer(correctAnswer, option1.getText().toString()));
            option2.setOnClickListener(v -> checkAnswer(correctAnswer, option2.getText().toString()));
            option3.setOnClickListener(v -> checkAnswer(correctAnswer, option3.getText().toString()));

            nextButton.setOnClickListener(v -> {
                currentQuestionIndex++;
                if (currentQuestionIndex < quizQuestions.size()) {
                    displayQuestion();
                } else {
                    showResults();
                }
            });
        }
    }

    private void checkAnswer(String correctAnswer, String selectedAnswer) {
        if (correctAnswer.equals(selectedAnswer)) {
            correctAnswers++;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Incorrect! Try Again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showResults() {
        Toast.makeText(this, "Quiz Completed! You got " + correctAnswers + " out of " + quizQuestions.size(), Toast.LENGTH_LONG).show();
    }
}
