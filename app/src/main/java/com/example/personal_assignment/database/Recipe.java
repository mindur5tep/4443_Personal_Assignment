package com.example.personal_assignment.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "time")
    public String time;

    @ColumnInfo(name = "imageRes")
    public int imageRes;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "ingredients")
    public String ingredients;

    @ColumnInfo(name = "instructions")
    public String instructions;

    @ColumnInfo(name = "overview")
    public String overview;

    @ColumnInfo(name = "is_saved")
    private boolean isSaved;

    public Recipe(String overview, String title, String time, int imageRes, String category, String ingredients, String instructions, boolean isSaved) {
        this.overview = overview;
        this.title = title;
        this.time = time;
        this.imageRes = imageRes;
        this.category = category;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getOverview() {
        return overview;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public int getImageRes() {
        return imageRes;
    }

    public String getCategory() {
        return category;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public boolean isSaved() { return isSaved; }
    public void setSaved(boolean saved) { isSaved = saved; }
}
