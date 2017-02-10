package com.example.adamoconnor.test02maps;

/**
 * Created by Adam O'Connor on 30/01/2017.
 */
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

/**
 * Created by Ravi Tamada on 07/10/16.
 * www.androidhive.info
 */

@IgnoreExtraProperties
public class Place {

    public String name;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String email;
    public ArrayList<String> information;

    public ArrayList<String> getInformation() {
        return information;
    }

    public void setInformation(ArrayList<String> information) {
        this.information = information;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }




    // Default constructor required for calls to
    // DataSnapshot.getValue(Place.class)
    public Place() {
    }

}