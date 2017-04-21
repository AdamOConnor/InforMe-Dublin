package com.example.adamoconnor.test02maps.LoginAndRegister;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.adamoconnor.test02maps.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Random;

public class SetupActivity extends AppCompatActivity {

    // declared textfield and image button.
    private ImageButton mSetupImageButton;
    private EditText mNameField;

    // declare the setup button.
    private Button mSetupButton;

    //declare the brogress bar.
    private ProgressDialog mProgress;

    // declare firebasebase database and storage reference.
    private DatabaseReference mDatabaseUsers;
    private StorageReference mStorageImage;

    //declare image URI
    private Uri mImageUri = null;

    //declare the gallery request value.
    private static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        //setting screen orientation to stop fragments view showing on eachother.
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // grant permission.
        isStoragePermissionGranted();

        //initiate progress bar.
        mProgress = new ProgressDialog(this);

        // get instances of database and storage.
        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");

        // getting reference to the buttons and textfields.
        mSetupImageButton = (ImageButton) findViewById(R.id.profileSetup);
        mNameField = (EditText) findViewById(R.id.nameSetup);
        mSetupButton = (Button) findViewById(R.id.submitSetup);

        // profile image which needs to be selected
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

        //setup the profile of the user.
        mSetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSetupAccount();
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
     * setting up of an account when user signs in by google.
     */
    private void startSetupAccount() {

        // getting name of user.
        final String name = mNameField.getText().toString().trim();
        // get the authenticated users id.
        final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(!TextUtils.isEmpty(name) && mImageUri != null) {

            mProgress.setMessage("Setting up Account ...");
            mProgress.show();

            StorageReference filepath = mStorageImage.child(random());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    @SuppressWarnings("VisibleForTests")
                    String downloadUri = taskSnapshot.getDownloadUrl().toString();

                    mDatabaseUsers.child(user_id).child("name").setValue(name);
                    mDatabaseUsers.child(user_id).child("image").setValue(downloadUri);

                    mProgress.dismiss();

                    Intent sendToMainIntent = new Intent(SetupActivity.this, EmailPasswordAuthentication.class);
                    sendToMainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(sendToMainIntent);
                }
            });

        }

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

            mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }

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
                return true;
            } else {
                //permission revoked
                Toast.makeText(SetupActivity.this, "Permission is revoked", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                Intent backIntent = new Intent(SetupActivity.this, EmailPasswordAuthentication.class);
                backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                backIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(backIntent);
                return false;
            }
        }
        else {
            //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }
}
