package com.example.plantdex.common.control;

import com.example.plantdex.common.ApiClient;
import com.example.plantdex.common.entity.AuditLogEntry;

import java.util.List;

/** Business logic for retrieving the audit trail. */
public final class AuditLogController {

    private AuditLogController() {}

    public interface Callback {
        void onSuccess(List<AuditLogEntry> entries);
        void onError(String message);
    }

    public static void loadEntries(Callback callback) {
        ApiClient.getAuditLog(new ApiClient.Callback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                // Boundary classes only ever see AuditLogEntry objects, never raw JSON —
                // this is the one place that conversion happens.
                callback.onSuccess(AuditLogEntry.listFromJson(response.optJSONArray("entries")));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
