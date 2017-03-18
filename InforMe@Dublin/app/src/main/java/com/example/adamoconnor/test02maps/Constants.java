package com.example.adamoconnor.test02maps;


import com.google.android.gms.maps.model.LatLng;
import java.util.HashMap;
/**
 * Created by Adam O'Connor on 09/12/2016.
**/


import com.google.android.gms.location.Geofence;

public class Constants {

    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = Geofence.NEVER_EXPIRE;
    public static final float GEOFENCE_RADIUS_IN_METERS = 50;
    public static final HashMap<String, LatLng> LANDMARKS = new HashMap<>();
    public static String sharedValue = null;


}
