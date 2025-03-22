package com.example.personal_assignment.database;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(
        tableName = "diary",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "uid",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        ),
        indices = {@Index(value = "userId")}
)
public class DiaryEntry {
    @PrimaryKey(autoGenerate = true)
    private int entryId;
    private int userId;
    private String title;
    private String content;

    private String createdTime;
    private String updatedTime;

    public DiaryEntry(){

    }
    @Ignore
    public DiaryEntry(int userId, String title, String content) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdTime = getCurrentTimestamp();
        this.updatedTime = getCurrentTimestamp();
    }

    public String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Getters and setters
    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedTime (){
        return createdTime;
    }

    public void setUpdatedTime() {
        this.updatedTime = getCurrentTimestamp();
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) { this.updatedTime = updatedTime; }


}
