package com.example.personal_assignment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Context;

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
public class TC005 {
    private UserDao userDao;

    @Rule
    public ActivityScenarioRule<Onboarding> activityRule = new ActivityScenarioRule<>(Onboarding.class);

    private static final String TEST_USERNAME = "jd2001";
    private static final String TEST_PASSWORD = "Passwd#1";
    private static final String TEST_FULL_NAME = "John Doe";
    private static final String TEST_BIRTHDATE = "2001-11-21";
    private static final String TEST_ADDRESS = "4700 Keele St, North York, ON, M3J 1P3";
    private static final String TEST_PHONE_NUMBER = "555-123-4567";
    private static final String ORIGINAL_TITLE = "First note";
    private static final String ORIGINAL_CONTENT = "Today I ate cereal!";


    @Before
    public void setUpDatabase() throws Exception {
        // Populate database with user informations, and create new user
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ProfileDatabase db = ProfileDatabase.getDatabase(context);
        userDao = db.userDao();

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
    public void testAddDiaryEntry() {

        // Click on "Get Started" button
        onView(withId(R.id.getStartedButton)).perform(click());

        // Login
        onView(withId(R.id.etUsername)).perform(typeText(TEST_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText(TEST_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // Click the add diary Button
        onView(withId(R.id.fabAddEntry)).perform(click());

        // Verify that the user is in the "Diary" screen
        onView(withId(R.id.diaryTitle)).check(matches(isDisplayed()));

        // Enter diary title
        onView(withId(R.id.etDiaryTitle)).perform(typeText(ORIGINAL_TITLE), closeSoftKeyboard());

        // Enter diary content
        onView(withId(R.id.etDiaryContent)).perform(typeText(ORIGINAL_CONTENT), closeSoftKeyboard());

        // Click the save button
        onView(withId(R.id.saveDiaryButton)).perform(click());

        // Verify the diary appears in the diary list
        onView(withText(ORIGINAL_TITLE)).check(matches(isDisplayed()));
        onView(withText(ORIGINAL_CONTENT)).check(matches(isDisplayed()));

        // Ensure the diary list is visible
        onView(withId(R.id.recyclerViewEntries)).check(matches(isDisplayed()));
    }
}
