package com.example.adamoconnor.test02maps.PostingInformationAndComments;

/**
 * Created by Adam O'Connor on 12/04/2017.
 */

public class Comments {

    private String title;
    private String description;
    private String image;
    private String username;
    private Long timestamp;
    private String profile;


    public Comments(){}

    public Comments(String title, String description, String image, String username, Long timestamp, String profile) {

        this.title = title;
        this.description = description;
        this.image = image;
        this.username = username;
        this.timestamp = timestamp;
        this.profile = profile;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) { this.username = username;}

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
