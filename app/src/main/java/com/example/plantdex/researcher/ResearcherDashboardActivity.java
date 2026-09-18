package com.example.plantdex.researcher;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Placeholder — replaced with the real Researcher dashboard in a later part. */
public class ResearcherDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Researcher Dashboard — coming in a later part");
        tv.setPadding(48, 96, 48, 48);
        setContentView(tv);
    }
}
