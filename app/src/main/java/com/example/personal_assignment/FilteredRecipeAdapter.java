package com.example.personal_assignment;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_assignment.database.ProfileDatabase;
import com.example.personal_assignment.database.Recipe;
import com.example.personal_assignment.database.RecipeDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilteredRecipeAdapter extends RecyclerView.Adapter<FilteredRecipeAdapter.ViewHolder> {

    private List<Recipe> recipes = new ArrayList<>();
    private Context context;
    private static OnRecipeClickListener clickListener;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private RecipeDao recipeDao;


    public FilteredRecipeAdapter(Context context, OnRecipeClickListener clickListener, RecipeDao recipeDao) {
        this.context = context;
        this.clickListener = clickListener;
        this.recipeDao = recipeDao;
    }

    public void submitList(List<Recipe> updatedList) {
        recipes = updatedList;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage, recipeFavoriteIcon;
        TextView recipeTitle, recipeOverview, recipeIngredients, recipeTime;

        public ViewHolder(View itemView) {
            super(itemView);
            recipeOverview = itemView.findViewById(R.id.recipeOverview);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeTitle = itemView.findViewById(R.id.recipeTitle);
            recipeTime = itemView.findViewById(R.id.recipeTime);
            recipeIngredients = itemView.findViewById(R.id.recipeIngredients);
            recipeFavoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }

        public void bind(Recipe recipe) {
            recipeTitle.setText(recipe.getTitle());
            recipeIngredients.setText(recipe.getTime());

            String[] ingredients = recipe.getIngredients().split("\n");
            recipeIngredients.setText(ingredients.length + " ingredients");
            Glide.with(itemView.getContext())
                    .load(recipe.getImageRes())
                    .into(recipeImage);

            itemView.setOnClickListener(v -> clickListener.onRecipeClick(recipe));

            if (recipe.isSaved()) {
                recipeFavoriteIcon.setImageResource(R.drawable.ic_heart_filled);
            } else {
                recipeFavoriteIcon.setImageResource(R.drawable.ic_heart);
            }

            recipeFavoriteIcon.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    // Toggle the favorite (saved) state
                    boolean newState = !recipe.isSaved();
                    recipe.setSaved(newState);
                    updateFavoriteIcon(recipe);
                    notifyItemChanged(pos);

                    // Persist the change in a background thread
                    executor.execute(() -> {
                        recipeDao.update(recipe);
                        Recipe updated = recipeDao.getRecipeById(recipe.getId());
                        Log.d("FilteredRecipeAdapter", "DB now has recipe " + updated.getId() + " saved: " + updated.isSaved());
                    });
                }
            });
        }

        private void updateFavoriteIcon(Recipe recipe) {
            if (recipe.isSaved()) {
                // When saved, show a filled heart icon.
                recipeFavoriteIcon.setImageResource(R.drawable.ic_heart_filled);
            } else {
                // When not saved, show an outlined heart icon.
                recipeFavoriteIcon.setImageResource(R.drawable.ic_heart);
            }
        }
    }


    @NonNull
    @Override
    public FilteredRecipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilteredRecipeAdapter.ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }




}

