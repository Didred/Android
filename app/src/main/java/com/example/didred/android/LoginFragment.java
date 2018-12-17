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
public class LoginFragment extends Fragment {

    private Button goToRegistrationButton;
    private Button loginButton;

    private EditText emailEditText;
    private EditText passwordEditText;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        goToRegistrationButton = view.findViewById(R.id.l_register);
        goToRegistrationButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_loginFragment_to_registrationFragment));

        loginButton = view.findViewById(R.id.l_login);
        loginButton.setOnClickListener(login);

        emailEditText = view.findViewById(R.id.l_email);
        passwordEditText = view.findViewById(R.id.l_password);
    }

    private View.OnClickListener login = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            disableButtons(goToRegistrationButton, loginButton);

            final String email = emailEditText.getText().toString().trim();
            final String password = passwordEditText.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                enableButtons(goToRegistrationButton, loginButton);
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(getActivity(), MainActivity.class));
                                } else {
                                    Log.d("Error email", email + task.getException().getMessage());
                                    Log.d("Error password", password);
                                    Toast.makeText(getContext(), R.string.auth_error_message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                enableButtons(goToRegistrationButton, loginButton);
                Toast.makeText(getContext(), R.string.auth_error_message, Toast.LENGTH_SHORT).show();
            }
        }
    };
    private void disableButtons(Button createNewUserButton, Button loginButton) {
        createNewUserButton.setEnabled(false);
        loginButton.setEnabled(false);
    }

    private void enableButtons(Button createNewUserButton, Button loginButton) {
        createNewUserButton.setEnabled(true);
        loginButton.setEnabled(true);
    }
}
