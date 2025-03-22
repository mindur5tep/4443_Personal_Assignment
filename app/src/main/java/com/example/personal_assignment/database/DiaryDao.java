package com.example.personal_assignment.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DiaryDao {

    @Insert
    long insertEntry(DiaryEntry entry);

    @Update
    void updateEntry(DiaryEntry entry);

    @Delete
    void deleteEntry(DiaryEntry entry);

    @Query("SELECT * FROM diary WHERE entryId = :entryId")
    DiaryEntry getEntryById(int entryId);

    @Query("SELECT * FROM diary")
    List<DiaryEntry> getAllEntries();


    @Query("SELECT * FROM diary WHERE userId = :uid")
    List<DiaryEntry> getUserDiariesByUid(int uid);

    @Query("DELETE FROM diary WHERE entryId = :entryId")
    void deleteEntryById(int entryId);
}
