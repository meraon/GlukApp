package com.example.glukdataapp.realm;

import com.example.glukdataapp.realm.models.GlucoseEntry;
import com.example.glukdataapp.realm.models.IModel;
import com.example.glukdataapp.realm.models.InsulinEntry;

import java.util.List;

public interface IRealmControl {
    boolean saveValue(IModel item);
    List<GlucoseEntry> getGlucoseList();
    List<InsulinEntry> getInsulinList();
    void deleteEntry(IModel item);
    int generateGlucoseId();
    int generateInsulinId();
    void updateDate(IModel item, int year, int month, int dayOfMonth);
    void updateTime(IModel item, int hours, int mins);
    void updateValue(IModel item, Float value);
    void clearGlucoses();
    void clearInsulins();
    void clearRealm();
}
