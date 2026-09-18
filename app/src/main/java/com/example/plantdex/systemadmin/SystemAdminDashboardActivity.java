package com.example.plantdex.systemadmin;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Placeholder — replaced with the real System Admin dashboard in a later part. */
public class SystemAdminDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("System Admin Dashboard — coming in a later part");
        tv.setPadding(48, 96, 48, 48);
        setContentView(tv);
    }
}
