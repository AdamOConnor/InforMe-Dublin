package com.example.adamoconnor.test02maps.PostingInformationAndComments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.adamoconnor.test02maps.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import static com.example.adamoconnor.test02maps.MapsAndGeofencing.Place.getMonumentName;


public class InformationBackCommentsFragment extends Fragment {

    // declare recycler view.
    private RecyclerView commentList;

    //declare boolean.
    private boolean mValueLike = false;

    //declare database references.
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseLike;

    //declare firebase authentication.
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_back, container, false);
        setHasOptionsMenu(true);

        // get instance of the firebase databases.
        mDatabase = FirebaseDatabase.getInstance().getReference().child("comments").child(getMonumentName());
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("likes");

        //get the firebase authentication instance.
        mAuth = FirebaseAuth.getInstance();

        //keep both database references synced.
        mDatabase.keepSynced(true);
        mDatabaseLike.keepSynced(true);

        //declare the RecyclerView with reference to the layout.
        commentList = (RecyclerView) view.findViewById(R.id.comment_list);
        commentList.setHasFixedSize(true);
        commentList.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    /**
     * when the activity is called.
     */
    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Comments, CommentHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentHolder>(
                Comments.class,
                R.layout.comments_row,
                CommentHolder.class,
                mDatabase

        ) {

            /**
             * used to populate each post on the specific monument.
             * @param viewHolder
             * holder of information
             * @param model
             * used as a reference to comment model.
             * @param position
             * where on the view each attribute is located.
             */
            protected void populateViewHolder(final CommentHolder viewHolder,final Comments model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setImage(getActivity(),model.getImage());
                viewHolder.setProfileImage(getActivity(), model.getProfile());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setTime(model.getTimestamp());
                viewHolder.setLikes(post_key);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent viewComments = new Intent(getActivity(), CommentsActivity.class);
                        viewComments.putExtra("post_id", post_key);
                        viewComments.putExtra("location_id", getMonumentName());
                        startActivity(viewComments);
                    }
                });

                // used to set like button
                viewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mValueLike = true;

                        mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(mValueLike) {

                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                        viewHolder.likeButton.setLiked(false);
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mValueLike = false;
                                    } else {
                                        viewHolder.likeButton.setLiked(true);
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("random");
                                        mValueLike = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }
        };

        commentList.setAdapter(firebaseRecyclerAdapter);
    }


    /**
     * comment holder used to populate the layouts
     * attributes.
     */
    public static class CommentHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView post_NumLikes;
        DatabaseReference mDatabaseLikes;
        FirebaseAuth mAuth;
        LikeButton likeButton;

        public CommentHolder(View itemView) {
            super(itemView);

            mView = itemView;

            post_NumLikes = (TextView) mView.findViewById(R.id.commentThumbsUp);
            likeButton = (LikeButton) mView.findViewById(R.id.commentLike);

            mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("likes");
            mAuth = FirebaseAuth.getInstance();
            mDatabaseLikes.keepSynced(true);

        }

        /**
         * setting the number of likes of the posted monument
         * @param post_key
         * get the post key to find number of likes.
         */
        public void setLikes(final String post_key) {

            mDatabaseLikes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int numOfLikes = (int) dataSnapshot.child(post_key).getChildrenCount();

                    post_NumLikes.setText("Likes - "+numOfLikes);

                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                        likeButton.setLiked(true);
                    } else {
                        likeButton.setLiked(false);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        /**
         * setting the title
         * @param title
         * setting the title to textView.
         */
        public void setTitle(String title) {

            TextView post_title = (TextView) mView.findViewById(R.id.commentTitle);
            post_title.setText(title);

        }

        /**
         * setting the description
         * @param description
         * setting the description to textView
         */
        public void setDescription(String description) {

            TextView post_description = (TextView) mView.findViewById(R.id.commentDescription);
            post_description.setText(description);

        }

        /**
         * setting the image of the post
         * @param mContext
         * the context of the activity
         * @param image
         * the url string to load with picasso
         */
        public void setImage(final Context mContext,final String image) {

            final ImageView post_image = (ImageView) mView.findViewById(R.id.commentImage);
            Picasso.with(mContext).load(image).resize(1200,750).into(post_image);

        }

        /**
         * setting the username into textView
         * @param username
         * username to set to textView.
         */
        public void setUsername(String username) {

            TextView post_username = (TextView) mView.findViewById(R.id.commentUsername);
            post_username.setText("Posted by : "+username);
        }

        /**
         * setting the time of the post description.
         * @param timestamp
         * long timestamp to change to data and time.
         */
        public void setTime(Long timestamp) {

            TextView post_username = (TextView) mView.findViewById(R.id.commentTime);
            try {

                DateFormat sdf = new SimpleDateFormat().getDateTimeInstance();
                Date netDate = (new Date(timestamp));
                post_username.setText(sdf.format(netDate));
            }catch(Exception ex) {

            }

        }

        /**
         * setting of the profile image of the user who posted.
         * @param mContext
         * activity which is shown.
         * @param profile
         * string reference for the url to load.
         */
        public void setProfileImage(final Context mContext, final String profile) {

            ImageView post_image = (ImageView) mView.findViewById(R.id.commentProfile);

            Picasso.with(mContext).load(profile)
                    .placeholder(R.drawable.defaulticon).error(R.drawable.defaulticon)
                    .into(post_image);

        }
    }

    /**
     * creation of the menu options.
     * @param menu
     * menu reference.
     * @param inflater
     * inflater reference.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.menu_custom, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * post information on historic monument.
     * @param item
     * selected item
     * @return
     * the options
     */
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_add) {
            Intent postActivity = new Intent(getContext(),PostActivity.class);
            startActivity(postActivity);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * resume method.
     */
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

    }

    /**
     * pause method.
     */
    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

    }

}
