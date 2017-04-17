package com.example.adamoconnor.test02maps.PostingInformationAndComments;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adamoconnor.test02maps.R;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private String mPost_key = null;

    private DatabaseReference mDatabase;

    private ImageView postImage;
    private TextView postTitle;
    private TextView postDescription;
    private TextView postTimestamp;
    private TextView postUsername;
    private CircleImageView postProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("comments");
        mPost_key = getIntent().getExtras().getString("post_id");

        postProfile = (CircleImageView) findViewById(R.id.commentProfile);
        postUsername = (TextView) findViewById(R.id.commentUsername);
        postTimestamp = (TextView) findViewById(R.id.commentTime);

        postImage = (ImageView) findViewById(R.id.commentImage);
        postTitle = (TextView) findViewById(R.id.commentTitle);
        postDescription = (TextView) findViewById(R.id.commentDescription);

        mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String post_title = (String)dataSnapshot.child("title").getValue();
                String post_desc = (String)dataSnapshot.child("description").getValue();
                String post_image = (String)dataSnapshot.child("image").getValue();
                String post_profile = (String)dataSnapshot.child("profile").getValue();
                String post_username = (String)dataSnapshot.child("username").getValue();
                String post_timestamp = (String)dataSnapshot.child("timestamp").getValue();
                String post_uid = (String)dataSnapshot.child("uid").getValue();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
