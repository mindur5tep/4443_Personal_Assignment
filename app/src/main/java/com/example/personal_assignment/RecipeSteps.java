package com.example.personal_assignment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ai.picovoice.porcupine.PorcupineException;
import ai.picovoice.porcupine.PorcupineManager;
import ai.picovoice.porcupine.PorcupineManagerCallback;

import java.util.ArrayList;
import java.util.Locale;

public class RecipeSteps extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private static final int REQUEST_MICROPHONE_PERMISSION = 2000;
    private TextView textTv;
    private Button buttonNext, buttonBack;
    private PorcupineManager porcupineManager;
    private int currentStep;  // Variable to track the current step
    private ArrayList<String> steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_steps);

        textTv = findViewById(R.id.textRecipe);
        buttonNext = findViewById(R.id.buttonNext);
        buttonBack = findViewById(R.id.buttonPrev);

        // Retrieve steps passed from the previous activity
        steps = getIntent().getStringArrayListExtra("steps");
        currentStep = 0;

        if (steps != null && !steps.isEmpty()) {
            updateStep();
        } else {
            Toast.makeText(RecipeSteps.this, "No Steps Found", Toast.LENGTH_SHORT).show();
        }

        // Set up manual navigation buttons
        buttonNext.setOnClickListener(v -> {
            currentStep++;
            updateStep();
        });

        buttonBack.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                updateStep();
            }
        });

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Request microphone permission for voice recognition
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE_PERMISSION);
        } else {
            initPorcupine();
        }
    }

    private void initPorcupine() {
        try {
            porcupineManager = new PorcupineManager.Builder()
                    .setAccessKey("vQ5sqYyrsipysWAHUQjUh8AQeG8uETG8zBXx2oVxR2PGe2ZyKGd53g==")  // Replace with your actual access key
                    .setKeywordPath("Hey-Food_en_android_v3_0_0.ppn")
                    .setSensitivity(0.6f)
                    .build(getApplicationContext(), porcupineManagerCallback);
            porcupineManager.start();
        } catch (PorcupineException e) {
            Toast.makeText(this, "Failed to initialize Porcupine: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PORCUPINE_ERROR", "Error initializing Porcupine", e);
        }
    }

    private final PorcupineManagerCallback porcupineManagerCallback = new PorcupineManagerCallback() {
        @Override
        public void invoke(int keywordIndex) {
            runOnUiThread(() -> {
                if (keywordIndex >= 0) {
                    speak();
                }
            });
        }
    };

    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi, speak something");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = result.get(0).toLowerCase();

            if (spokenText.contains("next")) {
                currentStep++;
                updateStep();
            } else if (spokenText.contains("back")) {
                if (currentStep > 0) currentStep--;
                updateStep();
            } else if (spokenText.contains("i am done")) {
                try {
                    porcupineManager.stop();
                } catch (PorcupineException e) {
                    throw new RuntimeException(e);
                }
                // Navigate back to the Recipe page (adjust target as needed)
                Intent intent = new Intent(RecipeSteps.this, RecipeActivity.class);
                intent.putExtra("uid", getIntent().getIntExtra("uid", -1));
                startActivity(intent);
                finish();
            }
        }
    }

    private void updateStep() {
        if (steps != null && currentStep >= 0 && currentStep < steps.size()) {
            String stepLabel = "Step " + (currentStep + 1);
            SpannableStringBuilder builder = new SpannableStringBuilder(stepLabel);

            // Apply bold and increased size to the step label
            builder.setSpan(new StyleSpan(Typeface.BOLD),
                    0,
                    builder.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new RelativeSizeSpan(1.3f),
                    0,
                    builder.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(":\n\n");
            builder.append(steps.get(currentStep));

            textTv.setText(builder);
        } else {
            textTv.setText("You've completed the recipe.\nSay 'back' to review a step.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MICROPHONE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initPorcupine();
            } else {
                Toast.makeText(this, "Microphone permission is required", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (porcupineManager != null) {
            try {
                porcupineManager.stop();
            } catch (PorcupineException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
