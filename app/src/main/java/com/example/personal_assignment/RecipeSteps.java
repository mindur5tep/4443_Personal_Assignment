package com.example.personal_assignment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
    private ImageButton buttonBackScreen;
    private PorcupineManager porcupineManager;
    private int currentStep;  // Variable to track the current step
    private ArrayList<String> steps;
    private int userId;

    private long stepStartTime;
    private long[] stepDurations; // Array to store duration of each step
    private boolean[] stepVisited; // Array to check if the step was previously visited
    private int errorCount;

    private int initialBatteryLevel = -1;
    private int finalBatteryLevel = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_steps);

        textTv = findViewById(R.id.textRecipe);
        buttonNext = findViewById(R.id.buttonNext);
        buttonBack = findViewById(R.id.buttonPrev);
        buttonBackScreen = findViewById(R.id.backButton);

        // Retrieve steps and uid from intent extras
        steps = getIntent().getStringArrayListExtra("steps");
        userId = getIntent().getIntExtra("uid", -1);

        stepDurations = new long[steps.size()];
        stepVisited = new boolean[steps.size()];

        currentStep = 0;
        if (steps != null && !steps.isEmpty()) {
            updateStep();
        } else {
            Toast.makeText(RecipeSteps.this, "No Steps Found", Toast.LENGTH_SHORT).show();
        }

        // Set up manual navigation buttons
        buttonNext.setOnClickListener(v -> {
            if (currentStep < steps.size() - 1) {
                recordCurrentStepDuration();
                currentStep++;
                updateStep();
            } else if (currentStep == steps.size() - 1) {
                // Last step: record and finish, same as saying "I am done"
                recordCurrentStepDuration();
                displayAllInformationAndProceed();
                try {
                    porcupineManager.stop();
                } catch (PorcupineException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        buttonBack.setOnClickListener(v -> {
            if (currentStep > 0) {
                recordCurrentStepDuration();
                currentStep--;
                updateStep();
            } else {
                errorCount++;
                Toast.makeText(this, "You are at the first step already.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonBackScreen.setOnClickListener(v -> finish());

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        stepStartTime = System.currentTimeMillis();

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
                    .setAccessKey("OZvSgrqLL+t7aLeQg8twNip3ZnHU0qhx5U1zU/L5pdcrcQEtQ574sw==")  // Replace with your actual access key
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
                if (currentStep < steps.size() - 1) {
                    recordCurrentStepDuration();
                    currentStep++;
                    updateStep();
                } else {
                    recordCurrentStepDuration();
                    displayAllInformationAndProceed();
                    try {
                        porcupineManager.stop();
                    } catch (PorcupineException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (spokenText.contains("back")) {
                if (currentStep > 0) {
                    recordCurrentStepDuration();
                    currentStep--;
                    updateStep();
                } else {
                    errorCount++;
                    Toast.makeText(this, "No previous steps to go back.", Toast.LENGTH_SHORT).show();
                }
            } else if (spokenText.contains("i am done") || spokenText.contains("i'm done")) {
                recordCurrentStepDuration();
                displayAllInformationAndProceed();
                try {
                    porcupineManager.stop();
                } catch (PorcupineException e) {
                    throw new RuntimeException(e);
                }
                Toast.makeText(this, "Total user errors: " + errorCount, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateStep() {
        long currentTime = System.currentTimeMillis();
        if (currentStep > 0) {
            long elapsed = currentTime - stepStartTime;
            if (currentStep <= steps.size()) {
                stepDurations[currentStep - 1] += elapsed; // Add elapsed time for the previous step
                logStepDuration(currentStep - 1, stepDurations[currentStep - 1]);
            }
        }
        stepStartTime = currentTime; // Reset start time for next step

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
            String stepText = steps.get(currentStep).replaceFirst("^Step \\d+:\\s*", "");
            builder.append(stepText);
            textTv.setText(builder);

            // Update button text based on position
            if (currentStep == steps.size() - 1) {
                buttonNext.setText("Finish");
            } else {
                buttonNext.setText("Next");
            }
        } else {
            textTv.setText("You've completed the recipe.\nSay 'back' to review a step.");
            displayTotalDuration();
            logAllStepDurations();
        }
    }

    private void recordCurrentStepDuration() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - stepStartTime;
        if (currentStep >= 0 && currentStep < stepDurations.length) {
            stepDurations[currentStep] += elapsed;
            logStepDuration(currentStep, stepDurations[currentStep]);
        }
        stepStartTime = currentTime; // Reset after recording
    }

    private void logStepDuration(int stepIndex, long duration) {
        String formattedDuration = formatDuration(duration);
        Log.d("StepDuration", "Step " + (stepIndex + 1) + " took " + formattedDuration);
    }

    private void logAllStepDurations() {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("All Step Durations:\n");
        for (int i = 0; i < stepDurations.length; i++) {
            logMessage.append("Step ").append(i + 1).append(": ")
                    .append(formatDuration(stepDurations[i])).append("\n");
        }
        Log.d("StepDurations", logMessage.toString());
    }

    private String formatDuration(long millis) {
        long hours = (millis / 3600000);
        long minutes = (millis / 60000) % 60;
        long seconds = (millis / 1000) % 60;
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void displayTotalDuration() {
        long total = 0;
        for (long duration : stepDurations) {
            total += duration;
        }
        String totalFormatted = formatDuration(total);
        Toast.makeText(this, "Total time: " + totalFormatted, Toast.LENGTH_LONG).show();
    }

    private void displayAllInformation() {
        StringBuilder displayText = new StringBuilder();
        long totalTime = 0;
        for (long duration : stepDurations) {
            totalTime += duration;
        }
        String totalFormatted = formatDuration(totalTime);
        displayText.append("🕒 Total Time: ").append(totalFormatted).append("\n\n");
        displayText.append("📋 All Step Durations:\n");
        for (int i = 0; i < stepDurations.length; i++) {
            displayText.append("Step ").append(i + 1).append(": ")
                    .append(formatDuration(stepDurations[i])).append("\n");
        }
        displayText.append("\n❌ Total Error Count: ").append(errorCount).append("\n");
        String batteryUsage = getBatteryUsage();
        displayText.append("🔋 Battery Used: ").append(batteryUsage);
        buttonBack.setVisibility(View.INVISIBLE);
        buttonNext.setVisibility(View.INVISIBLE);
        textTv.setText(displayText.toString());
    }

    private String getBatteryUsage() {
        if (initialBatteryLevel != -1 && finalBatteryLevel != -1) {
            return (initialBatteryLevel - finalBatteryLevel) + "%";
        }
        return "Not Available";
    }

    private void displayAllInformationAndProceed() {
        displayAllInformation();
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(RecipeSteps.this, RecipeActivity.class);
            intent.putExtra("uid", getIntent().getIntExtra("uid", -1));
            startActivity(intent);
            finish();
        }, 45000); // Delay, e.g., 45 seconds
    }

    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (initialBatteryLevel == -1) {
                initialBatteryLevel = level;
            }
            finalBatteryLevel = level;
            float batteryPct = finalBatteryLevel * 100 / (float) intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            Log.d("BATTERY_INFO", "Current Battery Level: " + batteryPct + "%");
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryInfoReceiver, ifilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(batteryInfoReceiver);
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
