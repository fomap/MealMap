package com.example.mealmap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EntryPointActivity extends AppCompatActivity {


    Button emailSignInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_entry_point);


        emailSignInBtn = findViewById(R.id.btn_email);
        emailSignInBtn.setOnClickListener(v -> {
            startActivity(new Intent(EntryPointActivity.this, SignUpActivity.class));
        });

    }
}