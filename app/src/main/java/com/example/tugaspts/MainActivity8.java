package com.example.tugaspts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity8 extends AppCompatActivity {

    private TextView tvUsername;
    private View btnLogoutBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main8);

        // Animasi masuk
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);

        initViews();
        setupClickListeners();
        loadUserData();
    }

    @Override
    public void finish() {
        super.finish();
        // Animasi keluar
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tv_username);
        btnLogoutBg = findViewById(R.id.btn_logout_bg);

        // Debug logging
        if (tvUsername == null) {
            System.out.println("tvUsername is NULL");
        } else {
            System.out.println("tvUsername found: " + tvUsername.getId());
        }

        if (btnLogoutBg == null) {
            System.out.println("btnLogoutBg is NULL");
        } else {
            System.out.println("btnLogoutBg found: " + btnLogoutBg.getId());
        }
    }

    private void setupClickListeners() {
        // Logout button
        if (btnLogoutBg != null) {
            btnLogoutBg.setOnClickListener(v -> {
                System.out.println("Logout button clicked");
                logoutUser();
            });
        } else {
            System.out.println("Logout button not found");
        }
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Coba ambil data dari berbagai sumber
        String username = prefs.getString("username", "");
        String userEmail = prefs.getString("user_email", "");

        // Jika tidak ada di SharedPreferences, coba dari Intent
        if (username.isEmpty()) {
            username = getIntent().getStringExtra("user_name");
            if (username == null || username.isEmpty()) {
                // Coba dari Firebase data yang mungkin disimpan
                username = getIntent().getStringExtra("userName");
            }
        }

        // Set username dengan format yang diinginkan
        if (username != null && !username.isEmpty()) {
            if (tvUsername != null) {
                tvUsername.setText(username);
                System.out.println("Username set to: " + username);
            }
        } else {
            // Default value
            if (tvUsername != null) {
                tvUsername.setText("Users");
                System.out.println("Username set to default: Users");
            }
        }

        // Simpan data untuk penggunaan berikutnya
        if (username != null && !username.isEmpty()) {
            saveUserData(username, userEmail);
        }
    }

    private void saveUserData(String username, String email) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("user_email", email != null ? email : "");
        editor.putBoolean("is_logged_in", true);
        editor.apply();
        System.out.println("User data saved: " + username);
    }

    private void logoutUser() {
        System.out.println("Starting logout process...");

        // Clear Firebase auth (jika menggunakan Firebase)
        try {
            // Jika menggunakan Firebase Auth, uncomment baris berikut:
            // FirebaseAuth.getInstance().signOut();
            System.out.println("Firebase signout attempted");
        } catch (Exception e) {
            System.out.println("Firebase signout error: " + e.getMessage());
        }

        // Clear user session dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("is_logged_in");
        editor.remove("username");
        editor.remove("user_email");
        editor.remove("user_id");
        editor.apply();
        System.out.println("SharedPreferences cleared");

        // Clear Google Sign-In (jika digunakan)
        try {
            // Jika menggunakan Google Sign-In, uncomment baris berikut:
            // GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this,
            //     GoogleSignInOptions.DEFAULT_SIGN_IN);
            // googleSignInClient.signOut();
            System.out.println("Google signout attempted");
        } catch (Exception e) {
            System.out.println("Google signout error: " + e.getMessage());
        }

        // Redirect ke login page dengan clear task
        Intent intent = new Intent(MainActivity8.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
        System.out.println("Redirected to login page");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data user setiap kali activity resume
        loadUserData();
    }
}   