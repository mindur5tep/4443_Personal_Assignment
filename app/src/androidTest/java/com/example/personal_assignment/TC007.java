package com.example.personal_assignment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.os.SystemClock;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.personal_assignment.database.DiaryDao;
import com.example.personal_assignment.database.DiaryEntry;
import com.example.personal_assignment.database.ProfileDatabase;
import com.example.personal_assignment.database.User;
import com.example.personal_assignment.database.UserDao;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class TC007 {

    private ProfileDatabase testDatabase;
    private UserDao userDao;
    private DiaryDao diaryDao;

    // Test user credentials
    private static final String TEST_USERNAME = "jd2001";
    private static final String TEST_PASSWORD = "Passwd#1";

    private static final String TEST_FULL_NAME = "John Doe";
    private static final String TEST_BIRTHDATE = "2001-11-21";
    private static final String TEST_ADDRESS = "4700 Keele St, North York, ON, M3J 1P3";
    private static final String TEST_PHONE_NUMBER = "555-123-4567";

    private static final String UPDATED_TITLE = "Breakfast";
    private static final String UPDATED_CONTENT = "Today I ate cereal! It was good!";

    @Rule
    public ActivityScenarioRule<Onboarding> activityRule =
            new ActivityScenarioRule<>(Onboarding.class);

    @Before
    public void setUpDatabase() throws Exception {
        // Populate database with user informations, and create new user
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ProfileDatabase db = ProfileDatabase.getDatabase(context);
        UserDao userDao = db.userDao();
        diaryDao = db.diaryDao();

        User existingUser = userDao.getUserByUsername("jd2001");

        if (existingUser == null) {
            User testUser = new User();
            testUser.setUsername(TEST_USERNAME);
            testUser.setFullName(TEST_FULL_NAME);
            testUser.setPassword(AesHelper.encrypt(TEST_PASSWORD));
            testUser.setBirthDate(TEST_BIRTHDATE);
            testUser.setAddress(TEST_ADDRESS);
            testUser.setPhoneNumber(TEST_PHONE_NUMBER);
            userDao.addUser(testUser);
        }

        int userId = userDao.getUserByUsername(TEST_USERNAME).getUid();
        List<DiaryEntry> entries = diaryDao.getUserDiariesByUid(userId);
        if (entries.isEmpty()) {
            // Insert diary entry
            DiaryEntry entry = new DiaryEntry(userId, UPDATED_TITLE, UPDATED_CONTENT);
            diaryDao.insertEntry(entry);
        }
    }

    @Test
    public void testLogout() {
        // Click on "Get Started" button
        onView(withId(R.id.getStartedButton)).perform(click());

        // Log in with test credentials
        onView(withId(R.id.etUsername)).perform(typeText(TEST_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText(TEST_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // Verify diary entry
        onView(withId(R.id.recyclerViewEntries)).check(matches(hasDescendant(withText(UPDATED_TITLE))))
                .check(matches(hasDescendant(withText(UPDATED_CONTENT))));

        // Navigate to account/profile page
        onView(withId(R.id.nav_account)).perform(click());

        //  Click the 'Logout' layout or button
        onView(withId(R.id.linearLayoutLogout)).perform(click());

        // Click log out button in the confirmation dialog
        onView(withText("Log Out")).perform(click());

        // Verify if user get directed to login page
        onView(withId(R.id.etUsername)).check(matches(isDisplayed()));
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()));

    }
}
