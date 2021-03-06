package com.example.didred.android;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;

public class ProfileEditFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_CAMERA = 321;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    private Boolean isPhotoChanged = false;
    private NavController navController;

    private ImageView profileImage;
    private EditText phoneField;
    private EditText fullNameField;
    private ProgressBar progressBar;
    private Button saveButton;

    private UserRepository userRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        userRepository = new UserRepository();

        saveButton = view.findViewById(R.id.profileEditSaveButton);
        progressBar = view.findViewById(R.id.progressBar);
        disableButtons();

        navController = Navigation.findNavController(view);

        profileImage = view.findViewById(R.id.profileEditImageView);
        profileImage.setOnClickListener(profileImageListener);


        phoneField = view.findViewById(R.id.profileEditPhoneField);
        fullNameField = view.findViewById(R.id.profileEditFullNameField);

        saveButton.setOnClickListener(saveButtonListener);



        userRepository.getProfileImageBitmap()
                .addOnSuccessListener(successImageLoadListener)
                .addOnFailureListener(failureImageLoadListener);

        userRepository.addProfileEventListener(profileEventListener);
    }

    private ValueEventListener profileEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
            if (userProfile != null){
                fullNameField.setText(userProfile.getFullName());
                phoneField.setText(userProfile.getPhoneNumber());
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.d(String.valueOf(R.string.profileEditInfo), databaseError.getMessage());
            Toast.makeText(getContext(), R.string.profile_edit_error_message, Toast.LENGTH_SHORT).show();
        }
    };

    private OnSuccessListener<byte[]> successImageLoadListener = new OnSuccessListener<byte[]>() {
        @Override
        public void onSuccess(byte[] bytes) {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
            profileImage.setImageBitmap(bmp);
            enableButtons();
        }
    };

    private OnFailureListener failureImageLoadListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d(String.valueOf(R.string.profileEditImage), exception.getMessage());
            enableButtons();
        }
    };

    private final View.OnClickListener saveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            disableButtons();

            String fullName = fullNameField.getText().toString().trim();
            String phoneNumber = phoneField.getText().toString().trim();

            final UserProfile profile = new UserProfile(fullName, phoneNumber);
            userRepository.setUserProfile(profile);

            if(isPhotoChanged){
                userRepository.putProfileImageBitmap(imageToByteArray()).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d(String.valueOf(R.string.profileEditImage), exception.getMessage());
                        Toast.makeText(getContext(), R.string.profile_edit_error_message, Toast.LENGTH_SHORT).show();
                        enableButtons();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ((MainActivity) getActivity()).updateNavImage();
                        navController.navigate(R.id.profileFragment);
                    }
                });
            }
            else {
                enableButtons();
                navController.navigate(R.id.profileFragment);
            }

            ((MainActivity) getActivity()).hideKeyboard();
        }
    };

    private View.OnClickListener profileImageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showPictureDialog();
        }
    };

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getContext());
        pictureDialog.setTitle(getResources().getString(R.string.profile_edit_select_action));
        String[] pictureDialogItems = {
                getResources().getString(R.string.profile_edit_select_from_gallery),
                getResources().getString(R.string.profile_edit_capture_from_camera)
        };

        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallery();
                                break;
                            case 1:
                                choosePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    private byte[] imageToByteArray(){
        if (profileImage.getDrawable() instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
            return bytes.toByteArray();
        }
        return null;
    }

    private void choosePhotoFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, String.valueOf(R.string.selectPicture)), REQUEST_GALLERY);
    }

    private void choosePhotoFromCamera(){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.CAMERA }, PERMISSIONS_REQUEST_CAMERA);
        } else {
            takePhotoFromCamera();
        }
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhotoFromCamera();
                }
                else {
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        if (requestCode == REQUEST_CAMERA) {
            isPhotoChanged = true;
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            profileImage.setImageBitmap(imageBitmap);
        } else if (requestCode == REQUEST_GALLERY) {
            isPhotoChanged = true;
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void disableButtons() {
        saveButton.setEnabled(false);
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void enableButtons() {
        saveButton.setEnabled(true);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }
}
