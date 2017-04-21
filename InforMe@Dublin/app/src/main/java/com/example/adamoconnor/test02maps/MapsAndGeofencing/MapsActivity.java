package com.example.adamoconnor.test02maps.MapsAndGeofencing;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

import com.example.adamoconnor.test02maps.LoginAndRegister.EmailPasswordAuthentication;
import com.example.adamoconnor.test02maps.LoginAndRegister.Progress;
import com.example.adamoconnor.test02maps.PostingInformationAndComments.AddInformation;
import com.example.adamoconnor.test02maps.PostingInformationAndComments.InformationFlipActivity;
import com.example.adamoconnor.test02maps.R;
import com.example.adamoconnor.test02maps.Settings.CheckConnectivity;
import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.Map;
import static com.example.adamoconnor.test02maps.R.id.map;

public class MapsActivity extends Progress
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationSource,
        LocationListener,
        ResultCallback<Status>,
        SensorEventListener {

    Marker mGeofenceMarker;

    //declare TAG.
    private static final String TAG = MapsActivity.class.getSimpleName();
    //declare GoogleMap for fragment
    private GoogleMap mGoogleMap;
    //used for populating of geofences
    boolean populate = true;
    //used for the map fragment
    private SupportMapFragment mapFrag;
    //used to get last location of user.
    protected Location mLastLocation;
    // used to recieve location updates
    private myReceiver myReceiver;
    //geofence arraylist
    protected ArrayList<Geofence> mGeofenceList;
    //declare googleAPIClient
    protected GoogleApiClient mGoogleApiClient;
    //declare sensor
    private SensorManager mSensorManager;
    //declare type of sensor used
    private Sensor mProximity;
    //sensitivity of sensor
    private static final int SENSOR_SENSITIVITY = 4;
    //declare location manager
    protected LocationManager locationManager;
    //declare location changed listener
    protected OnLocationChangedListener mListener;
    // checking for gps connection.
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    // declare the location request.
    protected LocationRequest mLocationRequest;
    //declare handler.
    protected Handler h;
    //declare runnable.
    protected Runnable myrunnable;
    //declare check connectivity class.
    private CheckConnectivity checkConnectivity;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // stop the activity resetting don't allow landscape mode.
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // check permission if build is marshmallow or over.
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //check location on.
        checkConnectivity = new CheckConnectivity();
        checkConnectivity.startInternetEnabled(this);
        checkConnectivity.startLocationEnabled(this);

        // getting preferences of the user to see if battery saver is on.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(preferences.getBoolean("battery_switch",true) == true) {

            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        // create a new thread to start the focusing method for the view on the map.
        new Thread(new Runnable() {
            @Override

            public void run() {
                Looper.prepare();
                try {
                    Thread.sleep(5000);
                    h = new Handler();
                    final int delay = 2000; //milliseconds

                    h.postDelayed(myrunnable = new Runnable(){
                        public void run(){
                            startLocationUpdates();
                            h.postDelayed(this, delay);
                        }
                    }, delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }).start();

        // used to send email of new monument to InforMe@Dublin.
        FloatingActionButton addInfo = (FloatingActionButton)  this.findViewById(R.id.floatingAddInfoButton);
        addInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this ,AddInformation.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        // used to toggle focus on and off.
        final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleFocus);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    // The toggle is enabled
                    toggle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.TransparetFocus)));
                    h = new Handler();
                    final int delay = 2000; //milliseconds

                    h.postDelayed(myrunnable = new Runnable(){
                        public void run(){
                            startLocationUpdates();
                            h.postDelayed(this, delay);
                        }
                    }, delay);

                } else {
                    // The toggle is disabled
                    toggle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.TransparetNonFocus)));
                    try {
                        h.removeCallbacks(myrunnable);
                        h.removeCallbacks(myrunnable);
                    }catch (NullPointerException ex) {

                    }
                    stopLocationUpdates();
                }
            }
        });

        // geofence array list.
        mGeofenceList = new ArrayList<>();

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFrag.getMapAsync(this);
        createLocationRequest();

    }

    /**
     * creating the map on the creation of the activity.
     * set each of the ui components of the map, such as
     * compass etc.
     * @param googleMap
     * pass the map on which is being shown on the activity.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
        // type of map needed to display.
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setZoomControlsEnabled(false);
        googleMap.setTrafficEnabled(false);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    /**
     * call the api client needed to retrieve information.
     * from google such as maps and the location API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    /**
     * call to location updates on when the device changes
     * coordinates, its then updated on the map.
     * @param bundle
     * the information of the activity.
     */
    @Override
    public void onConnected(Bundle bundle) {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            createLocationRequest();
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * when requesting location, needs interval set and what accuracy
     * the developer wants to achieve.
     */
    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        mLocationSettingsRequest = builder.build();
    }

    /**
     * used to focus the map view when the user moves.
     * camera is then animated to find users coordinates.
     */
    protected void startLocationUpdates() {
        LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient,
                mLocationSettingsRequest
        ).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        checkLocationPermission();
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                mGoogleApiClient, mLocationRequest, MapsActivity.this);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                "location settings ");
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                        Log.e(TAG, errorMessage);
                        Toast.makeText(MapsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        mRequestingLocationUpdates = false;
                }
            }
        });

    }

    /**
     * It is a good practice to remove location requests when the activity is in a paused or
     * stopped state. Doing so helps battery performance and is especially
     * recommended in applications that request frequent location updates.
     */
    protected void stopLocationUpdates() {

        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
            }
        });
    }

    /** Activates this provider. This provider will notify the supplied listener
     * periodically, until you call deactivate().
     * This method is automatically invoked by enabling my-location layer.
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        // We need to keep a reference to my-location layer's listener so we can push forward
        // location updates to it when we receive them from Location Manager.
        mListener = listener;
        String bestAvailableProvider = LocationManager.GPS_PROVIDER;
        long minTime = 1000;
        float minDistance = 10;

        // Request location updates from Location Manager
        if (bestAvailableProvider != null) {
            checkLocationPermission();
            locationManager.requestLocationUpdates(bestAvailableProvider, minTime, minDistance, (android.location.LocationListener) this);
        } else {
            // (Display a message/dialog) No Location Providers currently available.
        }
    }

    /** Deactivates this provider.
     *  This method is automatically invoked by disabling my-location layer.
     */
    @Override
    public void deactivate() {
        // Remove location updates from Location Manager
        locationManager.removeUpdates((android.location.LocationListener) this);

        mListener = null;
    }

    /**
     * when the location of the user is changed the method as follows is
     * initiated.
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

        // get the last location which was found on device.
        mLastLocation = location;
        //declare LatLng for coordinates of camera animation.
        LatLng latLng;

        if (mGeofenceMarker != null) {
            mGeofenceMarker.remove();
        }

        double latitude = 0;
        double longitude = 0;
        try {
            // find the location from the Gps on the map.
            Location findMe = mGoogleMap.getMyLocation();
            latitude = findMe.getLatitude();
            longitude = findMe.getLongitude();

        }catch(Exception ex) {
            ex.getLocalizedMessage();
        }finally {
            if(latitude != 0 && longitude != 0) {
                latLng = new LatLng(latitude, longitude);
            }
            else {
                // use the location lat and long if map location cannot be found
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
            }
        }

        // animation of the camera.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(20)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(45)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // clear anything not needed.
        mGoogleMap.clear();

        /*
        * used for the loading of all geofences which where called from firebase when
        * the user has signed in.
        */
        for (Map.Entry<String, LatLng> entry : Constants.LANDMARKS.entrySet()) {

            // creation of the geofence colour and border colour.
            // setting of each specific geofence.
            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(entry.getValue().latitude,entry.getValue().longitude))
                    .strokeColor(Color.argb(50, 70,70,70))
                    .fillColor(getResources().getColor(R.color.Geofence))
                    .radius(50);
            mGoogleMap.addCircle( circleOptions );

            //Place current location marker
            LatLng geo = new LatLng(entry.getValue().latitude,entry.getValue().longitude);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(geo);
            markerOptions.title(entry.getKey());
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.informe));//BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)

            //adding marker to each geofence.
            mGeofenceMarker = mGoogleMap.addMarker(markerOptions);

            // add each to the geofence array list with the lat and long as well as the radius in meters.
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

        // used to populate the check if user is in specific geofence.
        if(populate) {
            try{
                PopulateGeofences();
            }catch (IllegalStateException ex) {

            }

            populate = false;
        }

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    /**
     * used to check the location permission
     * @return
     * dialog if user has not set permissions
     */
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                //(just doing it here for now, note that with this code, no explanation is shown)
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     *
     * @param requestCode
     * start to see if user connected
     * @param permissions
     * permissions which are needed for the application
     * @param grantResults
     * if the user has granted access to sensors.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    checkLocationPermission();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * used to see if user has entered a geofence.
     */
    public void PopulateGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "Google API Client not connected!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
        }
    }

    /**
     * get the geofence which is requested.
     * @return
     * to the pending intent.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    /**
     * sending the notification to the user when geofence is entered.
     * @return
     */
    private PendingIntent getGeofencePendingIntent() {

        Log.d(TAG, "Geo fence pending intent");
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * used for when the activity has started.
     */
    @Override
    protected void onStart() {
        // used as a broadcast receiver
        myReceiver = new myReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GeofenceTransitionsIntentService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);

        super.onStart();
    }

    /**
     * used to register the proximity sensor of the device.
     */
    @Override
    public void onResume() {
        super.onResume();
        try {
            mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        }catch (NullPointerException ex) {

        }

    }

    /**
     * used to stop the proximity sensor as well as the location provider.
     */
    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        try {
            mSensorManager.unregisterListener(this);
        }catch(NullPointerException ex) {

        }

        if (mGoogleApiClient != null) {
            try {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }catch(IllegalStateException ex ){

            }

        }
    }

    /**
     * used to check wheather the sensor is blocked or not
     * which changes the brightness of the screen when on the
     * map.
     * @param event
     * change of the screen brightness.
     */
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                //near
                WindowManager.LayoutParams layout = getWindow().getAttributes();
                layout.screenBrightness = 0F;
                getWindow().setAttributes(layout);
            } else {
                //far
                WindowManager.LayoutParams layout = getWindow().getAttributes();
                layout.screenBrightness = 0.7F;
                getWindow().setAttributes(layout);
            }
        }
    }

    /**
     * needed to call the sensor but not needed in this case.
     * @param sensor
     * what sensor are we using.
     * @param accuracy
     * the accuracy that is needed.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * unregister the broadcast receiver.
     */
    @Override
    public void onStop() {
        unregisterReceiver(myReceiver);
        super.onStop();
    }

    /**
     * when the connection has failed.
     * @param result
     * result returned when an error has been found.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) { result.getErrorCode(); }

    /**
     * when the connection of the application has become suspended
     * @param cause
     * the cause of the activity becoming suspended.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    /**
     * when the geofences have been added a toast will appear.
     * @param status
     * see if the geofences have been added.
     */
    public void onResult(Status status)
    {
        if (status.isSuccess()) {
            Toast.makeText(this, "Geofences Added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,"Sorry an Error has occurred",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * broadcast receiver used to produce the dialog on the map activity if user has entered
     * a geofence.
     */
    private class myReceiver extends BroadcastReceiver {

        /**
         * when an notification has been initiated it will be sent to the following
         * dialog and will be sent to the users map to display.
         * @param arg0
         * the context of the intent
         * @param arg1
         * the intent
         */
        @Override
        public void onReceive(Context arg0, Intent arg1) {

            // getting the information from the GeofenceTransitionIntentService.
            final String datapassed = arg1.getStringExtra("DATAPASSED");

            // display dialog to the specific user.
            new AlertDialog.Builder(MapsActivity.this)
                    .setTitle("Historic Monument")
                    .setMessage("Do you want to view information on the monument area you have entered ? - "+datapassed)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // send user to the specific Information activity.
                            Intent monumentInformationIntent = new Intent(getApplicationContext(),InformationFlipActivity.class);
                            // pass the name of the monument to the activity to retrieve information from firebase.
                            monumentInformationIntent.putExtra("monumentInformation", datapassed);
                            startActivity(monumentInformationIntent);

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(R.drawable.informe4)
                    .show();

        }

    }



}


