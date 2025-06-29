package com.EvgeniG_EladO_HalelF.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu, menu);

        // Apply different colors
        setMenuItemColor(menu.findItem(R.id.menu_settings), Color.BLACK);
        setMenuItemColor(menu.findItem(R.id.menu_about), Color.BLUE);
        setMenuItemColor(menu.findItem(R.id.menu_exit), Color.RED);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.menu_about) {
            String release = android.os.Build.VERSION.RELEASE;
            int sdkVersion = android.os.Build.VERSION.SDK_INT;
            String appId = getApplicationContext().getPackageName();

            String fullVersion = "Android version: " + release + " (SDK " + sdkVersion + ")\n";

            String aboutStr = "Where Did I Park?\n" +
                    appId + "\n" +
                    fullVersion +
                    "Submit Date: 29.06.25\n" +
                    "Elad Ozery\nEvgeni Glushko\n Halel Fruman";

            new AlertDialog.Builder(this)
                    .setTitle("About")
                    .setMessage(aboutStr)
                    .setNegativeButton("Close", (dialog, which) -> {
                        dialog.dismiss(); // Close the dialog
                    })
                    .show();
            return true;
        } else if (id == R.id.menu_exit) {
            new AlertDialog.Builder(this)
                    .setTitle("Exit?")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        finishAffinity();
                        System.exit(0);// Close all activities
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss(); // Close the dialog
                    })
                    .show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void setMenuItemColor(MenuItem item, int color) {
        SpannableString spanString = new SpannableString(item.getTitle());
        spanString.setSpan(new ForegroundColorSpan(color), 0, spanString.length(), 0);
        item.setTitle(spanString);
    }
}
