package com.example.tugaspts;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;

public class MainActivity4 extends AppCompatActivity {

    private NumberPicker numberPickerHour, numberPickerMinute, numberPickerSecond;
    private ImageView btnPlay, btnReset, btnBell;

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis = 0;
    private long totalTimeInMillis = 0;

    private MediaPlayer tickMediaPlayer;
    private MediaPlayer alarmMediaPlayer;
    private boolean isAlarmSet = true;
    private ImageView ivProfile;
    private Handler alarmStopHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        initViews();
        setupNumberPickers();
        setupClickListeners();
        updateTimerDisplay();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void initViews() {
        // NumberPickers
        numberPickerHour = findViewById(R.id.numberPicker_hour);
        numberPickerMinute = findViewById(R.id.numberPicker_minute);
        numberPickerSecond = findViewById(R.id.numberPicker_second);

        // Control buttons
        btnPlay = findViewById(R.id.btn_play);
        btnReset = findViewById(R.id.btn_reset);
        btnBell = findViewById(R.id.btn_bell);
        ivProfile = findViewById(R.id.iv_profile);
    }

    private void setupNumberPickers() {
        // Set range untuk masing-masing picker
        numberPickerHour.setMinValue(0);
        numberPickerHour.setMaxValue(23);
        numberPickerMinute.setMinValue(0);
        numberPickerMinute.setMaxValue(59);
        numberPickerSecond.setMinValue(0);
        numberPickerSecond.setMaxValue(59);

        // Set formatter untuk menampilkan 2 digit
        NumberPicker.Formatter formatter = value -> String.format("%02d", value);

        numberPickerHour.setFormatter(formatter);
        numberPickerMinute.setFormatter(formatter);
        numberPickerSecond.setFormatter(formatter);

        // Set nilai default
        numberPickerHour.setValue(0);
        numberPickerMinute.setValue(0);
        numberPickerSecond.setValue(0);

        // Styling untuk NumberPicker
        styleNumberPicker(numberPickerHour);
        styleNumberPicker(numberPickerMinute);
        styleNumberPicker(numberPickerSecond);

        // Listeners untuk update timer real-time
        numberPickerHour.setOnValueChangedListener((picker, oldVal, newVal) -> updateTimerFromPickers());
        numberPickerMinute.setOnValueChangedListener((picker, oldVal, newVal) -> updateTimerFromPickers());
        numberPickerSecond.setOnValueChangedListener((picker, oldVal, newVal) -> updateTimerFromPickers());
    }

