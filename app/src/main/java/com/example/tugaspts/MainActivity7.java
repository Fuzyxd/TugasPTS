package com.example.tugaspts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity7 extends AppCompatActivity {
    private NumberPicker numberPickerHour, numberPickerMinute;
    private EditText etNama;
    private ImageButton btnClose, btnConfirm;
    private ImageView ivDelete;
    private TextView tvDelete, tvTitle;

    // Switch buttons untuk hari
    private ImageButton switchSenin, switchSelasa, switchRabu, switchKamis,
            switchJumat, switchSabtu, switchSetiapHari;

    // Status hari
    private boolean[] dayStates = new boolean[7]; // 0:Senin, 1:Selasa, ..., 6:Minggu
    private boolean isEditMode = false;
    private int editAlarmIndex = -1;
    private int existingAlarmId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);

        initViews();
        setupTimePickers();
        setupClickListeners();
        checkEditMode();
    }

    private void initViews() {
        // Time Pickers
        numberPickerHour = findViewById(R.id.numberPicker_hour);
        numberPickerMinute = findViewById(R.id.numberPicker_minute);

        // Input Nama
        etNama = findViewById(R.id.et_nama);

        // Header Buttons
        btnClose = findViewById(R.id.btn_close);
        btnConfirm = findViewById(R.id.btn_confirm);
        tvTitle = findViewById(R.id.tv_title);

        // Delete Section
        ivDelete = findViewById(R.id.iv_delete);
        tvDelete = findViewById(R.id.tv_delete);

        // Day Switches
        switchSenin = findViewById(R.id.switch_senin);
        switchSelasa = findViewById(R.id.switch_selasa);
        switchRabu = findViewById(R.id.switch_rabu);
        switchKamis = findViewById(R.id.switch_kamis);
        switchJumat = findViewById(R.id.switch_jumat);
        switchSabtu = findViewById(R.id.switch_sabtu);
        switchSetiapHari = findViewById(R.id.switch_setiap_hari);
    }

    private void setupTimePickers() {
        // Set range jam: 0-23
        numberPickerHour.setMinValue(0);
        numberPickerHour.setMaxValue(23);
        numberPickerHour.setFormatter(value -> String.format("%02d", value));

        // Set range menit: 0-59
        numberPickerMinute.setMinValue(0);
        numberPickerMinute.setMaxValue(59);
        numberPickerMinute.setFormatter(value -> String.format("%02d", value));

        // Set nilai default ke waktu sekarang
        Calendar calendar = Calendar.getInstance();
        numberPickerHour.setValue(calendar.get(Calendar.HOUR_OF_DAY));
        numberPickerMinute.setValue(calendar.get(Calendar.MINUTE));
    }

    private void setupClickListeners() {
        // Close Button
        btnClose.setOnClickListener(v -> finish());

        // Confirm Button
        btnConfirm.setOnClickListener(v -> saveAlarm());

        // Delete Button
        View.OnClickListener deleteListener = v -> deleteAlarm();
        ivDelete.setOnClickListener(deleteListener);
        tvDelete.setOnClickListener(deleteListener);

        // Day Switches
        setupDaySwitchListeners();
    }

    private void setupDaySwitchListeners() {
        switchSenin.setOnClickListener(v -> toggleDay(0, switchSenin));
        switchSelasa.setOnClickListener(v -> toggleDay(1, switchSelasa));
        switchRabu.setOnClickListener(v -> toggleDay(2, switchRabu));
        switchKamis.setOnClickListener(v -> toggleDay(3, switchKamis));
        switchJumat.setOnClickListener(v -> toggleDay(4, switchJumat));
        switchSabtu.setOnClickListener(v -> toggleDay(5, switchSabtu));

        // Setiap Hari - toggle semua hari
        switchSetiapHari.setOnClickListener(v -> toggleAllDays(switchSetiapHari));
    }

    private void toggleDay(int dayIndex, ImageButton switchButton) {
        dayStates[dayIndex] = !dayStates[dayIndex];
        updateSwitchAppearance(switchButton, dayStates[dayIndex]);

        // Update "Setiap Hari" status
        updateSetiapHariStatus();
    }

    private void toggleAllDays(ImageButton switchButton) {
        boolean newState = !isAllDaysSelected();

        // Set semua hari ke state yang sama
        for (int i = 0; i < dayStates.length; i++) {
            dayStates[i] = newState;
        }

        // Update semua switch appearance
        updateAllSwitchesAppearance();
        updateSwitchAppearance(switchButton, newState);
    }

    private void updateAllSwitchesAppearance() {
        updateSwitchAppearance(switchSenin, dayStates[0]);
        updateSwitchAppearance(switchSelasa, dayStates[1]);
        updateSwitchAppearance(switchRabu, dayStates[2]);
        updateSwitchAppearance(switchKamis, dayStates[3]);
        updateSwitchAppearance(switchJumat, dayStates[4]);
        updateSwitchAppearance(switchSabtu, dayStates[5]);
        updateSwitchAppearance(switchSetiapHari, isAllDaysSelected());
    }

    private void updateSwitchAppearance(ImageButton switchButton, boolean isOn) {
        switchButton.setImageResource(isOn ? R.drawable.switch_on : R.drawable.switch_off);
    }

    private boolean isAllDaysSelected() {
        for (int i = 0; i < 6; i++) { // Hanya Senin-Sabtu
            if (!dayStates[i]) {
                return false;
            }
        }
        return true;
    }

    private void updateSetiapHariStatus() {
        boolean allSelected = isAllDaysSelected();
        updateSwitchAppearance(switchSetiapHari, allSelected);
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("alarm_index")) {
            isEditMode = true;
            editAlarmIndex = intent.getIntExtra("alarm_index", -1);
            existingAlarmId = intent.getIntExtra("alarm_id", -1);

            // Ubah judul
            tvTitle.setText("Edit Alarm");

            // Tampilkan delete button
            ivDelete.setVisibility(View.VISIBLE);
            tvDelete.setVisibility(View.VISIBLE);

            // Load data alarm yang akan diedit
            loadAlarmData();
        } else {
            // Mode tambah alarm baru
            ivDelete.setVisibility(View.GONE);
            tvDelete.setVisibility(View.GONE);
        }
    }

    private void loadAlarmData() {
        try {
            List<Alarm> alarms = getAlarmsFromStorage();
            if (editAlarmIndex >= 0 && editAlarmIndex < alarms.size()) {
                Alarm alarm = alarms.get(editAlarmIndex);

                // Set waktu
                String[] timeParts = alarm.getTime().split(":");
                if (timeParts.length == 2) {
                    numberPickerHour.setValue(Integer.parseInt(timeParts[0]));
                    numberPickerMinute.setValue(Integer.parseInt(timeParts[1]));
                }

                // Set nama
                etNama.setText(alarm.getLabel());

                // Set hari
                parseAndSetDays(alarm.getDays());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading alarm data", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseAndSetDays(String daysString) {
        // Reset semua hari ke false
        for (int i = 0; i < dayStates.length; i++) {
            dayStates[i] = false;
        }

        if (daysString.contains("Setiap Hari")) {
            // Set semua hari ke true
            for (int i = 0; i < 6; i++) {
                dayStates[i] = true;
            }
        } else {
            // Parse hari individual
            if (daysString.contains("Senin")) dayStates[0] = true;
            if (daysString.contains("Selasa")) dayStates[1] = true;
            if (daysString.contains("Rabu")) dayStates[2] = true;
            if (daysString.contains("Kamis")) dayStates[3] = true;
            if (daysString.contains("Jum'at")) dayStates[4] = true;
            if (daysString.contains("Sabtu")) dayStates[5] = true;
        }

        // Update UI
        updateAllSwitchesAppearance();
    }

    private void saveAlarm() {
        String nama = etNama.getText().toString().trim();
        if (nama.isEmpty()) {
            nama = "Alarm";
        }

        // Format waktu dari NumberPicker
        String time = String.format("%02d:%02d",
                numberPickerHour.getValue(),
                numberPickerMinute.getValue());

        // Format hari yang dipilih
        String days = getSelectedDaysString();

        // Buat objek Alarm
        Alarm alarm = new Alarm(time, nama, days, true);

        if (isEditMode && existingAlarmId != -1) {
            alarm.setAlarmId(existingAlarmId);
        }

        // Simpan ke storage
        saveAlarmToStorage(alarm);

        // Set alarm di AlarmManager
        AlarmHelper.setAlarm(this, alarm);

        Toast.makeText(this, "Alarm disimpan", Toast.LENGTH_SHORT).show();

        // Kembali ke MainActivity2 dengan result OK
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private String getSelectedDaysString() {
        if (isAllDaysSelected()) {
            return "Setiap Hari";
        }

        List<String> selectedDays = new ArrayList<>();
        if (dayStates[0]) selectedDays.add("Senin");
        if (dayStates[1]) selectedDays.add("Selasa");
        if (dayStates[2]) selectedDays.add("Rabu");
        if (dayStates[3]) selectedDays.add("Kamis");
        if (dayStates[4]) selectedDays.add("Jum'at");
        if (dayStates[5]) selectedDays.add("Sabtu");

        if (selectedDays.isEmpty()) {
            return "Sekali";
        }

        return String.join(", ", selectedDays);
    }

    private void saveAlarmToStorage(Alarm newAlarm) {
        SharedPreferences prefs = getSharedPreferences("alarms", MODE_PRIVATE);
        List<Alarm> alarms = getAlarmsFromStorage();

        if (isEditMode && editAlarmIndex >= 0 && editAlarmIndex < alarms.size()) {
            // Edit alarm yang sudah ada
            alarms.set(editAlarmIndex, newAlarm);
        } else {
            // Tambah alarm baru
            alarms.add(newAlarm);
        }

        saveAllAlarmsToStorage(alarms);
    }

    private void deleteAlarm() {
        if (!isEditMode || editAlarmIndex == -1) {
            return;
        }

        // Cancel alarm terlebih dahulu
        if (existingAlarmId != -1) {
            AlarmHelper.cancelAlarm(this, existingAlarmId);
        }

        // Hapus dari storage
        SharedPreferences prefs = getSharedPreferences("alarms", MODE_PRIVATE);
        List<Alarm> alarms = getAlarmsFromStorage();

        if (editAlarmIndex >= 0 && editAlarmIndex < alarms.size()) {
            alarms.remove(editAlarmIndex);
            saveAllAlarmsToStorage(alarms);
        }

        Toast.makeText(this, "Alarm dihapus", Toast.LENGTH_SHORT).show();

        // Kembali ke MainActivity2 dengan result OK
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private List<Alarm> getAlarmsFromStorage() {
        List<Alarm> alarms = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("alarms", MODE_PRIVATE);
        String alarmsJson = prefs.getString("alarms_list", "[]");

        try {
            JSONArray jsonArray = new JSONArray(alarmsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Alarm alarm = new Alarm();
                alarm.setTime(jsonObject.getString("time"));
                alarm.setLabel(jsonObject.getString("label"));
                alarm.setDays(jsonObject.getString("days"));
                alarm.setActive(jsonObject.getBoolean("active"));

                if (jsonObject.has("alarmId")) {
                    alarm.setAlarmId(jsonObject.getInt("alarmId"));
                }

                alarms.add(alarm);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return alarms;
    }

    private void saveAllAlarmsToStorage(List<Alarm> alarms) {
        SharedPreferences prefs = getSharedPreferences("alarms", MODE_PRIVATE);

        try {
            JSONArray jsonArray = new JSONArray();
            for (Alarm alarm : alarms) {
                JSONObject alarmJson = new JSONObject();
                alarmJson.put("time", alarm.getTime());
                alarmJson.put("label", alarm.getLabel());
                alarmJson.put("days", alarm.getDays());
                alarmJson.put("active", alarm.isActive());
                alarmJson.put("alarmId", alarm.getAlarmId());
                jsonArray.put(alarmJson);
            }

            prefs.edit().putString("alarms_list", jsonArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}