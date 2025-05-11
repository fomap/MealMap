package com.example.mealmap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;


public class UserProfileActivity extends AppCompatActivity {
    private TextView tvEmail;
    private RadioGroup measurementGroup, apiKeyGroup;
    private FirebaseUser currentUser;
    private String[] apiKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        apiKeys = getResources().getStringArray(R.array.api_keys);

        setupToolbar();
        initViews();
        loadUserData();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        tvEmail = findViewById(R.id.tv_email);
        measurementGroup = findViewById(R.id.radio_measurement);
        apiKeyGroup = findViewById(R.id.radio_api_keys);

        if (currentUser != null) {
            tvEmail.setText("Email: " + currentUser.getEmail());
        }
    }

    private void loadUserData() {
        boolean isMetric = PreferenceManager.isMetric(this);
        measurementGroup.check(isMetric ? R.id.radio_metric : R.id.radio_us);

        int selectedKeyIndex = getSharedPreferences("api_prefs", MODE_PRIVATE)
                .getInt("selected_key_index", 0);
        if (selectedKeyIndex < apiKeyGroup.getChildCount()) {
            ((RadioButton) apiKeyGroup.getChildAt(selectedKeyIndex)).setChecked(true);
        }
    }

    private void setupListeners() {
        //findViewById(R.id.btn_change_email).setOnClickListener(v -> showEmailDialog());
        findViewById(R.id.btn_change_password).setOnClickListener(v -> showPasswordDialog());

        measurementGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isMetric = checkedId == R.id.radio_metric;
            PreferenceManager.setMetric(UserProfileActivity.this, isMetric);
        });

        apiKeyGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedIndex = apiKeyGroup.indexOfChild(findViewById(checkedId));
            getSharedPreferences("api_prefs", MODE_PRIVATE).edit()
                    .putInt("selected_key_index", selectedIndex)
                    .apply();
        });

        findViewById(R.id.btn_save_keys).setOnClickListener(v -> {
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Email");

        View view = getLayoutInflater().inflate(R.layout.dialog_auth, null);
        EditText etCurrentPassword = view.findViewById(R.id.et_current_password);
        EditText etNewEmail = view.findViewById(R.id.et_new_value);

        etCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etNewEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etNewEmail.setHint("New email");

        builder.setView(view);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String currentPassword = etCurrentPassword.getText().toString();
            String newEmail = etNewEmail.getText().toString().trim();

            if (isValidEmail(newEmail)) {
                reAuthenticateAndUpdateEmail(currentPassword, newEmail);
            } else {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        View view = getLayoutInflater().inflate(R.layout.dialog_auth, null);
        EditText etCurrentPassword = view.findViewById(R.id.et_current_password);
        EditText etNewPassword = view.findViewById(R.id.et_new_value);

        etCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etNewPassword.setHint("New password (min 6 characters)");

        builder.setView(view);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString().trim();

            if (newPassword.length() >= 6) {
                reAuthenticateAndUpdatePassword(currentPassword, newPassword);
            } else {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void reAuthenticateAndUpdateEmail(String currentPassword, String newEmail) {
        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(this, "User or email is null", Toast.LENGTH_SHORT).show();
            return;
        }

        for (UserInfo profile : currentUser.getProviderData()) {
            Log.d("PROVIDER", "Provider: " + profile.getProviderId());
        }

        AuthCredential credential = EmailAuthProvider
                .getCredential(currentUser.getEmail(), currentPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("AUTH", "Re-authentication successful");
                        updateEmail(newEmail);
                    } else {
                        Exception e = task.getException();
                        Log.e("AUTH", "Re-authentication failed: " + (e != null ? e.getMessage() : "Unknown error"));
                        e.printStackTrace(); // See full stack trace in Logcat
                        Toast.makeText(this, "Re-auth failed: " + (e != null ? e.getMessage() : ""), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void reAuthenticateAndUpdatePassword(String currentPassword, String newPassword) {
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider
                .getCredential(currentUser.getEmail(), currentPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updatePassword(newPassword);
                    } else {
                        Toast.makeText(this, "Re-authentication failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateEmail(String newEmail) {
        currentUser.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("EMAIL", "Email update successful");
                        currentUser.sendEmailVerification();
                        tvEmail.setText("email;" + newEmail);
                        Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Exception e = task.getException();
                        Log.e("EMAIL", "Email update failed: " + (e != null ? e.getMessage() : "Unknown error"));
                        e.printStackTrace(); // For detailed debugging
                        Toast.makeText(this, "Update failed: " + (e != null ? e.getMessage() : ""), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updatePassword(String newPassword) {
        if (currentUser == null) return;

        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public static String getCurrentApiKey(Context context) {
        SharedPreferences apiPrefs = context.getSharedPreferences("api_prefs", MODE_PRIVATE);
        int index = apiPrefs.getInt("selected_key_index", 0);
        return context.getResources().getStringArray(R.array.api_keys)[index];
    }
}