package com.example.plantdex.useradmin.boundary;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.plantdex.R;
import com.example.plantdex.common.BottomNavHelper;
import com.example.plantdex.common.Roles;
import com.example.plantdex.common.boundary.AuditLogActivity;
import com.example.plantdex.common.boundary.MyAccountActivity;
import com.example.plantdex.common.control.AuditLogController;
import com.example.plantdex.common.entity.AuditLogEntry;
import com.example.plantdex.useradmin.boundary.profiletype.ProfileTypeManagementActivity;
import com.example.plantdex.useradmin.boundary.useraccount.UserAccountManagementActivity;
import com.example.plantdex.useradmin.control.ActivityController;
import com.example.plantdex.useradmin.entity.ActivityDay;

import java.util.List;

/**
 * Entry point for the User Admin role. The two big tiles hand off to Profile
 * Type Management and User Account Management; the rest of this screen —
 * the weekly activity chart and the latest audit entry preview — is just a
 * live summary that refreshes every time the screen is shown again.
 */
public class UserAdminDashboardActivity extends AppCompatActivity {

    private static final int CHART_HEIGHT_DP = 140;

    private LinearLayout barsContainer;
    private LinearLayout dayLabelsContainer;
    private View latestAuditEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_admin_dashboard);

        BottomNavHelper.setup(this, findViewById(R.id.navContainer), Roles.USER_ADMIN);
        ((TextView) findViewById(R.id.header).findViewById(R.id.tvRoleId)).setText(Roles.displayId(Roles.USER_ADMIN));

        View tileProfileType = findViewById(R.id.tileProfileType);
        ((ImageView) tileProfileType.findViewById(R.id.actionIcon)).setImageResource(R.drawable.ic_person);
        ((TextView) tileProfileType.findViewById(R.id.actionLabel)).setText("Profile Type Management");
        tileProfileType.setOnClickListener(v -> startActivity(new Intent(this, ProfileTypeManagementActivity.class)));

        View tileUserAccount = findViewById(R.id.tileUserAccount);
        ((ImageView) tileUserAccount.findViewById(R.id.actionIcon)).setImageResource(R.drawable.ic_person);
        ((TextView) tileUserAccount.findViewById(R.id.actionLabel)).setText("User Account Management");
        tileUserAccount.setOnClickListener(v -> startActivity(new Intent(this, UserAccountManagementActivity.class)));

        View.OnClickListener auditEntryClick = v -> {
            Intent intent = new Intent(this, AuditLogActivity.class);
            intent.putExtra(MyAccountActivity.EXTRA_ROLE, Roles.USER_ADMIN);
            startActivity(intent);
        };
        latestAuditEntry = findViewById(R.id.latestAuditEntry);
        latestAuditEntry.setOnClickListener(auditEntryClick);
        findViewById(R.id.tvViewMoreAudit).setOnClickListener(auditEntryClick);

        barsContainer = findViewById(R.id.barsContainer);
        dayLabelsContainer = findViewById(R.id.dayLabelsContainer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // onResume rather than onCreate, so coming back from Profile Type/User
        // Account Management (or from Audit Log) shows fresh numbers, not a
        // stale snapshot from when the dashboard first opened.
        loadActivityChart();
        loadLatestAuditEntry();
    }

    private void loadActivityChart() {
        ActivityController.loadWeeklyActivity(new ActivityController.Callback() {
            @Override
            public void onSuccess(List<ActivityDay> days) {
                renderChart(days);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(UserAdminDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Builds the bar chart entirely out of plain Views — no charting library.
     * barsContainer and dayLabelsContainer are two parallel horizontal rows
     * in the layout (bars on top, day labels underneath); for each day we
     * add one column to each row, in the same order, so they line up.
     */
    private void renderChart(List<ActivityDay> days) {
        barsContainer.removeAllViews();
        dayLabelsContainer.removeAllViews();

        int maxCount = 0;
        for (ActivityDay day : days) maxCount = Math.max(maxCount, day.getCount());
        // Y-axis intervals of 10 (0/10/20/30…), with 30 as the floor. If a day
        // ever has more than 30 logins this just keeps stepping up by 10
        // until it fits, so the chart never clips.
        int yMax = 30;
        while (maxCount > yMax) yMax += 10;

        float density = getResources().getDisplayMetrics().density;
        int chartHeightPx = (int) (CHART_HEIGHT_DP * density);

        for (ActivityDay day : days) {
            // Each "column" is an equal-width slice of the row (weight 1f),
            // holding just the bar — the bar's own height is what actually
            // encodes the count, scaled against yMax.
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            barParams.setMarginEnd((int) (4 * density));
            barParams.setMarginStart((int) (4 * density));

            View bar = new View(this);
            // A day with 1 login would otherwise round down to a 0px bar and
            // vanish — floor it at 4dp so "something happened" is still visible.
            int barHeightPx = Math.max((int) (chartHeightPx * (day.getCount() / (float) yMax)), day.getCount() > 0 ? (int) (4 * density) : 0);
            LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, barHeightPx);
            bar.setLayoutParams(innerParams);
            bar.setBackgroundResource(R.drawable.bg_card);
            bar.setBackgroundColor(getColor(R.color.plantdex_button));

            LinearLayout column = new LinearLayout(this);
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            column.setLayoutParams(barParams);
            column.addView(bar);
            barsContainer.addView(column);

            // Matching label, same weight, added to the row directly below —
            // same index in both rows keeps this one under its own bar.
            TextView label = new TextView(this);
            label.setText(day.getLabel());
            label.setTextColor(getColor(R.color.plantdex_text_muted));
            label.setTextSize(10);
            label.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            label.setLayoutParams(labelParams);
            dayLabelsContainer.addView(label);
        }
    }

    private void loadLatestAuditEntry() {
        AuditLogController.loadEntries(new AuditLogController.Callback() {
            @Override
            public void onSuccess(List<AuditLogEntry> entries) {
                if (entries.isEmpty()) return;
                AuditLogEntry latest = entries.get(0);
                ((TextView) latestAuditEntry.findViewById(R.id.entryName)).setText(latest.getUsername());
                ((TextView) latestAuditEntry.findViewById(R.id.entryTimestamp)).setText(latest.getTimestamp());
                ((TextView) latestAuditEntry.findViewById(R.id.entryRef)).setText("#" + latest.getId());
                ((TextView) latestAuditEntry.findViewById(R.id.entryUsername)).setText("Username: " + latest.getUsername());
                ((TextView) latestAuditEntry.findViewById(R.id.entryProfileType)).setText("User Profile Type: " + latest.getProfileTypeName());
                ((TextView) latestAuditEntry.findViewById(R.id.entryAction)).setText("Action: " + latest.getAction());
                String details = latest.getDetails();
                ((TextView) latestAuditEntry.findViewById(R.id.entryDetails)).setText(
                        "Details: " + (details != null && !details.isEmpty() ? details : "—"));
            }

            @Override
            public void onError(String message) {
                // Non-fatal — the dashboard's preview tile just stays as-is.
            }
        });
    }
}
