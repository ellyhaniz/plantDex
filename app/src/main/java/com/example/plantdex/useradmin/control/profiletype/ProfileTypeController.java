package com.example.plantdex.useradmin.control.profiletype;

import com.example.plantdex.common.ApiClient;
import com.example.plantdex.common.control.AuthController;
import com.example.plantdex.useradmin.entity.ProfileType;

import java.util.List;

/** Business logic for the User Admin's Profile Type Management (list + edit name/description). */
public final class ProfileTypeController {

    private ProfileTypeController() {}

    public interface ListCallback {
        void onSuccess(List<ProfileType> profileTypes);
        void onError(String message);
    }

    public static void listTypes(ListCallback callback) {
        ApiClient.getProfileTypes(new ApiClient.Callback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                callback.onSuccess(ProfileType.listFromJson(response.optJSONArray("profileTypes")));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    /** Renames a profile type and/or edits its description/permissions. There's deliberately no roleCode parameter — that part never changes. */
    public static void updateType(int profileTypeId, String name, String description, String permissions,
                                   int updatedByUserId, AuthController.SimpleCallback callback) {
        ApiClient.updateProfileType(profileTypeId, name, description, permissions, updatedByUserId, new ApiClient.Callback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
