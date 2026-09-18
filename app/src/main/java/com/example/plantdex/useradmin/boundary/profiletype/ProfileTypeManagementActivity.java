package com.example.plantdex.useradmin.boundary.profiletype;

import android.content.Intent;
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
import com.example.plantdex.useradmin.control.profiletype.ProfileTypeController;
import com.example.plantdex.useradmin.entity.ProfileType;

import java.util.List;

/**
 * Lists the app's 4 fixed profile types. Reuses the generic
 * activity_list_with_search layout that Collectible/Record management also
 * use, but with search and filter hidden — there's only ever 4 rows here,
 * searching them would be pointless.
 */
public class ProfileTypeManagementActivity extends AppCompatActivity {

    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_with_search);

        BottomNavHelper.setup(this, findViewById(R.id.navContainer), Roles.USER_ADMIN);
        ((TextView) findViewById(R.id.header).findViewById(R.id.tvRoleId)).setText(Roles.displayId(Roles.USER_ADMIN));
        ((TextView) findViewById(R.id.tvScreenTitle)).setText("User Profile Type Management");
        findViewById(R.id.etSearch).setVisibility(View.GONE);
        findViewById(R.id.btnFilter).setVisibility(View.GONE);

        container = findViewById(R.id.listContainer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload every time we come back (e.g. after editing one), so renames show up immediately.
        container.removeAllViews();
        loadTypes();
    }

    private void loadTypes() {
        ProfileTypeController.listTypes(new ProfileTypeController.ListCallback() {
            @Override
            public void onSuccess(List<ProfileType> profileTypes) {
                for (ProfileType type : profileTypes) {
                    View tile = LayoutInflater.from(ProfileTypeManagementActivity.this)
                            .inflate(R.layout.tile_profile_type, container, false);
                    ((TextView) tile.findViewById(R.id.typeName)).setText(type.getName());
                    ((TextView) tile.findViewById(R.id.typeIdLabel)).setText("Profile Type ID: " + type.getId());

                    View.OnClickListener open = v -> {
                        Intent intent = new Intent(ProfileTypeManagementActivity.this, EditProfileTypeActivity.class);
                        intent.putExtra(EditProfileTypeActivity.EXTRA_ID, type.getId());
                        intent.putExtra(EditProfileTypeActivity.EXTRA_ROLE_CODE, type.getRoleCode());
                        intent.putExtra(EditProfileTypeActivity.EXTRA_NAME, type.getName());
                        intent.putExtra(EditProfileTypeActivity.EXTRA_DESCRIPTION, type.getDescription());
                        intent.putExtra(EditProfileTypeActivity.EXTRA_PERMISSIONS, type.getPermissions());
                        startActivity(intent);
                    };
                    tile.findViewById(R.id.btnEditType).setOnClickListener(open);
                    tile.setOnClickListener(open);
                    container.addView(tile);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProfileTypeManagementActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
