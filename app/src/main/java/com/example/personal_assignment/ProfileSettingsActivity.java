package com.example.personal_assignment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personal_assignment.database.ProfileDatabase;
import com.example.personal_assignment.database.User;
import com.example.personal_assignment.database.UserDao;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileSettingsActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private ExecutorService executor;
    private LinearLayout layoutUsername, layoutPassword, layoutFullName, layoutAddress, layoutPhone, layoutDeleteAccount;
    private int userId = -1;
    private ImageView profilePicLayout;
    private Button uploadImage;

    private TextInputLayout passwordConfirmationLayout;
    boolean hasError = false;
    private String newProfilePictureUri = null;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize ExecutorService
        executor = Executors.newSingleThreadExecutor();

        profilePicLayout = findViewById(R.id.profilePictureLayout);
        uploadImage = findViewById(R.id.uploadImageBut);

        // Initialize UI elements
        topAppBar = findViewById(R.id.topAppBar);
        layoutUsername = findViewById(R.id.layoutUsername);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutFullName = findViewById(R.id.layoutFullName);
        layoutAddress = findViewById(R.id.layoutAddress);
        layoutPhone = findViewById(R.id.layoutPhone);
        layoutDeleteAccount = findViewById(R.id.layoutDeleteAccount);

        // Get userId from Intent
        userId = getIntent().getIntExtra("uid", -1);

        executor.execute(() -> {
            userDao = ProfileDatabase.getDatabase(getApplicationContext()).userDao();
            String profilePicSrc = userDao.getUserProfilePictureByUid(userId);
            if (profilePicSrc != null) {
                runOnUiThread(() -> {
                    Uri uriImage = Uri.parse(profilePicSrc);
                    profilePicLayout.setImageURI(uriImage);
                });
            }
        });

        uploadImage.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));

        layoutDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());


        // Setup top app bar with back button
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (ProfileSettingsActivity.this, MainActivity.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                finish();
            }
        });

        // Set click listeners to open the edit page
        setupClickListeners();
    }

    // Register a photo picker activity launcher
    private final ActivityResultLauncher<String[]> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                    // Update new profile picture Uri
                    newProfilePictureUri = uri.toString();
                    profilePicLayout.setImageURI(uri);
                    executor.execute(() -> {
                        // Update user database
                        User currentUser = userDao.getUserByUid(userId);
                        currentUser.setProfilePic(newProfilePictureUri);
                        userDao.updateUser(currentUser);
                    });
                }
            });
    private void showDeleteConfirmationDialog() {
        // Prompt confirmation dialog when user choose to delete account
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action is irreversible.")
                .setPositiveButton("Delete", (dialog, which) -> promptPasswordBeforeDeletion())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void promptPasswordBeforeDeletion() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_password_input, null);

        passwordConfirmationLayout = dialogView.findViewById(R.id.editTextPasswordLayout);
        TextInputEditText etDialogPassword = dialogView.findViewById(R.id.editTextPassword);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Password")
                .setView(dialogView)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Confirm", null);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // If user clicked confirm
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String enteredPassword = etDialogPassword.getText().toString().trim();
            if (enteredPassword.isEmpty()) {
                // Show error on the TextInputLayout
                passwordConfirmationLayout.setError(getString(R.string.error_required));
            } else {
                // Clear error and proceed
                passwordConfirmationLayout.setError(null);
                verifyPasswordAndDelete(enteredPassword);
            }
        });
    }

    private void verifyPasswordAndDelete(String enteredPassword) {
        executor.execute(() -> {
            String encryptedPassword = userDao.getUserPasswordByUid(userId);

            try {
                // Decrypt current user password
                String decryptedPassword = AesHelper.decrypt(encryptedPassword);

                // Check if the user-typed password matches the decrypted password
                if (decryptedPassword.equals(enteredPassword)) {
                    // If correct, delete the user
                    userDao.deleteUserById(userId);
                    runOnUiThread(() -> {
                        passwordConfirmationLayout.setError(null);
                        Toast.makeText(this, "Account deleted successfully!", Toast.LENGTH_SHORT).show();
                        // Go back to login page
                        Intent intent = new Intent(this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    // If password is incorrect
                    runOnUiThread(() -> {
                        passwordConfirmationLayout.setError("Incorrect password!");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                // If there's an error decrypting
                runOnUiThread(() ->
                        Snackbar.make(findViewById(android.R.id.content), "Decryption error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void setupClickListeners() {
        // Pass fieldKey and fieldLabel to identify which settings clicked by the user
        layoutUsername.setOnClickListener(v -> openEditPage("username", "Username"));
        layoutPassword.setOnClickListener(v -> openEditPage("password", "Password"));
        layoutFullName.setOnClickListener(v -> openEditPage("fullName", "Full Name"));
        layoutAddress.setOnClickListener(v -> openEditPage("address", "Address"));
        layoutPhone.setOnClickListener(v -> openEditPage("phoneNumber", "Phone Number"));
    }

    private void openEditPage(String fieldKey, String fieldLabel) {
        Intent intent = new Intent(this, EditProfileItemActivity.class);
        // pass userId, fieldKey and fieldLabel value
        intent.putExtra("uid", userId);
        intent.putExtra("fieldKey", fieldKey);
        intent.putExtra("fieldLabel", fieldLabel);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
