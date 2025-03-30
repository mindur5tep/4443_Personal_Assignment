package com.example.personal_assignment;

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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PreferenceSettingsActivity extends AppCompatActivity {

    private ChipGroup chipGroup;
    private Button savePreferencesBtn;
    private int userId;
    private UserDao userDao;
    private ProfileDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final String[] preferenceOptions = {
            "Dessert","Breakfast",  "Chinese", "Japanese", "Indian", "Thai",
            "Mexican", "Middle Eastern", "Italian", "French", "Korean", "Greek"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_settings);

        chipGroup = findViewById(R.id.chipGroup);
        savePreferencesBtn = findViewById(R.id.savePreferencesBtn);
        userId = getIntent().getIntExtra("uid", -1);

        db = ProfileDatabase.getDatabase(getApplicationContext());
        userDao = db.userDao();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 8);
            return insets;
        });

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());

        // Populate Chips
        for (String pref : preferenceOptions) {
            Chip chip = new Chip(this);
            chip.setText(pref);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_bg_selector);
            chipGroup.addView(chip);
        }

        executor.execute(() -> {
            List<String> currentPrefs = userDao.getPreferencesByUser(userId);
            runOnUiThread(() -> {
                for (int i = 0; i < chipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroup.getChildAt(i);
                    if (currentPrefs.contains(chip.getText().toString())) {
                        chip.setChecked(true);
                    }
                }
            });
        });

        savePreferencesBtn.setOnClickListener(v -> {
            List<String> selectedPrefs = new ArrayList<>();
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                if (chip.isChecked()) selectedPrefs.add(chip.getText().toString());
            }

            executor.execute(() -> {
                userDao.clearPreferencesForUser(userId);
                for (String pref : selectedPrefs) {
                    userDao.insertPreference(new UserPreference(userId, pref));
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, "Preferences updated!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });
    }
}
