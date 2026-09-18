package com.example.plantdex.useradmin.entity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** One bar of the User Admin dashboard's activity chart — a day and how many distinct users logged in. */
public class ActivityDay {

    private final String date;  // "2026-09-18", not shown directly — kept for possible future use
    private final String label; // "Thu" — what actually gets drawn under the bar
    private final int count;

    public ActivityDay(String date, String label, int count) {
        this.date = date;
        this.label = label;
        this.count = count;
    }

    public static ActivityDay fromJson(JSONObject json) {
        return new ActivityDay(
                json.optString("date", ""),
                json.optString("label", ""),
                json.optInt("count", 0));
    }

    public static List<ActivityDay> listFromJson(JSONArray array) {
        List<ActivityDay> days = new ArrayList<>();
        if (array == null) return days;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj != null) days.add(fromJson(obj));
        }
        return days;
    }

    public String getDate() {
        return date;
    }

    public String getLabel() {
        return label;
    }

    public int getCount() {
        return count;
    }
}
