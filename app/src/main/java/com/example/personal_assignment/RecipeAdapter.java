package com.example.personal_assignment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_assignment.database.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private final List<Recipe> recipes = new ArrayList<>();
    private final OnRecipeClickListener clickListener;

    public RecipeAdapter(OnRecipeClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void submitList(List<Recipe> newRecipes) {
        recipes.clear();
        recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.itemView.setOnClickListener(v -> clickListener.onRecipeClick(recipe));

        Glide.with(holder.itemView.getContext())
                .load(recipe.getImageRes())
                .into(holder.imgRecipe);

        holder.tvTitle.setText(recipe.getTitle());
        holder.tvDuration.setText(recipe.getTime());
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRecipe;
        TextView tvTitle, tvDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRecipe = itemView.findViewById(R.id.recipeImage);
            tvTitle = itemView.findViewById(R.id.recipeTitle);
            tvDuration = itemView.findViewById(R.id.recipeTime);
        }
    }
}
