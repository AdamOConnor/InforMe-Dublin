package com.example.adamoconnor.test02maps.PostingInformationAndComments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.adamoconnor.test02maps.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateActivity extends AppCompatActivity {

    private String mPost_key = null;
    private String mLocation_key = null;
    private String post_uid = null;

    private DatabaseReference mDatabase;

    private ImageView postImage;
    private EditText postTitle;
    private EditText postDescription;
    private TextView postTimestamp;
    private TextView postUsername;
    private CircleImageView postProfile;
    private Button postUpdate;

    private ProgressDialog mProgress;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        mPost_key = getIntent().getExtras().getString("post_id");
        mLocation_key = getIntent().getExtras().getString("location_id");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("comments").child(mLocation_key);
        mAuth = FirebaseAuth.getInstance();

        postProfile = (CircleImageView) findViewById(R.id.commentProfile);
        postUsername = (TextView) findViewById(R.id.commentUsername);
        postTimestamp = (TextView) findViewById(R.id.commentTime);

        postImage = (ImageView) findViewById(R.id.commentImage);
        postTitle = (EditText) findViewById(R.id.commentTitle);
        postDescription = (EditText) findViewById(R.id.commentDescription);

        postUpdate = (Button) findViewById(R.id.updatePost);

        mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String post_title = (String)dataSnapshot.child("title").getValue();
                String post_desc = (String)dataSnapshot.child("description").getValue();
                String post_image = (String)dataSnapshot.child("image").getValue();

                String post_profile = (String)dataSnapshot.child("profile").getValue();
                String post_username = (String)dataSnapshot.child("username").getValue();
                Long post_timestamp = (Long)dataSnapshot.child("timestamp").getValue();
                post_uid = (String)dataSnapshot.child("uid").getValue();

                postTitle.setText(post_title);
                postDescription.setText(post_desc);
                postUsername.setText(post_username);

                try {
                    DateFormat sdf = new SimpleDateFormat().getDateTimeInstance();
                    Date netDate = (new Date(post_timestamp));
                    postTimestamp.setText(sdf.format(netDate));
                } catch(NullPointerException ex) {
                    ex.getStackTrace();
                }


                Picasso.with(UpdateActivity.this).load(post_image).resize(1200,750).into(postImage);
                Picasso.with(UpdateActivity.this).load(post_profile)
                        .placeholder(R.drawable.defaulticon).error(R.drawable.defaulticon)
                        .into(postProfile);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        postUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress = new ProgressDialog(UpdateActivity.this);
                mProgress.setMessage("Processing Request...");
                mProgress.setIndeterminate(false);
                mProgress.setCancelable(false);
                mProgress.show();
                if(mAuth.getCurrentUser().getUid().equals(post_uid)) {

                    mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            dataSnapshot.getRef().child("title").setValue(postTitle.getText().toString().trim());
                            dataSnapshot.getRef().child("description").setValue(postDescription.getText().toString().trim());
                            dataSnapshot.getRef().child("timestamp").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()) {
                                        mProgress.dismiss();
                                        Intent startCommentFragment = new Intent(UpdateActivity.this, InformationFlipActivity.class);
                                        startCommentFragment.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(startCommentFragment);
                                    }
                                    else {
                                        mProgress.dismiss();
                                        Toast.makeText(UpdateActivity.this, "OOps looks like something went wrong, please check internet connection ...",
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

            }
        });

    }


}
