package com.example.personal_assignment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import android.content.Context;
import com.example.personal_assignment.database.ProfileDatabase;
import com.example.personal_assignment.database.User;
import com.example.personal_assignment.database.UserDao;
import com.example.personal_assignment.Login;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TC003 {

    private static final String TEST_USERNAME = "jd2001";
    private static final String TEST_PASSWORD = "Passwd#1";
    private static final String TEST_FULL_NAME = "John Doe";
    private static final String TEST_BIRTHDATE = "2001-11-21";
    private static final String TEST_ADDRESS = "4700 Keele St, Toronto, ON, M3J 1P3";
    private static final String TEST_PHONE_NUMBER = "555-123-4567";


    @Rule
    public ActivityScenarioRule<Onboarding> activityRule = new ActivityScenarioRule<>(Onboarding.class);

    @Before
    public void setUpDatabase() throws Exception {
        // Populate database with user information, and create new user
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ProfileDatabase db = ProfileDatabase.getDatabase(context);
        UserDao userDao = db.userDao();

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
    public void testLogin() {
        // Click on "Get Started" button
        onView(withId(R.id.getStartedButton)).perform(click());

        // Enter username
        onView(withId(R.id.etUsername)).perform(typeText(TEST_USERNAME), closeSoftKeyboard());

        // Enter password
        onView(withId(R.id.etPassword)).perform(typeText(TEST_PASSWORD), closeSoftKeyboard());

        // Click Login button
        onView(withId(R.id.loginButton)).perform(click());

        // Verify that the user is directed to the main page
        onView(withId(R.id.imageView1)).check(matches(isDisplayed()));


    }
}
