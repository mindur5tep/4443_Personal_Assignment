package com.example.personal_assignment.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RecipeDao {

    @Insert
    void insert(Recipe recipe);

    @Query("SELECT * FROM recipes")
    LiveData<List<Recipe>> getAllRecipes();

    @Query("SELECT * FROM recipes WHERE category IN (:categories)")
    LiveData<List<Recipe>> getRecipesByPreferences(List<String> categories);

    @Query("SELECT * FROM recipes WHERE LOWER(category) = LOWER(:category)")
    LiveData<List<Recipe>> getRecipesByCategory(String category);

    @Query("SELECT * FROM recipes WHERE is_saved = 1")
    LiveData<List<Recipe>> getSavedRecipes();

    @Query("UPDATE recipes SET is_saved = :isSaved WHERE id = :recipeId")
    void updateSavedStatus(int recipeId, boolean isSaved);

    @Query("SELECT * FROM recipes ORDER BY id LIMIT 10")
    LiveData<List<Recipe>> getTopPopularRecipes();

    @Update
    void update(Recipe recipe);

    @Query("SELECT COUNT(*) FROM recipes")
    int getRecipeCount();

    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    Recipe getRecipeById(int recipeId);
}
