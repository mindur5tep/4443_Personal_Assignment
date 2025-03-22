package com.example.personal_assignment.database;

public class ProfileItem {
    private String label;
    private String key;
    private String value;

    public ProfileItem (String label, String key, String value){
        this.label = label;
        this.key = key;
        this.value = value;
    }

    public String getLabel(){
        return label;
    }
    public String getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }

}
