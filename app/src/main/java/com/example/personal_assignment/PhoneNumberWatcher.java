package com.example.personal_assignment;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;

public class PhoneNumberWatcher implements TextWatcher {

    private boolean isFormatting;
    private TextInputEditText editText;

    public PhoneNumberWatcher(TextInputEditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) {
        if (isFormatting) return;

        isFormatting = true;

        // Remove all non-digits
        String digits = s.toString().replaceAll("\\D+", "");
        StringBuilder sb = new StringBuilder();

        // If we there are 3 digits (XXX)
        if (digits.length() > 3) {
            sb.append(digits.substring(0, 3)).append("-");
            // If there are  6 digits (XXX-XXX)
            if (digits.length() > 6) {
                sb.append(digits.substring(3, 6)).append("-");
                // Remainder last 4 digits
                sb.append(digits.substring(6));
            } else {
                sb.append(digits.substring(3));
            }
        } else {
            // Less than 3 digits - just place them
            sb.append(digits);
        }

        editText.removeTextChangedListener(this);
        editText.setText(sb.toString());
        editText.setSelection(sb.length());
        editText.addTextChangedListener(this);

        isFormatting = false;
    }
}

