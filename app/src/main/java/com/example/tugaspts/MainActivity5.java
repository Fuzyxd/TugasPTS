package com.example.tugaspts;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity5 extends AppCompatActivity {

    private TextView tvHours, tvMinutes, tvSeconds;
    private ImageView btnPlay, btnReset, btnBell;

    private Handler handler = new Handler();
    private Runnable runnable;
    private boolean isRunning = false;
    private long startTime = 0;
    private long elapsedTime = 0;
    private long pausedTime = 0;

    private MediaPlayer mediaPlayer;
    private boolean isSoundEnabled = true;
    private ImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        initViews();
        setupClickListeners();
        updateTimeDisplay();
        updateBellButton(); // Initialize bell button state
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void initViews() {
        // Time display
        tvHours = findViewById(R.id.tv_hours);
        tvMinutes = findViewById(R.id.tv_minutes);
        tvSeconds = findViewById(R.id.tv_seconds);

        // Control buttons
        btnPlay = findViewById(R.id.btn_play);
        btnReset = findViewById(R.id.btn_reset);
        btnBell = findViewById(R.id.btn_bell);
        ivProfile = findViewById(R.id.iv_profile);
    }

    private void setupClickListeners() {
        // Control buttons
        btnPlay.setOnClickListener(v -> toggleStopwatch());
        btnReset.setOnClickListener(v -> resetStopwatch());
        btnBell.setOnClickListener(v -> toggleSound());

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

        findViewById(R.id.nav_setting).setOnClickListener(v -> {
            navigateToActivity(MainActivity6.class);
        });
        ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity5.this, Login.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    private void navigateToActivity(Class<?> cls) {
        Intent intent = new Intent(MainActivity5.this, cls);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void toggleStopwatch() {
        if (isRunning) {
            pauseStopwatch();
        } else {
            startStopwatch();
        }
    }

    private void startStopwatch() {
        if (!isRunning) {
            if (pausedTime == 0) {
                // Start from beginning
                startTime = System.currentTimeMillis();
            } else {
                // Resume from paused time
                startTime = System.currentTimeMillis() - pausedTime;
            }

            isRunning = true;
            updatePlayButton();

            // Start sound if enabled
            if (isSoundEnabled) {
                playSound();
            }

            runnable = new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        elapsedTime = System.currentTimeMillis() - startTime;
                        updateTimeDisplay();
                        handler.postDelayed(this, 100);
                    }
                }
            };
            handler.postDelayed(runnable, 100);
        }
    }

    private void pauseStopwatch() {
        if (isRunning) {
            isRunning = false;
            pausedTime = elapsedTime;
            updatePlayButton();
            handler.removeCallbacks(runnable);

            // Stop sound when paused
            stopSound();
        }
    }

    private void resetStopwatch() {
        isRunning = false;
        elapsedTime = 0;
        pausedTime = 0;
        updateTimeDisplay();
        updatePlayButton();
        handler.removeCallbacks(runnable);

        // Stop sound when reset
        stopSound();
    }

    private void toggleSound() {
        isSoundEnabled = !isSoundEnabled;
        updateBellButton();

        if (isSoundEnabled) {
            Toast.makeText(this, "Sound diaktifkan", Toast.LENGTH_SHORT).show();
            // If stopwatch is running and sound was enabled, start sound
            if (isRunning) {
                playSound();
            }
        } else {
            Toast.makeText(this, "Sound dimatikan", Toast.LENGTH_SHORT).show();
            // Stop sound when disabled
            stopSound();
        }
    }

    private void updateBellButton() {
        if (isSoundEnabled) {
            btnBell.setImageResource(R.drawable.sound);
            btnBell.setColorFilter(getResources().getColor(android.R.color.white));
        } else {
            btnBell.setImageResource(R.drawable.sound_off);
            btnBell.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void updateTimeDisplay() {
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;

        tvHours.setText(String.format("%02dj", hours));
        tvMinutes.setText(String.format("%02dm", minutes));
        tvSeconds.setText(String.format("%02dd", seconds));
    }

    private void updatePlayButton() {
        if (isRunning) {
            btnPlay.setImageResource(R.drawable.pause);
        } else {
            btnPlay.setImageResource(R.drawable.play);
        }
    }

    private void playSound() {
        if (isSoundEnabled && !isSoundPlaying()) {
            try {
                // Release any existing media player first
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }

                mediaPlayer = MediaPlayer.create(this, R.raw.timer_ticks);
                if (mediaPlayer != null) {
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error memutar sound", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopSound() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private boolean isSoundPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        stopSound();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRunning) {
            pauseStopwatch();
        }
        stopSound();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Don't auto-resume stopwatch to prevent unexpected behavior
    }
}