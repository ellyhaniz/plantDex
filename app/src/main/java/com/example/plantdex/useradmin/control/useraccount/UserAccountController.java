package com.example.plantdex.useradmin.control.useraccount;

import com.example.plantdex.common.ApiClient;
import com.example.plantdex.common.control.AuthController;
import com.example.plantdex.useradmin.entity.UserAccount;

import java.util.List;

/** Business logic for the User Admin's account management (list, create, edit, reset password). */
public final class UserAccountController {

    private UserAccountController() {}

    public interface ListCallback {
        void onSuccess(List<UserAccount> accounts);
        void onError(String message);
    }

    public interface AccountCallback {
        void onSuccess(UserAccount account);
        void onError(String message);
    }

    // Every write below (create/update/reset password) takes a *ByUserId
    // parameter — that's always the admin doing the action, not the account
    // being acted on, so it can be logged as "who did this" in the audit trail.

    public static void getAccount(int userId, AccountCallback callback) {
        ApiClient.getUser(userId, new ApiClient.Callback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                callback.onSuccess(UserAccount.fromJson(response));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public static void listAccounts(ListCallback callback) {
        ApiClient.getUsers(new ApiClient.Callback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                callback.onSuccess(UserAccount.listFromJson(response.optJSONArray("users")));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public static void createAccount(String email, String fullName, String username, String password,
                                      int profileTypeId, int createdByUserId, AuthController.SimpleCallback callback) {
        ApiClient.createUserAccount(email, fullName, username, password, profileTypeId, createdByUserId, new ApiClient.Callback() {
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

    public static void updateAccount(int userId, String fullName, String email, int profileTypeId,
                                      String accountStatus, int updatedByUserId, AuthController.SimpleCallback callback) {
        ApiClient.updateUserAccount(userId, fullName, email, profileTypeId, accountStatus, updatedByUserId, new ApiClient.Callback() {
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

    public static void resetPassword(int userId, String newPassword, int resetByUserId, AuthController.SimpleCallback callback) {
        ApiClient.resetPassword(userId, newPassword, resetByUserId, new ApiClient.Callback() {
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
