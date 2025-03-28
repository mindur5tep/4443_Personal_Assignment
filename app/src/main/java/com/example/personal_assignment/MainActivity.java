package com.example.personal_assignment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_assignment.database.ProfileDatabase;
import com.example.personal_assignment.database.Recipe;
import com.example.personal_assignment.database.RecipeDao;
import com.example.personal_assignment.database.UserDao;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private FloatingActionButton addEntry;
    private RecyclerView recyclerViewEntries;
    private LinearLayout emptyStateContainer; // Empty state illustration

    // Background DB operations
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ImageView profilePic;
    private TextView headerName;
    private int userId = -1;
    private RecipeAdapter carouselAdapter;
    private FilteredRecipeAdapter filteredAdapter;
    private ChipGroup chipGroupCategory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        profilePic = findViewById(R.id.profilePicture);
        // Floating Action Button ID
        headerName = findViewById(R.id.main_header);

        // Get userID passed from the login activity
        userId = getIntent().getIntExtra("uid", -1);
        chipGroupCategory = findViewById(R.id.chipGroup);

        chipGroupCategory.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedCategory = ((Chip) group.findViewById(checkedId)).getText().toString();
            filterByCategory(selectedCategory);
        });


        RecipeDao recipeDao = ProfileDatabase.getDatabase(getApplicationContext()).recipeDao();
        UserDao userDao = ProfileDatabase.getDatabase(getApplicationContext()).userDao();

        RecyclerView rvPopular = findViewById(R.id.rvRecipes);
        rvPopular.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        carouselAdapter = new RecipeAdapter(this::openRecipeDetail);
        rvPopular.setAdapter(carouselAdapter);

        executorService.execute(() -> {
            List<Recipe> popularRecipes = recipeDao.getTopPopularRecipes(); // e.g. top 5 by ID or name
            runOnUiThread(() -> carouselAdapter.submitList(popularRecipes));
        });

        RecyclerView rvFiltered = findViewById(R.id.rvFilteredRecipes);
        rvFiltered.setLayoutManager(new LinearLayoutManager(this));
        filteredAdapter = new FilteredRecipeAdapter(this, this::openRecipeDetail, recipeDao);
        rvFiltered.setAdapter(filteredAdapter);


        executorService.execute(() -> {
            seedRecipes(recipeDao);
            String fullName = userDao.getUserFullNameByUid(userId); // Method to get full name by UID
            String profilePicSrc = userDao.getUserProfilePictureByUid(userId);

            List<String> userPrefs = userDao.getPreferencesByUser(userId);

            List<Recipe> filteredRecipes;

            if (userPrefs.isEmpty()) {
                filteredRecipes = recipeDao.getAllRecipes(); // fallback
            } else {
                filteredRecipes = recipeDao.getRecipesByPreferences(userPrefs);
            }

            runOnUiThread(() -> {
                if (fullName != null && !fullName.isEmpty()) {
                    headerName.setText(getString(R.string.greeting_message, fullName));
                }
                if (profilePicSrc != null){
                    Uri uriImage = Uri.parse(profilePicSrc);
                    profilePic.setImageURI(uriImage);
                }

                filteredAdapter.submitList(filteredRecipes);
            });
        });


        // Bottom Navigation
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            // Detect if the home or the profile settings button is selected
            if (itemId == R.id.nav_home) {
                // Already on Home, do nothing
                return true;
            } else if (itemId == R.id.nav_saved) {
                // Navigate to Saved Recipes page
                Intent intent = new Intent(MainActivity.this, SavedRecipes.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_account) {
                // Navigate to Profile page
                Intent intent = new Intent(MainActivity.this, AccountProfile.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                return true;
            }
            return false;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void loadPopularRecipes() {
        RecipeDao recipeDao = ProfileDatabase.getDatabase(getApplicationContext()).recipeDao();
        executorService.execute(() -> {
            List<Recipe> popularRecipes = recipeDao.getTopPopularRecipes(); // This pulls correct isSaved values
            runOnUiThread(() -> {
                filteredAdapter.submitList(popularRecipes);
            });
        });
    }


    private void openRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(MainActivity.this, RecipeActivity.class);
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

    private void seedRecipes(RecipeDao recipeDao) {
        if (recipeDao.getAllRecipes().isEmpty()) {
            List<Recipe> sampleRecipes = new ArrayList<>();

            sampleRecipes.add(new Recipe(
                    getString(R.string.recipe_overview_creamy_pasta),
                    getString(R.string.recipe_title_creamy_pasta),
                    getString(R.string.recipe_time_creamy_pasta),
                    R.drawable.creamy_pasta,
                    "Italian",
                    getString(R.string.recipe_ingredients_creamy_pasta),
                    getString(R.string.creamy_pasta_instructions), false
            ));

            sampleRecipes.add(new Recipe(
                    getString(R.string.mapo_tofu_overview),
                    getString(R.string.mapo_tofu_title),
                    getString(R.string.mapo_tofu_time),
                    R.drawable.mapo_tofu,
                    "Chinese",
                    getString(R.string.mapo_tofu_ingredients),
                    getString(R.string.mapo_tofu_instructions), false
            ));

            sampleRecipes.add(new Recipe(
                    getString(R.string.sushi_rolls_overview),
                    getString(R.string.sushi_rolls_title),
                    getString(R.string.sushi_rolls_time),
                    R.drawable.sushi_rolls,
                    "Japanese",
                    getString(R.string.sushi_rolls_ingredients),
                    getString(R.string.sushi_rolls_instructions), false
            ));

            sampleRecipes.add(new Recipe(
                    getString(R.string.chicken_tikka_overview),
                    getString(R.string.chicken_tikka_title),
                    getString(R.string.chicken_tikka_time),
                    R.drawable.chicken_tikka,
                    "Indian",
                    getString(R.string.chicken_tikka_ingredients),
                    getString(R.string.chicken_tikka_instructions), false
            ));

            sampleRecipes.add(new Recipe(
                    getString(R.string.greek_salad_overview),
                    getString(R.string.greek_salad_title),
                    getString(R.string.greek_salad_time),
                    R.drawable.greek_salad,
                    "Greek",
                    getString(R.string.greek_salad_ingredients),
                    getString(R.string.greek_salad_instructions), false
            ));

            sampleRecipes.add(new Recipe(
                    getString(R.string.pad_thai_overview),
                    getString(R.string.pad_thai_title),
                    getString(R.string.pad_thai_time),
                    R.drawable.pad_thai,
                    "Thai",
                    getString(R.string.pad_thai_ingredients),
                    getString(R.string.pad_thai_instructions), false
            ));

            sampleRecipes.add(new Recipe(
                    getString(R.string.falafel_wrap_overview),
                    getString(R.string.falafel_wrap_title),
                    getString(R.string.falafel_wrap_time),
                    R.drawable.falafel_wrap,
                    "Middle Eastern",
                    getString(R.string.falafel_wrap_ingredients),
                    getString(R.string.falafel_wrap_instructions), false
            ));

            sampleRecipes.add(new Recipe(
                    getString(R.string.elote_overview),
                    getString(R.string.elote_title),
                    getString(R.string.elote_time),
                    R.drawable.elote,
                    "Mexican",
                    getString(R.string.elote_ingredients),
                    getString(R.string.elote_instructions), false
            ));

            sampleRecipes.add(new Recipe(
                    getString(R.string.tiramisu_overview),
                    getString(R.string.tiramisu_title),
                    getString(R.string.tiramisu_time),
                    R.drawable.tiramisu,
                    "Italian",
                    getString(R.string.tiramisu_ingredients),
                    getString(R.string.tiramisu_instructions), false
            ));

            sampleRecipes.add(new Recipe(
                    getString(R.string.bibimbap_overview),
                    getString(R.string.bibimbap_title),
                    getString(R.string.bibimbap_time),
                    R.drawable.bibimbap,
                    "Korean",
                    getString(R.string.bibimbap_ingredients),
                    getString(R.string.bibimbap_instructions), false
            ));

            for (Recipe recipe : sampleRecipes) {
                recipeDao.insert(recipe);
            }
        }
    }


    private void filterByCategory(String category) {
        executorService.execute(() -> {
            RecipeDao recipeDao = ProfileDatabase.getDatabase(getApplicationContext()).recipeDao();
            UserDao userDao = ProfileDatabase.getDatabase(getApplicationContext()).userDao();
            List<Recipe> filtered;

            if (category.equalsIgnoreCase("All")) {
                List<String> userPrefs = userDao.getPreferencesByUser(userId);
                if (userPrefs.isEmpty()) {
                    List<Recipe> allRecipes = recipeDao.getAllRecipes();
                    if (allRecipes.size() > 10) {
                        List<Recipe> shuffled = new ArrayList<>(allRecipes);
                        java.util.Collections.shuffle(shuffled);
                        filtered = shuffled.subList(0, 10);
                    } else {
                        filtered = allRecipes;
                    }
                } else {
                    filtered = recipeDao.getRecipesByPreferences(userPrefs);
                }
            } else {
                filtered = recipeDao.getRecipesByCategory(category);
            }

            runOnUiThread(() -> {
                filteredAdapter.submitList(filtered);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        RecipeDao recipeDao = ProfileDatabase.getDatabase(getApplicationContext()).recipeDao();
        UserDao userDao = ProfileDatabase.getDatabase(getApplicationContext()).userDao();

        executorService.execute(() -> {
            List<String> userPrefs = userDao.getPreferencesByUser(userId);
            List<Recipe> filteredRecipes;

            if (userPrefs.isEmpty()) {
                filteredRecipes = recipeDao.getAllRecipes(); // fallback
            } else {
                filteredRecipes = recipeDao.getRecipesByPreferences(userPrefs);
            }

            runOnUiThread(() -> filteredAdapter.submitList(filteredRecipes));
        });
    }

}
