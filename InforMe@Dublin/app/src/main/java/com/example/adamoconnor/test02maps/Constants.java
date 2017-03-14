package com.example.adamoconnor.test02maps;


import com.google.android.gms.maps.model.LatLng;
import java.util.HashMap;
/**
 * Created by Adam O'Connor on 09/12/2016.
**/


import com.google.android.gms.location.Geofence;

public class Constants {

    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = Geofence.NEVER_EXPIRE;
    public static final float GEOFENCE_RADIUS_IN_METERS = 150;

    public static final HashMap<String, LatLng> LANDMARKS = new HashMap<>();
    static {
        // San Francisco International Airport.
        LANDMARKS.put("Sante", new LatLng(53.350308,-6.440021));

        // Googleplex.
        LANDMARKS.put("nursing home", new LatLng(53.349037,-6.439661));

        // Test
        LANDMARKS.put("AGP", new LatLng(53.346655,-6.440477));

        LANDMARKS.put("College", new LatLng(53.404601, -6.378524));
    }
}
