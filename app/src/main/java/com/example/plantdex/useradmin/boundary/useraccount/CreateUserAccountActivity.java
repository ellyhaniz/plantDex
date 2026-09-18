package com.example.plantdex.useradmin.boundary.useraccount;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.List;

/** Lets a User Admin create System Admin / User Admin / Researcher (or Visitor) accounts directly. */
public class CreateUserAccountActivity extends AppCompatActivity {

    private Spinner spinnerRole;
    private EditText etFullName, etEmail, etUsername, etPassword;
    private android.view.View layoutError;
    private TextView tvError;
    private Button btnSave;
    private final List<ProfileType> profileTypes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_account);

        BottomNavHelper.setup(this, findViewById(R.id.navContainer), Roles.USER_ADMIN);
        ((TextView) findViewById(R.id.header).findViewById(R.id.tvRoleId)).setText(Roles.displayId(Roles.USER_ADMIN));

        spinnerRole = findViewById(R.id.spinnerRole);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        layoutError = findViewById(R.id.layoutCreateUserError);
        tvError = findViewById(R.id.tvCreateUserError);
        btnSave = findViewById(R.id.btnSaveUserAccount);

        btnSave.setEnabled(false);
        loadProfileTypes();

        btnSave.setOnClickListener(v -> attemptCreate());
        findViewById(R.id.btnCancelUserAccount).setOnClickListener(v -> finish());
    }

    /**
     * The role spinner is populated from the real ProfileTypes table (not a
     * hardcoded list), so a renamed profile type shows its current name here
     * too. Save stays disabled until this finishes, since submitting before
     * the spinner has real options would create the account with garbage.
     */
    private void loadProfileTypes() {
        ProfileTypeController.listTypes(new ProfileTypeController.ListCallback() {
            @Override
            public void onSuccess(List<ProfileType> types) {
                profileTypes.clear();
                profileTypes.addAll(types);

                ArrayAdapter<ProfileType> adapter = new ArrayAdapter<>(
                        CreateUserAccountActivity.this, R.layout.spinner_item, profileTypes);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinnerRole.setAdapter(adapter);
                btnSave.setEnabled(true);
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    private void attemptCreate() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(username)
                || TextUtils.isEmpty(password)) {
            showError("Please fill in every field");
            return;
        }

        ProfileType selected = (ProfileType) spinnerRole.getSelectedItem();
        if (selected == null) {
            showError("Select an account type");
            return;
        }
        int createdByUserId = Session.getUserId(this);

        layoutError.setVisibility(android.view.View.GONE);
        btnSave.setEnabled(false);
        UserAccountController.createAccount(email, fullName, username, password, selected.getId(), createdByUserId,
                new AuthController.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        btnSave.setEnabled(true);
                        Toast.makeText(CreateUserAccountActivity.this, username + " was created as " + selected.getName() + ".", Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        btnSave.setEnabled(true);
                        showError(message);
                    }
                });
    }

    private void showError(String message) {
        tvError.setText("⚠ " + message);
        layoutError.setVisibility(android.view.View.VISIBLE);
    }
}
