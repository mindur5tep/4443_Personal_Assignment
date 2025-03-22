package com.example.personal_assignment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_assignment.database.DiaryEntry; // Confirm this package is correct
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    private List<DiaryEntry> diaryEntries;
    private final Context context;

    public DiaryAdapter(Context context, List<DiaryEntry> diaryEntries) {
        this.context = context;
        this.diaryEntries = diaryEntries;
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diary_entry, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        DiaryEntry entry = diaryEntries.get(position);
        holder.tvTitle.setText(entry.getTitle());
        holder.tvContent.setText(entry.getContent());
        holder.tvTimestamp.setText(entry.getUpdatedTime());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Diary.class);
            intent.putExtra("uid", entry.getUserId()); // Pass uid
            intent.putExtra("entryId", entry.getEntryId()); // Pass entry ID
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return diaryEntries.size();
    }

    public void updateEntries(List<DiaryEntry> newEntries) {
        this.diaryEntries.clear();  // Clear old data
        this.diaryEntries.addAll(newEntries);  // Add new data
        notifyDataSetChanged();  // Notify RecyclerView of changes
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTimestamp;
        MaterialCardView cardView;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvContent = itemView.findViewById(R.id.tvContent);
            cardView = itemView.findViewById(R.id.cardView);

        }
    }
}
