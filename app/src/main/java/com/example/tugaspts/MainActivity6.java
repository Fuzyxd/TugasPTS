package com.example.tugaspts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity6 extends AppCompatActivity {

    private ImageButton switchNotifikasi;
    private ImageView iconAkun, iconBahasa, iconNotifikasi, iconNada;
    private TextView tvAkun, tvBahasa, tvNotifikasi, tvNada, tvVersion;

    private boolean isNotifikasiEnabled = true;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SettingsPrefs";
    private static final String KEY_NOTIFICATION = "notification_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);

        initViews();
        loadSettings();
        setupClickListeners();
        updateSwitchAppearance();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void initViews() {
        // Switch
        switchNotifikasi = findViewById(R.id.switch_notifikasi);

        // Icons
        iconAkun = findViewById(R.id.icon_akun);
        iconBahasa = findViewById(R.id.icon_bahasa);
        iconNotifikasi = findViewById(R.id.icon_notifikasi);
        iconNada = findViewById(R.id.icon_nada);

        // TextViews
        tvAkun = findViewById(R.id.tv_akun);
        tvBahasa = findViewById(R.id.tv_bahasa);
        tvNotifikasi = findViewById(R.id.tv_notifikasi);
        tvNada = findViewById(R.id.tv_nada);
        tvVersion = findViewById(R.id.tv_version);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void loadSettings() {
        // Load notification setting from SharedPreferences, default is true (enabled)
        isNotifikasiEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATION, true);
    }

    private void setupClickListeners() {
        // Notification switch
        switchNotifikasi.setOnClickListener(v -> toggleNotifikasi());

        // Account setting
        iconAkun.setOnClickListener(v -> showAccountSettings());
        tvAkun.setOnClickListener(v -> showAccountSettings());

        // Language setting
        iconBahasa.setOnClickListener(v -> showLanguageSettings());
        tvBahasa.setOnClickListener(v -> showLanguageSettings());

        // Notification setting (also opens when clicking the text/icon)
        iconNotifikasi.setOnClickListener(v -> toggleNotifikasi());
        tvNotifikasi.setOnClickListener(v -> toggleNotifikasi());

        // Ringtone setting
        iconNada.setOnClickListener(v -> showRingtoneSettings());
        tvNada.setOnClickListener(v -> showRingtoneSettings());

        // Bottom navigation
        findViewById(R.id.nav_alarm).setOnClickListener(v -> {
            navigateToActivity(MainActivity2.class);
        });

        findViewById(R.id.nav_world).setOnClickListener(v -> {
            navigateToActivity(MainActivity3.class);
        });

        findViewById(R.id.nav_timer).setOnClickListener(v -> {
            navigateToActivity(MainActivity4.class);
        });

        findViewById(R.id.nav_stopwatch).setOnClickListener(v -> {
            navigateToActivity(MainActivity5.class);
        });

        findViewById(R.id.nav_setting).setOnClickListener(v -> {
            // Already on settings page
            Toast.makeText(this, "Anda sudah di halaman Setting", Toast.LENGTH_SHORT).show();
        });

        iconAkun.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity6.this, Login.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
        tvAkun.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity6.this, Login.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    private void navigateToActivity(Class<?> cls) {
        Intent intent = new Intent(MainActivity6.this, cls);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void toggleNotifikasi() {
        isNotifikasiEnabled = !isNotifikasiEnabled;

        // Save setting to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_NOTIFICATION, isNotifikasiEnabled);
        editor.apply();

        // Update switch appearance
        updateSwitchAppearance();

        // Show toast message
        if (isNotifikasiEnabled) {
            Toast.makeText(this, "Notifikasi diaktifkan", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Notifikasi dimatikan", Toast.LENGTH_SHORT).show();
        }

        // Here you can add additional logic for enabling/disabling actual notifications
        handleNotificationSettingChange();
    }

    private void updateSwitchAppearance() {
        if (isNotifikasiEnabled) {
            switchNotifikasi.setImageResource(R.drawable.switch_on);
        } else {
            switchNotifikasi.setImageResource(R.drawable.switch_off);
        }
    }

    private void handleNotificationSettingChange() {
        // Add your notification logic here
        // For example:
        // - Schedule/cancel alarm notifications
        // - Update notification channels
        // - etc.

        if (isNotifikasiEnabled) {
            // Enable notifications in your app
            // NotificationManagerCompat.from(this).setNotificationEnabled(true);
        } else {
            // Disable notifications in your app
            // NotificationManagerCompat.from(this).setNotificationEnabled(false);
        }
    }

    private void showAccountSettings() {
        Toast.makeText(this, "Pengaturan Akun", Toast.LENGTH_SHORT).show();
        // You can start a new activity for account settings here
        // Intent intent = new Intent(this, AccountSettingsActivity.class);
        // startActivity(intent);
    }

    private void showLanguageSettings() {
        Toast.makeText(this, "Pengaturan Bahasa", Toast.LENGTH_SHORT).show();
        // You can start a new activity for language settings here
        // Intent intent = new Intent(this, LanguageSettingsActivity.class);
        // startActivity(intent);
    }

    private void showRingtoneSettings() {
        Toast.makeText(this, "Pengaturan Nada Dering", Toast.LENGTH_SHORT).show();
        // You can start a new activity for ringtone settings here
        // Intent intent = new Intent(this, RingtoneSettingsActivity.class);
        // startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload settings when returning to this activity
        loadSettings();
        updateSwitchAppearance();
    }
}