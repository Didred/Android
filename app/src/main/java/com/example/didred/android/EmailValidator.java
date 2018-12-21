package com.example.didred.android;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.EditText;

public class EmailValidator implements TextWatcher {
    private EditText editText;
    private Context context;

    public EmailValidator(EditText editText, Context context) {
        this.editText = editText;
        this.context = context;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String email = editText.getText().toString();
        if (email.isEmpty()) {
            editText.setError("Empty email");
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editText.setError("Incorrect email");
        }
        else {
            editText.setError(null);
        }
    }
}
