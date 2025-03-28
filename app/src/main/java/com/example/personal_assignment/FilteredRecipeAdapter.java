package com.example.personal_assignment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;


import androidx.annotation.NonNull;
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
    private OnRecipeClickListener clickListener;
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image, favoriteIcon;
        TextView title, time, ingredients, overview;

        public ViewHolder(View itemView) {
            super(itemView);
            overview = itemView.findViewById(R.id.recipeOverview);
            image = itemView.findViewById(R.id.recipeImage);
            title = itemView.findViewById(R.id.recipeTitle);
            time = itemView.findViewById(R.id.recipeTime);
            ingredients = itemView.findViewById(R.id.recipeIngredients);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
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
        holder.title.setText(recipe.getTitle());
        holder.time.setText(recipe.getTime());

        String[] ingredients = recipe.getIngredients().split("\n");
        holder.ingredients.setText(ingredients.length + " ingredients");
        Glide.with(holder.itemView.getContext())
                .load(recipe.getImageRes())
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> clickListener.onRecipeClick(recipe));

        holder.favoriteIcon.setImageResource(recipe.isSaved() ?
                R.drawable.ic_heart_filled : R.drawable.ic_heart);
        holder.favoriteIcon.setOnClickListener(v -> {
            boolean newSavedStatus = !recipe.isSaved();
            recipe.setSaved(newSavedStatus);
            // Update UI
            notifyItemChanged(holder.getAdapterPosition());
            // Update in DB
            executor.execute(() -> recipeDao.updateSavedStatus(recipe.getId(), newSavedStatus));
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }


}

