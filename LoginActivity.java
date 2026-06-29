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

public class LoginActivity extends AppCompatActivity {

    EditText emailInput;
    EditText passwordInput;
    Button loginButton;
    TextView signupRedirect;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailEditText);
        passwordInput = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupRedirect = findViewById(R.id.signupRedirect);

        signupRedirect.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        loginButton.setOnClickListener(v -> {

            String email = emailInput != null ? emailInput.getText().toString().trim() : "";
            String password = passwordInput != null ? passwordInput.getText().toString().trim() : "";

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            loginButton.setEnabled(false);

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        loginButton.setEnabled(true);
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}
