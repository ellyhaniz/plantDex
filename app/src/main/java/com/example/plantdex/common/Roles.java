package com.example.plantdex.common;

import com.example.plantdex.researcher.ResearcherDashboardActivity;
import com.example.plantdex.systemadmin.SystemAdminDashboardActivity;
import com.example.plantdex.useradmin.boundary.UserAdminDashboardActivity;
import com.example.plantdex.visitor.VisitorDashboardActivity;

/**
 * Shared role constants and the dashboard each one lands on. These 4 strings
 * are the app's internal "RoleCode" — they mirror the RoleCode column in the
 * ProfileTypes table exactly and must never change, since a User Admin can
 * rename a profile type's *display* name without this routing breaking.
 */
public final class Roles {

    public static final String VISITOR = "VISITOR";
    public static final String RESEARCHER = "RESEARCHER";
    public static final String USER_ADMIN = "USER ADMIN";
    public static final String SYSTEM_ADMIN = "SYSTEM ADMIN";

    private Roles() {}

    /** Which dashboard Activity a given role opens after login (or when "Home" is tapped in the bottom nav). */
    public static Class<?> dashboardFor(String role) {
        if (role == null) return VisitorDashboardActivity.class;
        switch (role) {
            case USER_ADMIN:
                return UserAdminDashboardActivity.class;
            case SYSTEM_ADMIN:
                return SystemAdminDashboardActivity.class;
            case RESEARCHER:
                return ResearcherDashboardActivity.class;
            default:
                return VisitorDashboardActivity.class;
        }
    }

    /** Whether this role's bottom nav includes the camera / identify shortcut. */
    public static boolean hasCameraTab(String role) {
        return VISITOR.equals(role);
    }

    public static String displayId(String role) {
        if (USER_ADMIN.equals(role)) return "User Admin #ID";
        if (SYSTEM_ADMIN.equals(role)) return "System Admin #ID";
        if (RESEARCHER.equals(role)) return "Researcher #ID";
        return "Visitor #ID";
    }
}
