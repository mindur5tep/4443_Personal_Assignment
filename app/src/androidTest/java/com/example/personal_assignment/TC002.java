package com.example.personal_assignment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.personal_assignment.R;
import com.example.personal_assignment.Login;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TC002 {

    private static final String TEST_USERNAME = "jd2001";
    private static final String TEST_PASSWORD = "Passwd#1";
    private static final String TEST_FULL_NAME = "John Doe";
    private static final String TEST_BIRTHDATE = "2001-11-21";
    private static final String TEST_ADDRESS = "4700 Keele St, Toronto, ON, M3J 1P3";
    private static final String TEST_PHONE_NUMBER = "555-123-4567";


    @Rule
    public ActivityScenarioRule<Onboarding> activityScenarioRule = new ActivityScenarioRule<>(Onboarding.class);

    @Test
    public void testRegistration() {

        // Click on "Get Started" button
        onView(withId(R.id.getStartedButton)).perform(click());
        // Click on "Register" button
        onView(withId(R.id.signUpBut)).perform(click());

        // Fill in registration form
        onView(withId(R.id.etFullName)).perform(typeText(TEST_FULL_NAME), closeSoftKeyboard());
        onView(withId(R.id.etUsernameCreate)).perform(typeText(TEST_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.etPasswordCreate)).perform(typeText(TEST_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.etBirthDate)).perform(replaceText(TEST_BIRTHDATE));
        onView(withId(R.id.etPhoneNumber)).perform(typeText(TEST_PHONE_NUMBER), closeSoftKeyboard());
        onView(withId(R.id.etAddress)).perform(typeText(TEST_ADDRESS), closeSoftKeyboard());

        // Click "Continue" button to return to login screen
        onView(withId(R.id.continueSignupBut)).perform(click());

        // Verify that the app returns to the login screen
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }
}
