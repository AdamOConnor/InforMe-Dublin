package com.example.adamoconnor.test02maps.PostingInformationAndComments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import static com.example.adamoconnor.test02maps.MapsAndGeofencing.Place.getMonumentName;

/**
 * Created by Adam O'Connor on 10/04/2017.
 */

public class InformationBackCommentsFragment extends Fragment {

    private RecyclerView commentList;
    private DatabaseReference mDatabase;
    private static ProgressDialog mProgress;
    private ImageView post_image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_back, container, false);
        setHasOptionsMenu(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("comments").child(getMonumentName());

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

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setImage(getActivity(),model.getImage());

            }
        };

        commentList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class CommentHolder extends RecyclerView.ViewHolder {

        View mView;

        public CommentHolder(View itemView) {
            super(itemView);

            mView = itemView;
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
