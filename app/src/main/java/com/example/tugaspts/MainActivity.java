package com.example.tugaspts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final int RC_GOOGLE_SIGN_IN = 100;
    private static final String TAG = "MainActivity";

    private TextView tvLoginButton;
    private ImageButton btnGoogle;
    private EditText etEmail, etPassword;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupFirebase();
        setupGoogleSignIn();
        setupClickListeners();

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void initViews() {
        tvLoginButton = findViewById(R.id.tv_login_button);
        btnGoogle = findViewById(R.id.btn_google);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        // Debug logging
        Log.d(TAG, "tvLoginButton: " + (tvLoginButton != null));
        Log.d(TAG, "btnGoogle: " + (btnGoogle != null));
        Log.d(TAG, "etEmail: " + (etEmail != null));
        Log.d(TAG, "etPassword: " + (etPassword != null));
    }

    private void setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "Firebase Auth initialized: " + (firebaseAuth != null));
    }

    private void setupGoogleSignIn() {
        try {
            String webClientId = getString(R.string.default_web_client_id);
            Log.d(TAG, "Web Client ID: " + webClientId);

            if (webClientId.isEmpty() || webClientId.equals("YOUR_WEB_CLIENT_ID_HERE")) {
                showToast("Please configure Web Client ID in strings.xml");
                return;
            }

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(this, gso);
            Log.d(TAG, "Google Sign-In client initialized: " + (googleSignInClient != null));

        } catch (Exception e) {
            Log.e(TAG, "Google Sign-In setup failed", e);
            showToast("Google Sign-In setup failed. Check configuration.");
        }
    }

    private void setupClickListeners() {
        if (tvLoginButton != null) {
            tvLoginButton.setOnClickListener(v -> performManualLogin());
        }

        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> signInWithGoogle());
        }
    }

    private void performManualLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Input validation
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please enter both email and password");
            return;
        }

        if (password.length() < 6) {
            showToast("Password should be at least 6 characters");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address");
            return;
        }

        showToast("Logging in...");
        Log.d(TAG, "Attempting login for: " + email);

        // Firebase email/password authentication
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Login successful for: " + user.getEmail());
                            showToast("Welcome " + user.getEmail() + "!");
                            navigateToHome(
                                    user.getDisplayName() != null ? user.getDisplayName() : "User",
                                    user.getEmail(),
                                    user.getUid()
                            );
                        }
                    } else {
                        // Login failed, try to register new user
                        Log.w(TAG, "Login failed, attempting registration", task.getException());
                        showToast("Creating new account...");
                        registerNewUser(email, password);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Login error", e);
                    showToast("Login error: " + e.getMessage());
                });
    }

    private void registerNewUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration successful
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Registration successful for: " + user.getEmail());
                            showToast("Account created for " + user.getEmail() + "!");
                            navigateToHome(
                                    "User", // New users don't have display name yet
                                    user.getEmail(),
                                    user.getUid()
                            );
                        }
                    } else {
                        // Registration failed
                        Log.e(TAG, "Registration failed", task.getException());
                        String errorMessage = "Authentication failed";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                            // More user-friendly error messages
                            if (errorMessage.contains("email address is already")) {
                                errorMessage = "Email already registered. Please login instead.";
                            } else if (errorMessage.contains("password is invalid")) {
                                errorMessage = "Invalid password. Please try again.";
                            }
                        }
                        showToast(errorMessage);
                    }
                });
    }

    private void signInWithGoogle() {
        try {
            if (googleSignInClient == null) {
                showToast("Google Sign-In not configured properly");
                return;
            }

            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } catch (Exception e) {
            Log.e(TAG, "Google Sign-In error", e);
            showToast("Google Sign-In error: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                Log.d(TAG, "Google Sign-In successful: " + account.getEmail());
                firebaseAuthWithGoogle(account);
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google Sign-In failed: " + e.getStatusCode(), e);
            String errorMessage = "Google Sign-In failed";
            switch (e.getStatusCode()) {
                case 10:
                    errorMessage = "Development configuration issue. Check SHA-1 fingerprint.";
                    break;
                case 12501:
                    errorMessage = "Google Sign-In cancelled";
                    break;
                case 7:
                    errorMessage = "Network error. Check your internet connection.";
                    break;
            }
            showToast(errorMessage);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        try {
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "Firebase auth with Google successful: " + user.getEmail());
                                showToast("Welcome " + user.getDisplayName() + "!");
                                navigateToHome(
                                        user.getDisplayName(),
                                        user.getEmail(),
                                        user.getUid()
                                );
                            }
                        } else {
                            Log.e(TAG, "Firebase auth with Google failed", task.getException());
                            showToast("Authentication failed. Please try again.");
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Google authentication error", e);
            showToast("Google authentication error");
        }
    }

    private void navigateToHome(String userName, String userEmail, String userId) {
        try {
            // Simpan data user ke SharedPreferences
            saveUserData(userName, userEmail, userId);

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("user_name", userName);
            intent.putExtra("user_email", userEmail);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Navigation error", e);
            showToast("Navigation error");
        }
    }

    // Method baru untuk menyimpan data user
    private void saveUserData(String userName, String userEmail, String userId) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", userName);
        editor.putString("user_email", userEmail);
        editor.putString("user_id", userId);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
        Log.d(TAG, "User data saved: " + userName);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Cek apakah user sudah login
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            // Jika sudah login, langsung ke MainActivity2
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                Log.d(TAG, "User already logged in: " + currentUser.getEmail());
                navigateToHome(
                        currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User",
                        currentUser.getEmail(),
                        currentUser.getUid()
                );
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}