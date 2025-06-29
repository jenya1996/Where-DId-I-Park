package com.EvgeniG_EladO_HalelF.myapplication;

import android.app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.EvgeniG_EladO_HalelF.myapplication.DatabaseProvider;
import com.EvgeniG_EladO_HalelF.myapplication.LocationDatabaseHelper;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppSettings";
    private static final String RINGTONE_URI_KEY = "notification_sound_uri";
    private static final String NOTIFICATION_TIME_KEY = "notification_time";
    private static final String NAVIGATION_MODE_KEY = "navigation_mode";


    private Uri selectedRingtoneUri;
    private TextView soundNameText;
    private EditText notificationTimeInput;
    private RadioGroup navigationModeGroup;

    private ActivityResultLauncher<Intent> ringtonePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        selectedRingtoneUri = Uri.parse(prefs.getString(RINGTONE_URI_KEY, ""));

        notificationTimeInput = findViewById(R.id.notification_time_input);
        soundNameText = findViewById(R.id.sound_name_text);
        navigationModeGroup = findViewById(R.id.navigation_mode_group);

        int savedTime = prefs.getInt(NOTIFICATION_TIME_KEY, 10);
        notificationTimeInput.setText(String.valueOf(savedTime));

        String navMode = prefs.getString(NAVIGATION_MODE_KEY, "walking");
        if ("walking".equals(navMode)) {
            navigationModeGroup.check(R.id.walking_mode);
        } else {
            navigationModeGroup.check(R.id.transit_mode);
        }

        updateSoundTitle();

        Button selectSoundButton = findViewById(R.id.select_sound_button);
        Button saveSettingsButton = findViewById(R.id.save_settings_button);

        selectSoundButton.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.select_sound));
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtoneUri);
            ringtonePickerLauncher.launch(intent);
        });

        saveSettingsButton.setOnClickListener(v -> {
            // Save notification time
            String timeText = notificationTimeInput.getText().toString();
            if (!timeText.isEmpty()) {
                prefs.edit().putInt(NOTIFICATION_TIME_KEY, Integer.parseInt(timeText)).apply();
            }

            // Save navigation mode
            int checkedId = navigationModeGroup.getCheckedRadioButtonId();
            String mode = (checkedId == R.id.walking_mode) ? "walking" : "transit";
            prefs.edit().putString(NAVIGATION_MODE_KEY, mode).apply();

            // Save ringtone URI
            if (selectedRingtoneUri != null) {
                prefs.edit().putString(RINGTONE_URI_KEY, selectedRingtoneUri.toString()).apply();
            }

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();


        });

        Button deleteLocationsButton = findViewById(R.id.delete_locations_button);

        deleteLocationsButton.setOnClickListener(view -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete all saved locations?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        LocationDatabaseHelper dbHelper = DatabaseProvider.get();
                        int rowsDeleted = dbHelper.deleteAllLocations();

                        if (rowsDeleted > 0) {
                            Toast.makeText(this, "All saved locations deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "No locations to delete", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss(); // closes the dialog
                    })
                    .show();
        });

        ringtonePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        if (uri != null) {
                            selectedRingtoneUri = uri;
                            prefs.edit().putString(RINGTONE_URI_KEY, uri.toString()).apply();
                            updateSoundTitle();
                            RingtoneManager.getRingtone(this, uri).play();
                        }
                    }
                });

        // Setup Bottom Navigation
        NavigationUtils.setupBottomNavBar(this);
    }

    private void updateSoundTitle() {
        if (selectedRingtoneUri != null) {
            Ringtone ringtone = RingtoneManager.getRingtone(this, selectedRingtoneUri);
            String title = ringtone.getTitle(this);
            soundNameText.setText(getString(R.string.current_sound_label, title));
        } else {
            soundNameText.setText(getString(R.string.current_sound_label, getString(R.string.default_sound)));
        }
    }

}
