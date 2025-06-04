package com.EvgeniG_EladO_HalelF.myapplication;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    private static final int RINGTONE_PICKER_REQUEST_CODE = 1001;
    private static final String PREFS_NAME = "AppSettings";
    private static final String RINGTONE_URI_KEY = "notification_sound_uri";
    private static final String NOTIF_TIME_KEY = "notification_time";
    private static final String NAV_MODE_KEY = "navigation_mode";
    private static final String CHANNEL_ID = "notify_channel";

    private Uri selectedRingtoneUri;
    private TextView soundNameText;
    private EditText notificationTimeInput;
    private RadioGroup navigationModeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Views
        Button selectSoundButton = findViewById(R.id.select_sound_button);
        Button testNotificationButton = findViewById(R.id.test_notification_button);
        soundNameText = findViewById(R.id.sound_name_text);
        notificationTimeInput = findViewById(R.id.notification_time_input);
        navigationModeGroup = findViewById(R.id.navigation_mode_group);
        RadioButton walkingMode = findViewById(R.id.walking_mode);
        RadioButton transitMode = findViewById(R.id.transit_mode);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load and display saved ringtone
        String savedUri = prefs.getString(RINGTONE_URI_KEY, null);
        if (savedUri != null) {
            selectedRingtoneUri = Uri.parse(savedUri);
            Ringtone ringtone = RingtoneManager.getRingtone(this, selectedRingtoneUri);
            if (ringtone != null) {
                soundNameText.setText("Current: " + ringtone.getTitle(this));
            }
        }

        // Load saved notification time
        int savedTime = prefs.getInt(NOTIF_TIME_KEY, 10); // default 10 min
        notificationTimeInput.setText(String.valueOf(savedTime));

        // Load navigation mode
        String navMode = prefs.getString(NAV_MODE_KEY, "walking");
        if ("transit".equals(navMode)) {
            transitMode.setChecked(true);
        } else {
            walkingMode.setChecked(true);
        }

        // Sound selection
        selectSoundButton.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Notification Sound");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtoneUri);

            startActivityForResult(intent, RINGTONE_PICKER_REQUEST_CODE);
        });

        // Test notification
        testNotificationButton.setOnClickListener(v -> {
            saveSettings(); // Save before showing
            showTestNotification();
        });

        // Save settings on navigation mode change
        navigationModeGroup.setOnCheckedChangeListener((group, checkedId) -> saveSettings());

        // Save settings when editing is done
        notificationTimeInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveSettings();
            }
        });

        // Bottom Navigation
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_settings);
        nav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_map) {
                startActivity(new Intent(this, NavigationActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else {
                return itemId == R.id.nav_settings;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RINGTONE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedRingtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (selectedRingtoneUri != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(this, selectedRingtoneUri);
                String title = ringtone.getTitle(this);

                soundNameText.setText("Current: " + title);

                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString(RINGTONE_URI_KEY, selectedRingtoneUri.toString());
                editor.apply();

                Toast.makeText(this, "Selected: " + title, Toast.LENGTH_SHORT).show();
                ringtone.play();
            } else {
                Toast.makeText(this, "No sound selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

        // Save time
        try {
            int time = Integer.parseInt(notificationTimeInput.getText().toString().trim());
            editor.putInt(NOTIF_TIME_KEY, time);
        } catch (NumberFormatException ignored) {}

        // Save mode
        int checkedId = navigationModeGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.transit_mode) {
            editor.putString(NAV_MODE_KEY, "transit");
        } else {
            editor.putString(NAV_MODE_KEY, "walking");
        }

        // Save sound already done elsewhere

        editor.apply();
    }

    private void showTestNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Reminder", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("App reminder notifications");
            if (selectedRingtoneUri != null) {
                channel.setSound(selectedRingtoneUri, null);
            }
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Make sure this icon exists
                .setContentTitle("Test Notification")
                .setContentText("This is a test with your selected sound.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && selectedRingtoneUri != null) {
            builder.setSound(selectedRingtoneUri);
        }

        notificationManager.notify(1, builder.build());
    }
}
