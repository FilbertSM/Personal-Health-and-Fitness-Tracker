package com.example.personalhealthandfitnesstracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextUtils;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {

    private Button loginButton, signUpButton;
    private EditText txtEmail, txtPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views // Initialize Views
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        auth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = txtEmail.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    txtEmail.setError("Please enter an email address.");
                    return;
                } else if (TextUtils.isEmpty(password)){
                    txtPassword.setError("Please enter a password.");
                    return;
                } else {
                    loginUser(email, password);
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = txtEmail.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    txtEmail.setError("Please enter an email address.");
                    return;
                } else if (TextUtils.isEmpty(password)){
                    txtPassword.setError("Please enter a password.");
                    return;
                } else {
                    signUpUser(email, password);
                }
            }
        });
    }
    private void signUpUser(String email, String password){
        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Please enter a valid email address.");
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            txtPassword.setError("Password must be at least 6 characters.");
            return;
        }

        // Proceed with Firebase sign-up
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AuthActivity.this, "Registration Successful.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AuthActivity.this, "Registration Failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loginUser(String email, String password){
        // Proceed with Firebase login
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        Toast.makeText(AuthActivity.this, "Welcome, " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        // Navigate to DashboardActivity
                         Intent intent = new Intent(AuthActivity.this, DashboardActivity.class);
                         startActivity(intent);
                         finish();
                    }
                } else {
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed.";
                    Toast.makeText(AuthActivity.this, "Incorrect email or password: ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}