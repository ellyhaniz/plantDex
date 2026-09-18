package com.example.plantdex.common;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Talks to the Flask API (see /backend) over plain HTTP. No third-party
 * networking library — just HttpURLConnection and org.json, both already
 * part of the Android SDK.
 */
public final class ApiClient {

    // Tunneled straight to this Mac's port 5000 via `adb reverse tcp:5000 tcp:5000`
    // (bypasses the emulator's virtual NIC, which was timing out unreliably).
    // Re-run that command after every emulator restart — or, once you're on a
    // physical device, replace this with your computer's LAN IP address
    // (see backend/README.md).
    private static final String BASE_URL = "http://localhost:5000";

    private static final int TIMEOUT_MS = 20000;
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private ApiClient() {}

    // Every call below is fire-and-forget from the caller's point of view —
    // it always runs on a background thread and reports back on this one
    // via onSuccess/onError, so callers never have to worry about threading.
    public interface Callback {
        void onSuccess(JSONObject response);
        void onError(String message);
    }

    // ------------------------------------------------------------- auth --

    public static void register(String email, String fullName, String username, String password, Callback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("fullName", fullName);
            body.put("username", username);
            body.put("password", password);
        } catch (Exception e) {
            deliverError(callback, "Something went wrong. Please try again.");
            return;
        }
        sendJson("POST", "/api/auth/register", body, callback);
    }

    public static void login(String username, String password, Callback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("username", username);
            body.put("password", password);
        } catch (Exception e) {
            deliverError(callback, "Something went wrong. Please try again.");
            return;
        }
        sendJson("POST", "/api/auth/login", body, callback);
    }

    public static void logout(int userId, Callback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("userId", userId);
        } catch (Exception e) {
            deliverError(callback, "Something went wrong.");
            return;
        }
        sendJson("POST", "/api/auth/logout", body, callback);
    }

    /** Self-service "forgot password" — no login required, identified by email + username. */
    public static void resetPasswordSelf(String email, String username, String newPassword, Callback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("username", username);
            body.put("newPassword", newPassword);
        } catch (Exception e) {
            deliverError(callback, "Something went wrong. Please try again.");
            return;
        }
        sendJson("POST", "/api/auth/reset-password", body, callback);
    }

    /** Self-service profile edit — full name and email only. */
    public static void updateMyProfile(int userId, String fullName, String email, Callback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("fullName", fullName);
            body.put("email", email);
        } catch (Exception e) {
            deliverError(callback, "Something went wrong. Please try again.");
            return;
        }
        sendJson("PUT", "/api/users/" + userId, body, callback);
    }

    public static void getAuditLog(Callback callback) {
        sendJson("GET", "/api/audit-log", null, callback);
    }

    // --------------------------------------------------- admin: profile types --

    public static void getProfileTypes(Callback callback) {
        sendJson("GET", "/api/profile-types", null, callback);
    }

    public static void updateProfileType(int profileTypeId, String name, String description, String permissions,
                                          int updatedByUserId, Callback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("description", description);
            body.put("permissions", permissions);
            body.put("updatedByUserId", updatedByUserId);
        } catch (Exception e) {
            deliverError(callback, "Something went wrong. Please try again.");
            return;
        }
        sendJson("PUT", "/api/profile-types/" + profileTypeId, body, callback);
    }

    public static void getSessionStats(Callback callback) {
        sendJson("GET", "/api/admin/session-stats", null, callback);
    }

    // ---------------------------------------------------------- admin: users --

    public static void getUsers(Callback callback) {
        sendJson("GET", "/api/admin/users", null, callback);
    }

    public static void getUser(int userId, Callback callback) {
        sendJson("GET", "/api/admin/users/" + userId, null, callback);
    }

    /** Admin-created account with an explicit profile type — used by "Create User Account". */
    public static void createUserAccount(String email, String fullName, String username, String password,
                                          int profileTypeId, int createdByUserId, Callback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("fullName", fullName);
            body.put("username", username);
            body.put("password", password);
            body.put("profileTypeId", profileTypeId);
            body.put("createdByUserId", createdByUserId);
        } catch (Exception e) {
            deliverError(callback, "Something went wrong. Please try again.");
            return;
        }
        sendJson("POST", "/api/admin/create-user", body, callback);
    }

    public static void updateUserAccount(int userId, String fullName, String email, int profileTypeId,
                                          String accountStatus, int updatedByUserId, Callback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("fullName", fullName);
            body.put("email", email);
            body.put("profileTypeId", profileTypeId);
            body.put("accountStatus", accountStatus);
            body.put("updatedByUserId", updatedByUserId);
        } catch (Exception e) {
            deliverError(callback, "Something went wrong. Please try again.");
            return;
        }
        sendJson("PUT", "/api/admin/users/" + userId, body, callback);
    }

    public static void resetPassword(int userId, String newPassword, int resetByUserId, Callback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("newPassword", newPassword);
            body.put("resetByUserId", resetByUserId);
        } catch (Exception e) {
            deliverError(callback, "Something went wrong. Please try again.");
            return;
        }
        sendJson("POST", "/api/admin/users/" + userId + "/reset-password", body, callback);
    }

    // ------------------------------------------------------------- plumbing --

    // Shared by every public method above — handles the actual HTTP call so
    // none of the individual endpoints have to repeat this boilerplate.
    // Retries once on a network failure before giving up, since the emulator's
    // connection to the backend occasionally hiccups for no real reason.
    private static void sendJson(String method, String path, JSONObject body, Callback callback) {
        new Thread(() -> {
            for (int attempt = 1; attempt <= 2; attempt++) {
                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) new URL(BASE_URL + path).openConnection();
                    conn.setRequestMethod(method);
                    conn.setConnectTimeout(TIMEOUT_MS);
                    conn.setReadTimeout(TIMEOUT_MS);
                    if (body != null) {
                        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                        conn.setDoOutput(true);
                        try (OutputStream os = conn.getOutputStream()) {
                            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                        }
                    }
                    handleResponse(conn, callback);
                    return; // got a response either way, no need to retry
                } catch (IOException e) {
                    android.util.Log.e("ApiClient", method + " " + path + " failed (attempt " + attempt + ")", e);
                    if (attempt == 2) {
                        deliverError(callback, "Couldn't reach the server. Check your connection and try again.");
                    }
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }
        }).start();
    }

    // Reads the response body regardless of status code, then routes it to
    // onSuccess/onError based on whether the server considered the request
    // a success (2xx) or not. The Flask side always sends a "message" field
    // on errors, so that's what we surface to the user.
    private static void handleResponse(HttpURLConnection conn, Callback callback) throws IOException {
        int code = conn.getResponseCode();
        InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String responseBody = readStream(stream);

        JSONObject json;
        try {
            json = responseBody.isEmpty() ? new JSONObject() : new JSONObject(responseBody);
        } catch (Exception e) {
            deliverError(callback, "The server sent back something unexpected.");
            return;
        }

        if (code >= 200 && code < 300) {
            deliverSuccess(callback, json);
        } else {
            deliverError(callback, json.optString("message", "Request failed (" + code + ")"));
        }
    }

    // HttpURLConnection only hands you a raw InputStream, so this just drains
    // it into a String — there's no built-in one-liner for that at this API level.
    private static String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int read;
        while ((read = stream.read(data)) != -1) {
            buffer.write(data, 0, read);
        }
        return buffer.toString(StandardCharsets.UTF_8.name());
    }

    // These two hop back onto the main thread before invoking the callback,
    // since sendJson runs the actual request on a background Thread and
    // Android forbids touching Views from anywhere else.
    private static void deliverSuccess(Callback callback, JSONObject json) {
        MAIN_HANDLER.post(() -> callback.onSuccess(json));
    }

    private static void deliverError(Callback callback, String message) {
        MAIN_HANDLER.post(() -> callback.onError(message));
    }
}
