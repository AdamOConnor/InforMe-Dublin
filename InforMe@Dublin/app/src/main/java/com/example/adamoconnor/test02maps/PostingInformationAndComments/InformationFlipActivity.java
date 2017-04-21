package com.example.adamoconnor.test02maps.PostingInformationAndComments;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.example.adamoconnor.test02maps.MapsAndGeofencing.MapsActivity;
import com.example.adamoconnor.test02maps.R;
import static com.example.adamoconnor.test02maps.MapsAndGeofencing.Place.setMonumentName;

/**
 * Demonstrates a "card-flip" animation using custom fragment transactions ({@link
 * android.app.FragmentTransaction#setCustomAnimations(int, int)}).
 *
 * <p>This sample shows an "info" action bar button that shows the back of a "card", rotating the
 * front of the card out and the back of the card in. The reverse animation is played when the user
 * presses the system Back button or the "photo" action bar button.</p>
 */
public class InformationFlipActivity extends AppCompatActivity
        implements FragmentManager.OnBackStackChangedListener {
    /**
     * A handler object, used for deferring UI operations.
     */
    private Handler mHandler = new Handler();
    private String monumentName = null;

    /**
     * Whether or not we're showing the back of the card (otherwise showing the front).
     */
    private boolean mShowingBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_flip);

        //setting screen orientation to stop fragments view showing on eachother.
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // get the specific monument entered.
        Bundle extras = null;
        if (savedInstanceState == null) {
            extras = getIntent().getExtras();
            if(extras == null) {
                monumentName = null;
            } else {
                monumentName = extras.getString("1");
                setMonumentName(monumentName);
                if(monumentName == null) {
                    monumentName = extras.getString("monumentInformation");
                    setMonumentName(monumentName);
                }
            }
        } else {
            try {
                monumentName = extras.getString("monumentInformation");
                setMonumentName(monumentName);
            } catch (NullPointerException ex) {

            }
        }

        // send to new thread
        MyThread myThread = new MyThread();
        myThread.start();

        // Monitor back stack changes to ensure the action bar shows the appropriate
        // button (either "photo" or "info").
        getFragmentManager().addOnBackStackChangedListener(this);
    }

    /**
     * start new thread to use fragment manager
     * to speed things up and allow less computation on
     * the UI thread.
     */
    public class MyThread extends Thread{

        @Override
        public void run() {

            // If there is no saved instance state, add a fragment representing the
            // front of the card to this activity. If there is saved instance state,
            // this fragment will have already been added to the activity.
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new InformationFrontFragment())
                    .commit();

        }

    }

    /**
     * used to set the configuration of the orientation of the
     * activity
     * @param newConfig
     * configuration which has been set.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //here you can handle orientation change
    }

    /**
     * create the options menu when activity is created.
     * @param menu
     * imenu reference
     * @return
     * return the action.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Add either a "photo" or "finish" button to the action bar, depending on which page
        // is currently selected.
        MenuItem item = menu.add(Menu.NONE, R.id.action_flip, Menu.NONE,
                mShowingBack
                        ? R.string.action_photo
                        : R.string.action_info);
        item.setIcon(mShowingBack
                ? R.drawable.ic_action_photo
                : R.drawable.ic_action_info);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    /**
     * find which item has been selected like an
     * onClickListener.
     * @param item
     * items which can be selected.
     * @return
     * return the boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate "up" the demo structure to the launchpad activity.
                // See http://developer.android.com/design/patterns/navigation.html for more.
                NavUtils.navigateUpTo(this, new Intent(this, MapsActivity.class));
                return true;

            case R.id.action_flip:
                flipCard();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * used to flip the fragments when user wants to change.
     */
    private void flipCard() {
        if (mShowingBack) {
            getFragmentManager().popBackStack();
            return;
        }

        // Flip to the back.
        mShowingBack = true;

        // Create and commit a new fragment transaction that adds the fragment for the back of
        // the card, uses custom animations, and is part of the fragment manager's back stack.
        getFragmentManager()
                .beginTransaction()

                // Replace the default fragment animations with animator resources representing
                // rotations when switching to the back of the card, as well as animator
                // resources representing rotations when flipping back to the front (e.g. when
                // the system Back button is pressed).
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)

                // Replace any fragments currently in the container view with a fragment
                // representing the next page (indicated by the just-incremented currentPage
                // variable).
                .replace(R.id.container, new InformationBackCommentsFragment())

                // Add this transaction to the back stack, allowing users to press Back
                // to get to the front of the card.
                .addToBackStack(null)

                // Commit the transaction.
                .commit();

        // Defer an invalidation of the options menu (on modern devices, the action bar). This
        // can't be done immediately because the transaction may not yet be committed. Commits
        // are asynchronous in that they are posted to the main thread's message loop.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
            }
        });
    }

    /**
     * check when fragments on the back of the stack.
     */
    @Override
    public void onBackStackChanged() {
        mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);

        // When the back stack changes, invalidate the options menu (action bar).
        invalidateOptionsMenu();
    }

    /**
     * onResume method for activity.
     */
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

    }

    /**
     * onPause method used for activity.
     */
    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

    }

}
