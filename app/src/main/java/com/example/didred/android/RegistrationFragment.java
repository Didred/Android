package com.example.didred.android;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;


public class RegistrationFragment extends Fragment {

    private UserRepository userRepository;

    private Button registerButton;
    private Button loginButton;

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        userRepository = new UserRepository();

        registerButton = view.findViewById(R.id.r_register);

        loginButton = view.findViewById(R.id.back);
        registerButton.setOnClickListener(register);

        emailEditText = view.findViewById(R.id.r_email);
        emailEditText.addTextChangedListener(new EmailValidator(emailEditText, getContext()));

        passwordEditText = view.findViewById(R.id.r_password);
        confirmPasswordEditText = view.findViewById(R.id.confirmPassword);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Navigation.findNavController(v).popBackStack();
            }
        });
    }

    private View.OnClickListener register = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (emailEditText.getError() != null) return;
            disableButtons();

            final String email = emailEditText.getText().toString().trim();
            final String password = passwordEditText.getText().toString().trim();
            final String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty() && password.equals(confirmPassword)) {
                userRepository.createNewUser(email, password).addOnCompleteListener(completeRegisterListener);
            } else {
                enableButtons();
                Toast.makeText(getContext(), R.string.registration_error, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private OnCompleteListener<AuthResult> completeRegisterListener = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            enableButtons();
            if (task.isSuccessful()) {
                startActivity(new Intent(getActivity(), MainActivity.class));
            } else {
                Log.d(String.valueOf(R.string.registration), task.getException().getMessage());
                Toast.makeText(getContext(), R.string.auth_error_message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void disableButtons() {
        registerButton.setEnabled(false);
        loginButton.setEnabled(false);
    }

    private void enableButtons() {
        registerButton.setEnabled(true);
        loginButton.setEnabled(true);
    }
}


