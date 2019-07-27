package com.example.glukdataapp.realm.models;

import android.support.v7.widget.CardView;

import com.example.glukdataapp.MyResources;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class GlucoseEntry extends RealmObject implements IModel {

    @PrimaryKey
    private int _id;
    @Required
    private Long timestamp;
    @Required
    private Float value;

    public GlucoseEntry() {
    }

    public GlucoseEntry(int id, Long timestamp, Float value) {
        set_id(id);
        setTimestamp(timestamp);
        setValue(value);
    }

    @Override
    public int get_id() {
        return _id;
    }

    @Override
    public void set_id(int _id) {
        this._id = _id;
    }

    @Override
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public Float getValue() {
        return value;
    }



    public void setValue(Float value) {
        this.value = value;
    }

    @Override
    public String getDateString() {
        MyResources resources = MyResources.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(resources.getDateFormat());
        Date date = new Date(timestamp);
        return df.format(date);
    }

    @Override
    public String getTimeString() {
        MyResources resources = MyResources.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(resources.getTimeFormat());
        Date time = new Date(timestamp);
        return df.format(time);
    }

    @Override
    public String getValueString() {
        MyResources resources = MyResources.getInstance();
        DecimalFormat df = new DecimalFormat(resources.getGlucoseValueFormat());
        return df.format(value);
    }

    @Override
    public int getYear() {
        Date d = new Date(timestamp);
        Calendar date_time = Calendar.getInstance();
        date_time.setTime(d);
        return date_time.get(Calendar.YEAR);
    }

    @Override
    public int getMonth() {
        Date d = new Date(timestamp);
        Calendar date_time = Calendar.getInstance();
        date_time.setTime(d);
        return date_time.get(Calendar.MONTH);
    }

    @Override
    public int getDayOfMonth() {
        Date d = new Date(timestamp);
        Calendar date_time = Calendar.getInstance();
        date_time.setTime(d);
        return date_time.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public int getHours() {
        Date d = new Date(timestamp);
        Calendar date_time = Calendar.getInstance();
        date_time.setTime(d);
        return date_time.get(Calendar.HOUR_OF_DAY);
    }

    @Override
    public int getMinutes() {
        Date d = new Date(timestamp);
        Calendar date_time = Calendar.getInstance();
        date_time.setTime(d);
        return date_time.get(Calendar.MINUTE);
    }

    @Override
    public void setTime(int hours, int mins) {
        Date d = new Date(timestamp);
        Calendar date_time = Calendar.getInstance();
        date_time.setTime(d);
        date_time.set(Calendar.HOUR_OF_DAY, hours);
        date_time.set(Calendar.MINUTE, mins);

        Date date = date_time.getTime();
        this.timestamp = date.getTime();
    }

    @Override
    public void setDate(int year, int month, int dayOfMonth) {
        Date d = new Date(timestamp);
        Calendar date_time = Calendar.getInstance();
        date_time.setTime(d);
        date_time.set(Calendar.YEAR, year);
        date_time.set(Calendar.MONTH, month);
        date_time.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        Date date = date_time.getTime();
        this.timestamp = date.getTime();
    }
}
