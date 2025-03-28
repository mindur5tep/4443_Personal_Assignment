package com.example.personal_assignment.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
        tableName = "user_preferences",
        primaryKeys = {"uid", "preference"},
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "uid",
                childColumns = "uid",
                onDelete = ForeignKey.CASCADE
        )
)
public class UserPreference {
    @ColumnInfo(name = "uid")
    public int uid;

    @NonNull
    @ColumnInfo(name = "preference")
    public String preference;

    public UserPreference(int uid, @NonNull String preference) {
        this.uid = uid;
        this.preference = preference;
    }
}

