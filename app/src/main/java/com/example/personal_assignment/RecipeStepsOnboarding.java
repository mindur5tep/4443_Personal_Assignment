package com.example.personal_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeStepsOnboarding extends AppCompatActivity {

    private TextView tutorialTextView;
    private final List<String> tutorialSteps = Arrays.asList(
            "Say \"Next\" to go to the next step",
            "Say \"Back\" to go to the previous step",
            "And say \"I am done\" once you are finished",
            "Let's Cook!"
    );
    private int currentStep = 0;
    private int userId;
    private ArrayList<String> recipeSteps;
    private static final long STEP_DURATION_MS = 2000; // 2 seconds

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_steps_onboarding);

        tutorialTextView = findViewById(R.id.tutorialTextView);
        userId = getIntent().getIntExtra("uid", -1);
        recipeSteps = getIntent().getStringArrayListExtra("steps");

        // Start showing slides
        showNextStep();
    }

    private void showNextStep() {
        tutorialTextView.setText(tutorialSteps.get(currentStep));

        currentStep++;
        if (currentStep < tutorialSteps.size()) {
            handler.postDelayed(this::showNextStep, STEP_DURATION_MS);
        } else {
            handler.postDelayed(() -> {
                Intent intent = new Intent(RecipeStepsOnboarding.this, RecipeSteps.class);
                intent.putExtra("uid", userId);
                intent.putStringArrayListExtra("steps", recipeSteps);
                startActivity(intent);
                finish();
            }, STEP_DURATION_MS);
        }
    }
}
