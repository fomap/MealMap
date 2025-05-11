package com.example.mealmap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;


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

        findViewById(R.id.btn_change_email).setOnClickListener(v -> showEmailDialog());
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

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setText(currentUser.getEmail());
        builder.setView(input);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String newEmail = input.getText().toString().trim();
            if (isValidEmail(newEmail)) {
                updateEmail(newEmail);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateEmail(String newEmail) {
        currentUser.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tvEmail.setText("Email: " + newEmail);
                        Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String newPassword = input.getText().toString().trim();
            if (newPassword.length() >= 6) {
                updatePassword(newPassword);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updatePassword(String newPassword) {
        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
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