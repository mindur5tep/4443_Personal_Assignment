package com.example.personal_assignment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_assignment.database.DiaryDao;
import com.example.personal_assignment.database.DiaryEntry;
import com.example.personal_assignment.database.ProfileDatabase;
import com.example.personal_assignment.database.User;
import com.example.personal_assignment.database.UserDao;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private FloatingActionButton addEntry;
    private RecyclerView recyclerViewEntries;
    private DiaryAdapter diaryAdapter;
    private LinearLayout emptyStateContainer; // Empty state illustration

    // Background DB operations
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ImageView profilePic;
    private TextView headerName;
    private int userId = -1;//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        profilePic = findViewById(R.id.profilePicture);
        // Floating Action Button ID
        addEntry = findViewById(R.id.fabAddEntry);
        headerName = findViewById(R.id.main_header);

        // Get userID passed from the login activity
        userId = getIntent().getIntExtra("uid", -1);

        executorService.execute(() -> {
            UserDao userDao = ProfileDatabase.getDatabase(getApplicationContext()).userDao();
            String fullName = userDao.getUserFullNameByUid(userId); // Method to get full name by UID
            String profilePicSrc = userDao.getUserProfilePictureByUid(userId);
            runOnUiThread(() -> {
                if (fullName != null && !fullName.isEmpty()) {
                    headerName.setText(getString(R.string.greeting_message, fullName));
                }
                if (profilePicSrc != null){
                    Uri uriImage = Uri.parse(profilePicSrc);
                    profilePic.setImageURI(uriImage);
                }
            });
        });


        // Floating Action Button to add new Diary entry
        addEntry.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Diary.class);
            intent.putExtra("uid", userId); // Pass uid to Diary page
            startActivity(intent);
        });

        // Bottom Navigation
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            // Detect if the home or the profile settings button is selected
            if (itemId == R.id.nav_journal) {
                // Already in MainActivity, do nothing
                return true;
            } else if (itemId == R.id.nav_account) {
                // Go to Profile page
                Intent intent = new Intent(MainActivity.this, AccountProfile.class);
                // Pass uid to Profile Settings activity
                intent.putExtra("uid", userId);
                startActivity(intent);
                return true;
            } else {
                return false;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize empty state & RecyclerView for illustrations and diary entry
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        recyclerViewEntries = findViewById(R.id.recyclerViewEntries);
        recyclerViewEntries.setLayoutManager(new LinearLayoutManager(this));

        // Load diary entries
        loadEntriesFromDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEntriesFromDatabase();
    }

    private void loadEntriesFromDatabase() {
        executorService.execute(() -> {
            // Retrieve diary entries for the logged-in user
            DiaryDao diaryDao = ProfileDatabase.getDatabase(getApplicationContext()).diaryDao();
            List<DiaryEntry> userEntries = diaryDao.getUserDiariesByUid(userId);

            runOnUiThread(() -> {
                // If no diary entries if found, show illustrations, else show the entry
                if (userEntries.isEmpty()) {
                    emptyStateContainer.setVisibility(View.VISIBLE);
                    recyclerViewEntries.setVisibility(View.GONE);
                } else {
                    emptyStateContainer.setVisibility(View.GONE);
                    recyclerViewEntries.setVisibility(View.VISIBLE);

                    // Prevent multiple adapters from being recreated
                    if (diaryAdapter == null) {
                        diaryAdapter = new DiaryAdapter(MainActivity.this, userEntries);
                        recyclerViewEntries.setAdapter(diaryAdapter);
                    } else {
                        diaryAdapter.updateEntries(userEntries);
                    }
                }
            });
        });
    }
}
