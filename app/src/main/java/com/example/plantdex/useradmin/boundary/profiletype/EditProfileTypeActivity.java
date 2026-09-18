package com.example.plantdex.useradmin.boundary.profiletype;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.plantdex.R;
import com.example.plantdex.common.BottomNavHelper;
import com.example.plantdex.common.Roles;
import com.example.plantdex.common.Session;
import com.example.plantdex.common.control.AuthController;
import com.example.plantdex.useradmin.control.profiletype.ProfileTypeController;

/**
 * Rename a profile type / edit its description and permissions text. All the
 * current values arrive as Intent extras from the list screen — no separate
 * network fetch needed just to populate this form.
 */
public class EditProfileTypeActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_ROLE_CODE = "extra_role_code";
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    public static final String EXTRA_PERMISSIONS = "extra_permissions";

    private int profileTypeId;
    private EditText etTypeName, etTypeDescription, etTypePermissions;
    private android.view.View layoutError;
    private TextView tvError;
    private Button btnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_type);

        BottomNavHelper.setup(this, findViewById(R.id.navContainer), Roles.USER_ADMIN);
        ((TextView) findViewById(R.id.header).findViewById(R.id.tvRoleId)).setText(Roles.displayId(Roles.USER_ADMIN));

        profileTypeId = getIntent().getIntExtra(EXTRA_ID, 0);
        String roleCode = getIntent().getStringExtra(EXTRA_ROLE_CODE);
        String name = getIntent().getStringExtra(EXTRA_NAME);
        String description = getIntent().getStringExtra(EXTRA_DESCRIPTION);
        String permissions = getIntent().getStringExtra(EXTRA_PERMISSIONS);

        etTypeName = findViewById(R.id.etTypeName);
        etTypeDescription = findViewById(R.id.etTypeDescription);
        etTypePermissions = findViewById(R.id.etTypePermissions);
        layoutError = findViewById(R.id.layoutEditTypeError);
        tvError = findViewById(R.id.tvEditTypeError);
        btnUpdate = findViewById(R.id.btnUpdateType);

        ((TextView) findViewById(R.id.tvTypeTitle)).setText(name);
        ((TextView) findViewById(R.id.tvTypeIdLine)).setText("Profile Type ID: " + profileTypeId + " · Internal code: " + roleCode);
        etTypeName.setText(name);
        etTypeDescription.setText(description);
        etTypePermissions.setText(permissions);

        btnUpdate.setOnClickListener(v -> attemptUpdate());
        findViewById(R.id.btnCancelType).setOnClickListener(v -> finish());
    }

    private void attemptUpdate() {
        String name = etTypeName.getText().toString().trim();
        String description = etTypeDescription.getText().toString().trim();
        String permissions = etTypePermissions.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            showError("Profile type name is required");
            return;
        }

        layoutError.setVisibility(android.view.View.GONE);
        btnUpdate.setEnabled(false);
        int updatedByUserId = Session.getUserId(this);
        ProfileTypeController.updateType(profileTypeId, name, description, permissions, updatedByUserId, new AuthController.SimpleCallback() {
            @Override
            public void onSuccess() {
                btnUpdate.setEnabled(true);
                Toast.makeText(EditProfileTypeActivity.this, "Profile type updated.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                btnUpdate.setEnabled(true);
                showError(message);
            }
        });
    }

    private void showError(String message) {
        tvError.setText("⚠ " + message);
        layoutError.setVisibility(android.view.View.VISIBLE);
    }
}
