package com.example.personal_assignment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

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
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private RecipeDao recipeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_recipes);

        userId = getIntent().getIntExtra("uid", -1);

        RecyclerView recyclerView = findViewById(R.id.rvSavedRecipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recipeDao = ProfileDatabase.getDatabase(getApplicationContext()).recipeDao();

        adapter = new FilteredRecipeAdapter(this, this::openRecipeDetail, recipeDao);
        recyclerView.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,0);
            return insets;

        });


        executor.execute(() -> {
            List<Recipe> savedRecipes = recipeDao.getSavedRecipes();
            runOnUiThread(() -> adapter.submitList(savedRecipes));
        });

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_saved); // Ensure Saved is selected

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(SavedRecipes.this, MainActivity.class);
                    intent.putExtra("uid", userId);
                    startActivity(intent);
                    finish();
                return true;

            } else if (itemId == R.id.nav_saved) {
                // Already on SavedRecipes page
                return true;

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

    @Override
    protected void onResume() {
        super.onResume();
        executor.execute(() -> {
            List<Recipe> savedRecipes = recipeDao.getSavedRecipes();
            runOnUiThread(() -> adapter.submitList(savedRecipes));
        });
    }
}
