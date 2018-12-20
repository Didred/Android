package com.example.didred.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {
    private TextView phoneNumberView;
    private TextView fullNameView;
    private ImageView profileImageView;
    private ProgressBar progressBar;

    private Button editButton;
    private Button logoutButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        disableButtons();

        progressBar = view.findViewById(R.id.progressBar);

        editButton = view.findViewById(R.id.profileEditButton);
        logoutButton = view.findViewById(R.id.profileLogoutButton);

        editButton.setOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_profileFragment_to_profileEditFragment)
        );
        logoutButton.setOnClickListener(logoutButtonListener);

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        TextView emailView = view.findViewById(R.id.profileEmailView);
        emailView.setText(userEmail);

        phoneNumberView = view.findViewById(R.id.profilePhoneNumberView);
        fullNameView = view.findViewById(R.id.profileFullNameView);
        profileImageView = view.findViewById(R.id.profileImageView);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference reference = FirebaseStorage.getInstance().getReference().child(user.getUid());
        reference.getBytes(Long.MAX_VALUE)
                .addOnSuccessListener(successImageLoadListener)
                .addOnFailureListener(failureImageLoadListener);

        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference()
                .child("userProfiles").child(user.getUid());
        dbReference.addValueEventListener(profileEventListener);
    }

    private View.OnClickListener logoutButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            ((MainActivity) getActivity()).cleanArticlesCache();
            FirebaseAuth.getInstance().signOut();
            ((MainActivity) getActivity()).startAuthActivity();
        }
    };

    private OnSuccessListener<byte[]> successImageLoadListener = new OnSuccessListener<byte[]>() {
        @Override
        public void onSuccess(byte[] bytes) {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
            profileImageView.setImageBitmap(bmp);
            enableButtons();
        }
    };

    private OnFailureListener failureImageLoadListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("ProfileImage", exception.getMessage());
            enableButtons();
        }
    };

    private ValueEventListener profileEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
            if (userProfile!= null){
                fullNameView.setText(userProfile.getFullName());
                phoneNumberView.setText(userProfile.getPhoneNumber());
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.d("ProfileImage", databaseError.getMessage());
            Toast.makeText(getContext(), R.string.profile_show_error_message, Toast.LENGTH_SHORT).show();
        }
    };

    private void disableButtons() {
        editButton.setEnabled(false);
        logoutButton.setEnabled(false);
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void enableButtons() {
        editButton.setEnabled(true);
        logoutButton.setEnabled(true);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}