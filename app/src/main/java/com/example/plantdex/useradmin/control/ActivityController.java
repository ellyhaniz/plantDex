package com.example.plantdex.useradmin.control;

import com.example.plantdex.common.ApiClient;
import com.example.plantdex.useradmin.entity.ActivityDay;

import java.util.List;

/** Business logic for the User Admin dashboard's daily-active-users chart. */
public final class ActivityController {

    private ActivityController() {}

    public interface Callback {
        void onSuccess(List<ActivityDay> days);
        void onError(String message);
    }

    /** Trailing 7 days ending today, one entry per day (0 for days with no logins) — matches what the chart draws left to right. */
    public static void loadWeeklyActivity(Callback callback) {
        ApiClient.getSessionStats(new ApiClient.Callback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                callback.onSuccess(ActivityDay.listFromJson(response.optJSONArray("days")));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
