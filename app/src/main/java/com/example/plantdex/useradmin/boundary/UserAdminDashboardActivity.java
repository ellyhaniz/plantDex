package com.example.plantdex.useradmin.boundary;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Placeholder — replaced with the real User Admin dashboard in a later part. */
public class UserAdminDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("User Admin Dashboard — coming in a later part");
        tv.setPadding(48, 96, 48, 48);
        setContentView(tv);
    }
}
