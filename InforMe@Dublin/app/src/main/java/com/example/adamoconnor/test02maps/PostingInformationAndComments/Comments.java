package com.example.adamoconnor.test02maps.PostingInformationAndComments;

/**
 * Created by Adam O'Connor on 12/04/2017.
 */

public class Comments {

    private String title;
    private String description;
    private String image;

    public Comments(){}

    public Comments(String title, String description, String image) {
        this.title = title;
        this.description = description;
        this.image = image;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


}
