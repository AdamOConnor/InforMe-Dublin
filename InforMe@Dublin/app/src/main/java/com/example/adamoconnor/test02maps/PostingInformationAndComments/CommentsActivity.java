package com.example.adamoconnor.test02maps.PostingInformationAndComments;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adamoconnor.test02maps.R;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private String mPost_key = null;
    private String mLocation_key = null;
    private String post_uid = null;

    private DatabaseReference mDatabase;

    private ImageView postImage;
    private TextView postTitle;
    private TextView postDescription;
    private TextView postTimestamp;
    private TextView postUsername;
    private CircleImageView postProfile;
    private Button postRemove;

    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mPost_key = getIntent().getExtras().getString("post_id");
        mLocation_key = getIntent().getExtras().getString("location_id");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("comments").child(mLocation_key);
        mAuth = FirebaseAuth.getInstance();

        postProfile = (CircleImageView) findViewById(R.id.commentProfile);
        postUsername = (TextView) findViewById(R.id.commentUsername);
        postTimestamp = (TextView) findViewById(R.id.commentTime);

        postImage = (ImageView) findViewById(R.id.commentImage);
        postTitle = (TextView) findViewById(R.id.commentTitle);
        postDescription = (TextView) findViewById(R.id.commentDescription);

        postRemove = (Button) findViewById(R.id.commentRemove);

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


                Picasso.with(CommentsActivity.this).load(post_image).resize(1200,750).into(postImage);
                Picasso.with(CommentsActivity.this).load(post_profile)
                        .placeholder(R.drawable.defaulticon).error(R.drawable.defaulticon)
                        .into(postProfile);

                if(mAuth.getCurrentUser().getUid().equals(post_uid)) {

                    postRemove.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        postRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mAuth.getCurrentUser().getUid().equals(post_uid)) {

                    mDatabase.child(mPost_key).removeValue();

                    Intent InformationIntent = new Intent(CommentsActivity.this, InformationFlipActivity.class);
                    InformationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    InformationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(InformationIntent);

                }

            }
        });
    }
}
