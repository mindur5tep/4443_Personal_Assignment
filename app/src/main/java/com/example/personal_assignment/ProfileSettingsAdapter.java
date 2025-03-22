package com.example.personal_assignment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_assignment.database.ProfileItem;
import java.util.List;

public class ProfileSettingsAdapter extends RecyclerView.Adapter<ProfileSettingsAdapter.ViewHolder> {

    private Context context;
    private List<ProfileItem> profileItems;
    private int userId;

    public ProfileSettingsAdapter(Context context, List<ProfileItem> profileItems, int userId) {
        this.context = context;
        this.profileItems = profileItems;
        this.userId = userId;
    }

    public void updateItems(List<ProfileItem> newItems) {
        this.profileItems.clear();
        this.profileItems.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfileSettingsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile_field, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProfileItem item = profileItems.get(position);
        holder.tvLabel.setText(item.getLabel());
        // Click to open EditProfileItemActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditProfileItemActivity.class);
            // Pass uid, fieldKey, fieldLabel and fieldValue to identify which options clicked by the user,
            // and use them to populate the top nav bar title, text input field type and initial contents
            // of the input field (current user credentials)
            intent.putExtra("uid", userId);
            intent.putExtra("fieldKey", item.getKey());
            intent.putExtra("fieldLabel", item.getLabel());
            intent.putExtra("fieldValue", item.getValue());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return profileItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        ImageView ivArrow;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}
