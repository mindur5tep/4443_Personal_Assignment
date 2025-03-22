package com.example.personal_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personal_assignment.database.ProfileDatabase;
import com.example.personal_assignment.database.User;
import com.example.personal_assignment.database.UserDao;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Login extends AppCompatActivity {
    private Button signUp, login;
    private ProfileDatabase userDB;
    private TextInputEditText etUsername, etPassword;
    private TextInputLayout usernameLayout, passwordLayout;
    private ExecutorService executorService = Executors.newSingleThreadExecutor(); // For background DB tasks

    private String decryptedPassword;
    boolean hasError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        signUp = findViewById(R.id.signUpBut);
        login = findViewById(R.id.loginButton);

        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);

        // Initialize Room Database
        userDB = ProfileDatabase.getDatabase(getApplicationContext());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        // Sign-up button navigates to SignUp activity
        signUp.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, SignUp.class);
            startActivity(intent);
        });

        // Login button listener + logic
        login.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        hasError = false;
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            usernameLayout.setError(getString(R.string.error_need_both_inputs));
            hasError = true;
        } else {
            usernameLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordLayout.setError(getString(R.string.error_need_both_inputs));
            hasError = true;
        } else {
            passwordLayout.setError(null);
        }

        if (hasError) {
            return;
        }

        executorService.execute(() -> {
            UserDao userDao = userDB.userDao();
            User user = userDao.getUserByUsername(username);

            runOnUiThread(() -> {
                if (user == null) {
                    usernameLayout.setError(getString(R.string.error_invalid_credentials));
                    passwordLayout.setError(getString(R.string.error_invalid_credentials));
                    hasError = true;
                } else {
                    String encryptedPassword = user.getPassword();
                    try {
                        decryptedPassword = AesHelper.decrypt(encryptedPassword);
                        if (decryptedPassword.equals(password)) {
                            Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            intent.putExtra("uid", user.getUid());
                            startActivity(intent);
                            finish();
                        } else {
                            usernameLayout.setError(getString(R.string.error_invalid_credentials));
                            passwordLayout.setError(getString(R.string.error_invalid_credentials));
                            hasError = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (hasError) {
                    return;
                }
            });
        });
    }
}
