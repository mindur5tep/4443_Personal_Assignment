package com.example.personal_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RecipeActivity extends AppCompatActivity {

    private TextView recipeOverview, recipeTime, recipeIngredients, recipeInstructions;
    private ImageView recipeImage;
    private Button btnSave;
    private ExtendedFloatingActionButton btnCookNow;
    private ImageButton btnBack;
    private MaterialToolbar topAppBar;
    private int userId;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe);

        userId = getIntent().getIntExtra("uid", -1);


        recipeOverview = findViewById(R.id.recipeOverview);
        recipeTime = findViewById(R.id.recipeTime);
        recipeIngredients = findViewById(R.id.recipeIngredients);
        recipeInstructions = findViewById(R.id.recipeInstructions);
        recipeImage = findViewById(R.id.recipeImage);
        //btnSave = asdsadas(R.id.saveRecipeBtn);
        btnCookNow = findViewById(R.id.fabCookNow);

        topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (RecipeActivity.this, MainActivity.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                finish();
            }
        });

        Intent intentCurrent = getIntent();
        String overview = intentCurrent.getStringExtra("overview");
        String title = intentCurrent.getStringExtra("title");
        String time = intentCurrent.getStringExtra("time");
        String ingredients = intentCurrent.getStringExtra("ingredients");
        String instructions = intentCurrent.getStringExtra("instructions");
        int imageRes = intentCurrent.getIntExtra("imageRes", R.drawable.creamy_pasta);
        String[] rawSteps = instructions.split("•\\s*<b>Step");

        List<String> cleanSteps = new ArrayList<>();
        for (String step : rawSteps) {
            if (!step.trim().isEmpty()) {
                // Strip HTML tags for clean display
                String fixedStepHtml = "<b>Step" + step;
                Spanned spanned = Html.fromHtml(fixedStepHtml, Html.FROM_HTML_MODE_LEGACY);
                String plainText = spanned.toString().trim();

                // Only add meaningful steps
                if (!plainText.isEmpty()) {
                    cleanSteps.add(plainText);
                }
            }
        }

        btnCookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (RecipeActivity.this, RecipeSteps.class);
                intent.putStringArrayListExtra("steps", new ArrayList<>(cleanSteps));
                intent.putExtra("uid", userId);
                startActivity(intent);
                finish();
            }
        });

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            // Detect if the home or the profile settings button is selected
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(RecipeActivity.this, MainActivity.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_saved) {
                // Navigate to Saved Recipes page
                Intent intent = new Intent(RecipeActivity.this, SavedRecipes.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_account) {
                // Navigate to Profile page
                Intent intent = new Intent(RecipeActivity.this, AccountProfile.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                return true;
            }
            return false;
        });


        recipeOverview.setText(overview);
        topAppBar.setTitle(title);
        recipeTime.setText(time);
        recipeInstructions.setText(Html.fromHtml(instructions, Html.FROM_HTML_MODE_LEGACY));
        recipeIngredients.setText(ingredients);
        Glide.with(this)
                .load(imageRes)
                .transform(new CenterCrop(), new RoundedCorners(16))
                .into(recipeImage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }
}