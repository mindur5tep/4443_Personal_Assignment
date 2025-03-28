package com.example.personal_assignment.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeDao {

    @Insert
    void insert(Recipe recipe);

    @Query("SELECT * FROM recipes")
    List<Recipe> getAllRecipes();

    @Query("SELECT * FROM recipes WHERE category IN (:categories)")
    List<Recipe> getRecipesByPreferences(List<String> categories);

    @Query("SELECT * FROM recipes WHERE LOWER(category) = LOWER(:category)")
    List<Recipe> getRecipesByCategory(String category);

    @Query("SELECT * FROM recipes WHERE is_saved = 1")
    List<Recipe> getSavedRecipes();

    @Query("UPDATE recipes SET is_saved = :isSaved WHERE id = :recipeId")
    void updateSavedStatus(int recipeId, boolean isSaved);

    @Query("SELECT * FROM recipes ORDER BY id LIMIT 10")
    List<Recipe> getTopPopularRecipes();
}
