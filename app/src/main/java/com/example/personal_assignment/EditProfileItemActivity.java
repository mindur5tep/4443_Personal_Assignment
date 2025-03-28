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
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProfileItemActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private TextInputEditText etUsername, etPassword, etFullName, etBirthDate;
    private TextInputLayout usernameLayout, passwordLayout, fullNameLayout, birthDateLayout;
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
        etBirthDate = findViewById(R.id.etBirthDate);

        btnSave = findViewById(R.id.btnSave);

        // Initialize UI layout (parent elements)
        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        fullNameLayout = findViewById(R.id.fullNameLayout);
        birthDateLayout = findViewById(R.id.birthdateLayout);

        etBirthDate.setOnClickListener(v -> showDatePickerDialog());

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
                birthDateLayout.setVisibility(View.GONE);

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
                    case "birthDate":
                        etBirthDate.setText(currentUser.getBirthDate());
                        birthDateLayout.setVisibility(View.VISIBLE);
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

                case "birthDate": {
                    String dateOfBirth = etBirthDate.getText().toString().trim();

                    if (dateOfBirth.isEmpty()) {
                        runOnUiThread(() -> fullNameLayout.setError(getString(R.string.error_required)));
                        return;
                    }else {
                        currentUser.setBirthDate(dateOfBirth);
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

    private void showDatePickerDialog() {
        // Method for showing calendar for birth date
        long today = MaterialDatePicker.todayInUtcMilliseconds();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(today);
        cal.add(Calendar.YEAR, -100);
        long startRange = cal.getTimeInMillis();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setStart(startRange)
                .setEnd(today)
                .setValidator(DateValidatorPointBackward.now());

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(today)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            utc.setTimeInMillis(selection);
            int year = utc.get(Calendar.YEAR);
            int month = utc.get(Calendar.MONTH);
            int day = utc.get(Calendar.DAY_OF_MONTH);

            Calendar localCal = Calendar.getInstance();
            localCal.clear();
            localCal.set(year, month, day);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etBirthDate.setText(sdf.format(localCal.getTime()));
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }
}
