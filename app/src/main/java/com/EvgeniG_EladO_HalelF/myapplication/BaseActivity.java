package com.EvgeniG_EladO_HalelF.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu, menu);

        // Apply different colors
        setMenuItemColor(menu.findItem(R.id.menu_settings), Color.RED);
        setMenuItemColor(menu.findItem(R.id.menu_about), Color.BLUE);
        setMenuItemColor(menu.findItem(R.id.menu_exit), Color.BLACK); // green

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.menu_about) {
            Toast.makeText(this, "WhereDidIPark v1.0", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_exit) {
            finishAffinity(); // Exits the app
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
