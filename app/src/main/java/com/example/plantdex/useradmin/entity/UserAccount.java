package com.example.plantdex.useradmin.entity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** One row of the User Account Management list — any account a User Admin can manage. */
public class UserAccount {

    private final int userId;
    private final String username;
    private final String fullName;
    private final String email;
    private final String accountStatus;
    private final String createdDatetime;
    private final int profileTypeId;
    private final String role;          // fixed RoleCode, e.g. "SYSTEM ADMIN"
    private final String profileTypeName; // editable display name, e.g. "System Admin"

    public UserAccount(int userId, String username, String fullName, String email, String accountStatus,
                        String createdDatetime, int profileTypeId, String role, String profileTypeName) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.accountStatus = accountStatus;
        this.createdDatetime = createdDatetime;
        this.profileTypeId = profileTypeId;
        this.role = role;
        this.profileTypeName = profileTypeName;
    }

    public static UserAccount fromJson(JSONObject json) {
        return new UserAccount(
                json.optInt("userId", 0),
                json.optString("username", ""),
                json.optString("fullName", ""),
                json.optString("email", ""),
                json.optString("accountStatus", "ACTIVE"),
                json.optString("createdDatetime", ""),
                json.optInt("profileTypeId", 0),
                json.optString("role", ""),
                json.optString("profileTypeName", ""));
    }

    public static List<UserAccount> listFromJson(JSONArray array) {
        List<UserAccount> accounts = new ArrayList<>();
        if (array == null) return accounts;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj != null) accounts.add(fromJson(obj));
        }
        return accounts;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public String getCreatedDatetime() {
        return createdDatetime;
    }

    public int getProfileTypeId() {
        return profileTypeId;
    }

    public String getRole() {
        return role;
    }

    public String getProfileTypeName() {
        return profileTypeName;
    }
}
