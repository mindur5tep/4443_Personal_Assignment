package com.example.personal_assignment.database;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class UserPreferenceHelper {
    @Embedded
    public User user;

    @Relation(
            parentColumn = "uid",
            entityColumn = "uid"
    )
    public List<UserPreference> preferences;
}
