package adamoconnor.informe.MapsAndGeofencing;


import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class Constants {

    // used to allow geofence to never expire.
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = Geofence.NEVER_EXPIRE;

    // show the radius of the geofence
    public static final float GEOFENCE_RADIUS_IN_METERS = 50;

    // hashmap to hold the geo-fences.
    public static final HashMap<String, LatLng> LANDMARKS = new HashMap<>();

}
