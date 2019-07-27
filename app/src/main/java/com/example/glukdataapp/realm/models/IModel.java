package com.example.glukdataapp.realm.models;

public interface IModel {
    Long getTimestamp();
    Float getValue();

    String getDateString();
    String getTimeString();
    String getValueString();
    int getYear();
    int getMonth();
    int getDayOfMonth();
    int getHours();
    int getMinutes();
    int get_id();
    void setTime(int hours, int mins);
    void setDate(int year, int month, int dayOfMonth);
    void setValue(Float value);
    void setTimestamp(Long timestamp);
    void set_id(int id);


}
