package com.example.adamoconnor.test02maps.MapsAndGeofencing;

public class Place {

    // monument name of the historical place
    private static String MonumentName;

    /**
     * get the name of the monument which was set.
     * @return
     * the monument name.
     */
    public static String getMonumentName() {
        return MonumentName;
    }

    /**
     * set the name of the monument
     * @param monumentName
     * setting of the monument name.
     */
    public static void setMonumentName(String monumentName) {
        MonumentName = monumentName;
    }
}