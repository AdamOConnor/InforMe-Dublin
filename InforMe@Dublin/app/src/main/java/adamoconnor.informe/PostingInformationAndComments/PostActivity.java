package adamoconnor.informe.PostingInformationAndComments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import adamoconnor.informe.R;
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
import static adamoconnor.informe.MapsAndGeofencing.Place.getMonumentName;

public class PostActivity extends AppCompatActivity {

    //declare context.
    private Context mContext;

    //declare image button
    private ImageButton selectImage;

    //declare editTexts.
    private EditText postTitle;
    private EditText postDescription;

    //declare button.
    private Button submitPost;

    //declare Uri
    private Uri imageUri = null;

    //declare gallery request.
    private static final int GALLERY_REQUEST = 1;

    //declare progress dialog.
    private ProgressDialog mProgress;

    //declare database references
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseUsersProfilePicture;

    //declare firebase authentication.
    private FirebaseAuth mAuth;

    //declare firebase user.
    private FirebaseUser mCurrentUser;

    //declare firebase storage.
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //setting screen orientation to stop fragments view showing on eachother.
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // declare context reference
        mContext = this;

        //declare authentication instance
        mAuth = FirebaseAuth.getInstance();

        // get the users authentcation
        mCurrentUser = mAuth.getCurrentUser();

        // get the storage and database instances with each reference
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("comments").child(getMonumentName());
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid());
        mDatabaseUsersProfilePicture = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid()).child("image");

        //reference to selecting image.
        selectImage = (ImageButton) findViewById(R.id.imageSelect);

        //reference to edit text fields
        postTitle = (EditText) findViewById(R.id.title);
        postDescription = (EditText) findViewById(R.id.description);

        //reference to the button on the layout
        submitPost = (Button) findViewById(R.id.submitButton);

        //setting reference to progress dialog.
        mProgress = new ProgressDialog(this);

        //selecting of the image listener.
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {

                        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        //only look at images.
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent,GALLERY_REQUEST);
                    }
                }.start();

            }
        });

        //submit the post.
        submitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new LongOperation().execute();
            }
        });

    }

    /**
     * random string generator for the uploaded image
     * @return
     * random string to return
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
     * display the image on the ImageButton
     * @param requestCode
     * get request code
     * @param resultCode
     * get result code
     * @param data
     * retrieve image.
     */
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

    /**
     * onResume method.
     */
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

    }

    /**
     * onPause method.
     */
    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

    }

    /**
     * long operation to post the information about the specific
     * monument.
     */
    private class LongOperation extends AsyncTask<Void,String, String> {

        /**
         * pre execution used for the progress
         * dialog.
         */
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

        /**
         * run a method in the background.
         * @param params
         * pass parameters
         * @return
         * return execution.
         */
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
            } else {
                mProgress.dismiss();
                Toast.makeText(PostActivity.this, "Please fill in the form...",
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

        }


    }

}
