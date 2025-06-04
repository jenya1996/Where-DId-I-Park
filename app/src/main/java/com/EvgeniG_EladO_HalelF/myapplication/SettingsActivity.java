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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppSettings";
    private static final String RINGTONE_URI_KEY = "notification_sound_uri";
    private static final String NOTIFICATION_TIME_KEY = "notification_time";
    private static final String NAVIGATION_MODE_KEY = "navigation_mode";
    private static final String CHANNEL_ID = "notify_channel";

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
        Button testNotificationButton = findViewById(R.id.test_notification_button);

        selectSoundButton.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.select_sound));
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtoneUri);
            ringtonePickerLauncher.launch(intent);
        });

        testNotificationButton.setOnClickListener(v -> showTestNotification());

        ringtonePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        if (uri != null) {
                            selectedRingtoneUri = uri;
                            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putString(RINGTONE_URI_KEY, uri.toString()).apply();
                            updateSoundTitle();
                            RingtoneManager.getRingtone(this, uri).play();
                        }
                    }
                });

        navigationModeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String mode = checkedId == R.id.walking_mode ? "walking" : "transit";
            prefs.edit().putString(NAVIGATION_MODE_KEY, mode).apply();
        });

        notificationTimeInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String timeText = notificationTimeInput.getText().toString();
                if (!timeText.isEmpty()) {
                    prefs.edit().putInt(NOTIFICATION_TIME_KEY, Integer.parseInt(timeText)).apply();
                }
            }
        });

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_settings);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_map) {
                startActivity(new Intent(this, NavigationActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
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

    private void showTestNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(getString(R.string.channel_description));
            channel.setSound(selectedRingtoneUri, null);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.test_notification_title))
                .setContentText(getString(R.string.test_notification_message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && selectedRingtoneUri != null) {
            builder.setSound(selectedRingtoneUri);
        }

        notificationManager.notify(1, builder.build());
    }
}
