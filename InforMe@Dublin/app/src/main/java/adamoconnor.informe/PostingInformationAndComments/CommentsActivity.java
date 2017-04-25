package adamoconnor.informe.PostingInformationAndComments;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import adamoconnor.informe.R;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    // strings needed for the post id and monument name id.
    private String mPost_key = null;
    private String mLocation_key = null;

    // used for the post unique id.
    private String post_uid = null;

    //database reference.
    private DatabaseReference mDatabase;

    // textviews for the posted information.
    private TextView postTitle;
    private TextView postDescription;
    private TextView postTimestamp;
    private TextView postUsername;

    // declare image views for the posted image and posted user.
    private CircleImageView postProfile;
    private ImageView postImage;

    // declare post remove and update buttons.
    private Button postRemove;
    private Button postUpdate;

    //firebase authentication.
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        //setting screen orientation to stop fragments view showing on eachother.
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // getting the reference of the specific post.
        mPost_key = getIntent().getExtras().getString("post_id");
        mLocation_key = getIntent().getExtras().getString("location_id");

        //get instance of the database.
        mDatabase = FirebaseDatabase.getInstance().getReference().child("comments").child(mLocation_key);
        // get the authentication instance.
        mAuth = FirebaseAuth.getInstance();

        // getting the textviews reference form the layout.
        postUsername = (TextView) findViewById(R.id.commentUsername);
        postTimestamp = (TextView) findViewById(R.id.commentTime);
        postTitle = (TextView) findViewById(R.id.commentTitle);
        postDescription = (TextView) findViewById(R.id.commentDescription);

        // getting the imageView reference.
        postImage = (ImageView) findViewById(R.id.commentImage);
        postProfile = (CircleImageView) findViewById(R.id.commentProfile);

        // getting the button references
        postRemove = (Button) findViewById(R.id.commentRemove);
        postUpdate = (Button) findViewById(R.id.updatePost);

        // getting the information from firebase and display on the activity.
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

                //used to get the correct date and time format from timestamp.
                try {

                    DateFormat sdf = new SimpleDateFormat().getDateTimeInstance();
                    Date netDate = (new Date(post_timestamp));
                    postTimestamp.setText(sdf.format(netDate));

                } catch(NullPointerException ex) {
                    ex.getStackTrace();
                }

                // load the images from the reference with picasso
                Picasso.with(CommentsActivity.this).load(post_image).resize(1200,750).into(postImage);
                Picasso.with(CommentsActivity.this).load(post_profile)
                        .placeholder(R.drawable.defaulticon).error(R.drawable.defaulticon)
                        .into(postProfile);

                //only when the same user is authenticated my they have the option to delete or update post.
                if(mAuth.getCurrentUser().getUid().equals(post_uid)) {

                    postRemove.setVisibility(View.VISIBLE);
                    postUpdate.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //used for removing the post.
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

        //used for updating a post.
        postUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mAuth.getCurrentUser().getUid().equals(post_uid)) {
                    Intent InformationIntent = new Intent(CommentsActivity.this, UpdateActivity.class);
                    InformationIntent.putExtra("post_id", mPost_key);
                    InformationIntent.putExtra("location_id", mLocation_key);
                    startActivity(InformationIntent);
                }
            }
        });
    }
}
