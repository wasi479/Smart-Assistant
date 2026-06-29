package com.example.smartassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    EditText nameInput;
    EditText emailInput;
    EditText passwordInput;
    Button createAccountButton;
    TextView loginRedirectText;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameInput = findViewById(R.id.nameEditText);
        emailInput = findViewById(R.id.emailEditText);
        passwordInput = findViewById(R.id.passwordEditText);
        createAccountButton = findViewById(R.id.createAccountButton);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        loginRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        });

        createAccountButton.setOnClickListener(v -> {

            String name = nameInput != null ? nameInput.getText().toString().trim() : "";
            String email = emailInput != null ? emailInput.getText().toString().trim() : "";
            String password = passwordInput != null ? passwordInput.getText().toString().trim() : "";

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password) || password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            createAccountButton.setEnabled(false);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {

                        String uid = result.getUser().getUid();

                        Map<String, Object> profile = new HashMap<>();
                        profile.put("fullName", TextUtils.isEmpty(name) ? "User" : name);
                        profile.put("email", email);
                        profile.put("createdAt", System.currentTimeMillis());

                        db.collection("users")
                                .document(uid)
                                .set(profile)
                                .addOnCompleteListener(task -> {

                                    auth.signOut();

                                    Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                });
                    })
                    .addOnFailureListener(e -> {
                        createAccountButton.setEnabled(true);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}
