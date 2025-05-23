package com.example.personal_assignment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ImageView profilePic;
    private TextView headerName;
    private int userId = -1;
    private RecipeAdapter carouselAdapter;
    private FilteredRecipeAdapter filteredAdapter;
    private ChipGroup chipGroupCategory;
    private RecipeDao recipeDao;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        profilePic = findViewById(R.id.profilePicture);
        headerName = findViewById(R.id.main_header);

        // Initialize DB access objects
        recipeDao = ProfileDatabase.getDatabase(getApplicationContext()).recipeDao();
        userDao = ProfileDatabase.getDatabase(getApplicationContext()).userDao();

        // Get userID passed from the login activity
        userId = getIntent().getIntExtra("uid", -1);

        // Setup popular recipes carousel
        RecyclerView rvPopular = findViewById(R.id.rvRecipes);
        rvPopular.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        carouselAdapter = new RecipeAdapter(this::openRecipeDetail);
        rvPopular.setAdapter(carouselAdapter);

        // Setup filtered recipes list
        RecyclerView rvFiltered = findViewById(R.id.rvFilteredRecipes);
        rvFiltered.setLayoutManager(new LinearLayoutManager(this));
        filteredAdapter = new FilteredRecipeAdapter(this, this::openRecipeDetail, recipeDao);
        rvFiltered.setAdapter(filteredAdapter);

        // Query and update popular recipes synchronously
        executorService.execute(() -> {
            List<Recipe> popularRecipes = recipeDao.getTopPopularRecipes(); // Synchronous call
            runOnUiThread(() -> carouselAdapter.submitList(popularRecipes));
        });

        // Query and update all recipes synchronously for the filtered list
        executorService.execute(() -> {
            List<Recipe> allRecipes = recipeDao.getAllRecipes(); // Synchronous call
            runOnUiThread(() -> filteredAdapter.submitList(allRecipes));
        });

        chipGroupCategory = findViewById(R.id.chipGroup);
        chipGroupCategory.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedCategory = ((Chip) group.findViewById(checkedId)).getText().toString();
            filterByCategory(selectedCategory);
        });

        // Seed the database if empty and update user header info
        executorService.execute(() -> {
            int count = recipeDao.getRecipeCount();
            if (count == 0) {
                seedRecipes(recipeDao);
            }

            List<Recipe> popularRecipes = recipeDao.getTopPopularRecipes();

            List<Recipe> allRecipes = recipeDao.getAllRecipes();

            String fullName = userDao.getUserFullNameByUid(userId);
            String profilePicSrc = userDao.getUserProfilePictureByUid(userId);

            runOnUiThread(() -> {
                if (fullName != null && !fullName.isEmpty()) {
                    headerName.setText(getString(R.string.greeting_message, fullName));
                }
                if (profilePicSrc != null) {
                    Uri uriImage = Uri.parse(profilePicSrc);
                    profilePic.setImageURI(uriImage);
                }
                // Update adapters
                carouselAdapter.submitList(popularRecipes);
                filteredAdapter.submitList(allRecipes);
            });
        });

        // Bottom Navigation
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on Home, do nothing
                return true;
            } else if (itemId == R.id.nav_saved) {
                Intent intent = new Intent(MainActivity.this, SavedRecipes.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_account) {
                Intent intent = new Intent(MainActivity.this, AccountProfile.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
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
        int count = recipeDao.getRecipeCount();
        if (count == 0) {
            List<Recipe> sampleRecipes = new ArrayList<>();

            sampleRecipes.add(new Recipe(
                    getString(R.string.fruit_salad_overview),
                    getString(R.string.fruit_salad_title),
                    getString(R.string.fruit_salad_time),
                    R.drawable.fruit_salad,
                    "Dessert",
                    getString(R.string.fruit_salad_ingredients),
                    getString(R.string.fruit_salad_instructions),
                    false
            ));

            sampleRecipes.add(new Recipe(
                    getString(R.string.omelette_overview),
                    getString(R.string.omelette_title),
                    getString(R.string.omelette_time),
                    R.drawable.omelette,
                    "Breakfast",
                    getString(R.string.omelette_ingredients),
                    getString(R.string.omelette_instructions),
                    false
            ));

            sampleRecipes.add(new Recipe(
                    getString(R.string.chicken_teriyaki_overview),
                    getString(R.string.chicken_teriyaki_title),
                    getString(R.string.chicken_teriyaki_time),
                    R.drawable.chicken_teriyaki,
                    "Chinese",
                    getString(R.string.chicken_teriyaki_ingredients),
                    getString(R.string.chicken_teriyaki_instructions),
                    false
            ));

            sampleRecipes.add(new Recipe(
                    getString(R.string.parfait_overview),
                    getString(R.string.parfait_title),
                    getString(R.string.parfait_time),
                    R.drawable.yogurt_parfait,
                    "Dessert",
                    getString(R.string.parfait_ingredients),
                    getString(R.string.parfait_instructions),
                    false
            ));

            sampleRecipes.add(new Recipe(
                    getString(R.string.cookie_dough_overview),
                    getString(R.string.cookie_dough_title),
                    getString(R.string.cookie_dough_time),
                    R.drawable.cookie_dough,
                    "Dessert",
                    getString(R.string.cookie_dough_ingredients),
                    getString(R.string.cookie_dough_instructions),
                    false
            ));

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
        if (category.equalsIgnoreCase("All")) {
            executorService.execute(() -> {
                List<Recipe> allRecipes = recipeDao.getAllRecipes();
                runOnUiThread(() -> filteredAdapter.submitList(allRecipes));
            });
        } else {
            executorService.execute(() -> {
                List<Recipe> catRecipes = recipeDao.getRecipesByCategory(category);
                runOnUiThread(() -> filteredAdapter.submitList(catRecipes));
            });
        }
    }
}