    private void styleNumberPicker(NumberPicker numberPicker) {
        try {
            // Set text color for wheel
            Field selectorWheelPaintField = numberPicker.getClass().getDeclaredField("mSelectorWheelPaint");
            selectorWheelPaintField.setAccessible(true);
            android.graphics.Paint wheelPaint = (android.graphics.Paint) selectorWheelPaintField.get(numberPicker);
            wheelPaint.setColor(getResources().getColor(android.R.color.white));

            // Set text color for selected value
            final int count = numberPicker.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = numberPicker.getChildAt(i);
                if (child instanceof EditText) {
                    EditText editText = (EditText) child;
                    editText.setTextColor(getResources().getColor(android.R.color.white));
                    editText.setTextSize(36f);
                    editText.setTypeface(editText.getTypeface(), android.graphics.Typeface.BOLD);
                    break;
                }
            }

            // Hide selection dividers
            Field selectionDividerField = numberPicker.getClass().getDeclaredField("mSelectionDivider");
            selectionDividerField.setAccessible(true);
            selectionDividerField.set(numberPicker, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTimerFromPickers() {
        if (!isTimerRunning) {
            int hours = numberPickerHour.getValue();
            int minutes = numberPickerMinute.getValue();
            int seconds = numberPickerSecond.getValue();

            totalTimeInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L;
            timeLeftInMillis = totalTimeInMillis;
            updateTimerDisplay();
        }
    }

    private void setupClickListeners() {
        // Control buttons
        btnPlay.setOnClickListener(v -> toggleTimer());
        btnReset.setOnClickListener(v -> resetTimer());
        btnBell.setOnClickListener(v -> toggleAlarm());

        // Bottom navigation
        findViewById(R.id.nav_alarm).setOnClickListener(v -> {
            navigateToActivity(MainActivity2.class);
        });

        findViewById(R.id.nav_world).setOnClickListener(v -> {
            navigateToActivity(MainActivity3.class);
        });

        findViewById(R.id.nav_stopwatch).setOnClickListener(v -> {
            navigateToActivity(MainActivity5.class);
        });

        findViewById(R.id.nav_setting).setOnClickListener(v -> {
            navigateToActivity(MainActivity6.class);
        });
        ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity4.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    private void navigateToActivity(Class<?> cls) {
        Intent intent = new Intent(MainActivity4.this, cls);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void toggleTimer() {
        if (isTimerRunning) {
            pauseTimer();
        } else {
            if (timeLeftInMillis > 0) {
                startTimer();
            } else {
                // Jika timer 0, ambil nilai dari number picker
                updateTimerFromPickers();
                if (timeLeftInMillis > 0) {
                    startTimer();
                } else {
                    Toast.makeText(this, "Set timer terlebih dahulu", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void startTimer() {
        // Pindah ke halaman timer running
        Intent intent = new Intent(MainActivity4.this, TimerRunningActivity.class);
        intent.putExtra("TIMER_DURATION", timeLeftInMillis);
        intent.putExtra("IS_ALARM_SET", isAlarmSet);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        updatePlayButton();
        setNumberPickersEnabled(true);

        // Stop tick sound when paused
        stopTickSound();
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;

        // Stop all sounds
        stopTickSound();
        stopAlarmSound();

        // Reset ke nilai dari number picker
        updateTimerFromPickers();
        updatePlayButton();
        setNumberPickersEnabled(true);
    }

    private void setNumberPickersEnabled(boolean enabled) {
        numberPickerHour.setEnabled(enabled);
        numberPickerMinute.setEnabled(enabled);
        numberPickerSecond.setEnabled(enabled);

        // Visual feedback untuk disabled state
        if (enabled) {
            numberPickerHour.setAlpha(1.0f);
            numberPickerMinute.setAlpha(1.0f);
            numberPickerSecond.setAlpha(1.0f);
        } else {
            // Tambahkan efek fade ketika disabled
            numberPickerHour.setAlpha(0.7f);
            numberPickerMinute.setAlpha(0.7f);
            numberPickerSecond.setAlpha(0.7f);
        }
    }

    private void toggleAlarm() {
        isAlarmSet = !isAlarmSet;
        if (isAlarmSet) {
            btnBell.setImageResource(R.drawable.notif);
            btnBell.setColorFilter(getResources().getColor(android.R.color.white));
            Toast.makeText(this, "Alarm diaktifkan", Toast.LENGTH_SHORT).show();
        } else {
            btnBell.setImageResource(R.drawable.sound_off);
            btnBell.setColorFilter(getResources().getColor(android.R.color.darker_gray));
            Toast.makeText(this, "Alarm dimatikan", Toast.LENGTH_SHORT).show();

            // Stop alarm sound immediately when turned off
            stopAlarmSound();
        }
    }

    private void updateTimerDisplay() {
        if (timeLeftInMillis <= 0) {
            // Update number pickers ke nilai 0
            if (!isTimerRunning) {
                numberPickerHour.setValue(0);
                numberPickerMinute.setValue(0);
                numberPickerSecond.setValue(0);
            }
            return;
        }

        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = ((int) (timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        // Update number pickers hanya jika timer tidak berjalan
        if (!isTimerRunning) {
            numberPickerHour.setValue(hours);
            numberPickerMinute.setValue(minutes);
            numberPickerSecond.setValue(seconds);
        }
    }

    private void updatePlayButton() {
        if (isTimerRunning) {
            btnPlay.setImageResource(R.drawable.pause);
        } else {
            btnPlay.setImageResource(R.drawable.play);
        }
    }

    private void startTickSound() {
        try {
            tickMediaPlayer = MediaPlayer.create(this, R.raw.timer_ticks);
            if (tickMediaPlayer != null) {
                tickMediaPlayer.setLooping(true);
                tickMediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error memutar tick sound", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Error memutar alarm", Toast.LENGTH_SHORT).show();
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
        // Remove any pending stop handlers
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
        if (isTimerRunning && timeLeftInMillis > 0) {
            startTimer();
        }
    }
}