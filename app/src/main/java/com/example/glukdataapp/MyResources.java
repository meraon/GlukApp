package com.example.glukdataapp;

public class MyResources {
    private static final MyResources resources = new MyResources();

    public static MyResources getInstance() {
        return resources;
    }

    private String dateFormat;
    private String timeFormat;
    private String GlucoseValueFormat;
    private String InsulinValueFormat;

    public String getGlucoseValueFormat() {
        return GlucoseValueFormat;
    }

    public void setGlucoseValueFormat(String glucoseValueFormat) {
        GlucoseValueFormat = glucoseValueFormat;
    }

    public String getInsulinValueFormat() {
        return InsulinValueFormat;
    }

    public void setInsulinValueFormat(String insulinValueFormat) {
        InsulinValueFormat = insulinValueFormat;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }


    private MyResources() {
    }
}
