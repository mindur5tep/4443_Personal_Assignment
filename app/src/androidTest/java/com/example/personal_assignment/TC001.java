package com.example.personal_assignment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.personal_assignment.R;
import com.example.personal_assignment.Login;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TC001 {

    private static final String TEST_USERNAME = "jd2001";
    private static final String TEST_PASSWORD = "Passwd#1";
    @Rule
    public ActivityScenarioRule<Onboarding> activityScenarioRule = new ActivityScenarioRule<>(Onboarding.class);

    @Test
    public void testFailedLoginWithNonExistingUser() {
        // Click on "Get Started" button
        onView(withId(R.id.getStartedButton)).perform(click());

        // Fill in username and password field
        onView(withId(R.id.etUsername)).perform(ViewActions.typeText(TEST_USERNAME));
        onView(withId(R.id.etPassword)).perform(ViewActions.typeText(TEST_PASSWORD));

        // Click "Login" button
        onView(withId(R.id.loginButton)).perform(ViewActions.click());

        // Check if the text input field for username and password display error text
        onView(withId(R.id.usernameLayout)).check(matches(TextInputLayoutErrorMatcher
                .hasTextInputLayoutError("Invalid username or password")));
        onView(withId(R.id.passwordLayout)).check(matches(TextInputLayoutErrorMatcher
                .hasTextInputLayoutError("Invalid username or password")));

    }
}



