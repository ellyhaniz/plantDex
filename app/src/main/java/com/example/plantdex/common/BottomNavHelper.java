package com.example.plantdex.common;

import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.plantdex.R;
import com.example.plantdex.common.boundary.MyAccountActivity;
import com.example.plantdex.common.control.AuthController;
import com.example.plantdex.visitor.CapturePhotoActivity;

/**
 * Inflates and wires the bottom navigation bar shared by every actor's
 * screens. Visitor gets the 5-icon bar (with the camera / identify
 * shortcut); every other role gets the 4-icon bar.
 */
public final class BottomNavHelper {

    private BottomNavHelper() {}

    public static void setup(AppCompatActivity activity, FrameLayout container, String role) {
        if (container == null) return;

        int layoutRes = Roles.hasCameraTab(role) ? R.layout.bottom_nav_5 : R.layout.bottom_nav_4;
        android.view.View navView = LayoutInflater.from(activity).inflate(layoutRes, container, true);

        ImageButton navHome = navView.findViewById(R.id.navHome);
        ImageButton navProfile = navView.findViewById(R.id.navProfile);
        ImageButton navNotifications = navView.findViewById(R.id.navNotifications);
        ImageButton navLogout = navView.findViewById(R.id.navLogout);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(activity, Roles.dashboardFor(role));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(activity, MyAccountActivity.class);
            intent.putExtra(MyAccountActivity.EXTRA_ROLE, role);
            activity.startActivity(intent);
        });

        navNotifications.setOnClickListener(v ->
                Toast.makeText(activity, "You're all caught up — no new notifications.", Toast.LENGTH_SHORT).show());

        navLogout.setOnClickListener(v -> showLogoutDialog(activity, role));

        if (Roles.hasCameraTab(role)) {
            ImageButton navCamera = navView.findViewById(R.id.navCamera);
            navCamera.setOnClickListener(v -> activity.startActivity(new Intent(activity, CapturePhotoActivity.class)));
        }
    }

    /** Shows the "are you sure?" confirmation, then actually logs the account out once confirmed. */
    public static void showLogoutDialog(AppCompatActivity activity, String role) {
        String message;
        String note = null;

        // Admin roles get an extra note since their sign-out is audit-worthy;
        // Visitor/Researcher just get the plain heads-up.
        switch (role) {
            case Roles.USER_ADMIN:
                message = "Your admin session will end and you will return to the login page.";
                note = "This log out will be recorded in the Audit Log against User Admin #ID.";
                break;
            case Roles.SYSTEM_ADMIN:
                message = "Your admin session will end and you will return to the login page.";
                note = "Administration functions will not be accessible until System Admin #ID logs in again.";
                break;
            case Roles.RESEARCHER:
                message = "Your session will end. Your collectibles and discovery history stay saved.";
                break;
            default:
                message = "Your session will end. Your collectibles and discovery history stay saved.";
        }

        DialogHelper.showLogout(activity, message, note, () -> {
            int userId = Session.getUserId(activity);
            AuthController.logout(activity, userId, new AuthController.SimpleCallback() {
                @Override
                public void onSuccess() {
                    DialogHelper.logoutToLogin(activity);
                }

                @Override
                public void onError(String message) {
                    // Even if the server call failed (e.g. no network), still
                    // send them back to Login — getting stuck on a dead
                    // screen would be worse than a logout that didn't reach
                    // the audit log.
                    DialogHelper.logoutToLogin(activity);
                }
            });
        });
    }
}
