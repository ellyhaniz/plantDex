package com.example.plantdex.common.boundary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.plantdex.R;
import com.example.plantdex.common.BottomNavHelper;
import com.example.plantdex.common.Roles;
import com.example.plantdex.common.control.AuditLogController;
import com.example.plantdex.common.entity.AuditLogEntry;

import java.util.List;

/** Shared "Audit Logs" list screen used by both User Admin and System Admin. */
public class AuditLogActivity extends AppCompatActivity {

    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audit_log);

        String role = getIntent().getStringExtra(MyAccountActivity.EXTRA_ROLE);
        if (role == null) role = Roles.USER_ADMIN;

        BottomNavHelper.setup(this, findViewById(R.id.navContainer), role);
        ((TextView) findViewById(R.id.header).findViewById(R.id.tvRoleId)).setText(Roles.displayId(role));

        container = findViewById(R.id.entriesContainer);
        loadEntries();
    }

    private void loadEntries() {
        AuditLogController.loadEntries(new AuditLogController.Callback() {
            @Override
            public void onSuccess(List<AuditLogEntry> entries) {
                if (entries.isEmpty()) {
                    TextView empty = new TextView(AuditLogActivity.this);
                    empty.setText("No audit log entries yet.");
                    empty.setPadding(0, 24, 0, 24);
                    container.addView(empty);
                    return;
                }

                // One tile per entry, newest first (the API already sorts
                // them that way) — nothing fancy, just inflate and fill in.
                for (AuditLogEntry entry : entries) {
                    View tile = LayoutInflater.from(AuditLogActivity.this)
                            .inflate(R.layout.tile_audit_entry, container, false);

                    ((TextView) tile.findViewById(R.id.entryName)).setText(entry.getUsername());
                    ((TextView) tile.findViewById(R.id.entryTimestamp)).setText(entry.getTimestamp());
                    ((TextView) tile.findViewById(R.id.entryRef)).setText("#" + entry.getId());
                    ((TextView) tile.findViewById(R.id.entryUsername)).setText("Username: " + entry.getUsername());
                    ((TextView) tile.findViewById(R.id.entryProfileType)).setText("User Profile Type: " + entry.getProfileTypeName());
                    ((TextView) tile.findViewById(R.id.entryAction)).setText("Action: " + entry.getAction());

                    String affected = entry.getAffectedRecord();
                    View affectedRow = tile.findViewById(R.id.entryAffected);
                    if (affected != null && !affected.isEmpty()) {
                        ((TextView) affectedRow).setText("Affected: " + affected);
                        affectedRow.setVisibility(View.VISIBLE);
                    }

                    String details = entry.getDetails();
                    ((TextView) tile.findViewById(R.id.entryDetails)).setText(
                            "Details: " + (details != null && !details.isEmpty() ? details : "—"));

                    container.addView(tile);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AuditLogActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
