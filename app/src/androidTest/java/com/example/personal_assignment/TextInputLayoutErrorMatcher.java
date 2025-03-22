package com.example.personal_assignment;

import androidx.test.espresso.matcher.BoundedMatcher;
import com.google.android.material.textfield.TextInputLayout;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class TextInputLayoutErrorMatcher {

    // Validate if TextInput displays error text
    public static Matcher<Object> hasTextInputLayoutError(final String expectedError) {
        return new BoundedMatcher<Object, TextInputLayout>(TextInputLayout.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("Expected error text: " + expectedError);
            }

            @Override
            protected boolean matchesSafely(TextInputLayout textInputLayout) {
                return textInputLayout.getError() != null &&
                        textInputLayout.getError().toString().equals(expectedError);
            }
        };
    }
}

