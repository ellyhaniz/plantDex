package com.example.plantdex.useradmin.boundary.useraccount;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.plantdex.R;
import com.example.plantdex.common.BottomNavHelper;
import com.example.plantdex.common.Roles;
import com.example.plantdex.useradmin.control.useraccount.UserAccountController;
import com.example.plantdex.useradmin.entity.UserAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Every account in the system, with live search by name/username. Filtering
 * happens client-side against allAccounts rather than re-querying the API on
 * every keystroke — fine at this scale, would need rethinking with a much
 * bigger user base.
 */
public class UserAccountManagementActivity extends AppCompatActivity {

    private LinearLayout container;
    private EditText etSearch;
    private final List<UserAccount> allAccounts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_with_search);

        BottomNavHelper.setup(this, findViewById(R.id.navContainer), Roles.USER_ADMIN);
        ((TextView) findViewById(R.id.header).findViewById(R.id.tvRoleId)).setText(Roles.displayId(Roles.USER_ADMIN));
        ((TextView) findViewById(R.id.tvScreenTitle)).setText("User Account Management");

        android.widget.Button btnCreateNew = findViewById(R.id.btnCreateNew);
        btnCreateNew.setVisibility(View.VISIBLE);
        btnCreateNew.setText("+ Create user account");
        btnCreateNew.setOnClickListener(v -> startActivity(new Intent(this, CreateUserAccountActivity.class)));

        findViewById(R.id.btnFilter).setVisibility(View.GONE);

        container = findViewById(R.id.listContainer);
        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { render(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload every time we come back (e.g. after creating/editing an account).
        loadAccounts();
    }

    private void loadAccounts() {
        UserAccountController.listAccounts(new UserAccountController.ListCallback() {
            @Override
            public void onSuccess(List<UserAccount> accounts) {
                allAccounts.clear();
                allAccounts.addAll(accounts);
                render(etSearch.getText().toString());
            }

            @Override
            public void onError(String message) {
                Toast.makeText(UserAccountManagementActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Re-draws the list from allAccounts, filtered by whatever's currently typed into search. Called both after a fresh load and on every keystroke. */
    private void render(String query) {
        container.removeAllViews();
        String q = query.trim().toLowerCase(Locale.ROOT);

        for (UserAccount acc : allAccounts) {
            if (!q.isEmpty()
                    && !acc.getFullName().toLowerCase(Locale.ROOT).contains(q)
                    && !acc.getUsername().toLowerCase(Locale.ROOT).contains(q)) {
                continue;
            }

            View tile = LayoutInflater.from(this).inflate(R.layout.tile_user_account, container, false);
            ((TextView) tile.findViewById(R.id.accName)).setText(acc.getFullName());
            ((TextView) tile.findViewById(R.id.accUserId)).setText("User ID: " + acc.getUserId());
            ((TextView) tile.findViewById(R.id.accUsername)).setText("Username: " + acc.getUsername());
            ((TextView) tile.findViewById(R.id.accStatus)).setText("Status: " + acc.getAccountStatus());
            ((TextView) tile.findViewById(R.id.accProfileType)).setText("User Profile Type: " + acc.getProfileTypeName());

            View.OnClickListener open = v -> {
                Intent intent = new Intent(this, EditAccountDetailsActivity.class);
                intent.putExtra(EditAccountDetailsActivity.EXTRA_USER_ID, acc.getUserId());
                startActivity(intent);
            };
            tile.findViewById(R.id.btnEditAcc).setOnClickListener(open);
            tile.setOnClickListener(open);
            container.addView(tile);
        }

        if (container.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText(allAccounts.isEmpty() ? "No accounts yet." : "No accounts match your search.");
            empty.setTextColor(getColor(R.color.white));
            empty.setPadding(0, 24, 0, 24);
            container.addView(empty);
        }
    }
}
