package com.example.plantdex.useradmin.boundary.useraccount;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.plantdex.R;
import com.example.plantdex.common.BottomNavHelper;
import com.example.plantdex.common.Roles;
import com.example.plantdex.common.Session;
import com.example.plantdex.common.control.AuthController;
import com.example.plantdex.useradmin.control.profiletype.ProfileTypeController;
import com.example.plantdex.useradmin.control.useraccount.UserAccountController;
import com.example.plantdex.useradmin.entity.ProfileType;
import com.example.plantdex.useradmin.entity.UserAccount;

import java.util.ArrayList;
import java.util.List;

/**
 * The full edit screen for one account — full name, email, and profile type
 * are editable; username and creation date are shown read-only. Account
 * status toggles right on this screen; password changes go through a
 * separate confirmation dialog rather than a plain-text field, so a full
 * password isn't just sitting visible on the form.
 */
public class EditAccountDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "extra_user_id";

    private int userId;
    private String currentStatus = "ACTIVE";

    private EditText etFullName, etEmail;
    private Spinner spinnerProfileType;
    private TextView tvAccountStatus, tvError;
    private View layoutError;
    private final List<ProfileType> profileTypes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account_details);

        BottomNavHelper.setup(this, findViewById(R.id.navContainer), Roles.USER_ADMIN);
        ((TextView) findViewById(R.id.header).findViewById(R.id.tvRoleId)).setText(Roles.displayId(Roles.USER_ADMIN));

        userId = getIntent().getIntExtra(EXTRA_USER_ID, 0);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        spinnerProfileType = findViewById(R.id.spinnerProfileType);
        tvAccountStatus = findViewById(R.id.tvAccountStatus);
        layoutError = findViewById(R.id.layoutEditAccError);
        tvError = findViewById(R.id.tvEditAccError);

        tvAccountStatus.setOnClickListener(v -> toggleStatus());
        findViewById(R.id.btnResetPassword).setOnClickListener(v -> showResetPasswordDialog());
        findViewById(R.id.btnUpdateAccount).setOnClickListener(v -> attemptUpdate());
        findViewById(R.id.btnCancelAccount).setOnClickListener(v -> finish());

        setFieldsEnabled(false); // stay locked until both loads below finish
        loadProfileTypesThenAccount();
    }

    // Deliberately sequential rather than two parallel calls: the profile
    // type spinner needs its options populated *before* bindAccount() can
    // select the account's current one, otherwise there'd be nothing to select yet.
    private void loadProfileTypesThenAccount() {
        ProfileTypeController.listTypes(new ProfileTypeController.ListCallback() {
            @Override
            public void onSuccess(List<ProfileType> types) {
                profileTypes.clear();
                profileTypes.addAll(types);
                ArrayAdapter<ProfileType> adapter = new ArrayAdapter<>(
                        EditAccountDetailsActivity.this, R.layout.spinner_item, profileTypes);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinnerProfileType.setAdapter(adapter);
                loadAccount();
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    private void loadAccount() {
        UserAccountController.getAccount(userId, new UserAccountController.AccountCallback() {
            @Override
            public void onSuccess(UserAccount account) {
                bindAccount(account);
                setFieldsEnabled(true);
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    private void bindAccount(UserAccount account) {
        ((TextView) findViewById(R.id.tvEditAccName)).setText(account.getFullName());
        setRow(R.id.rowUserId, "User ID:", "#" + account.getUserId());
        setRow(R.id.rowUsername, "Username:", account.getUsername());
        setRow(R.id.rowCreationDate, "Creation Date:", account.getCreatedDatetime());

        etFullName.setText(account.getFullName());
        etEmail.setText(account.getEmail());

        // Spinner only knows positions, not IDs — find the row matching this
        // account's current profile type and select it there.
        for (int i = 0; i < profileTypes.size(); i++) {
            if (profileTypes.get(i).getId() == account.getProfileTypeId()) {
                spinnerProfileType.setSelection(i);
                break;
            }
        }

        currentStatus = account.getAccountStatus();
        renderStatus();
    }

    // Status isn't saved here — it only flips the local currentStatus and
    // re-colors the chip. The actual write happens together with everything
    // else when Update is tapped, same as the other fields.
    private void toggleStatus() {
        currentStatus = "ACTIVE".equals(currentStatus) ? "INACTIVE" : "ACTIVE";
        renderStatus();
    }

    private void renderStatus() {
        tvAccountStatus.setText(currentStatus);
        boolean active = "ACTIVE".equals(currentStatus);
        tvAccountStatus.setBackgroundResource(active ? R.drawable.bg_pill : R.drawable.bg_chip_unselected);
        tvAccountStatus.setTextColor(getColor(active ? R.color.plantdex_success_text : R.color.plantdex_text_muted));
    }

    private void attemptUpdate() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        ProfileType selected = (ProfileType) spinnerProfileType.getSelectedItem();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || selected == null) {
            showError("Please fill in every field");
            return;
        }

        layoutError.setVisibility(View.GONE);
        setFieldsEnabled(false);
        int updatedByUserId = Session.getUserId(this);
        UserAccountController.updateAccount(userId, fullName, email, selected.getId(), currentStatus, updatedByUserId,
                new AuthController.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        setFieldsEnabled(true);
                        Toast.makeText(EditAccountDetailsActivity.this, "Account updated.", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        setFieldsEnabled(true);
                        showError(message);
                    }
                });
    }

    private void showResetPasswordDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("New password (min. 6 characters)");

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setView(input)
                .setPositiveButton("Reset", (dialog, which) -> {
                    String newPassword = input.getText().toString();
                    if (newPassword.length() < 6) {
                        Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int resetByUserId = Session.getUserId(this);
                    UserAccountController.resetPassword(userId, newPassword, resetByUserId, new AuthController.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(EditAccountDetailsActivity.this, "Password reset.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(EditAccountDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setRow(int includeId, String label, String value) {
        View row = findViewById(includeId);
        ((TextView) row.findViewById(R.id.rowLabel)).setText(label);
        ((TextView) row.findViewById(R.id.rowValue)).setText(value);
    }

    private void setFieldsEnabled(boolean enabled) {
        etFullName.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        spinnerProfileType.setEnabled(enabled);
        findViewById(R.id.btnUpdateAccount).setEnabled(enabled);
        findViewById(R.id.btnResetPassword).setEnabled(enabled);
    }

    private void showError(String message) {
        tvError.setText("⚠ " + message);
        layoutError.setVisibility(View.VISIBLE);
    }
}
