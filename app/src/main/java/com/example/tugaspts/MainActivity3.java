package com.example.tugaspts;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

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

    private Handler handler;
    private ImageView ivProfile;
    private Runnable timeUpdater;
    private boolean isUpdating = false;

    // Timezone data untuk masing-masing negara
    private final String[][] timeZones = {
            {"Asia/Jakarta", "Indonesia", "WIB"},           // Indonesia
            {"America/New_York", "Amerika Serikat", "EDT"}, // Amerika
            {"Europe/London", "Inggris", "BST"},            // Inggris
            {"Asia/Tokyo", "Jepang", "JST"}                 // Jepang
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        initViews();
        setupClickListeners();
        startTimeUpdates();
    }

    private void initViews() {
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

        // Set initial location text sesuai gambar
        tvMainLocation.setText("Indonesia (WIB)");
        tvAmerikaLocation.setText("Amerika Serikat (EDT)");
        tvInggrisLocation.setText("Inggris (BST)");
        tvJepangLocation.setText("Jepang (JST)");

        ivProfile = findViewById(R.id.iv_profile);
    }

    private void setupClickListeners() {
        // Bottom navigation
        findViewById(R.id.nav_alarm).setOnClickListener(v -> {
            navigateToActivity(MainActivity2.class);
        });

        findViewById(R.id.nav_timer).setOnClickListener(v -> {
            navigateToActivity(MainActivity4.class);
        });

        findViewById(R.id.nav_stopwatch).setOnClickListener(v -> {
            navigateToActivity(MainActivity5.class);
        });

        findViewById(R.id.nav_setting).setOnClickListener(v -> {
            navigateToActivity(MainActivity6.class);
        });
        ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity3.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void navigateToActivity(Class<?> cls) {
        Intent intent = new Intent(MainActivity3.this, cls);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void startTimeUpdates() {
        if (isUpdating) return;

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

        // Update immediately
        updateAllTimes();
    }

    private void stopTimeUpdates() {
        if (handler != null && timeUpdater != null) {
            handler.removeCallbacks(timeUpdater);
            isUpdating = false;
        }
    }

    private void updateAllTimes() {
        // Update waktu untuk semua negara secara realtime
        updateTimeForCountry(timeZones[0][0], tvMainTime, tvMainDate, timeZones[0][1], timeZones[0][2]);
        updateTimeForCountry(timeZones[1][0], tvAmerikaTime, tvAmerikaDate, timeZones[1][1], timeZones[1][2]);
        updateTimeForCountry(timeZones[2][0], tvInggrisTime, tvInggrisDate, timeZones[2][1], timeZones[2][2]);
        updateTimeForCountry(timeZones[3][0], tvJepangTime, tvJepangDate, timeZones[3][1], timeZones[3][2]);
    }

    private void updateTimeForCountry(String timeZoneId, TextView timeView, TextView dateView,
                                      String countryName, String timeZoneCode) {
        try {
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
            Date now = new Date();

            // Format waktu: HH.mm (24 jam format)
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm", Locale.getDefault());
            timeFormat.setTimeZone(timeZone);
            String time = timeFormat.format(now);

            // Format tanggal: EEEE, d MMM (hari, tanggal bulan)
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMM", new Locale("id", "ID"));
            dateFormat.setTimeZone(timeZone);
            String date = dateFormat.format(now);

            // Update UI di thread utama
            runOnUiThread(() -> {
                if (timeView != null) {
                    timeView.setText(time);
                }
                if (dateView != null) {
                    dateView.setText(date);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: set waktu default
            runOnUiThread(() -> {
                if (timeView != null) {
                    timeView.setText("--.--");
                }
                if (dateView != null) {
                    dateView.setText("-");
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimeUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimeUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isUpdating) {
            startTimeUpdates();
        }
    }
}