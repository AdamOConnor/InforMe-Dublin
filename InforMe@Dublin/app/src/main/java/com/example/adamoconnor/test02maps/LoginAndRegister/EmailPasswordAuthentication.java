package com.example.adamoconnor.test02maps.LoginAndRegister;

/**
 * Created by Adam O'Connor on 04/11/2016.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.example.adamoconnor.test02maps.MapsAndGeofencing.Constants.LANDMARKS;
import static com.example.adamoconnor.test02maps.R.id.action_settings;
import static com.example.adamoconnor.test02maps.R.id.resetPassword;
import static com.example.adamoconnor.test02maps.R.id.settings_prefs;

public class EmailPasswordAuthentication extends Progress implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";

    private TextView mStatusTextView;
    private EditText mEmailField;
    private EditText mPasswordField;
    private String mState = null; // setting state
    private String userState = null;
    private SignInButton mGoogleLogin;


    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private DatabaseReference mDatabaseUsers;

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailpassword);

        isInternetOn();

        mProgressDialog = new ProgressDialog(this);

        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.to_map_button).setOnClickListener(this);
       // findViewById(R.id.forgotPassword).setOnClickListener(this);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseUsers.keepSynced(true);

        mGoogleLogin = (SignInButton) findViewById(R.id.googleSignIn);

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // Place is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    checkUserExistsDatabaseOnReEnter();

                } else {
                    // P lace is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // [START_EXCLUDE]
                updateUI(user);
                // [END_EXCLUDE]
            }
        };
        // [END auth_state_listener]


        // [Google signIn]
        GoogleSignInOptions googleLog = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        // connection failed, should be handled



                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleLog)
                .build();

        mGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(EmailPasswordAuthentication.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();

            }
            mProgressDialog.dismiss();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        mProgressDialog.setMessage("Signing in ...");
        mProgressDialog.show();

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
                            mProgressDialog.dismiss();
                        }
                        // ...
                    }
                });
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                //TODO:
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

    public Location getLocation() {

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

    public void addGeofences() {


        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses = null;
        String town = "Dublin";
        try {
            addresses = gcd.getFromLocation(getLocation().getLatitude(), getLocation().getLongitude(), 1);
        }catch (IOException ex) {
            ex.getStackTrace();
        }

        if (addresses.size() > 0)
        {
            town = addresses.get(0).getLocality();

        }

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference myRef = database.child("geofences").child(town.toLowerCase());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot alerts) {

                for (DataSnapshot alert : alerts.getChildren()) {
                    String myLandmarks = alert.getValue().toString();
                    String[] splited = myLandmarks.split("\\|");

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

        if(id == resetPassword) {

            Intent intent = new Intent(this, ResetActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;

        }
        if(id == settings_prefs) {

            Intent intent = new Intent(this, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;

        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();


        if (mState == "show") {
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setVisible(false);
            inflater.inflate(R.menu.pref_custom, menu);
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setVisible(true);
        }
        if(userState == "SHOWUSER_SETTINGS") {
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setVisible(false);
            inflater.inflate(R.menu.settings_custom, menu);
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setVisible(true);
        }


        return true;
    }


    private void isInternetOn() {

        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean network_enabled = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(this.getResources().getString(R.string.network_not_enabled));
            dialog.setPositiveButton(this.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(this.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void myMap() {
        Intent intent = new Intent(EmailPasswordAuthentication.this, MapsActivity.class);
        startActivity(intent);
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
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
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            mStatusTextView.setText(R.string.auth_failed);
                        }


                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }


    private void checkUserExistsDatabaseOnReEnter() {

        if(mAuth.getCurrentUser() != null) {

            final String user_id = mAuth.getCurrentUser().getUid();

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

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

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
     */
    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt, user.getEmail()));

            addGeofences();
            mState = "show";
            userState = "HIDE_MENU";
            invalidateOptionsMenu();

            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
            findViewById(R.id.googleSignIn).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.to_map_button).setVisibility(View.VISIBLE);

        } else {
            mStatusTextView.setText(R.string.signed_out);


            mState = "HIDE_MENU";
            userState = "SHOWUSER_SETTINGS";

            invalidateOptionsMenu();

            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.googleSignIn).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            findViewById(R.id.to_map_button).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            //createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
            Intent intent = new Intent(EmailPasswordAuthentication.this, RegisterAccount.class);
            startActivity(intent);
        } else if (i == R.id.email_sign_in_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.sign_out_button) {
            signOut();
       // } else if(i == R.id.forgotPassword) {
         //   forgottenPassword();
        }
        else if (i == R.id.to_map_button) {
            myMap();
        }
    }
}

