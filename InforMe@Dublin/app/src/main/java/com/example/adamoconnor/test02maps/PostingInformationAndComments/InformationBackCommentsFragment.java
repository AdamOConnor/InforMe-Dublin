package com.example.adamoconnor.test02maps.PostingInformationAndComments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.Tag;
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
import android.widget.Toast;

import com.example.adamoconnor.test02maps.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


import static com.example.adamoconnor.test02maps.MapsAndGeofencing.Place.getMonumentName;

/**
 * Created by Adam O'Connor on 10/04/2017.
 */

public class InformationBackCommentsFragment extends Fragment {

    private RecyclerView commentList;
    private DatabaseReference mDatabase;
    private static ProgressDialog mProgress;
    private ImageView post_image;
    private boolean mValueLike = false;
    private DatabaseReference mDatabaseLike;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_back, container, false);
        setHasOptionsMenu(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("comments").child(getMonumentName());
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("likes");
        mAuth = FirebaseAuth.getInstance();

        mDatabase.keepSynced(true);
        mDatabaseLike.keepSynced(true);

        commentList = (RecyclerView) view.findViewById(R.id.comment_list);
        commentList.setHasFixedSize(true);
        commentList.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Comments, CommentHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentHolder>(
                Comments.class,
                R.layout.comments_row,
                CommentHolder.class,
                mDatabase

        ) {

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

                       // Toast.makeText(getContext(), post_key, Toast.LENGTH_LONG).show();

                        Intent viewComments = new Intent(getActivity(), CommentsActivity.class);
                        viewComments.putExtra("post_id", post_key);
                        startActivity(viewComments);


                    }
                });


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
                                        Log.d("MainClass", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!REMOVE");
                                        mValueLike = false;
                                    } else {
                                        viewHolder.likeButton.setLiked(true);
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("random");
                                        Log.d("MainClass", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!ADD");
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

    public static class CommentHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView post_title;
        DatabaseReference mDatabaseLikes;
        FirebaseAuth mAuth;
        LikeButton likeButton;

        public CommentHolder(View itemView) {
            super(itemView);

            mView = itemView;

            likeButton = (LikeButton) mView.findViewById(R.id.commentLike);

            mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("likes");
            mAuth = FirebaseAuth.getInstance();
            mDatabaseLikes.keepSynced(true);

        }

        public void setLikes(final String post_key) {

            mDatabaseLikes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

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

        public void setTitle(String title) {

            TextView post_title = (TextView) mView.findViewById(R.id.commentTitle);
            post_title.setText(title);

        }

        public void setDescription(String description) {

            TextView post_description = (TextView) mView.findViewById(R.id.commentDescription);
            post_description.setText(description);

        }

        public void setImage(final Context mContext,final String image) {

            final ImageView post_image = (ImageView) mView.findViewById(R.id.commentImage);
            Picasso.with(mContext).load(image).resize(1200,750).into(post_image);

        }

        public void setUsername(String username) {

            TextView post_username = (TextView) mView.findViewById(R.id.commentUsername);
            post_username.setText("Posted by : "+username);
        }

        public void setTime(Long timestamp) {

            TextView post_username = (TextView) mView.findViewById(R.id.commentTime);
            try {

                DateFormat sdf = new SimpleDateFormat().getDateTimeInstance();
                Date netDate = (new Date(timestamp));
                post_username.setText(sdf.format(netDate));
            }catch(Exception ex) {

            }

        }

        public void setProfileImage(final Context mContext, final String profile) {

            ImageView post_image = (ImageView) mView.findViewById(R.id.commentProfile);

            Picasso.with(mContext).load(profile)
                    .placeholder(R.drawable.defaulticon).error(R.drawable.defaulticon)
                    .into(post_image);

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.menu_custom, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_add) {
            Intent postActivity = new Intent(getContext(),PostActivity.class);
            startActivity(postActivity);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

    }

}
