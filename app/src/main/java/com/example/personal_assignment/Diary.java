package com.example.personal_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personal_assignment.database.DiaryDao;
import com.example.personal_assignment.database.DiaryEntry;
import com.example.personal_assignment.database.ProfileDatabase;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Diary extends AppCompatActivity {

    private TextInputEditText etDiaryTitle, etDiaryContent;
    private Button saveDiaryEntry;
    private ImageView deleteButton;
    private MaterialToolbar topAppBar;
    private int entryId = -1; // If editing /adding an entry, store its entryID
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Initialize UI elements
        etDiaryTitle = findViewById(R.id.etDiaryTitle);
        deleteButton = findViewById(R.id.ivDelete);

        etDiaryContent = findViewById(R.id.etDiaryContent);
        saveDiaryEntry = findViewById(R.id.saveDiaryButton);
        topAppBar = findViewById(R.id.topAppBar);

        // Get intent data
        userId = getIntent().getIntExtra("uid", -1);
        if (userId == -1) {
            // handle error: no valid user ID found
            return;
        }
        entryId = getIntent().getIntExtra("entryId", -1);

        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(Diary.this, MainActivity.class);
            intent.putExtra("uid", userId);
            startActivity(intent);
            finish();
        });

        if (entryId != -1) {
            loadExistingEntry(entryId);
        } else {
            // If adding a new entry, hide the delete button
            deleteButton.setVisibility(View.GONE);
        }

        saveDiaryEntry.setOnClickListener(v -> saveDiaryEntry());
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void saveDiaryEntry() {
        String title = etDiaryTitle.getText().toString().trim();
        String content = etDiaryContent.getText().toString().trim();

        // Validate input, ensure that both the title and content are not empty.
        if (title.isEmpty() || content.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please enter both title and content", Snackbar.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            ProfileDatabase db = ProfileDatabase.getDatabase(getApplicationContext());
            DiaryDao diaryDao = db.diaryDao();

            if (entryId == -1) {
                DiaryEntry newEntry = new DiaryEntry(userId, title, content);
                diaryDao.insertEntry(newEntry);
            } else {
                DiaryEntry existingEntry = diaryDao.getEntryById(entryId);
                if (existingEntry != null) {
                    existingEntry.setTitle(title);
                    existingEntry.setContent(content);
                    existingEntry.setUpdatedTime();
                    diaryDao.updateEntry(existingEntry);
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Diary saved", Toast.LENGTH_SHORT).show();
                // Go back to main page after successfully saving the diary entry.
                Intent intent = new Intent(Diary.this, MainActivity.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                finish();
            });
        });
    }

    private void loadExistingEntry(int entryId) {
        executorService.execute(() -> {
            ProfileDatabase db = ProfileDatabase.getDatabase(getApplicationContext());
            DiaryEntry existingEntry = db.diaryDao().getEntryById(entryId);

            if (existingEntry != null) {
                runOnUiThread(() -> {
                    etDiaryTitle.setText(existingEntry.getTitle());
                    etDiaryContent.setText(existingEntry.getContent());
                });
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        //Show confirmation dialog after user click on the delete button
        new MaterialAlertDialogBuilder(this).setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this diary entry?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEntry())
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void deleteEntry() {
        executorService.execute(() -> {
            DiaryDao diaryDao = ProfileDatabase.getDatabase(getApplicationContext()).diaryDao();
            diaryDao.deleteEntryById(entryId); // Delete from database

            runOnUiThread(() -> {
                Toast.makeText(this, "Diary deleted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Diary.this, MainActivity.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                finish();
            });
        });
    }
}
