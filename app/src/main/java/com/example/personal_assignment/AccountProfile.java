package com.example.personal_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountProfile extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;
    private LinearLayout profileSettings, logout;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_profile);

        // Get intent data (userId)
        userId = getIntent().getIntExtra("uid", -1);

        // Initialize UI elements
        profileSettings = findViewById(R.id.linearLayoutProfileSettings);

        // Go to account information settings (username, full name, etc)
        profileSettings.setOnClickListener(v -> {
            Intent intent = new Intent(AccountProfile.this, ProfileSettingsActivity.class);
            intent.putExtra("uid", userId);
            startActivity(intent);
        });

        // Logout function
        logout = findViewById(R.id.linearLayoutLogout);
        logout.setOnClickListener(v ->{
            showLogoutDialog();
        });

        // Bottom navigation
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_account);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_journal) {
                // Go to Profile page
                Intent intent = new Intent(AccountProfile.this, MainActivity.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_account) {
                // Already in AccountProfile, do nothing or refresh
                return true;
            } else {
                return false;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    private void showLogoutDialog() {
        // Confirmation dialog before user's log out
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to log out?");

        builder.setPositiveButton("Log Out", (dialog, which) -> {
            Toast.makeText(AccountProfile.this, "Logging out...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AccountProfile.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // If user clicks "No", dismiss dialog
        builder.setNegativeButton("Back", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.create().show();
    }
}