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
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegistrationFragment extends Fragment {

    private Button registerButton;
    private Button login;

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;


    public RegistrationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        registerButton = view.findViewById(R.id.r_register);
        login = view.findViewById(R.id.back);
        registerButton.setOnClickListener(register);
        emailEditText = view.findViewById(R.id.r_email);
        passwordEditText = view.findViewById(R.id.r_password);
        confirmPasswordEditText = view.findViewById(R.id.confirmPassword);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Navigation.findNavController(v).popBackStack();
            }
        });
    }

    private View.OnClickListener register = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            disableButtons(registerButton, login);

            final String email = emailEditText.getText().toString().trim();
            final String password = passwordEditText.getText().toString().trim();
            final String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (!email.isEmpty() && password.equals(confirmPassword)) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(getActivity(), MainActivity.class));
                                } else {
                                    enableButtons(registerButton, login);
                                    Log.d("Error email", email + task.getException().getMessage());
                                    Log.d("Error password", password);
                                    Log.d("Error password confirm", confirmPassword);
                                    Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                enableButtons(registerButton, login);
                Toast.makeText(getContext(), R.string.registration_error, Toast.LENGTH_SHORT).show();
            }
        }
    };
    private void disableButtons(Button registerButton, Button loginButton) {
        registerButton.setEnabled(false);
        loginButton.setEnabled(false);
    }

    private void enableButtons(Button registerButton, Button loginButton) {
        registerButton.setEnabled(true);
        loginButton.setEnabled(true);
    }
}


