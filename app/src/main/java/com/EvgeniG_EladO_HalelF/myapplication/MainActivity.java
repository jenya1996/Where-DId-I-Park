package com.EvgeniG_EladO_HalelF.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hardcoded TextView (no XML layout)
        TextView textView = new TextView(this);
        textView.setText("MainActivity loaded successfully");
        textView.setTextSize(24);
        textView.setPadding(40, 300, 40, 40);
        setContentView(textView);
    }
}
