package com.example.plantdex.common.entity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** One row of the audit trail (a login, logout, or account action). */
public class AuditLogEntry {

    private final int id;
    private final String username;
    private final String role;
    private final String profileTypeName;
    private final String action;
    private final String affectedRecord;
    private final String details;
    private final String timestamp;

    public AuditLogEntry(int id, String username, String role, String profileTypeName, String action,
                          String affectedRecord, String details, String timestamp) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.profileTypeName = profileTypeName;
        this.action = action;
        this.affectedRecord = affectedRecord;
        this.details = details;
        this.timestamp = timestamp;
    }

    public static AuditLogEntry fromJson(JSONObject json) {
        return new AuditLogEntry(
                json.optInt("id", 0),
                json.optString("username", ""),
                json.optString("role", ""),
                json.optString("profileTypeName", ""),
                json.optString("action", ""),
                // affectedRecord is genuinely optional (e.g. null for a plain
                // login/logout, set for "admin X updated account Y") — keep
                // it as a real null rather than coercing it to an empty string.
                json.isNull("affectedRecord") ? null : json.optString("affectedRecord", null),
                json.optString("details", ""),
                json.optString("timestamp", ""));
    }

    /** Convenience for turning the whole "entries" array from the API response into a plain list in one call. */
    public static List<AuditLogEntry> listFromJson(JSONArray array) {
        List<AuditLogEntry> entries = new ArrayList<>();
        if (array == null) return entries;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj != null) entries.add(fromJson(obj));
        }
        return entries;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getProfileTypeName() {
        return profileTypeName;
    }

    public String getAction() {
        return action;
    }

    public String getAffectedRecord() {
        return affectedRecord;
    }

    public String getDetails() {
        return details;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
