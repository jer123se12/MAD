package com.sp.chatmate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage, uploadIcon;
    private EditText etUsername, etEmail, etPassword, etAbout, etHobbies;
    private Button btnNext;
    private Bitmap image;
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    String username;
    String email;
    String password;
    String about ;
    String hobbies ;









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
        databaseReference = FirebaseDatabase.getInstance("https://langify-a017b-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
        // Initialize UI Elements
        profileImage = findViewById(R.id.profile_image);
        image=profileImage.getDrawingCache();
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

    int pid=123;
    // Opens the Image Picker
    private void openImagePicker() {
        Intent camera_intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent,pid);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==pid){
            Bitmap photo=(Bitmap) data.getExtras().get("data");
            Log.i("iamge","working");
            profileImage.setImageBitmap(photo);
            image=photo;
        }
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
    private String bitmaptostring(Bitmap img){
        Bitmap bm = BitmapFactory.decodeFile("/path/to/image.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.JPEG, 100, baos); // bm is the bitmap object
        byte[] b = baos.toByteArray();
        String encodec= android.util.Base64.encodeToString(b, android.util.Base64.DEFAULT);
        return encodec;
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
        username = etUsername.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        about = etAbout.getText().toString().trim();
        hobbies = etHobbies.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || about.isEmpty() || hobbies.isEmpty()||image==null) {
            Toast.makeText(this, "Please fill in all fields and take a photo", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(firebaseAuth.getCurrentUser()==null){
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "email exists please sign in instead",Toast.LENGTH_LONG).show();
                }else{

                    Log.i("changed","went in");
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        saveProfileToFirebase(user.getUid(), username, email, about, hobbies);
                    }
                }
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
        userProfile.put("profileImageUrl", bitmaptostring(image)); // Store either uploaded image or Firebase-hosted default
        databaseReference.child(userId).setValue(userProfile)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                          @Override
                                          public void onSuccess(Void unused) {
                                              progressDialog.dismiss();
                                              startActivity(new Intent(ProfileActivity.this, FlashCards.class));
                                              finish();
                                          }
                                      }
                ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ProfileActivity", "Error saving user data", e);
                    }
                });

    }

}
