package com.example.personal_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.personal_assignment.database.ProfileDatabase;
import com.example.personal_assignment.database.User;
import com.example.personal_assignment.database.UserDao;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUp extends AppCompatActivity {

    private TextInputEditText etFullName, etUsernameCreate, etPasswordCreate, etBirthDate;
    private TextInputLayout fullNameLayout, usernameLayout, passwordLayout, birthDateLayout;
    private Button continueButton, uploadImageButton;
    private ImageButton backButton;
    private ImageView profilePictureLayout;
    private ProfileDatabase database;
    private UserDao userDao;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String profilePictureUri = null;

    private static final String KEY_ALIAS = "PersonalAssignmentEncryptionKey"; // Name of key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // Initialize database and UserDAO
        database = ProfileDatabase.getDatabase(getApplicationContext());
        userDao = database.userDao();
        
        backButton = findViewById(R.id.backButton);
        uploadImageButton = findViewById(R.id.uploadImageBut);
        profilePictureLayout = findViewById(R.id.profilePictureLayout);
        etFullName = findViewById(R.id.etFullName);
        etUsernameCreate = findViewById(R.id.etUsernameCreate);
        etPasswordCreate = findViewById(R.id.etPasswordCreate);
        etBirthDate = findViewById(R.id.etBirthDate);
        continueButton = findViewById(R.id.continueSignupBut);

        fullNameLayout = findViewById(R.id.fullNameLayout);
        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        birthDateLayout = findViewById(R.id.birthdateLayout);

        backButton.setOnClickListener(v -> finish());
        uploadImageButton.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));

        etBirthDate.setOnClickListener(v -> showDatePickerDialog());
        continueButton.setOnClickListener(v -> registerUser());

        // Apply window insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 8);
            return insets;
        });
    }

    // Register a photo picker activity launcher
    // https://developer.android.com/training/data-storage/shared/photopicker
    private final ActivityResultLauncher<String[]> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                    profilePictureUri = uri.toString();
                    profilePictureLayout.setImageURI(uri);
                }
            });
    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsernameCreate.getText().toString().trim();
        String password = etPasswordCreate.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();

        // Make sure non of the fields are empty
        boolean hasError = false;

        if (fullName.isEmpty()) {
            fullNameLayout.setError(getString(R.string.error_required));
            hasError = true;
        } else {
            fullNameLayout.setError(null);
        }

        if (username.isEmpty()) {
            usernameLayout.setError(getString(R.string.error_required));
            hasError = true;
        } else {
            usernameLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordLayout.setError(getString(R.string.error_required));
            passwordLayout.setErrorIconDrawable(null);
            hasError = true;
        } else {
            passwordLayout.setError(null);
        }

        if (birthDate.isEmpty()) {
            birthDateLayout.setError(getString(R.string.error_required));
            hasError = true;
        } else {
            birthDateLayout.setError(null);
        }

        if (hasError) {
            return;
        }

        // Password combination check
        if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=.]).{8,}$")) {
            passwordLayout.setError(getString(R.string.error_password));
            hasError = true;
        } else {
            passwordLayout.setError(null);
        }


        String encryptedPassword;
        try {
            encryptedPassword = AesHelper.encrypt(password);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        if (hasError) {
            return;
        }

        // Perform registration in background thread
        executorService.execute(() -> {
            // Check if username already exists
            User existingUser = userDao.getUserByUsername(username);

            runOnUiThread(() -> {
                if (existingUser != null) {
                    usernameLayout.setError(getString(R.string.error_username_exists));
                    return;
                } else {
                    usernameLayout.setError(null);
                    // Create a new user with numeric uid
                    User newUser = new User();
                    newUser.setProfilePic(profilePictureUri);
                    newUser.setUsername(username);
                    newUser.setFullName(fullName);
                    newUser.setPassword(encryptedPassword);
                    newUser.setBirthDate(birthDate);

                    executorService.execute(() -> {
                        // Insert user
                        long rowId = userDao.addUser(newUser); // This inserts the user
                        int insertedUid = (int) rowId;         // Convert to int if needed

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Profile created.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUp.this, FoodPreference.class);
                            intent.putExtra("uid", insertedUid);
                            startActivity(intent);
                        });
                    });
                }
            });
        });
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
