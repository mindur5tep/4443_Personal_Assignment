package com.example.personal_assignment;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_assignment.database.ProfileDatabase;
import com.example.personal_assignment.database.Recipe;
import com.example.personal_assignment.database.RecipeDao;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavedRecipes extends AppCompatActivity {

    private FilteredRecipeAdapter adapter;
    private int userId;
    private RecipeDao recipeDao;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_recipes);

        // Get user ID passed from the previous activity
        userId = getIntent().getIntExtra("uid", -1);

        // Set up RecyclerView and adapter
        RecyclerView recyclerView = findViewById(R.id.rvSavedRecipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeDao = ProfileDatabase.getDatabase(getApplicationContext()).recipeDao();
        adapter = new FilteredRecipeAdapter(this, this::openRecipeDetail, recipeDao);
        recyclerView.setAdapter(adapter);

        // Apply window insets for edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Fetch saved recipes synchronously in the background
        executorService.execute(() -> {
            List<Recipe> savedRecipes = recipeDao.getSavedRecipes(); // Synchronous call
            runOnUiThread(() -> adapter.submitList(savedRecipes));
        });

        // Bottom Navigation setup
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_saved); // Mark Saved as selected
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(SavedRecipes.this, MainActivity.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_saved) {
                return true; // Already on SavedRecipes page
            } else if (itemId == R.id.nav_account) {
                Intent intent = new Intent(SavedRecipes.this, AccountProfile.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void openRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra("uid", userId);
        intent.putExtra("overview", recipe.getOverview());
        intent.putExtra("title", recipe.getTitle());
        intent.putExtra("imageRes", recipe.getImageRes());
        intent.putExtra("time", recipe.getTime());
        intent.putExtra("ingredients", recipe.getIngredients());
        intent.putExtra("instructions", recipe.getInstructions());
        intent.putExtra("category", recipe.getCategory());
        startActivity(intent);
    }
}
