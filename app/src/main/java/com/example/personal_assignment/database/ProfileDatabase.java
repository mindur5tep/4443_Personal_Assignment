package com.example.personal_assignment.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {User.class, DiaryEntry.class}, version = 10, exportSchema = false)

public abstract class ProfileDatabase extends RoomDatabase {
    private static volatile ProfileDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract DiaryDao diaryDao();

    public static ProfileDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ProfileDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ProfileDatabase.class, "profile_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Modify this based on what has changed in your database
            database.execSQL("ALTER TABLE users ADD COLUMN profilePic TEXT");
        }
    };
}

