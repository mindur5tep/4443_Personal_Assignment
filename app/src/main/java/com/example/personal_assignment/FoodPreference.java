package com.example.personal_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personal_assignment.database.ProfileDatabase;
import com.example.personal_assignment.database.UserDao;
import com.example.personal_assignment.database.UserPreference;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;


public class FoodPreference extends AppCompatActivity {

    private ChipGroup chipGroup;
    private Button continueButton, skipButton;
    private UserDao userDao;

    private ProfileDatabase userDB;
    private int currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food_preference);

        // Initialize Room Database
        userDB = ProfileDatabase.getDatabase(getApplicationContext());
        // Initialize Room DAO
        userDao = userDB.userDao();

        currentUserUid = getIntent().getIntExtra("uid", -1);

        chipGroup = findViewById(R.id.chipGroup);
        continueButton = findViewById(R.id.btnContinueLogin);
        skipButton  = findViewById(R.id.btnSkip);

        continueButton.setOnClickListener(v -> savePreferences());
        skipButton.setOnClickListener(v -> goToLoginPage());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 8);
            return insets;
        });
    }

    private void savePreferences() {
        List<String> selected = new ArrayList<>();

        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selected.add(chip.getText().toString());
            }
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            // Clear old prefs
            userDao.clearPreferencesForUser(currentUserUid);

            // Insert new ones
            for (String pref : selected) {
                userDao.insertPreference(new UserPreference(currentUserUid, pref));
            }
        });

        runOnUiThread(this::goToLoginPage);
    }

    private void goToLoginPage() {
        Intent intent = new Intent(FoodPreference.this, Login.class);
        Toast.makeText(this, "Preferences saved. Please log in to continue.", Toast.LENGTH_SHORT).show();
        intent.putExtra("uid", currentUserUid);
        startActivity(intent);
        finish();
    }
}
