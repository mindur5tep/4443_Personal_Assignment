package com.example.personal_assignment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
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

import com.example.personal_assignment.database.ProfileDatabase;
import com.example.personal_assignment.database.User;
import com.example.personal_assignment.database.UserDao;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TC004 {
    private UserDao userDao;

    private static final String TEST_USERNAME = "jd2001";
    private static final String TEST_PASSWORD = "Passwd#1";
    private static final String TEST_FULL_NAME = "John Doe";
    private static final String TEST_BIRTHDATE = "2001-11-21";
    private static final String TEST_ADDRESS = "4700 Keele St, Toronto, ON, M3J 1P3";
    private static final String TEST_UPDATED_ADDRESS = "4700 Keele St, North York, ON, M3J 1P3";
    private static final String TEST_PHONE_NUMBER = "555-123-4567";


    @Rule
    public ActivityScenarioRule<Onboarding> activityRule =
            new ActivityScenarioRule<>(Onboarding.class);

    @Before
    public void insertUser() throws Exception {
        // Populate database with user informations, and create new user
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ProfileDatabase db = ProfileDatabase.getDatabase(context);
        userDao = db.userDao();

        // Ensure user exists
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
    }

    @Test
    public void testEditAddress() {

        // Click on "Get Started" button
        onView(withId(R.id.getStartedButton)).perform(click());

        // Log in
        onView(withId(R.id.etUsername)).perform(typeText(TEST_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText(TEST_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // Go to Profile page by clicking the profile button in the bottom navbar
        onView(withId(R.id.nav_account)).perform(click());

        // Click "Profile Settings"
        onView(withId(R.id.linearLayoutProfileSettings)).perform(click());

        // Click address
        onView(withId(R.id.layoutAddress)).perform(click());

        // Change the address text
        onView(withId(R.id.etAddress))
                .perform(clearText(), typeText(TEST_UPDATED_ADDRESS), closeSoftKeyboard());

        // Save changes
        onView(withId(R.id.btnSave)).perform(click());

        // Click address
        onView(withId(R.id.layoutAddress)).perform(click());

        // Validate if the address has been updated
        onView(withId(R.id.etAddress)).check(matches(withText(TEST_UPDATED_ADDRESS)));

    }
}
