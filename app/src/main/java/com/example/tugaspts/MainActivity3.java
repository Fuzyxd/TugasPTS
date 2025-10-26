package com.example.tugaspts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity3 extends AppCompatActivity {

    private TextView tvMainTime, tvMainLocation, tvMainDate;
    private TextView tvAmerikaTime, tvAmerikaLocation, tvAmerikaDate;
    private TextView tvInggrisTime, tvInggrisLocation, tvInggrisDate;
    private TextView tvJepangTime, tvJepangLocation, tvJepangDate;
    private TextView tvArabTime, tvArabLocation, tvArabDate;

    private Handler handler;
    private ImageView ivProfile;
    private Runnable timeUpdater;
    private boolean isUpdating = false;

    private static final String TAG = "MainActivity3";

    // Timezone data untuk masing-masing negara
    private final String[][] timeZones = {
            {"Asia/Jakarta", "Indonesia", "WIB"},           // Indonesia
            {"America/New_York", "Amerika Serikat", "EDT"}, // Amerika
            {"Europe/London", "Inggris", "BST"},            // Inggris
            {"Asia/Tokyo", "Jepang", "JST"},                // Jepang
            {"Asia/Riyadh", "Arab Saudi", "AST"}            // Arab Saudi
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        Log.d(TAG, "onCreate started");

        initViews();
        setupClickListeners();
        startTimeUpdates();

        Log.d(TAG, "onCreate completed");
    }

    private void initViews() {
        Log.d(TAG, "Initializing views...");

        // Jam utama (Indonesia)
        tvMainTime = findViewById(R.id.tv_main_time);
        tvMainLocation = findViewById(R.id.tv_main_location);
        tvMainDate = findViewById(R.id.tv_main_date);

        // Amerika
        tvAmerikaTime = findViewById(R.id.tv_amerika_time);
        tvAmerikaLocation = findViewById(R.id.tv_amerika_location);
        tvAmerikaDate = findViewById(R.id.tv_amerika_date);

        // Inggris
        tvInggrisTime = findViewById(R.id.tv_inggris_time);
        tvInggrisLocation = findViewById(R.id.tv_inggris_location);
        tvInggrisDate = findViewById(R.id.tv_inggris_date);

        // Jepang
        tvJepangTime = findViewById(R.id.tv_jepang_time);
        tvJepangLocation = findViewById(R.id.tv_jepang_location);
        tvJepangDate = findViewById(R.id.tv_jepang_date);

        // Arab
        tvArabTime = findViewById(R.id.tv_Arab_time);
        tvArabLocation = findViewById(R.id.tv_Arab_location);
        tvArabDate = findViewById(R.id.tv_Arab_date);

        // Profile icon
        ivProfile = findViewById(R.id.iv_profile);

        // Log untuk debugging
        logViewStatus("tvMainTime", tvMainTime);
        logViewStatus("tvAmerikaTime", tvAmerikaTime);
        logViewStatus("tvInggrisTime", tvInggrisTime);
        logViewStatus("tvJepangTime", tvJepangTime);
        logViewStatus("tvArabTime", tvArabTime);
        logViewStatus("ivProfile", ivProfile);

        // Set initial location text
        if (tvMainLocation != null) tvMainLocation.setText("Indonesia (WIB)");
        if (tvAmerikaLocation != null) tvAmerikaLocation.setText("Amerika Serikat (EDT)");
        if (tvInggrisLocation != null) tvInggrisLocation.setText("Inggris (BST)");
        if (tvJepangLocation != null) tvJepangLocation.setText("Jepang (JST)");
        if (tvArabLocation != null) tvArabLocation.setText("Arab Saudi (AST)");
    }

    private void logViewStatus(String viewName, View view) {
        if (view != null) {
            Log.d(TAG, viewName + " found successfully");
        } else {
            Log.e(TAG, viewName + " is NULL - check layout XML");
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners...");

        // Bottom navigation dengan null checks
        setupNavigationClickListener(R.id.nav_alarm_container, MainActivity2.class, "Alarm");
        setupNavigationClickListener(R.id.nav_world_container, null, "World Clock"); // Current page
        setupNavigationClickListener(R.id.nav_timer_container, MainActivity4.class, "Timer");
        setupNavigationClickListener(R.id.nav_stopwatch_container, MainActivity5.class, "Stopwatch");
        setupNavigationClickListener(R.id.nav_setting_container, MainActivity6.class, "Settings");

        // Profile icon click
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                Log.d(TAG, "Profile icon clicked");
                checkLoginStatusAndNavigate();
            });
        } else {
            Log.e(TAG, "ivProfile is null - cannot set click listener");
        }

        Log.d(TAG, "Click listeners setup completed");
    }

    private void setupNavigationClickListener(int viewId, Class<?> destination, String pageName) {
        View navView = findViewById(viewId);
        if (navView != null) {
            navView.setOnClickListener(v -> {
                Log.d(TAG, pageName + " navigation clicked");
                if (destination != null) {
                    navigateToActivity(destination);
                } else {
                    // Already on this page
                    Log.d(TAG, "Already on " + pageName + " page");
                }
            });
            Log.d(TAG, pageName + " navigation listener set successfully");
        } else {
            Log.e(TAG, "Navigation view not found: " + pageName + " (ID: " + viewId + ")");
            // Show toast untuk debugging
            runOnUiThread(() -> Toast.makeText(this,
                    "Navigation error: " + pageName + " not found", Toast.LENGTH_SHORT).show());
        }
    }

    // Method untuk cek status login
    private void checkLoginStatusAndNavigate() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        Log.d(TAG, "Login status: " + (isLoggedIn ? "Logged in" : "Not logged in"));

        if (isLoggedIn) {
            // Jika sudah login, pergi ke profile page (Logout)
            Intent intent = new Intent(MainActivity3.this, Logout.class);

            // Pass user data ke profile activity
            String username = prefs.getString("username", "User");
            String userEmail = prefs.getString("user_email", "");

            intent.putExtra("user_name", username);
            intent.putExtra("user_email", userEmail);

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            // Jika belum login, pergi ke login page (Login)
            Intent intent = new Intent(MainActivity3.this, Login.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void navigateToActivity(Class<?> cls) {
        try {
            Intent intent = new Intent(MainActivity3.this, cls);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to activity: " + e.getMessage());
            Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTimeUpdates() {
        if (isUpdating) {
            Log.d(TAG, "Time updates already running");
            return;
        }

        handler = new Handler();
        timeUpdater = new Runnable() {
            @Override
            public void run() {
                updateAllTimes();
                handler.postDelayed(this, 1000); // Update setiap 1 detik
            }
        };
        handler.post(timeUpdater);
        isUpdating = true;

        Log.d(TAG, "Time updates started");

        // Update immediately
        updateAllTimes();
    }

    private void stopTimeUpdates() {
        if (handler != null && timeUpdater != null) {
            handler.removeCallbacks(timeUpdater);
            isUpdating = false;
            Log.d(TAG, "Time updates stopped");
        }
    }

    private void updateAllTimes() {
        // Update waktu untuk semua negara secara realtime
        updateTimeForCountry(timeZones[0][0], tvMainTime, tvMainDate, timeZones[0][1], timeZones[0][2]);
        updateTimeForCountry(timeZones[1][0], tvAmerikaTime, tvAmerikaDate, timeZones[1][1], timeZones[1][2]);
        updateTimeForCountry(timeZones[2][0], tvInggrisTime, tvInggrisDate, timeZones[2][1], timeZones[2][2]);
        updateTimeForCountry(timeZones[3][0], tvJepangTime, tvJepangDate, timeZones[3][1], timeZones[3][2]);
        updateTimeForCountry(timeZones[4][0], tvArabTime, tvArabDate, timeZones[4][1], timeZones[4][2]);
    }

    private void updateTimeForCountry(String timeZoneId, TextView timeView, TextView dateView,
                                      String countryName, String timeZoneCode) {
        try {
            // Skip if views are null
            if (timeView == null || dateView == null) {
                return;
            }

            TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
            Date now = new Date();

            // Format waktu: HH.mm (24 jam format)
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm", Locale.getDefault());
            timeFormat.setTimeZone(timeZone);
            String time = timeFormat.format(now);

            // Format tanggal: EEEE, d MMM (hari, tanggal bulan)
            Locale locale;
            switch (countryName) {
                case "Indonesia":
                    locale = new Locale("id", "ID");
                    break;
                case "Jepang":
                    locale = new Locale("id", "ID");
                    break;
                case "Arab Saudi":
                    locale = new Locale("id", "ID");
                    break;
                default:
                    locale = new Locale("id", "ID");
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMM", locale);
            dateFormat.setTimeZone(timeZone);
            String date = dateFormat.format(now);

            // Update UI di thread utama
            runOnUiThread(() -> {
                timeView.setText(time);
                dateView.setText(date);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error updating time for " + countryName + ": " + e.getMessage());
            // Fallback: set waktu default
            runOnUiThread(() -> {
                if (timeView != null) timeView.setText("--.--");
                if (dateView != null) dateView.setText("-");
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimeUpdates();
        Log.d(TAG, "Activity destroyed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimeUpdates();
        Log.d(TAG, "Activity paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isUpdating) {
            startTimeUpdates();
        }
        Log.d(TAG, "Activity resumed");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        Log.d(TAG, "Back pressed");
    }
}