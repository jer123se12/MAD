package com.sp.chatmate;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class UploadFile extends AppCompatActivity {

    private Button btnBrowse, btnNext;
    private TextView txtFileName;
    private LinearLayout uploadBox;
    private ImageView uploadIcon;
    private Uri selectedFileUri;

    // File Picker Launcher
    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    updateFileName(selectedFileUri);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_file); // Using upload_file.xml

        btnBrowse = findViewById(R.id.btn_browse);
        btnNext = findViewById(R.id.btn_next);
        txtFileName = findViewById(R.id.txt_file_name);
        uploadBox = findViewById(R.id.upload_box);
        uploadIcon = findViewById(R.id.upload_icon);

        // Open file picker when "Browse" is clicked
        btnBrowse.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // Allow all file types
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(Intent.createChooser(intent, "Select a file"));
        });

        // Handle Drag and Drop functionality
        uploadBox.setOnDragListener((view, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    uploadBox.setBackgroundColor(0xFFB0E6E6); // Light blue to indicate drag over
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    uploadBox.setBackgroundColor(0xFFE5F4F4); // Reset color
                    return true;

                case DragEvent.ACTION_DROP:
                    uploadBox.setBackgroundColor(0xFFE5F4F4); // Reset color

                    ClipData clipData = event.getClipData();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        selectedFileUri = clipData.getItemAt(0).getUri();
                        updateFileName(selectedFileUri);
                    }
                    return true;

                default:
                    return false;
            }
        });

        // Handle "Next" button click
        btnNext.setOnClickListener(v -> {
            if (selectedFileUri != null) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("fileUri", selectedFileUri.toString());
                startActivity(intent);
            }
        });
    }

    // Method to update the file name and enable the "Next" button
    private void updateFileName(Uri fileUri) {
        if (fileUri != null) {
            txtFileName.setText(fileUri.getLastPathSegment());
            btnNext.setEnabled(true);
            uploadIcon.setImageResource(R.drawable.ic_upload); // Ensure icon is set
        }
    }
}