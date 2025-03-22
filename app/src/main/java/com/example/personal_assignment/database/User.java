package com.example.personal_assignment.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = "username", unique = true)}) // Ensure username is indexed
public class User {

    @ColumnInfo(name = "username")
    public String username; // Set as primary key

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    public int uid;
    @ColumnInfo(name = "full_name")
    public String fullName;

    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "birth_date")
    public String birthDate;

    @ColumnInfo(name = "address")
    public String address;

    @ColumnInfo(name = "phone_number")
    public String phoneNumber;

    @ColumnInfo(name = "profile_pic")
    public String profilePic; // Optional (work on later if enough time)

    @Ignore
    public User(@NonNull String username, String fullName, String password, String birthDate, String address,
                String phoneNumber) {
        this.username = username;
        this.fullName = fullName;
        this.password = password;
        this.birthDate = birthDate;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public User() {

    }

    public int getUid() {
        return uid;
    }
    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public void setProfilePic(String profilePicUri) {
        this.profilePic = profilePicUri;
    }
    public String getProfilePic() {
        return profilePic;
    }

}
