package com.example.plantdex.useradmin.entity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * One of the app's 4 account types. {@code roleCode} is fixed internally
 * (the app's dashboards route on it) — {@code name}/{@code description}/
 * {@code permissions} are the only parts a User Admin can edit. Permissions
 * is descriptive text only — nothing in the app enforces it yet.
 */
public class ProfileType {

    private final int id;
    private final String roleCode;
    private final String name;
    private final String description;
    private final String permissions;

    public ProfileType(int id, String roleCode, String name, String description, String permissions) {
        this.id = id;
        this.roleCode = roleCode;
        this.name = name;
        this.description = description;
        this.permissions = permissions;
    }

    public static ProfileType fromJson(JSONObject json) {
        return new ProfileType(
                json.optInt("id", 0),
                json.optString("roleCode", ""),
                json.optString("name", ""),
                json.optString("description", ""),
                json.optString("permissions", ""));
    }

    public static List<ProfileType> listFromJson(JSONArray array) {
        List<ProfileType> types = new ArrayList<>();
        if (array == null) return types;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj != null) types.add(fromJson(obj));
        }
        return types;
    }

    public int getId() {
        return id;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPermissions() {
        return permissions;
    }

    /** So this can be dropped straight into an ArrayAdapter<ProfileType> for the role-picker spinners and just show its name. */
    @Override
    public String toString() {
        return name;
    }
}
