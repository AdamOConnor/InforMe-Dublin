package com.example.adamoconnor.test02maps.PostingInformationAndComments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.adamoconnor.test02maps.LoginAndRegister.RegisterAccount;
import com.example.adamoconnor.test02maps.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Random;

import static com.example.adamoconnor.test02maps.MapsAndGeofencing.Place.getMonumentName;

public class PostActivity extends AppCompatActivity {

    private Context mContext;
    private ImageButton selectImage;
    private EditText postTitle;
    private EditText postDescription;
    private Button submitPost;
    private Uri imageUri = null;
    private static final int GALLERY_REQUEST = 1;
    private StorageReference mStorage;
    private ProgressDialog mProgress;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseUsers;

    private DatabaseReference mDatabaseUsersProfilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mContext = this;

        mAuth = FirebaseAuth.getInstance();

        mCurrentUser = mAuth.getCurrentUser();

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("comments").child(getMonumentName());

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid());

        mDatabaseUsersProfilePicture = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid()).child("image");

        selectImage = (ImageButton) findViewById(R.id.imageSelect);

        postTitle = (EditText) findViewById(R.id.title);
        postDescription = (EditText) findViewById(R.id.description);

        submitPost = (Button) findViewById(R.id.submitButton);

        mProgress = new ProgressDialog(this);

        selectImage.setOnClickListener(new View.OnClickListener() {
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

        submitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new LongOperation().execute();

            }
        });

    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            this.runOnUiThread(new Runnable() {
                public void run() {
                    imageUri = data.getData();
                    Picasso.with(mContext)
                            .load(imageUri)
                            .centerCrop().resize(1080,780)
                            .into(selectImage);
                }
            });

        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

    }

    private class LongOperation extends AsyncTask<Void,String, String> {

        @Override
        protected void onPreExecute()
        {
            mProgress = new ProgressDialog(PostActivity.this);
            mProgress.setMessage("Processing Request...");
            mProgress.setIndeterminate(false);
            mProgress.setCancelable(false);
            mProgress.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startPosting();
            return "Executed";
        }

        private void startPosting() {

            final String titleValue = String.valueOf(postTitle.getText()).trim();
            final String descriptionValue = String.valueOf(postDescription.getText()).trim();

            if (!TextUtils.isEmpty(titleValue) && !TextUtils.isEmpty(descriptionValue) && imageUri != null) {

                StorageReference filePath = mStorage.child("Comment_Images").child(random());

                filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override

                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests")
                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        final DatabaseReference newPost = mDatabase.push();

                        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                newPost.child("title").setValue(titleValue);
                                newPost.child("description").setValue(descriptionValue);
                                newPost.child("image").setValue(downloadUrl.toString());
                                newPost.child("uid").setValue(mCurrentUser.getUid());
                                newPost.child("username").setValue(dataSnapshot.child("name").getValue());
                                newPost.child("timestamp").setValue(ServerValue.TIMESTAMP);
                                newPost.child("profile").setValue(dataSnapshot.child("image").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Thread.sleep(5000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();

                                        if(task.isSuccessful()) {
                                            mProgress.dismiss();
                                            Intent startCommentFragment = new Intent(PostActivity.this, InformationFlipActivity.class);
                                            startCommentFragment.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(startCommentFragment);
                                        }
                                        else {
                                            mProgress.dismiss();
                                            Toast.makeText(PostActivity.this, "OOps looks like something went wrong, please check internet connection ...",
                                                    Toast.LENGTH_LONG).show();
                                        }


                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                });
            }
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

        }


    }

}
