package com.example.personal_assignment.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {


    @Insert
    long addUser(User user);

    @Update
    public void updateUser(User user);

    @Query("select * from users")
    public List<User> getAllUser();

    @Query("select * from users where username == :username")
    public User getUserByUsername(String username);

    // Get a user by their numeric uid
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    User getUserByUid(int uid);


    // Get a user password by their numeric uid
    @Query("SELECT password FROM users WHERE uid = :userId")
    String getUserPasswordByUid(int userId);

    // Delete user by their numeric uid
    @Query("DELETE FROM users WHERE uid = :userId")
    void deleteUserById(int userId);

    // Get a user full name  by their numeric uid
    @Query("SELECT full_name FROM users WHERE uid = :userId")
    String getUserFullNameByUid(int userId);

    @Query("SELECT profile_pic FROM users WHERE uid = :userId")
    String getUserProfilePictureByUid(int userId);

}
