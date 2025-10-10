package com.example.tugaspts;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TimerRunningActivity extends AppCompatActivity {

    private TextView tvTime, tvStatus;
    private ImageView btnPause, btnCancel, btnSound;

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = true;
    private long timeLeftInMillis;
    private MediaPlayer tickMediaPlayer;
    private MediaPlayer alarmMediaPlayer;
    private boolean isSoundEnabled = true;
    private Handler alarmStopHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_running);

        initViews();
        setupClickListeners();

        // Get timer duration from intent
        long duration = getIntent().getLongExtra("TIMER_DURATION", 60000); // default 1 minute
        timeLeftInMillis = duration;

        startTimer();
    }

    private void initViews() {
        tvTime = findViewById(R.id.tv_time);
        tvStatus = findViewById(R.id.tv_status);
        btnPause = findViewById(R.id.btn_pause);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSound = findViewById(R.id.btn_sound);

        // Set initial time display
        updateTimeDisplay();
    }

    private void setupClickListeners() {
        btnPause.setOnClickListener(v -> toggleTimer());
        btnCancel.setOnClickListener(v -> cancelTimer());
        btnSound.setOnClickListener(v -> toggleSound());

        // Bottom navigation
        findViewById(R.id.nav_alarm).setOnClickListener(v -> {
            Intent intent = new Intent(TimerRunningActivity.this, MainActivity2.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.nav_world).setOnClickListener(v -> {
            Intent intent = new Intent(TimerRunningActivity.this, MainActivity3.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.nav_timer).setOnClickListener(v -> {
            // Already on timer page, do nothing or show message
            Toast.makeText(this, "Anda sudah di halaman Timer", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.nav_stopwatch).setOnClickListener(v -> {
            Intent intent = new Intent(TimerRunningActivity.this, MainActivity5.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.nav_setting).setOnClickListener(v -> {
            Intent intent = new Intent(TimerRunningActivity.this, MainActivity6.class);
            startActivity(intent);
            finish();
        });
    }

    private void startTimer() {
        // Start tick sound
        startTickSound();

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimeDisplay();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                tvStatus.setText("Timer selesai!");
                updatePauseButton();

                // Stop tick sound
                stopTickSound();

                // Play alarm sound if enabled
                if (isSoundEnabled) {
                    playAlarmSound();
                }

                Toast.makeText(TimerRunningActivity.this, "Timer selesai!", Toast.LENGTH_LONG).show();
            }
        }.start();

        isTimerRunning = true;
        updatePauseButton();
        tvStatus.setText("Timer sedang berjalan...");
    }

    private void toggleTimer() {
        if (isTimerRunning) {
            pauseTimer();
        } else {
            resumeTimer();
        }
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        updatePauseButton();
        tvStatus.setText("Timer dijeda");
        stopTickSound();

        Toast.makeText(this, "Timer dijeda", Toast.LENGTH_SHORT).show();
    }

    private void resumeTimer() {
        startTimer();
        Toast.makeText(this, "Timer dilanjutkan", Toast.LENGTH_SHORT).show();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopTickSound();
        stopAlarmSound();

        // Kembali ke halaman timer setting
        Intent intent = new Intent(TimerRunningActivity.this, MainActivity4.class);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Timer dibatalkan", Toast.LENGTH_SHORT).show();
    }

    private void toggleSound() {
        isSoundEnabled = !isSoundEnabled;
        if (isSoundEnabled) {
            btnSound.setImageResource(R.drawable.sound);
            btnSound.setColorFilter(getResources().getColor(android.R.color.white));

            // Jika timer sedang berjalan, mulai kembali tick sound
            if (isTimerRunning) {
                startTickSound();
            }

            Toast.makeText(this, "Sound diaktifkan", Toast.LENGTH_SHORT).show();
        } else {
            btnSound.setImageResource(R.drawable.sound_off);
            btnSound.setColorFilter(getResources().getColor(android.R.color.darker_gray));

            // Stop semua sound
            stopTickSound();
            stopAlarmSound();

            Toast.makeText(this, "Sound dimatikan", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTimeDisplay() {
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = ((int) (timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        tvTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void updatePauseButton() {
        if (isTimerRunning) {
            btnPause.setImageResource(R.drawable.pause);
        } else {
            btnPause.setImageResource(R.drawable.play);
        }
    }

    private void startTickSound() {
        if (isSoundEnabled) {
            try {
                // Stop existing sound first
                stopTickSound();

                tickMediaPlayer = MediaPlayer.create(this, R.raw.timer_ticks);
                if (tickMediaPlayer != null) {
                    tickMediaPlayer.setLooping(true);
                    tickMediaPlayer.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopTickSound() {
        if (tickMediaPlayer != null) {
            if (tickMediaPlayer.isPlaying()) {
                tickMediaPlayer.stop();
            }
            tickMediaPlayer.release();
            tickMediaPlayer = null;
        }
    }

    private void playAlarmSound() {
        if (isSoundEnabled) {
            try {
                alarmMediaPlayer = MediaPlayer.create(this, R.raw.notif);
                if (alarmMediaPlayer != null) {
                    alarmMediaPlayer.setLooping(true);
                    alarmMediaPlayer.start();

                    // Auto stop setelah 10 detik
                    alarmStopHandler.postDelayed(() -> {
                        stopAlarmSound();
                    }, 10000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopAlarmSound() {
        if (alarmMediaPlayer != null) {
            if (alarmMediaPlayer.isPlaying()) {
                alarmMediaPlayer.stop();
            }
            alarmMediaPlayer.release();
            alarmMediaPlayer = null;
        }
        alarmStopHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopTickSound();
        stopAlarmSound();
        alarmStopHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTimerRunning && countDownTimer != null) {
            countDownTimer.cancel();
            stopTickSound();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Jika timer sedang berjalan tapi countDownTimer null (setelah pause), restart timer
        if (isTimerRunning && countDownTimer == null && timeLeftInMillis > 0) {
            startTimer();
        }
    }
}