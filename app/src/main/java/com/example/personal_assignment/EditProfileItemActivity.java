package com.example.personal_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personal_assignment.database.ProfileDatabase;
import com.example.personal_assignment.database.User;
import com.example.personal_assignment.database.UserDao;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProfileItemActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private TextInputEditText etUsername, etPassword, etFullName, etPhone, etAddress;
    private TextInputLayout usernameLayout, passwordLayout, fullNameLayout, phoneLayout,
            addressLayout;
    private Button btnSave;
    private String fieldKey, fieldLabel;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private int userId;
    private User currentUser;
    boolean requiresRelogin;  // Tracks if user must re-login after changes

    // List of valid provinces
    private static final List<String> CANADIAN_PROVINCES = Arrays.asList(
            "AB", "BC", "MB", "NB", "NL", "NS", "ON", "PE", "QC", "SK",
            "NT", "NU", "YT"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_item);

        // Initialize UI elements
        topAppBar = findViewById(R.id.topAppBar);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        //Add a watcher to the Phone Number input to format it into XXX-XXX-XXXX
        etPhone.addTextChangedListener(new PhoneNumberWatcher(etPhone));
        etAddress = findViewById(R.id.etAddress);
        btnSave = findViewById(R.id.btnSave);

        // Initialize UI layout (parent elements)
        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        fullNameLayout = findViewById(R.id.fullNameLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        addressLayout = findViewById(R.id.addressLayout);

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Get intent data
        userId = getIntent().getIntExtra("uid", -1);
        fieldKey = getIntent().getStringExtra("fieldKey");
        fieldLabel = getIntent().getStringExtra("fieldLabel");

        // Setup toolbar
        topAppBar.setNavigationOnClickListener(v -> finish());
        topAppBar.setTitle("Edit " + (fieldLabel != null ? fieldLabel : "Field"));

        // Load user data
        loadFieldData();

        // Save button click listener
        btnSave.setOnClickListener(v -> saveFieldValue());
    }

    private void loadFieldData() {
        executor.execute(() -> {
            UserDao userDao = ProfileDatabase.getDatabase(getApplicationContext()).userDao();
            currentUser = userDao.getUserByUid(userId);

            if (currentUser == null) {
                runOnUiThread(this::finish);
                return;
            }

            runOnUiThread(() -> {
                // Hide all layouts first
                usernameLayout.setVisibility(View.GONE);
                passwordLayout.setVisibility(View.GONE);
                fullNameLayout.setVisibility(View.GONE);
                phoneLayout.setVisibility(View.GONE);
                addressLayout.setVisibility(View.GONE);

                // Shows which text field to be shown in the activity,
                // and set the text of the text field with the initial user information.
                switch (fieldKey) {
                    case "username":
                        etUsername.setText(currentUser.getUsername());
                        usernameLayout.setVisibility(View.VISIBLE);
                        break;
                    case "password":
                        passwordLayout.setVisibility(View.VISIBLE);
                        break;
                    case "fullName":
                        etFullName.setText(currentUser.getFullName());
                        fullNameLayout.setVisibility(View.VISIBLE);
                        break;
                    case "phoneNumber":
                        etPhone.setText(currentUser.getPhoneNumber());
                        phoneLayout.setVisibility(View.VISIBLE);
                        break;
                    case "address":
                        etAddress.setText(currentUser.getAddress());
                        addressLayout.setVisibility(View.VISIBLE);
                        break;
                }
            });
        });
    }

    private void saveFieldValue() {
        UserDao userDao = ProfileDatabase.getDatabase(getApplicationContext()).userDao();

        executor.execute(() -> {
            // Check if the information has been updated
            boolean hasChanges = false;

            switch (fieldKey) {
                case "username": {
                    String newUsername = etUsername.getText().toString().trim();
                    // Clear previous errors
                    runOnUiThread(() -> usernameLayout.setError(null));

                    if (newUsername.isEmpty()) {
                        runOnUiThread(() -> usernameLayout.setError(getString(R.string.error_required)));
                        return;
                    }

                    if (!newUsername.equals(currentUser.getUsername())) {
                        // Check if there's existing user who used the same new username
                        User existingUser = userDao.getUserByUsername(newUsername);
                        if (existingUser != null && existingUser.getUid() != currentUser.getUid()) {
                            runOnUiThread(() -> usernameLayout.setError(getString(R.string.error_username_exists)));
                            return; // Stop, do not update
                        }
                        // Otherwise update current user's username
                        currentUser.setUsername(newUsername);
                        requiresRelogin = true; // must re-login if username changes
                        hasChanges = true;
                    }
                    break;
                }
                case "password": {
                    String newPassword = etPassword.getText().toString().trim();
                    // Clear previous errors
                    runOnUiThread(() -> passwordLayout.setError(null));

                    if (newPassword.isEmpty()) {
                        runOnUiThread(() -> passwordLayout.setError(getString(R.string.error_required)));
                        return;
                    }else {
                        // Check the new password format
                        if (!newPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=.]).{8,}$")) {
                            runOnUiThread(() -> passwordLayout.setError(getString(R.string.error_password)));
                            return; // Stop
                        } else {
                            // Encrypt new password
                            String encryptedPassword;
                            try {
                                encryptedPassword = AesHelper.encrypt(newPassword);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                            currentUser.setPassword(encryptedPassword);
                            requiresRelogin = true;
                            hasChanges = true;
                        }
                    }
                    break;
                }
                case "fullName": {
                    String newFullName = etFullName.getText().toString().trim();
                    // Clear errors
                    runOnUiThread(() -> fullNameLayout.setError(null));

                    if (newFullName.isEmpty()) {
                        runOnUiThread(() -> fullNameLayout.setError(getString(R.string.error_required)));
                        return;
                    }else if (!newFullName.equals(currentUser.getFullName())) {
                        currentUser.setFullName(newFullName);
                        hasChanges = true;
                    }
                    break;
                }
                case "phoneNumber": {
                    String newPhone = etPhone.getText().toString().trim();
                    // Clear errors
                    runOnUiThread(() -> phoneLayout.setError(null));

                    if (newPhone.isEmpty()) {
                        runOnUiThread(() -> phoneLayout.setError(getString(R.string.error_required)));
                        return;
                    }else {
                        if (newPhone.length() != 12) {
                            // Invalid phone number format
                            runOnUiThread(() -> phoneLayout.setError(getString(R.string.phone_number)));
                            return; // Stop
                        } else {
                            // Valid phone
                            if (!newPhone.equals(currentUser.getPhoneNumber())) {
                                currentUser.setPhoneNumber(newPhone);
                                hasChanges = true;
                            }
                        }
                    }
                    break;
                }
                case "address": {
                    String newAddress = etAddress.getText().toString().trim();

                    boolean hasError = false;

                    runOnUiThread(() -> {
                        addressLayout.setError(null);
                    });

                    // Check empty fields
                    if (newAddress.isEmpty()) {
                        runOnUiThread(() -> addressLayout.setError(getString(R.string.error_required)));
                        hasError = true;
                    }
                    // If any error was flagged, stop
                    if (hasError) {
                        return;
                    }

                    // Update fields if changed
                    if (!newAddress.equals(currentUser.getAddress())) {
                        currentUser.setAddress(newAddress);
                        hasChanges = true;
                    }
                    break;
                }


            }

            // Update all valid information if changes were made
            if (hasChanges) {
                userDao.updateUser(currentUser);
            }

            // If the changes require reLogin, user will be brought back to the login page.
            runOnUiThread(() -> {
                if (requiresRelogin) {
                    Toast.makeText(EditProfileItemActivity.this,
                            "Username or password changed. Please log in again.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditProfileItemActivity.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(EditProfileItemActivity.this,
                            "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
