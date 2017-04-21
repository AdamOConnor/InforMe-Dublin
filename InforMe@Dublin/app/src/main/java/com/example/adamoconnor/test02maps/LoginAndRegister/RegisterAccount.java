package com.example.adamoconnor.test02maps.LoginAndRegister;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.adamoconnor.test02maps.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.util.Random;

public class RegisterAccount extends Progress {

    private static final String TAG = "RegisterAccount";

    //declare boolean
    private boolean valid;

    // declare the textviews etc...
    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private ImageButton mSetupImageButton;

    // declare button to register.
    private Button mRegisterButton;

    // declare progress bar.
    private ProgressDialog mProgress;

    // declaring firebase authentication, database and storage reference.
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorageImage;

    //declare an image URI.
    private Uri mImageUri = null;

    // declaring GALLERY_REQUEST result.
    private static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_account);

        //setting screen orientation to stop fragments view showing on eachother.
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        isStoragePermissionGranted();

        // getting users authentication of application.
        mAuth = FirebaseAuth.getInstance();

        // database and storage reference's.
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");

        // textfield and button declarations to the layout file.
        mNameField = (EditText) findViewById(R.id.nameField);
        mEmailField = (EditText) findViewById(R.id.emailField);
        mPasswordField = (EditText) findViewById(R.id.passwordField);
        mRegisterButton = (Button) findViewById(R.id.registerButton);
        mSetupImageButton = (ImageButton) findViewById(R.id.profileSetup);

        // used for selecting an image for the user's profile.
        mSetupImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {

                        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent,GALLERY_REQUEST);

                    }
                }.start();

            }
        });

        // used for registering user.
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
                if(valid){
                    startRegister();
                }else {
                    Toast.makeText(RegisterAccount.this, "conditions are not correct",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * used to get a random string needed to apply to the
     * profile image of user to combat clashes with image names.
     * @return
     * return a random string.
     */
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    /**
     * start to register the user with an email address, password, name
     * and profile image.
     */
    private void startRegister() {

        // used for getting the information from the user.
        final String name = mNameField.getText().toString().trim();

        //set progress bar.
        mProgress = new ProgressDialog(RegisterAccount.this);
        mProgress.setMessage("Registering user ...");
        mProgress.show();

        // used for getting the information from the user.
        final String email = mEmailField.getText().toString().trim();
        final String password = mPasswordField.getText().toString().trim();

        //check if image is not selected.
        if(mImageUri != null) {

                // START create_user_with_email and password.
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                                if (task.isSuccessful()) {

                                    // getting current users authenticated id.
                                    final String user_id = mAuth.getCurrentUser().getUid();

                                    StorageReference filepath = mStorageImage.child(random());
                                    filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        // adding users information to the database.
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            @SuppressWarnings("VisibleForTests")
                                            String downloadUri = taskSnapshot.getDownloadUrl().toString();

                                            DatabaseReference currentUserDb = mDatabase.child(user_id);
                                            currentUserDb.child("name").setValue(name);
                                            currentUserDb.child("image").setValue(downloadUri);

                                        }
                                    });

                                    // allow a delay for the database being updated
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(4000);

                                                mProgress.dismiss();
                                                Intent mainIntent = new Intent(RegisterAccount.this, EmailPasswordAuthentication.class);
                                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(mainIntent);

                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();

                                }
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(RegisterAccount.this, R.string.auth_failed + "Please check internet connection",
                                            Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

        } else {
            Toast.makeText(RegisterAccount.this, "Please select an Image for your profile.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * used to validate the registration form.
     * @return
     * return the error if user has not entered text.
     */
    private boolean validateForm() {

        valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String name = mNameField.getText().toString();
        if (TextUtils.isEmpty(name)) {
            mNameField.setError("Required.");
            valid = false;
        } else {
            mNameField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        int size = password.length();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else
            if(size < 6) {
                Toast.makeText(RegisterAccount.this, "password is too short the minimum length is 6 characters",
                        Toast.LENGTH_LONG).show();
                valid = false;
            }
            else {
                mPasswordField.setError(null);
            }

        return valid;
    }

    /**
     *
     * @param requestCode
     * code on which was requested as the gallery intent.
     * @param resultCode
     * if the request was successful continue.
     * @param data
     * the data on which was selected, profile image.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            // get the image data send to the declared URI
            mImageUri = data.getData();

            // crop the image to specific size.
            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        // on cropped image display to the user in the image button.
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                Picasso.with(this)
                        .load(resultUri)
                        .centerCrop()
                        .resize(300,300)
                        .into(mSetupImageButton);

                mImageUri = resultUri;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    /**
     * getting android permission to access storage for the device.
     * @return
     * return true or false
     */
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(RegisterAccount.this, "Permission is granted", Toast.LENGTH_SHORT).show();
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                Toast.makeText(RegisterAccount.this, "Permission is revoked", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                Intent backIntent = new Intent(RegisterAccount.this, EmailPasswordAuthentication.class);
                backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                backIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(backIntent);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }
}
