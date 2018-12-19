package com.example.didred.android;


import android.content.Intent;
import android.net.Uri;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginFragment extends Fragment {

    private EditText emailField;
    private EditText passwordField;
    private Button createNewUserButton;
    private Button loginButton;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        progressBar = view.findViewById(R.id.progressBar);
        emailField = view.findViewById(R.id.emailField);
        passwordField = view.findViewById(R.id.passwordField);
        createNewUserButton = view.findViewById(R.id.createNewUserButton);
        loginButton = view.findViewById(R.id.loginButton);

        createNewUserButton.setOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_loginFragment_to_registrationFragment)
        );
        loginButton.setOnClickListener(loginButtonClickListener);
    }

    private View.OnClickListener loginButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            disableButtons();

            final String email = emailField.getText().toString().trim();
            final String password = passwordField.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(completeSignInListener);
            } else {
                enableButtons();
                Toast.makeText(getContext(), R.string.auth_error_message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private OnCompleteListener<AuthResult> completeSignInListener = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            enableButtons();
            if (task.isSuccessful()) {
                startActivity(new Intent(getActivity(), MainActivity.class));
            } else {
                Log.d("Login", task.getException().getMessage());
                Toast.makeText(getContext(), R.string.auth_error_message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void disableButtons() {
        createNewUserButton.setEnabled(false);
        loginButton.setEnabled(false);
    }

    private void enableButtons() {
        createNewUserButton.setEnabled(true);
        loginButton.setEnabled(true);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
