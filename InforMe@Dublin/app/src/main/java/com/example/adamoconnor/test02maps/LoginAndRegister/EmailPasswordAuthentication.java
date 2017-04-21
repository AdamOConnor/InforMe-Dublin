package com.example.adamoconnor.test02maps.LoginAndRegister;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.adamoconnor.test02maps.MapsAndGeofencing.MapsActivity;
import com.example.adamoconnor.test02maps.R;
import com.example.adamoconnor.test02maps.Settings.CheckConnectivity;
import com.example.adamoconnor.test02maps.Settings.SettingsActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.support.annotation.NonNull;
import java.util.List;
import java.util.Locale;
import static com.example.adamoconnor.test02maps.MapsAndGeofencing.Constants.LANDMARKS;
import static com.example.adamoconnor.test02maps.R.id.resetPassword;
import static com.example.adamoconnor.test02maps.R.id.settings_prefs;

public class EmailPasswordAuthentication extends Progress implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";

    // fields located in design
    private TextView mStatusTextView;
    private EditText mEmailField;
    private EditText mPasswordField;
    private SignInButton mGoogleLogin;

    //states used for the settings bar when user or out.
    private String mState = null; // setting state
    private String userState = null;
    private CheckConnectivity checkConnectivity;

    // declare auth.
    private FirebaseAuth mAuth;

    //used for firebase database of users.
    private DatabaseReference mDatabaseUsers;

    // declare auth listener.
    private FirebaseAuth.AuthStateListener mAuthListener;

    // used to see only one signed in.
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailpassword);

        //setting screen orientation to stop fragments view showing on eachother.
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // declare progress dialog
        mProgressDialog = new ProgressDialog(this);
        // declare connectivity checker
        checkConnectivity = new CheckConnectivity();

        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.to_map_button).setOnClickListener(this);
        findViewById(R.id.googleSignIn).setOnClickListener(this);

        // initialization of firebase authentication.
        mAuth = FirebaseAuth.getInstance();

        // instanciation of database instance in the users area.
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        // keep database synced.
        mDatabaseUsers.keepSynced(true);

        // authentication state listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    checkUserExistsDatabaseOnReEnter();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // update the UI
                updateUI(user);
            }
        };

        // used for the requesting of google authentication token.
        GoogleSignInOptions googleLog = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // creation of a google api client for the connection of a google token.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleLog)
                .build();

    }


    /**
     * Used for the signing in of a google authentication
     * token.
     */
    private void signIn() {
        mProgressDialog.setMessage("Signing in ...");
        mProgressDialog.show();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    /**
     *
     * @param requestCode
     * checking the allocated code on which allows for the sign in.
     * @param resultCode
     *  not really used in this instance.
     * @param data
     *requesting the information needed to retrieve the information
     * used for the signing in to the application.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // Get the intent of the data used for the sign in.
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(EmailPasswordAuthentication.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();

            }
            mProgressDialog.dismiss();
        }
    }

    /**
     * @param acct
     * retrieving credentials used in the sign in
     * and to authenticate the user on which will
     * be using the application.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(EmailPasswordAuthentication.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            checkUserExistsDatabaseOnReEnter();

                        }
                    }
                });
        // stop progress dialog.
        mProgressDialog.dismiss();
    }

    // check permission request of 99
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    /**
     * used for the checking of permissions for the use of location
     * services which cannot be just added to the manifest file.
     *
     * @return
     * return dialog on which user must choose to accept application
     * access.
     */
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                //(just doing it here for now, note that with this code, no explanation is shown)
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * used to get the location of the device in order
     * to populate geofences for the specific area...
     * @return
     * used to return the last known coordinates of the
     * device.
     */
    public Location getLocation() {
        //check permission before using.
        checkLocationPermission();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocationGPS != null) {
                return lastKnownLocationGPS;
            } else {
                Location loc =  locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                return loc;
            }
        } else {
            return null;
        }
    }

    /**
     * used for the adding of geofences to the map activity.
     * populates the landmarks array, which gets geofences ready for
     * the map when a user has been authenticated
     */
    public void addGeofences() {

        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses = null;
        String town = "dublin";

        // used in further work for specific areas around Ireland.
       /* try {
            addresses = gcd.getFromLocation(getLocation().getLatitude(), getLocation().getLongitude(), 1);
        }catch (IOException ex) {
            ex.getStackTrace();
        }

        if (addresses.size() > 0)
        {
            town = addresses.get(0).getLocality();

        }
        */
        //town.toLowerCase() used to get current users location in the future of InforMe@Ireland

        /**
         * creation of new firebase reference which populates an array of the specific,
         * populates with the specific name of the area and the coordinates
         */
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference myRef = database.child("geofences").child(town.toLowerCase());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot alerts) {

                for (DataSnapshot alert : alerts.getChildren()) {
                    String myLandmarks = alert.getValue().toString();
                    //splitted name | long | lat
                    String[] splited = myLandmarks.split("\\|");
                    // send to the package com.example.adamoconnor.test02maps.LoginAndRegister; Constants Landmarks Hashmap.
                    LANDMARKS.put(splited[0], new LatLng(Double.parseDouble(splited[1]), Double.parseDouble(splited[2])));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    // creating an options menu for settings page
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        // resetting password when user not signed in.
        if(id == resetPassword) {

            Intent intent = new Intent(this, ResetActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;

        }

        // setting preferences when user signed in.
        if(id == settings_prefs) {

            Intent intent = new Intent(this, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;

        }
        return false;
    }

    //create options menu when user is signed in or signed out.
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();

        if (mState.equals("show")) {

            // set the previous action view to non visible.
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setVisible(false);

            inflater.inflate(R.menu.pref_custom, menu);

            //set a new action to visible.
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setVisible(true);
        }
        if(userState.equals("SHOWUSER_SETTINGS")) {

            // set the previous action view to non visible.
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setVisible(false);

            inflater.inflate(R.menu.settings_custom, menu);

            //set a new action to visible.
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setVisible(true);
        }

        return true;
    }

    //START on_start_add_listener
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    //START on_stop_remove_listener
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * used for the changing of activity to the
     * Map Activity.
     */
    private void myMap() {

        // check if the internet and location is turned on.
        if(!checkConnectivity.isInternetOn() && !checkConnectivity.isLocationOn()) {
            finish();
        }

        // adding of geofences.
        addGeofences();

        // send user to map activity.
        Intent intent = new Intent(EmailPasswordAuthentication.this, MapsActivity.class);
        startActivity(intent);
    }

    /**
     * used for the signing in of a user which has logged in
     * with their email address and password.
     * @param email
     * email address of the user which created an account.
     * @param password
     * password on which user wants to use.
     */
    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        // validate the textfields user has filled in.
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // start the authentication of the email address and password of the user.
        final Task<AuthResult> authResultTask = mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        if(task.isSuccessful()) {
                            checkUserExistsDatabaseOnReEnter();
                        }
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(EmailPasswordAuthentication.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            mStatusTextView.setText(R.string.auth_failed);
                        }

                        hideProgressDialog();
                    }
                });
    }

    /**
     * used for the signing out of the user.
     */
    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    /**
     * check on whether the user that has been authenticated is
     * in the database and has created an account.
     */
    private void checkUserExistsDatabaseOnReEnter() {

        // see if user is authenticated.
        if(mAuth.getCurrentUser() != null) {

            // get the id of the authenticated user.
            final String user_id = mAuth.getCurrentUser().getUid();

            // see if the authenticated user is found in the firebase database.
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(user_id)) {

                        Toast.makeText(EmailPasswordAuthentication.this, "Authentication successful",
                                Toast.LENGTH_SHORT).show();
                    }
                    else
                        {

                            Intent intent = new Intent(EmailPasswordAuthentication.this, SetupActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                            Toast.makeText(EmailPasswordAuthentication.this, "Please setup your account...",
                                    Toast.LENGTH_SHORT).show();
                        }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    /**
     * validating the text-fields on which the user
     * must enter their personal information.
     * @return
     * return the error on screen for the text-field.
     */
    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    /**
     * show the login details to next activity !!
     * @param user
     * the following updates the login UI which sees
     * if the user is signed in or not.
     */
    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt, user.getEmail()));

            //check the connectivity of the internet and location.
            checkConnectivity.startInternetEnabled(this);
            checkConnectivity.startLocationEnabled(this);

            mState = "show";
            userState = "HIDE_MENU";

            // refresh the action bar menu.
            invalidateOptionsMenu();

            // reset buttons.
            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
            findViewById(R.id.googleSignIn).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.to_map_button).setVisibility(View.VISIBLE);

        } else {
            mStatusTextView.setText(R.string.signed_out);


            mState = "HIDE_MENU";
            userState = "SHOWUSER_SETTINGS";

            // refresh the action bar menu.
            invalidateOptionsMenu();

            // reset buttons.
            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.googleSignIn).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            findViewById(R.id.to_map_button).setVisibility(View.GONE);
        }
    }

    /**
     * used for the checking of which buttons have been clicked
     * by the user.
     * @param v
     * The view used to see which has been selected.
     */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            Intent intent = new Intent(EmailPasswordAuthentication.this, RegisterAccount.class);
            startActivity(intent);
        } else if (i == R.id.email_sign_in_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.sign_out_button) {
            signOut();
        }else if(i == R.id.googleSignIn) {
            signIn();
        } else if (i == R.id.to_map_button) {
            myMap();
        }
    }
}
