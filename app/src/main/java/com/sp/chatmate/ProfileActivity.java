package com.sp.chatmate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage, uploadIcon;
    private EditText etUsername, etEmail, etPassword, etAbout, etHobbies;
    private Button btnNext;
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;

    // File Picker for Profile Picture
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    profileImage.setImageURI(selectedImageUri); // Instantly update UI with chosen image
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile); // Load profile.xml

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI Elements
        profileImage = findViewById(R.id.profile_image);
        uploadIcon = findViewById(R.id.upload_icon);
        etUsername = findViewById(R.id.username);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etAbout = findViewById(R.id.about);
        etHobbies = findViewById(R.id.hobbies);
        btnNext = findViewById(R.id.btn_next);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);

        // Set Default Profile Image from res/drawable
        profileImage.setImageResource(R.drawable.default_profile_background);

        // Open file picker when profile image or upload icon is clicked
        profileImage.setOnClickListener(v -> openImagePicker());
        uploadIcon.setOnClickListener(v -> openImagePicker());

        // Enable "Next" button only when all fields are filled
        etUsername.addTextChangedListener(new TextWatcherAdapter());
        etEmail.addTextChangedListener(new TextWatcherAdapter());
        etPassword.addTextChangedListener(new TextWatcherAdapter());
        etAbout.addTextChangedListener(new TextWatcherAdapter());
        etHobbies.addTextChangedListener(new TextWatcherAdapter());

        // Save profile data and create Firebase account
        btnNext.setOnClickListener(v -> createFirebaseAccount());
    }

    // Opens the Image Picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    // Enable "Next" button only when all fields are filled
    private void validateForm() {
        boolean isFilled = !etUsername.getText().toString().trim().isEmpty() &&
                !etEmail.getText().toString().trim().isEmpty() &&
                !etPassword.getText().toString().trim().isEmpty() &&
                !etAbout.getText().toString().trim().isEmpty() &&
                !etHobbies.getText().toString().trim().isEmpty();

        btnNext.setEnabled(isFilled);
    }

    class TextWatcherAdapter implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validateForm();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    // Create a Firebase Authentication Account
    private void createFirebaseAccount() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String about = etAbout.getText().toString().trim();
        String hobbies = etHobbies.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || about.isEmpty() || hobbies.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            saveProfileToFirebase(user.getUid(), username, email, about, hobbies);
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Account creation failed", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Save profile data to Firebase (Uses drawable image if no upload)
    private void saveProfileToFirebase(String userId, String username, String email, String about, String hobbies) {
        if (selectedImageUri != null) {  // If user uploads an image, save it to Firebase
            StorageReference storageRef = firebaseStorage.getReference().child("profile_images/" + userId + ".jpg");
            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveUserData(userId, username, email, about, hobbies, uri.toString()); // Save uploaded image URL
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Image upload failed", Toast.LENGTH_LONG).show();
                    });
        } else {
            // If no image is uploaded, store a Firebase-hosted default profile image URL
            saveUserData(userId, username, email, about, hobbies, "https://firebasestorage.googleapis.com/YOUR_DEFAULT_IMAGE_URL");
        }
    }


    private void saveUserData(String userId, String username, String email, String about, String hobbies, String imageUrl) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("username", username);
        userProfile.put("email", email);
        userProfile.put("about", about);
        userProfile.put("hobbies", hobbies);
        userProfile.put("profileImageUrl", imageUrl); // Store either uploaded image or Firebase-hosted default

        databaseReference.child(userId).setValue(userProfile)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "Error saving profile", Toast.LENGTH_SHORT).show();
                });
    }

}
