package com.example.adamoconnor.test02maps.MapsAndGeofencing;

/**
 * Created by Adam O'Connor on 30/01/2017.
 */
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class Place {

    static String MonumentName;

    public static String getMonumentName() {
        return MonumentName;
    }

    public static void setMonumentName(String monumentName) {
        MonumentName = monumentName;
    }
}